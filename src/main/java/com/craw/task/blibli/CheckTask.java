package com.craw.task.blibli;

import com.arronlong.httpclientutil.HttpClientUtil;
import com.arronlong.httpclientutil.common.HttpConfig;
import com.craw.common.Common;
import com.craw.common.ProxyHolder;
import com.craw.task.runnable.NameRunnable;
import com.google.gson.JsonObject;
import com.mysql.cj.exceptions.DataConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public abstract class CheckTask implements NameRunnable {

    private static final Logger logger = LoggerFactory.getLogger(CheckTask.class);

    private static final ThreadLocal<Integer> errCount = ThreadLocal.withInitial(() -> 0);

    protected Optional<String> getData(String url, boolean isProxy) {
        long start = System.currentTimeMillis();
        try {
            logger.debug("【{}】准备请求 url={}", getName(), url);
            if (isProxy) {
                return Optional.of(HttpClientUtil
                        .get(HttpConfig.custom().url(url)
                                .client(ProxyHolder.getHttpClient(url))
                                .timeout(10000, true)
                                .headers(Common.getHeard().build())));
            } else {
                return Optional.of(HttpClientUtil.get(HttpConfig.custom().url(url).headers(Common.getHeard().build())));
            }
        } catch (Exception e) {
            if (isProxy) {
                ProxyHolder.deleteRemoteProxy(ProxyHolder.getCurrentProxy());
                ProxyHolder.initHttpClient();
                if (errCount.get() > 10) {
                    return Optional.empty();
                }
                errCount.set(errCount.get());
                logger.warn("【{}】 请求超时，重新获取代理...", getName());
                return getData(url, true);
            } else {
                return Optional.empty();
            }
        } finally {
            logger.debug("【{}】 请求耗时 {}ms url = {}", getName(), System.currentTimeMillis() - start, url);
        }
    }

    protected Optional<String> getData(String url) {
        return getData(url, false);
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
