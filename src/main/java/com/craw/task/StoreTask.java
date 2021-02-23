package com.craw.task;

import com.craw.model.User;
import com.craw.task.runnable.NameRunnable;

import java.util.concurrent.BlockingQueue;

/**
 * 负责存储用户信息
 */
public class StoreTask implements NameRunnable {

    private final BlockingQueue<User> userQueue;

    public StoreTask(BlockingQueue<User> userQueue) {
        this.userQueue = userQueue;
    }

    @Override
    public String getName() {
        return "信息存储任务";
    }

    @Override
    public void run() {

    }
}
