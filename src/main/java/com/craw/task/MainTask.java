package com.craw.task;

import com.arronlong.httpclientutil.HttpClientUtil;
import com.arronlong.httpclientutil.common.HttpConfig;
import com.arronlong.httpclientutil.common.HttpCookies;
import com.arronlong.httpclientutil.exception.HttpProcessException;
import com.craw.ShareStore;
import com.craw.common.Common;
import com.craw.task.runnable.NameRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 负责获取粉丝地址
 */
public class MainTask implements NameRunnable {

    private static final Logger logger = LoggerFactory.getLogger(MainTask.class);

    private static final String fansList = "https://weibo.com/p/$$$$$$/follow?relate=fans&ajaxpagelet=1&ajaxpagelet_v6=1&_t=FM_161399559700341&pids=$pids$&page=";
    private static final String attentionList = "https://weibo.com/p/$$$$$$/follow?page=";
    private static final Pattern PAGE_REX = Pattern.compile(">(\\d+?)<\\\\/a>");
    private static final ThreadLocal<Integer> errCount = ThreadLocal.withInitial(() -> 0);

    private static final HttpCookies cookies = Common.getCookies();
    private final BlockingQueue<String> finsDatesQueue;
    private final BlockingQueue<String> finsSearchQueue;
    private final BlockingQueue<String> stopQueue;

    /**
     * 遍历粉丝列表地址
     *
     * @param finsSearchQueue 入口数据
     * @param finsDatesQueue  粉丝页面解析队列
     */
    public MainTask(BlockingQueue<String> finsSearchQueue, BlockingQueue<String> finsDatesQueue) {
        Objects.requireNonNull(finsDatesQueue);
        Objects.requireNonNull(finsSearchQueue);
        this.finsDatesQueue = finsDatesQueue;
        this.finsSearchQueue = finsSearchQueue;
        this.stopQueue = new ArrayBlockingQueue<>(1);
    }

    @Override
    public String getName() {
        return "粉丝获取任务";
    }

    @Override
    public void run() {
        Common.takeRun(finsSearchQueue, getName(), 200, TimeUnit.MILLISECONDS, this::isStop, (wbId) -> {
            ShareStore.setCurrentPageMaxSize(6);
            for (int page = 1; page < ShareStore.getCurrentPageMaxSize() && !isStop(); page++) {
                String fansList = getFansList(wbId, page).orElseThrow(RuntimeException::new);
                parseFansListData(fansList);
                setCurrentPage(fansList);
                sleep();
            }
        });
    }

    private Optional<String> getFansList(String wbId, int page) {
        long start = System.currentTimeMillis();
        try {
            String url = fansList.replace("$$$$$$", wbId.substring(0, 16)) + page;
            url = url.replace("$pids$", wbId.substring(17));
            logger.info("【{}】准备查找粉丝{} 的第{}页粉丝列表 url={}", getName(), wbId, page, url);
            return Optional.of(HttpClientUtil.get(HttpConfig.custom().url(url)
                    .context(cookies.getContext())
                    .headers(Common.getHeard().build())));
        } catch (HttpProcessException e) {
            return Optional.empty();
        } finally {
            logger.info("{}【{}】 请求耗时 {}ms wbId={} page={}", Thread.currentThread().getName(), getName(), System.currentTimeMillis() - start, wbId, page);
        }
    }

    private void setCurrentPage(String html) {
        int allPage = 1;
        try {
            Matcher matcher = PAGE_REX.matcher(html);
            while (matcher.find()) {
                allPage = Integer.parseInt(matcher.group(1));
            }
        } catch (Exception e) {
            logger.error("【{}】获取当前总页数失败，忽略解析， 原总页数为 {}", getName(), ShareStore.getCurrentPageMaxSize());
        }
        logger.info("【{}】当前总页数 {}", getName(), ShareStore.getCurrentPageMaxSize());
        ShareStore.setCurrentPageMaxSize(Math.min(allPage, 6));
    }

    private void parseFansListData(String data) {
        this.finsDatesQueue.offer(data);
    }

    private void sleep() {
        try {
            TimeUnit.MILLISECONDS.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean isStop() {
        return !this.stopQueue.isEmpty();
    }

    public BlockingQueue<String> getStopQueue() {
        return this.stopQueue;
    }
}
