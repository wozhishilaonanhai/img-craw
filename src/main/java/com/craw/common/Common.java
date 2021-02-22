package com.craw.common;

import com.arronlong.httpclientutil.common.HttpCookies;
import com.arronlong.httpclientutil.common.HttpHeader;
import com.arronlong.httpclientutil.common.util.PropertiesUtil;
import com.craw.task.MainTask;
import com.google.gson.Gson;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

public class Common {

    private static final Properties properties = PropertiesUtil.getProperties("config.properties");
    private static final Gson gson = new Gson();

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
}
