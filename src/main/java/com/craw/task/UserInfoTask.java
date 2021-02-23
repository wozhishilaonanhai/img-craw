package com.craw.task;

import com.craw.common.Common;
import com.craw.model.User;
import com.craw.task.runnable.NameRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 负责获取其他详细信息
 */
public class UserInfoTask implements NameRunnable {

    private static final Logger logger = LoggerFactory.getLogger(UserInfoTask.class);

    private final BlockingQueue<User> userInfoQueue;

    public UserInfoTask(BlockingQueue<User> userInfoQueue) {
        Objects.requireNonNull(userInfoQueue);
        this.userInfoQueue = userInfoQueue;
    }

    @Override
    public String getName() {
        return "用户详细信息获取任务";
    }

    @Override
    public void run() {
        Common.takeRun(userInfoQueue, getName(), 200, TimeUnit.MILLISECONDS, null, (user) -> {

        });
    }
}
