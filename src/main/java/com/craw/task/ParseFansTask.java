package com.craw.task;

import com.craw.ShareStore;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析粉丝列表任务
 */
public class ParseFansTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ParseFansTask.class);

    private static final Pattern FANS_LIST_REX = Pattern.compile("^<script>parent\\.FM\\.view\\((.*)\\)</script>$");

    private final BlockingQueue<String> dataQueue;
    private final BlockingQueue<Img> imgDownQueue;
    private final BlockingQueue<User> userInfoQueue;
    private final BlockingQueue<String> notifyStopQueue;

    // 并非严格要求必须最大数是maxCount。结果最多会比预期多一点而已
    private final int maxCount;
    private int currentCount;

    public ParseFansTask(int maxCount,
                         BlockingQueue<String> dataQueue,
                         BlockingQueue<Img> imgDownQueue,
                         BlockingQueue<User> userInfoQueue,
                         BlockingQueue<String> notifyStopQueue) {
        this.maxCount = maxCount;
        this.dataQueue = dataQueue;
        this.imgDownQueue = imgDownQueue;
        this.userInfoQueue = userInfoQueue;
        this.notifyStopQueue = notifyStopQueue;
        this.currentCount = 0;
    }

    @Override
    public void run() {
        while (!needStop()) {
            try {
                String data = dataQueue.poll(1, TimeUnit.SECONDS);
                if (Objects.nonNull(data)) {
                    logger.debug("【解析粉丝列表任务】收到解析任务 字符串大小 {}", data.length());
                    parseFansListData(data);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("【解析粉丝列表任务】中断任务...");
                return;
            }
        }
    }

    private boolean needStop() {
        boolean isNeed = this.currentCount > this.maxCount;
        if (isNeed && Objects.nonNull(this.notifyStopQueue)) {
            this.notifyStopQueue.offer("stop");
        }
        return isNeed;
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
                if (user.getImg().contains("default_avatar")) {
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
            logger.info("【解析粉丝列表任务】当前总数 {}", currentCount);
            Element wPages = document.getElementsByClass("W_pages").get(0);
            Elements pageList = wPages.getElementsByClass("page S_txt1");
            if (pageList.size() > 0) {
                Element element = pageList.get(pageList.size() - 1);
                String val = element.text();
                ShareStore.setCurrentPageMaxSize(Integer.parseInt(val));
                logger.info("【解析粉丝列表任务】当前总页数 {}", ShareStore.getCurrentPageMaxSize());
            }
        }
    }
}
