package com.craw.blibli;

import com.craw.common.Common;
import com.craw.model.BlibliUser;
import com.craw.model.Img;
import com.craw.task.blibli.FansNumTask;
import com.craw.task.blibli.ImgTask;
import com.craw.task.blibli.MainTask;
import com.craw.task.blibli.StoreTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

public class BlibliTaskBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(BlibliTaskBootstrap.class);

    public void start() {
        BlockingQueue<BlibliUser> fansNumQueue = new LinkedBlockingQueue<>(100);
        BlockingQueue<BlibliUser> userStoreQueue = new LinkedBlockingQueue<>(1000);
        BlockingQueue<Img> imgDownQueue = new LinkedBlockingQueue<>(1000);

        FansNumTask fansNumTask = new FansNumTask(fansNumQueue, userStoreQueue);
        ImgTask imgTask = new ImgTask(imgDownQueue);
        StoreTask storeTask = new StoreTask(userStoreQueue);

        ExecutorService service = Executors.newCachedThreadPool();
        int startId = Integer.parseInt(Common.getPropertiesKey("blibli.userid.start", "51899757"));
        int add = Integer.parseInt(Common.getPropertiesKey("blibli.userid.add", "800000"));
        service.execute(new MainTask(startId, startId + add, fansNumQueue, userStoreQueue, null, imgDownQueue));
        service.execute(fansNumTask);
        service.execute(imgTask);
        service.execute(storeTask);

        service.execute(() -> {
            while (true) {
                logger.info("\n========队列监控=========\nfansNumQueue 大小：{}\nuserStoreQueue 大小：{}\nimgDownQueue 大小：{}\n===================",
                        fansNumQueue.size(), userStoreQueue.size(), imgDownQueue.size());
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    logger.error("中断异常，监控关闭...");
                    return;
                }
            }
        });

        LockSupport.park();
    }

    public static void main(String[] args) {
        BlibliTaskBootstrap bootstrap = new BlibliTaskBootstrap();
        bootstrap.start();
    }
}
