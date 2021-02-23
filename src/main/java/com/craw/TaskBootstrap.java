package com.craw;

import com.craw.model.Img;
import com.craw.model.User;
import com.craw.task.ImgTask;
import com.craw.task.MainTask;
import com.craw.task.ParseFansTask;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.LockSupport;

public class TaskBootstrap {

    public TaskBootstrap() {
    }

    public void start() {
        BlockingQueue<String> finsDateQueue = new LinkedBlockingQueue<>(1000);
        BlockingQueue<Img> imgDownQueue = new LinkedBlockingQueue<>(1000);
        BlockingQueue<User> userInfoQueue = new LinkedBlockingQueue<>(1000);

        MainTask mainTask = new MainTask(finsDateQueue);
        ParseFansTask parseFansTask = new ParseFansTask(100, finsDateQueue, imgDownQueue, userInfoQueue, mainTask.getStopQueue());
        ImgTask imgTask = new ImgTask(imgDownQueue);
        ExecutorService service = Executors.newCachedThreadPool();
        service.execute(mainTask);
        service.execute(parseFansTask);
        service.execute(imgTask);

        LockSupport.park();
    }

}
