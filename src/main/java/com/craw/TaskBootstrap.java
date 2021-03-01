package com.craw;

import com.craw.common.Common;
import com.craw.model.Img;
import com.craw.model.User;
import com.craw.task.*;
import com.google.common.hash.Hashing;
import org.apache.log4j.LogManager;
import org.apache.log4j.helpers.OptionConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

public class TaskBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(TaskBootstrap.class);
    private static String logFileHash = getLogPropertyHash();

    public TaskBootstrap() {
    }

    public void start() {
        BlockingQueue<String> finsSearchQueue = new LinkedBlockingQueue<>(500);
        BlockingQueue<String> finsDateQueue = new LinkedBlockingQueue<>(1000);
        BlockingQueue<Img> imgDownQueue = new LinkedBlockingQueue<>(2000);
        BlockingQueue<User> userInfoQueue = new LinkedBlockingQueue<>(2000);
        BlockingQueue<User> userStoreQueue = new LinkedBlockingQueue<>(1000);

        finsSearchQueue.add(Common.getPropertiesKey("task.main.start.userId", "1005052709577332|Pl_Official_HisRelation__59"));
        MainTask mainTask = new MainTask(finsSearchQueue, finsDateQueue);
        ParseFansTask parseFansTask = new ParseFansTask(500000, finsDateQueue, userInfoQueue, mainTask.getStopQueue());
        ImgTask imgTask = new ImgTask(imgDownQueue);
        UserInfoTask userInfoTask = new UserInfoTask(userInfoQueue, userStoreQueue, imgDownQueue);
        StoreTask storeTask = new StoreTask(userStoreQueue, finsSearchQueue);

        ExecutorService service = Executors.newCachedThreadPool();
        service.execute(mainTask);
        service.execute(mainTask);
        service.execute(mainTask);
        service.execute(mainTask);
        service.execute(mainTask);

        service.execute(parseFansTask);
        service.execute(parseFansTask);
        service.execute(parseFansTask);
        service.execute(parseFansTask);

        service.execute(imgTask);
        service.execute(imgTask);
        service.execute(imgTask);
        service.execute(imgTask);
        service.execute(imgTask);
        service.execute(imgTask);
        service.execute(imgTask);
        service.execute(imgTask);
        service.execute(imgTask);

        service.execute(userInfoTask);
        service.execute(userInfoTask);
        service.execute(userInfoTask);
        service.execute(userInfoTask);
        service.execute(userInfoTask);
        service.execute(userInfoTask);
        service.execute(userInfoTask);
        service.execute(userInfoTask);
        service.execute(userInfoTask);
        service.execute(userInfoTask);
        service.execute(userInfoTask);
        service.execute(userInfoTask);
        service.execute(userInfoTask);
        service.execute(userInfoTask);
        service.execute(userInfoTask);

        service.execute(storeTask);
        service.execute(storeTask);

        service.execute(() -> {
            while (true) {
                finsSearchQueue.add(Common.getPropertiesKey("task.main.start.userId", "1005052709577332|Pl_Official_HisRelation__59"));
                logger.info("\n========队列监控=========\nfinsSearchQueue大小：{}\nfinsDateQueue大小：{}\nimgDownQueue大小：{}\nuserInfoQueue大小：{}\nuserStoreQueue大小：{}\nallFansSet大小：{}\n===================",
                        finsSearchQueue.size(), finsDateQueue.size(), imgDownQueue.size(), userInfoQueue.size(), userStoreQueue.size(), ShareStore.addFansSize());
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    logger.error("中断异常，监控关闭...");
                    return;
                }

                if (finsSearchQueue.size() == 0 && finsDateQueue.size() == 0 && userStoreQueue.size() == 0){
                    finsSearchQueue.add(Common.getPropertiesKey("task.main.start.userId", "1005052709577332|Pl_Official_HisRelation__59"));
                }
            }
        });

        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);
        scheduledThreadPool.scheduleAtFixedRate(() -> {
            String property = System.getProperty(LogManager.DEFAULT_CONFIGURATION_KEY, null);
            if (property != null) {
                try {
                    String nowLogFileHash = getLogPropertyHash();
                    if (!logFileHash.equals(nowLogFileHash)) {
                        URL url = new URL(property);
                        OptionConverter.selectAndConfigure(url, null, LogManager.getLoggerRepository());
                        logger.warn("已更新日志配置文件");
                        logFileHash = nowLogFileHash;
                    }
                } catch (MalformedURLException e) {
                    logger.error("log4j 配置文件路径读取有误....");
                }
            }
        },1, 1, TimeUnit.MINUTES);

        LockSupport.park();
    }

    private static String getLogPropertyHash() {
        String property = System.getProperty(LogManager.DEFAULT_CONFIGURATION_KEY, null);
        if (Objects.isNull(property)) {
            return "";
        }
        try {
            URL url = new URL(property);
            String res = new String(Files.readAllBytes(Paths.get(url.toURI())), StandardCharsets.UTF_8);
            //noinspection UnstableApiUsage
            return Hashing.sha256().newHasher().putString(res, StandardCharsets.UTF_8).hash().toString();
        } catch (Exception e) {
            logger.error("log4j 配置文件路径读取有误....");
        }
        return "";
    }

}
