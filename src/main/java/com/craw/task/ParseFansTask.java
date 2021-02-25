package com.craw.task;

import com.craw.ShareStore;
import com.craw.common.Common;
import com.craw.model.Img;
import com.craw.model.User;
import com.craw.task.runnable.NameRunnable;
import com.craw.task.runnable.StopRunnable;
import com.google.gson.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析粉丝列表任务，并分发任务
 */
public class ParseFansTask implements NameRunnable, StopRunnable {

    private static final Logger logger = LoggerFactory.getLogger(ParseFansTask.class);
    private static final Pattern FANS_LIST_REX = Pattern.compile("<script>parent\\.FM\\.view\\((.*\"domid\":\"Pl_Official_HisRelation__\\d+\".*)\\)</script>");

    private final BlockingQueue<String> dataQueue;
    private final BlockingQueue<Img> imgDownQueue;
    private final BlockingQueue<User> userInfoQueue;
    private final BlockingQueue<String> notifyStopQueue;

    // 并非严格要求必须最大数是maxCount。结果最多会比预期多一点而已
    private final int maxCount;

    public ParseFansTask(int maxCount,
                         BlockingQueue<String> dataQueue,
                         BlockingQueue<Img> imgDownQueue,
                         BlockingQueue<User> userInfoQueue,
                         BlockingQueue<String> notifyStopQueue) {
        Objects.requireNonNull(dataQueue);
        Objects.requireNonNull(imgDownQueue);
        Objects.requireNonNull(userInfoQueue);
        this.maxCount = maxCount;
        this.dataQueue = dataQueue;
        this.imgDownQueue = imgDownQueue;
        this.userInfoQueue = userInfoQueue;
        this.notifyStopQueue = notifyStopQueue;
    }

    @Override
    public String getName() {
        return "解析粉丝列表任务";
    }

    @Override
    public void run() {
        Common.takeRun(dataQueue, getName(), 0, TimeUnit.SECONDS, this, (data) -> {
            try {
                this.parseFansListData(data);
            } catch (Exception e) {
                logger.error("【{}】发生意外，停止任务，并发送上层任务停止通知", getName(), e);
                this.sendStopNotify();
            }
        });
    }

    @Override
    public boolean needStop() {
        boolean isNeed = ShareStore.currentCountGet() > this.maxCount;
        if (isNeed && Objects.nonNull(this.notifyStopQueue)) {
            logger.info("【{}】已达到指定任务数据，发送通知并停止本任务-------------------------------", getName());
            this.sendStopNotify();
        }
        return isNeed;
    }

    private void sendStopNotify() {
        this.notifyStopQueue.offer("stop");
    }

    private void parseFansListData(String data) {
        parseFansHtml(data, user -> {
            if (Objects.isNull(user.getWbUserId()) || ShareStore.isContainsFans(user.getWbUserId())) {
                // 这里判断重复并不准确，多线程下可能失效，但错误数量不会多，可以忍受
                return;
            }
            Img imgQ = new Img(user.getImg(), user.getImgId());
            try {
                while (!imgDownQueue.offer(imgQ, 2, TimeUnit.SECONDS)) {
                    logger.warn("【{}】imgDownQueue 队列已满，正在等待重试入队", getName());
                }
                while (!userInfoQueue.offer(user, 20, TimeUnit.SECONDS)) {
                    logger.warn("【{}】userInfoQueue 队列已满，正在等待重试入队", getName());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            ShareStore.addFans(user.getWbUserId());
            ShareStore.currentCountIncrementAndGet();
        });
    }

    private void parseFansHtml(String data, Consumer<User> each) {
        Matcher matcher = FANS_LIST_REX.matcher(data);
        if (matcher.find()) {
            String jsonData = matcher.group(1);
            JsonObject jsonObject = Common.json().fromJson(jsonData, JsonObject.class);
            Document document = Jsoup.parse(jsonObject.get("html").getAsString());
            for (Element element : document.getElementsByClass("follow_item S_line2")) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                Element modPic = element.getElementsByClass("mod_pic").get(0);
                Element img = modPic.getElementsByTag("img").get(0);
                Element nameElement = modPic.getElementsByTag("a").get(0);
                String src = img.attr("src");
                String alt = img.attr("alt");
                String href = nameElement.attr("href");
                if (src.contains("default_avatar") || alt.contains("用户")) {
                    continue;
                }

                if (each != null) {
                    each.accept(new User()
                            .setName(alt)
                            .setDetailsUrl(href)
                            .setImg(src)
                            .initWbUserId()
                            .initImgId());
                }
            }
            logger.info("【{}】当前总数 {}", getName(), ShareStore.currentCountGet());
        } else {
            logger.error("【{}】html 解析失败，可能登录过期, 或地址id不正确", getName());
        }
    }

    public static void main(String[] args) throws InterruptedException {
        LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>(22);
        Thread.currentThread().interrupt();
        boolean sss = queue.offer("ssss", 5, TimeUnit.SECONDS);
        System.out.println(sss);
        sss = queue.offer("ssss", 5, TimeUnit.SECONDS);
        System.out.println(sss);
    }
}
