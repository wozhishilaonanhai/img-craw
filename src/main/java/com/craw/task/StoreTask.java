package com.craw.task;

import com.craw.model.User;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;

/**
 * 负责存储用户信息
 */
public class StoreTask implements Runnable {

    private final BlockingQueue<User> userQueue;

    public StoreTask(BlockingQueue<User> userQueue) {
        this.userQueue = userQueue;
    }

    @Override
    public void run() {

    }
}
