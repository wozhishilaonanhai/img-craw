package com.craw.task;

import com.craw.model.User;

import java.util.Queue;

/**
 * 负责存储用户信息
 */
public class StoreTask implements Runnable {

    private final Queue<User> userQueue;

    public StoreTask(Queue<User> userQueue) {
        this.userQueue = userQueue;
    }

    @Override
    public void run() {

    }
}
