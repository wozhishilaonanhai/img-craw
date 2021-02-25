package com.craw.task.blibli;

import com.arronlong.httpclientutil.HttpClientUtil;
import com.arronlong.httpclientutil.common.HttpConfig;
import com.arronlong.httpclientutil.exception.HttpProcessException;
import com.craw.common.Common;
import com.craw.task.runnable.NameRunnable;
import com.google.gson.JsonObject;
import com.mysql.cj.exceptions.DataConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public abstract class CheckTask implements NameRunnable {

    private static final Logger logger = LoggerFactory.getLogger(CheckTask.class);

    protected Optional<String> getData(String url) {
        try {
            logger.debug("【{}】准备请求 url={}", getName(), url);
            return Optional.of(HttpClientUtil.get(HttpConfig.custom().url(url)
                    .headers(Common.getHeard().build())));
        } catch (HttpProcessException e) {
            return Optional.empty();
        }
    }

    protected JsonObject checkData(String data, String userId) {
        JsonObject json = Common.json().fromJson(data, JsonObject.class);
        String code = json.get("code").getAsString();
        if ("0".equals(code)) {
            return json.get("data").getAsJsonObject();
        } else if ("-404".equals(code)) {
            return null;
        }
        logger.error("【{}】获取请求结果异常..异常id={} content={}", getName(), userId, data);
        throw new DataConversionException("结果转换异常");
    }
}
