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

import java.io.IOException;
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

    private static final String fansList = "https://weibo.com/p/1006052360812967/follow?pids=Pl_Official_HisRelation__61&relate=fans&ajaxpagelet=1&ajaxpagelet_v6=1&__ref=%2Fp%2F1006052360812967%2Ffollow%3Frelate%3Dfans%26page%3D2%23Pl_Official_HisRelation__61&_t=FM_161399559700341&page=";
    private static final HttpCookies cookies = Common.getCookies();

    private final BlockingQueue<String> finsDateQueue;
    private final BlockingQueue<String> stopQueue;

    /**
     * 遍历粉丝列表地址
     *
     * @param count         准备获取的粉丝数目
     * @param finsDateQueue 粉丝页面解析队列
     */
    public MainTask(BlockingQueue<String> finsDateQueue) {
        Objects.requireNonNull(finsDateQueue);
        this.finsDateQueue = finsDateQueue;
        this.stopQueue = new ArrayBlockingQueue<>(1);
    }

    @Override
    public String getName() {
        return "粉丝获取任务";
    }

    @Override
    public void run() {
        for (int page = 1; page < ShareStore.getCurrentPageMaxSize() && !isStop(); page++) {
            String fansList = getFansList(page).orElseThrow(RuntimeException::new);
            parseFansListData(fansList);
            sleep();
        }
    }

    private Optional<String> getFansList(int page) {
        try {
            logger.info("【{}】准备查找第{}页粉丝列表", getName(), page);
            return Optional.of(HttpClientUtil.get(HttpConfig.custom().url(fansList + page)
                    .context(cookies.getContext())
                    .headers(Common.getHeard().build())));
        } catch (HttpProcessException e) {
            return Optional.empty();
        }
    }

    private void parseFansListData(String data) {
        this.finsDateQueue.offer(data);
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

    public static void main(String[] args) throws IOException {
//        BlockingQueue<String> uiQ = new LinkedBlockingQueue<>();
//        MainTask mainTask = new MainTask(20, uiQ);
//        mainTask.run();
    }
}
