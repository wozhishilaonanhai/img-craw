package com.craw.task;

import com.craw.model.User;

import java.util.Queue;

/**
 * 负责获取其他详细信息
 */
public class UserInfoTask implements Runnable {

    private final Queue<User> userInfoQueue;

    public UserInfoTask(Queue<User> userInfoQueue) {
        this.userInfoQueue = userInfoQueue;
    }

    @Override
    public void run() {

    }
}
