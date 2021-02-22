package com.craw.task;

import com.arronlong.httpclientutil.HttpClientUtil;
import com.arronlong.httpclientutil.common.HttpConfig;
import com.arronlong.httpclientutil.common.HttpCookies;
import com.arronlong.httpclientutil.exception.HttpProcessException;
import com.craw.model.User;
import com.google.gson.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.craw.common.Common;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 负责获取粉丝地址，并分发任务
 */
public class MainTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(MainTask.class);

    private static final Pattern FANS_LIST_REX = Pattern.compile("^<script>parent\\.FM\\.view\\((.*)\\)</script>$");
    private static final String fansList = "https://weibo.com/p/1006052360812967/follow?pids=Pl_Official_HisRelation__61&relate=fans&page=3&ajaxpagelet=1&ajaxpagelet_v6=1&__ref=%2Fp%2F1006052360812967%2Ffollow%3Frelate%3Dfans%26page%3D2%23Pl_Official_HisRelation__61&_t=FM_161399559700341";
    private static final HttpCookies cookies = Common.getCookies();

    private final Queue<User> imgDownQueue;
    private final Queue<User> userInfoQueue;

    private int maxCount;

    /**
     * 遍历粉丝列表地址
     *
     * @param count         准备获取的粉丝数目
     * @param imgDownQueue  图像下载队列
     * @param userInfoQueue 用户信息获取队列
     */
    public MainTask(int count, Queue<User> imgDownQueue, Queue<User> userInfoQueue) {
        this.maxCount = count;
        this.imgDownQueue = imgDownQueue;
        this.userInfoQueue = userInfoQueue;
    }

    @Override
    public void run() {
        String fansList = getFansList().orElseThrow(RuntimeException::new);
        parseFansListData(fansList);
    }

    private void pageTurning() {

    }

    private Optional<String> getFansList() {
        try {
            return Optional.of(HttpClientUtil.get(HttpConfig.custom().url(fansList)
                    .context(cookies.getContext())
                    .headers(Common.getHeard().build())));
        } catch (HttpProcessException e) {
            return Optional.empty();
        }
    }

    private void parseFansListData(String data) {
        Matcher matcher = FANS_LIST_REX.matcher(data);
        if (matcher.find()) {
            String jsonData = matcher.group(1);
            JsonObject jsonObject = Common.json().fromJson(jsonData, JsonObject.class);
            String htmlData = jsonObject.get("html").getAsString();
            Document document = Jsoup.parse(htmlData);
            List<User> userList = new ArrayList<>();
            for (Element element : document.getElementsByClass("follow_item S_line2")) {
                User user = new User();
                Element modPic = element.getElementsByClass("mod_pic").get(0);
                Element img = modPic.getElementsByTag("img").get(0);
                Element nameElement = modPic.getElementsByTag("a").get(0);
                user.setImg(img.attr("src"));
                user.setImgId(UUID.randomUUID().toString().replace("-", ""));
                user.setName(img.attr("alt"));
                user.setDetailsUrl(nameElement.attr("href"));
                userList.add(user);
            }
        }
    }


    public static void main(String[] args) throws IOException {
//        MainTask mainTask = new MainTask();
//        mainTask.run();
        System.out.println(UUID.randomUUID().toString().replace("-", ""));
    }

}
