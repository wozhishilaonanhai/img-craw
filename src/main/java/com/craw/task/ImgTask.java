package com.craw.task;

import com.craw.model.Img;
import com.craw.model.User;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;

/**
 * 负责下载图片
 */
public class ImgTask implements Runnable {

    private final BlockingQueue<Img> imgDownQueue;

    public ImgTask(BlockingQueue<Img> imgDownQueue) {
        this.imgDownQueue = imgDownQueue;
    }

    @Override
    public void run() {

    }
}
