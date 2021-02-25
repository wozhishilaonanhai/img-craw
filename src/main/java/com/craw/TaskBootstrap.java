package com.craw;

import com.craw.model.Img;
import com.craw.model.User;
import com.craw.task.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

public class TaskBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(TaskBootstrap.class);

    public TaskBootstrap() {
    }

    public void start() {
        BlockingQueue<String> finsSearchQueue = new LinkedBlockingQueue<>(100);
        BlockingQueue<String> finsDateQueue = new LinkedBlockingQueue<>(1000);
        BlockingQueue<Img> imgDownQueue = new LinkedBlockingQueue<>(1000);
        BlockingQueue<User> userInfoQueue = new LinkedBlockingQueue<>(2000);
        BlockingQueue<User> userStoreQueue = new LinkedBlockingQueue<>(1000);

        finsSearchQueue.add("1003061192329374|Pl_Official_HisRelation__58");
        MainTask mainTask = new MainTask(finsSearchQueue, finsDateQueue);
        ParseFansTask parseFansTask = new ParseFansTask(500000, finsDateQueue, imgDownQueue, userInfoQueue, mainTask.getStopQueue());
        ImgTask imgTask = new ImgTask(imgDownQueue);
        UserInfoTask userInfoTask = new UserInfoTask(userInfoQueue, userStoreQueue, finsSearchQueue);
        StoreTask storeTask = new StoreTask(userStoreQueue);

        ExecutorService service = Executors.newCachedThreadPool();
        service.execute(mainTask);

        service.execute(parseFansTask);
        service.execute(parseFansTask);

        service.execute(imgTask);

        service.execute(userInfoTask);

        service.execute(storeTask);
        service.execute(storeTask);

        service.execute(() -> {
            while (true) {
                logger.info("\n========队列监控=========\nfinsSearchQueue大小：{}\nfinsDateQueue大小：{}\nimgDownQueue大小：{}\nuserInfoQueue大小：{}\nuserStoreQueue大小：{}\nallFansSet大小：{}\n===================",
                        finsSearchQueue.size(), finsDateQueue.size(), imgDownQueue.size(), userInfoQueue.size(), userStoreQueue.size(), ShareStore.addFansSize());
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("中断异常，监控关闭...");
                }
            }
        });

        LockSupport.park();
    }

}
