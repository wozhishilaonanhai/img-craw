package com.craw.task;

import com.craw.model.Img;
import com.craw.model.User;

import java.util.Queue;

/**
 * 负责下载图片
 */
public class ImgTask implements Runnable {

    private final Queue<Img> imgDownQueue;

    public ImgTask(Queue<Img> imgDownQueue) {
        this.imgDownQueue = imgDownQueue;
    }

    @Override
    public void run() {

    }
}
