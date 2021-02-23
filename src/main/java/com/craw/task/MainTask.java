package com.craw.task;

import com.arronlong.httpclientutil.HttpClientUtil;
import com.arronlong.httpclientutil.common.HttpConfig;
import com.arronlong.httpclientutil.common.HttpCookies;
import com.arronlong.httpclientutil.exception.HttpProcessException;
import com.craw.ShareStore;
import com.craw.common.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 负责获取粉丝地址，并分发任务
 */
public class MainTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(MainTask.class);

    private static final String fansList = "https://weibo.com/p/1006052360812967/follow?pids=Pl_Official_HisRelation__61&relate=fans&ajaxpagelet=1&ajaxpagelet_v6=1&__ref=%2Fp%2F1006052360812967%2Ffollow%3Frelate%3Dfans%26page%3D2%23Pl_Official_HisRelation__61&_t=FM_161399559700341&page=";
    private static final HttpCookies cookies = Common.getCookies();

    // 并非严格要求必须最大数是maxCount。结果最多会比预期多一点而已
    private final int maxCount;

    private final BlockingQueue<String> finsDateQueue;
    private BlockingQueue<String> stopQueue;

    /**
     * 遍历粉丝列表地址
     *
     * @param count         准备获取的粉丝数目
     * @param finsDateQueue 粉丝页面解析队列
     */
    public MainTask(int count, BlockingQueue<String> finsDateQueue) {
        Objects.requireNonNull(finsDateQueue);
        this.finsDateQueue = finsDateQueue;
        this.stopQueue = new ArrayBlockingQueue<>(1);
        this.maxCount = count;
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
            logger.info("【粉丝获取任务】准备查找第{}页粉丝列表", page);
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
        if (Objects.isNull(this.stopQueue)) {
            return false;
        }
        return !this.stopQueue.isEmpty();
    }

    public BlockingQueue<String> getTopQueue() {
        return this.stopQueue;
    }

    public static void main(String[] args) throws IOException {
        BlockingQueue<String> uiQ = new LinkedBlockingQueue<>();
        MainTask mainTask = new MainTask(20, uiQ);
        mainTask.run();
    }

}
