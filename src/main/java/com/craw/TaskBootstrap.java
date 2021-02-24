package com.craw;

import com.craw.model.Img;
import com.craw.model.User;
import com.craw.task.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.LockSupport;

public class TaskBootstrap {

    public TaskBootstrap() {
    }

    public void start() {
        BlockingQueue<String> finsSearchQueue = new LinkedBlockingQueue<>(1000);
        BlockingQueue<String> finsDateQueue = new LinkedBlockingQueue<>(1000);
        BlockingQueue<Img> imgDownQueue = new LinkedBlockingQueue<>(1000);
        BlockingQueue<User> userInfoQueue = new LinkedBlockingQueue<>(1000);
        BlockingQueue<User> userStoreQueue = new LinkedBlockingQueue<>(1000);

        finsSearchQueue.add("2360812967");
        MainTask mainTask = new MainTask(finsSearchQueue, finsDateQueue);
        ParseFansTask parseFansTask = new ParseFansTask(100, finsDateQueue, imgDownQueue, userInfoQueue, mainTask.getStopQueue(), finsSearchQueue);
        ImgTask imgTask = new ImgTask(imgDownQueue);
        UserInfoTask userInfoTask = new UserInfoTask(userInfoQueue, userStoreQueue);
        StoreTask storeTask = new StoreTask(userStoreQueue);

        ExecutorService service = Executors.newCachedThreadPool();
        service.execute(mainTask);
        service.execute(parseFansTask);
        service.execute(imgTask);

        service.execute(userInfoTask);
        service.execute(userInfoTask);
        service.execute(userInfoTask);

        service.execute(storeTask);
        service.execute(storeTask);

        LockSupport.park();
    }

}
