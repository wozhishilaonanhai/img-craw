package com.craw.task.blibli;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.craw.common.Common;
import com.craw.model.BlibliUser;
import com.craw.task.runnable.NameRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 负责存储用户信息
 */
public class StoreTask implements NameRunnable {

    private static final Logger logger = LoggerFactory.getLogger(StoreTask.class);
    private static final String INSERT_SQL_FORMAT = "INSERT INTO `blibli_user` VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', null, null, '%s' , '%s', null)";
    private static final String UPDATE_SQL_FORMAT = "UPDATE blibli_user SET `fansNum`= '%s' ,`attentionNum`='%s',`update_time`='%s' where userId = '%s'";

    private final BlockingQueue<BlibliUser> userQueue;

    public static DataSource dataSource;

    static {
        init();
    }

    private static void init() {
        try {
            Properties properties = new Properties();
            properties.load(StoreTask.class.getClassLoader().getResourceAsStream("druid.properties"));
            dataSource = DruidDataSourceFactory.createDataSource(properties);
        } catch (Exception e) {
            logger.error("【信息存储任务】数据库初始化异常...");
        }
    }

    public StoreTask(BlockingQueue<BlibliUser> userQueue) {
        this.userQueue = userQueue;
    }

    @Override
    public String getName() {
        return "信息存储任务";
    }

    @Override
    public void run() {
        Common.takeRun(userQueue, getName(), 0, TimeUnit.MILLISECONDS, null, this::saveData);
    }

    private void saveData(BlibliUser user) {
        String sql = getSql(user);
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            Statement statement = conn.createStatement();
            int res = statement.executeUpdate(sql);
            conn.commit();
            if (res <= 0) {
                logger.warn("【{}】数据存储失败 user={} sql={}", getName(), user, sql);
                return;
            }
            logger.info("【{}】数据存储成功 userId = {}", getName(), user.getUserId());
        } catch (SQLIntegrityConstraintViolationException e) {
            logger.warn("【{}】sql 存储异常 重复用户 user={} ", getName(), user);
        } catch (SQLException e) {
            logger.error("【{}】sql 存储异常 user={} sql={}", getName(), user, sql, e);
        }
    }

    private String getSql(BlibliUser user) {
        if (Objects.isNull(user.getFansInfo())) {
            return String.format(INSERT_SQL_FORMAT,
                    Common.strVal(user.getUserId()),
                    Common.strVal(user.getName()),
                    Common.strVal(user.getSex()),
                    Common.strVal(user.getBirthday()),
                    Common.strVal(user.getConstellation()),
                    Common.strVal(user.getImg()),
                    Common.strVal(user.getImgId()),
                    user.getLevel(),
                    user.getAllData(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } else {
            return String.format(UPDATE_SQL_FORMAT,
                    user.getFansInfo().getFansNum(),
                    user.getFansInfo().getAttentionNum(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    Common.strVal(user.getUserId()));
        }
    }
}
