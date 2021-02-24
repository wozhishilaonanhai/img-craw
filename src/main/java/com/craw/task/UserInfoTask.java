package com.craw.task;

import com.arronlong.httpclientutil.HttpClientUtil;
import com.arronlong.httpclientutil.common.HttpConfig;
import com.arronlong.httpclientutil.exception.HttpProcessException;
import com.craw.common.Common;
import com.craw.model.User;
import com.craw.task.runnable.NameRunnable;
import com.google.gson.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 负责获取其他详细信息
 */
public class UserInfoTask implements NameRunnable {

    private static final Logger logger = LoggerFactory.getLogger(UserInfoTask.class);

    private static final String INFO_URL = "https://weibo.com/p/100505%s/info?mod=pedit_more";

    private static final Pattern EXTRACT_HTML = Pattern.compile("<script>FM\\.view\\((.*\"domid\":\"Pl_Official_PersonalInfo__57\".*)\\)</script>");

    private final BlockingQueue<User> userInfoQueue;
    private final BlockingQueue<User> userStoreQueue;

    public UserInfoTask(BlockingQueue<User> userInfoQueue, BlockingQueue<User> userStoreQueue) {
        Objects.requireNonNull(userInfoQueue);
        Objects.requireNonNull(userStoreQueue);
        this.userInfoQueue = userInfoQueue;
        this.userStoreQueue = userStoreQueue;
    }

    @Override
    public String getName() {
        return "用户详细信息获取任务";
    }

    @Override
    public void run() {
        Common.takeRun(userInfoQueue, getName(), 200, TimeUnit.MILLISECONDS, null, (user) -> {
            String html = getUserInfoData(user).orElseThrow(RuntimeException::new);
            User newUser = parseUserInfoHtml(html, user.clone());
            try {
                while (!userStoreQueue.offer(newUser, 2, TimeUnit.SECONDS)) {
                    logger.warn("【{}】userStoreQueue 队列已满，正在等待重试入队", getName());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("【{}】中断任务", getName());
            }
        });
    }

    private Optional<String> getUserInfoData(User user) {
        String url = String.format(INFO_URL, user.getWbUserId());
        try {
            return Optional.of(HttpClientUtil.get(HttpConfig.custom().url(url).context(Common.getCookies().getContext()).headers(Common.getHeard().build())));
        } catch (HttpProcessException e) {
            logger.error("【{}】获取用户详细信息地址请求失败， user={}", getName(), user, e);
            return Optional.empty();
        }
    }

    private User parseUserInfoHtml(String html, User user) {
        Matcher matcher = EXTRACT_HTML.matcher(html);
        if (matcher.find()) {
            String jsonData = matcher.group(1);
            JsonObject jsonObject = Common.json().fromJson(jsonData, JsonObject.class);
            Document document = Jsoup.parse(jsonObject.get("html").getAsString());
            @SuppressWarnings("unchecked")
            Map<String, String> cache = Common.json().fromJson(Common.json().toJson(user), Map.class);
            for (Element element : document.getElementsByClass("li_1 clearfix")) {
                Element title = element.getElementsByClass("pt_title S_txt2").get(0);
                Element val = element.getElementsByClass("pt_detail").get(0);
                setUser(cache, title.text(), val.text());
            }
            User newUser = setTags(Common.json().fromJson(Common.json().toJson(cache), User.class), document);
            return newUser.initConstellation();
        }
        return user;
    }

    private void setUser(Map<String, String> user, String title, String val) {
        InfoLabel infoLabel = InfoLabel.get(title);
        if (Objects.nonNull(infoLabel)) {
            user.put(infoLabel.fieldName, val.trim());
        }
    }

    private User setTags(User user, Document document) {
        Elements tagDocuments = document.getElementsByClass("W_btn_tag");
        List<String> tags = new ArrayList<>(tagDocuments.size());
        for (Element tagDocument : tagDocuments) {
            tags.add(tagDocument.text().trim());
        }
        return user.setTags(tags);
    }

    enum InfoLabel {
        NAME("昵称", "name"),
        SEX("性别", "sex"),
        BIRTHDAY("生日", "birthday"),
        SITE("所在地", "site");

        private final String name;
        private final String fieldName;

        InfoLabel(String name, String fieldName) {
            this.name = name;
            this.fieldName = fieldName;
        }

        public static InfoLabel get(String name) {
            if (Objects.isNull(name)) {
                return null;
            }
            for (InfoLabel value : values()) {
                if (name.contains(value.name)) {
                    return value;
                }
            }
            return null;
        }
    }
}
