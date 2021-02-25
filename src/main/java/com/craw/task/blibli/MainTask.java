package com.craw.task.blibli;

import com.arronlong.httpclientutil.HttpClientUtil;
import com.arronlong.httpclientutil.common.HttpConfig;
import com.arronlong.httpclientutil.exception.HttpProcessException;
import com.craw.common.Common;
import com.craw.model.BlibliUser;
import com.craw.task.runnable.NameRunnable;
import com.craw.task.runnable.StopRunnable;
import com.google.gson.JsonObject;
import com.mysql.cj.exceptions.DataConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;

public class MainTask implements StopRunnable, NameRunnable {

    private static final Logger logger = LoggerFactory.getLogger(MainTask.class);

    private static final String USER_INFO_URL = "https://api.bilibili.com/x/space/acc/info?mid=%s&jsonp=jsonp";

    private final BlockingQueue<String> finsDatesQueue;
    private final BlockingQueue<String> finsSearchQueue;
    private final BlockingQueue<String> stopQueue;

    private final int minUserId;
    private final int maxUserId;

    public MainTask(int minUserId, int maxUserId,
                    BlockingQueue<String> finsDatesQueue,
                    BlockingQueue<String> finsSearchQueue,
                    BlockingQueue<String> stopQueue) {
        Objects.requireNonNull(finsDatesQueue);
        Objects.requireNonNull(finsSearchQueue);
        this.maxUserId = maxUserId;
        this.minUserId = minUserId;
        this.finsDatesQueue = finsDatesQueue;
        this.finsSearchQueue = finsSearchQueue;
        this.stopQueue = stopQueue;
    }

    @Override
    public String getName() {
        return "用户获取任务";
    }

    @Override
    public void run() {
        for (int i = minUserId; i < maxUserId; i++) {

        }
    }

    private Optional<String> getUserJson(String userId) {
        String url = String.format(USER_INFO_URL, userId);
        try {
            return Optional.of(HttpClientUtil.get(HttpConfig.custom().url(url)
                    .headers(Common.getHeard().build())));
        } catch (HttpProcessException e) {
            return Optional.empty();
        }
    }

    private JsonObject checkData(String data, String userId) {
        JsonObject json = Common.json().fromJson(data, JsonObject.class);
        String code = json.get("code").getAsString();
        if ("0".equals(code)) {
            return json.get("data").getAsJsonObject();
        }
        logger.error("【{}】获取用户结果异常..异常id={}", getName(), userId);
        throw new DataConversionException("结果转换异常");
    }

    private BlibliUser buildUser(JsonObject data){
        return null;
    }

    @Override
    public boolean needStop() {
        return !this.stopQueue.isEmpty();
    }
}
