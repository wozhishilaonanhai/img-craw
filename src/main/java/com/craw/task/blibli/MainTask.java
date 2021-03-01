package com.craw.task.blibli;

import com.craw.common.Common;
import com.craw.model.BlibliUser;
import com.craw.model.Img;
import com.craw.task.runnable.StopRunnable;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class MainTask extends CheckTask implements StopRunnable {

    private static final Logger logger = LoggerFactory.getLogger(MainTask.class);

    private static final String USER_INFO_URL = "https://api.bilibili.com/x/space/acc/info?mid=%s&jsonp=jsonp";

    private final BlockingQueue<BlibliUser> fansNumQueue;
    private final BlockingQueue<BlibliUser> userStoreQueue;
    private final BlockingQueue<String> stopQueue;
    private final BlockingQueue<Img> imgDownQueue;

    private final int minUserId;
    private final int maxUserId;

    public MainTask(int minUserId, int maxUserId,
                    BlockingQueue<BlibliUser> fansNumQueue,
                    BlockingQueue<BlibliUser> userStoreQueue,
                    BlockingQueue<String> stopQueue,
                    BlockingQueue<Img> imgDownQueue) {
        Objects.requireNonNull(fansNumQueue);
        Objects.requireNonNull(userStoreQueue);
        this.maxUserId = maxUserId;
        this.minUserId = minUserId;
        this.fansNumQueue = fansNumQueue;
        this.userStoreQueue = userStoreQueue;
        this.stopQueue = stopQueue;
        this.imgDownQueue = imgDownQueue;
    }

    @Override
    public String getName() {
        return "用户获取任务";
    }

    @Override
    public void run() {
        for (int i = minUserId; i < maxUserId && !Thread.currentThread().isInterrupted(); i++) {
            try {
                if (have(i)) {
                    continue;
                }
                TimeUnit.MILLISECONDS.sleep(Integer.parseInt(Common.getPropertiesKey("task.sleep.blibli","3000")));

                String url = String.format(USER_INFO_URL, i);
                String res = getData(url).orElseThrow(Exception::new);
                JsonObject json = checkData(res, String.valueOf(i));
                if (Objects.isNull(json)) {
                    continue;
                }
                BlibliUser user = buildUser(json);
                if (user.getImg().contains("noface")) {
                    continue;
                }
                while (!userStoreQueue.offer(user, 2, TimeUnit.SECONDS)) {
                    logger.warn("【{}】userStoreQueue 队列已满，等待重新入队", getName());
                }
                while (!imgDownQueue.offer(new Img(user.getImg(), user.getImgId()), 2, TimeUnit.SECONDS)) {
                    logger.warn("【{}】imgDownQueue 队列已满，等待重新入队", getName());
                }
                while (!fansNumQueue.offer(user, 2, TimeUnit.SECONDS)) {
                    logger.warn("【{}】fansNumQueue 队列已满，等待重新入队", getName());
                }
            } catch (Exception e) {
                logger.error("【{}】获取用户信息时异常.. userId={} ", getName(), i, e);
                System.exit(1);
            }
        }
    }

    private BlibliUser buildUser(JsonObject data) {
        return new BlibliUser().setBirthday(data.get("birthday").getAsString())
                .setImg(data.get("face").getAsString())
                .setUserId(data.get("mid").getAsString())
                .setLevel(data.get("level").getAsInt())
                .setImgId(data.get("mid").getAsString())
                .setSex(data.get("sex").getAsString())
                .setName(data.get("name").getAsString())
                .initConstellation()
                .setAllData(data.toString());
    }

    private boolean have(int userId) {
        String sql = String.format("SELECT COUNT(1) FROM blibli_user WHERE userId = '%s'", userId);
        try (Connection conn = StoreTask.dataSource.getConnection()) {
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean needStop() {
        return Objects.isNull(stopQueue) || !stopQueue.isEmpty();
    }
}
