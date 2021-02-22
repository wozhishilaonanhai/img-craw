package com.craw.task;

import com.arronlong.httpclientutil.HttpClientUtil;
import com.arronlong.httpclientutil.common.HttpConfig;
import com.arronlong.httpclientutil.common.HttpCookies;
import com.arronlong.httpclientutil.exception.HttpProcessException;
import com.craw.common.Common;
import com.craw.model.Img;
import com.craw.model.User;
import com.google.gson.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 负责获取粉丝地址，并分发任务
 */
public class MainTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(MainTask.class);

    private static final Pattern FANS_LIST_REX = Pattern.compile("^<script>parent\\.FM\\.view\\((.*)\\)</script>$");
    private static final String fansList = "https://weibo.com/p/1006052360812967/follow?pids=Pl_Official_HisRelation__61&relate=fans&ajaxpagelet=1&ajaxpagelet_v6=1&__ref=%2Fp%2F1006052360812967%2Ffollow%3Frelate%3Dfans%26page%3D2%23Pl_Official_HisRelation__61&_t=FM_161399559700341&page=";
    private static final HttpCookies cookies = Common.getCookies();

    private final BlockingQueue<Img> imgDownQueue;
    private final BlockingQueue<User> userInfoQueue;

    // 并非严格要求必须最大数是maxCount。结果最多会比预期多一点而已
    private final int maxCount;
    private int currentCount;

    // 当前页面翻页游标最大是多少
    private int currentPageMaxSize = 100000;

    /**
     * 遍历粉丝列表地址
     *
     * @param count         准备获取的粉丝数目
     * @param imgDownQueue  图像下载队列
     * @param userInfoQueue 用户信息获取队列
     */
    public MainTask(int count, BlockingQueue<Img> imgDownQueue, BlockingQueue<User> userInfoQueue) {
        Objects.requireNonNull(imgDownQueue);
        Objects.requireNonNull(userInfoQueue);
        this.maxCount = count;
        this.currentCount = 0;
        this.imgDownQueue = imgDownQueue;
        this.userInfoQueue = userInfoQueue;
    }

    @Override
    public void run() {
        for (int page = 0; page < currentPageMaxSize && this.currentCount < maxCount; page++) {
            String fansList = getFansList(page).orElseThrow(RuntimeException::new);
            parseFansListData(fansList);
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private Optional<String> getFansList(int page) {
        try {
            return Optional.of(HttpClientUtil.get(HttpConfig.custom().url(fansList + page)
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
                if (user.getImg().contains("default_avatar")){
                    continue;
                }
                user.setImgId(UUID.randomUUID().toString().replace("-", ""));
                user.setName(img.attr("alt"));
                user.setDetailsUrl(nameElement.attr("href"));
                user.initWbUserId();
                Img imgQ = new Img(user.getImg(), user.getImgId());
                imgDownQueue.offer(imgQ);
                userInfoQueue.offer(user);
                userList.add(user);
                currentCount++;
            }
            Element wPages = document.getElementsByClass("W_pages").get(0);
            Elements pageList = wPages.getElementsByClass("page S_txt1");
            if (pageList.size() > 0) {
                Element element = pageList.get(pageList.size() - 1);
                String val = element.text();
                currentPageMaxSize = Integer.parseInt(val);
            }
        }
    }


    public static void main(String[] args) throws IOException {
        BlockingQueue<User> uiQ = new LinkedBlockingQueue<>();
        BlockingQueue<Img> imgQ = new LinkedBlockingQueue<>();
        MainTask mainTask = new MainTask(20, imgQ, uiQ);
        mainTask.run();
    }

}
