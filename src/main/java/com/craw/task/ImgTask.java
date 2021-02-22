package com.craw.task;

import com.craw.model.User;

import java.util.Queue;

/**
 * 负责下载图片
 */
public class ImgTask implements Runnable {

    private final Queue<User> imgDownQueue;

    public ImgTask(Queue<User> imgDownQueue) {
        this.imgDownQueue = imgDownQueue;
    }

    @Override
    public void run() {

    }
}
