package com.craw.task;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.craw.ShareStore;
import com.craw.common.Common;
import com.craw.model.User;
import com.craw.task.runnable.NameRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 负责存储用户信息
 */
public class StoreTask implements NameRunnable {

    private static final Logger logger = LoggerFactory.getLogger(StoreTask.class);
    private static final String SQL_FORMAT = "INSERT INTO `user` VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s' , '%s', '%s')";

    private final BlockingQueue<User> userQueue;
    private final BlockingQueue<String> finsSearchQueue;

    private static DataSource dataSource;

    static {
        init();
    }

    private static void init() {
        try {
            dataSource = DruidDataSourceFactory.createDataSource(Common.getProperties());
        } catch (Exception e) {
            logger.error("【信息存储任务】数据库初始化异常...");
        }
    }

    public StoreTask(BlockingQueue<User> userQueue, BlockingQueue<String> finsSearchQueue) {
        this.userQueue = userQueue;
        this.finsSearchQueue = finsSearchQueue;
    }

    @Override
    public String getName() {
        return "信息存储任务";
    }

    @Override
    public void run() {
        Common.takeRun(userQueue, getName(), 0, TimeUnit.MILLISECONDS, null, this::saveData);
    }

    private void saveData(User user) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            Statement statement = conn.createStatement();
            int res = statement.executeUpdate(String.format(SQL_FORMAT,
                    Common.strVal(user.getWbUserId()),
                    Common.strVal(user.getImgId()),
                    Common.strVal(user.getName()),
                    Common.strVal(user.getSex()),
                    Common.strVal(user.getSite()),
                    Common.strVal(user.getBirthday()),
                    Common.strVal(user.getConstellation()),
                    String.join(",", user.getTags().toArray(new String[]{})),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    Common.strVal(user.getImg()),
                    user.getFansNum(),
                    user.getAttentionNum(),
                    user.getWbNum()));
            conn.commit();
            if (res <= 0) {
                logger.warn("【{}】数据存储失败 user={}", getName(), user);
                return;
            }
            if (user.getFansNum() > 0 && !finsSearchQueue.offer("100505" + user.getWbUserId() + "|" + "Pl_Official_HisRelation__59", 2, TimeUnit.SECONDS)) {
                logger.debug("【{}】finsSearchQueue 队列已满，停止入队", getName());
            }
            ShareStore.currentCountIncrementAndGet();
            logger.info("【{}】数据存储成功 userId = {}", getName(), user.getWbUserId());
        } catch (SQLException e) {
            logger.error("【{}】sql 存储异常 user={}", getName(), user, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("【{}】中断任务", getName());
        }
    }
}
