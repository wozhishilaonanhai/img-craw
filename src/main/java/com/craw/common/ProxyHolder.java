package com.craw.common;

import com.arronlong.httpclientutil.HttpClientUtil;
import com.arronlong.httpclientutil.builder.HCB;
import com.arronlong.httpclientutil.common.HttpConfig;
import com.arronlong.httpclientutil.common.Utils;
import com.arronlong.httpclientutil.exception.HttpProcessException;
import com.google.gson.JsonObject;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProxyHolder {

    private static final Logger logger = LoggerFactory.getLogger(ProxyHolder.class);

    private static final ThreadLocal<Map<String, HttpClient>> clients = new ThreadLocal<>();
    private static final ThreadLocal<HttpHost> currentProxy = new ThreadLocal<>();
    private static final ThreadLocal<Long> getProxyTime = ThreadLocal.withInitial(System::currentTimeMillis);

    private static final long timeOut = 60 * 1000 * 5; // 5分钟

    public static HttpClient getHttpClient(String url) {
        if (url.startsWith("https:")) {
            return getHttpClient(true);
        } else {
            return getHttpClient(false);
        }
    }

    public static HttpClient getHttpClient(boolean ssl) {
        Map<String, HttpClient> clientMap = clients.get();
        long current = System.currentTimeMillis();
        if (current > (getProxyTime.get() + timeOut) || Objects.isNull(clientMap)) {
            if (current > (getProxyTime.get() + timeOut)) {
                logger.debug("{} 代理过期..重新获取", Thread.currentThread().getName());
            }
            HttpHost proxy = _getProxy();
            try {
                clientMap = buildClients(proxy);
            } catch (Exception e) {
                return null;
            }
            clients.set(clientMap);
            currentProxy.set(proxy);
            getProxyTime.set(System.currentTimeMillis());
        }
        return getClient(clientMap, ssl);
    }

    public static void initHttpClient() {
        HttpHost proxy = _getProxy();
        Map<String, HttpClient> clientMap;
        try {
            clientMap = buildClients(proxy);
            clients.set(clientMap);
            currentProxy.set(proxy);
            getProxyTime.set(System.currentTimeMillis());
        } catch (HttpProcessException e) {
            Utils.errorException("创建https协议的HttpClient对象出错：{}", e);
        }
    }

    private static HttpClient getClient(Map<String, HttpClient> clientMap, boolean ssl) {
        if (ssl) {
            return clientMap.get("https");
        } else {
            return clientMap.get("http");
        }
    }

    private static Map<String, HttpClient> buildClients(HttpHost httpHost) throws HttpProcessException {
        Map<String, HttpClient> clientMap = new HashMap<>();
        if (Objects.isNull(httpHost)) {
            clientMap.put("http", HCB.custom().build());
            clientMap.put("https", HCB.custom().ssl().build());
            return clientMap;
        }
        clientMap.put("http", HCB.custom().proxy(httpHost.getHostName(), httpHost.getPort()).build());
        clientMap.put("https", HCB.custom().proxy(httpHost.getHostName(), httpHost.getPort()).ssl().build());
        return clientMap;
    }

    private static HttpHost _getProxy() {
        try {
            String res = HttpClientUtil.get(HttpConfig.custom().headers(Common.getHeard().build()).url(Common.getPropertiesKey("proxy.get.url")));
            logger.info(res);
            JsonObject json = Common.json().fromJson(res, JsonObject.class);
            String[] proxy = json.get("proxy").getAsString().split(":");
            return new HttpHost(proxy[0], Integer.parseInt(proxy[1]));
        } catch (HttpProcessException e) {
            logger.error("代理获取失败.....");
            return null;
        }
    }

    public static void deleteRemoteProxy(HttpHost proxy) {
        try {
            String url = Common.getPropertiesKey("proxy.del.url") + proxy.toHostString();
            HttpClientUtil.get(HttpConfig.custom().url(url));
        } catch (HttpProcessException e) {
            logger.error("代理删除失败");
        }
    }

    public static HttpHost getCurrentProxy() {
        return currentProxy.get();
    }
}
