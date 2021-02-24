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

/**
 * 负责获取粉丝地址
 */
public class MainTask implements NameRunnable {

    private static final Logger logger = LoggerFactory.getLogger(MainTask.class);

    private static final String fansList = "https://weibo.com/p/100605$$$$$$/follow?pids=Pl_Official_HisRelation__61&relate=fans&ajaxpagelet=1&ajaxpagelet_v6=1&__ref=%2Fp%2F100605$$$$$$%2Ffollow%3Frelate%3Dfans%26page%3D2%23Pl_Official_HisRelation__61&_t=FM_161399559700341&page=";
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
            for (int page = 1; page < ShareStore.getCurrentPageMaxSize() && !isStop(); page++) {
                String fansList = getFansList(wbId, page).orElseThrow(RuntimeException::new);
                parseFansListData(fansList);
                sleep();
            }
        });
    }

    private Optional<String> getFansList(String wbId, int page) {
        try {
            logger.info("【{}】准备查找粉丝{} 的第{}页粉丝列表", getName(), wbId, page);
            return Optional.of(HttpClientUtil.get(HttpConfig.custom().url(fansList.replace("$$$$$$", wbId) + page)
                    .context(cookies.getContext())
                    .headers(Common.getHeard().build())));
        } catch (HttpProcessException e) {
            return Optional.empty();
        }
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

    public static void main(String[] args) {
        System.out.println(fansList.replace("$$$$$$", "2876037162"));
    }
}
