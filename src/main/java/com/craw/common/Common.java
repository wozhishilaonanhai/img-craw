package com.craw.common;

import com.arronlong.httpclientutil.HttpClientUtil;
import com.arronlong.httpclientutil.builder.HCB;
import com.arronlong.httpclientutil.common.HttpConfig;
import com.arronlong.httpclientutil.common.HttpCookies;
import com.arronlong.httpclientutil.common.HttpHeader;
import com.arronlong.httpclientutil.common.Utils;
import com.arronlong.httpclientutil.common.util.PropertiesUtil;
import com.arronlong.httpclientutil.exception.HttpProcessException;
import com.craw.task.MainTask;
import com.craw.task.runnable.StopRunnable;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Common {

    private static final Logger logger = LoggerFactory.getLogger(Common.class);
    private static final Properties properties = PropertiesUtil.getProperties("config.properties");
    private static final Gson gson = new Gson();
    private ThreadLocal<Map<String, HttpClient>> clients = new ThreadLocal<>();

    public static HttpCookies getCookies() {
        HttpCookies cookies = HttpCookies.custom();
        String cookieStr = _getCookies();
        for (String cookie : cookieStr.split(";")) {
            String[] kv = cookie.split("=");
            if (kv.length > 1) {
                BasicClientCookie clientCookie = new BasicClientCookie(kv[0].trim(), kv[1].trim());
                clientCookie.setDomain(".weibo.com");
                cookies.getCookieStore().addCookie(clientCookie);
            }
        }
        return cookies;
    }

    private static String _getCookies() {
        try {
            URL resource = MainTask.class.getClassLoader().getResource("cookies.txt");
            if (Objects.isNull(resource)) {
                throw new FileNotFoundException("cookies 文件找不到");
            }
            return new String(Files.readAllBytes(Paths.get(resource.toURI())), StandardCharsets.UTF_8);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getPropertiesKey(String key) {
        return properties.getProperty(key);
    }

    public static HttpHeader getHeard() {
        return HttpHeader.custom().userAgent(Common.getPropertiesKey("http.head.user-agent"));
    }

    public static Gson json() {
        return gson;
    }

    public static String getResourcesPath() {
        URL resource = Common.class.getClassLoader().getResource("");
        Objects.requireNonNull(resource);
        return resource.getPath().substring(1);
    }

    public static String getResourcesPath(String name) {
        URL resource = Common.class.getClassLoader().getResource(name);
        Objects.requireNonNull(resource);
        return resource.getPath().substring(1);
    }

    public static <T> void takeRun(BlockingQueue<T> queue, String taskName, int sleep, TimeUnit unit, StopRunnable stop, Consumer<T> run) {
        Objects.requireNonNull(queue);
        Objects.requireNonNull(run);
        final Logger logger;
        if (Objects.isNull(taskName)) {
            taskName = Thread.currentThread().getName();
            logger = LoggerFactory.getLogger(taskName);
        } else {
            logger = LoggerFactory.getLogger("com.craw.task." + taskName);
        }

        stop = Objects.isNull(stop) ? () -> false : stop;

        while (!stop.needStop()) {
            try {
                T data = queue.poll(2, TimeUnit.SECONDS);
                if (Objects.nonNull(data)) {
                    logger.debug("{}【{}】收到任务 data = {}", Thread.currentThread().getName(), taskName, data.toString().length() > 400 ? data.toString().substring(0, 400) : data.toString());
                    run.accept(data);
                    unit.sleep(sleep);
                    continue;
                }
                logger.info("{}【{}】暂无任务...", Thread.currentThread().getName(), taskName);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("{}【{}】中断任务...", Thread.currentThread().getName(), taskName);
                return;
            }
        }
    }

    public static String strVal(String val) {
        return Objects.isNull(val) ? "" : val;
    }

    public static final class ConstellationUtils {
        private final static int[] dayArr = new int[]{20, 19, 21, 20, 21, 22, 23, 23, 23, 24, 23, 22};
        private final static String[] constellationArr = new String[]{"摩羯座", "水瓶座", "双鱼座", "白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座"};

        public static String getConstellation(int month, int day) {
            return day < dayArr[month - 1] ? constellationArr[month - 1] : constellationArr[month];
        }
    }

    public static Optional<String> matcher(Pattern pattern, String data, int groupIndex) {
        Matcher matcher = pattern.matcher(data);
        if (matcher.find()) {
            String res = matcher.group(groupIndex);
            return Optional.of(strVal(res));
        }
        return Optional.empty();
    }

    public static List<String> matchers(Pattern pattern, String data, int groupIndex) {
        Matcher matcher = pattern.matcher(data);
        List<String> res = new ArrayList<>();
        while (matcher.find()) {
            String val = matcher.group(groupIndex);
            if (Objects.isNull(val) || val.isEmpty()) {
                continue;
            }
            res.add(val);
        }
        return res;
    }

    public HttpClient getHttpClient(boolean ssl) {
        Map<String, HttpClient> clientMap = clients.get();
        if (Objects.isNull(clientMap)) {
            HttpHost proxy = getProxy();
            try {
                clientMap = buildClients(proxy);
            } catch (Exception e) {
                Utils.errorException("创建https协议的HttpClient对象出错：{}", e);
            }
            clients.set(clientMap);
        }
        if (ssl) {
            return clientMap.get("https");
        } else {
            return clientMap.get("http");
        }
    }

    private Map<String, HttpClient> buildClients(HttpHost httpHost) throws HttpProcessException {
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

    private static HttpHost getProxy() {
        try {
            String res = HttpClientUtil.get(HttpConfig.custom().url("http://127.0.0.1:5010/get/"));
            JsonObject json = Common.json().fromJson(res, JsonObject.class);
            String[] proxy = json.get("proxy").getAsString().split(":");
            return new HttpHost(proxy[0], Integer.parseInt(proxy[1]));
        } catch (HttpProcessException e) {
            logger.error("代理获取失败.....");
            return null;
        }
    }
}
