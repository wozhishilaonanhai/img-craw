package com.craw.task;

import com.craw.model.User;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;

/**
 * 负责获取其他详细信息
 */
public class UserInfoTask implements Runnable {

    private final BlockingQueue<User> userInfoQueue;

    public UserInfoTask(BlockingQueue<User> userInfoQueue) {
        this.userInfoQueue = userInfoQueue;
    }

    @Override
    public void run() {

    }
}
