package com.craw.task;

import com.arronlong.httpclientutil.HttpClientUtil;
import com.arronlong.httpclientutil.common.HttpConfig;
import com.arronlong.httpclientutil.exception.HttpProcessException;
import com.craw.common.Common;
import com.craw.common.ProxyHolder;
import com.craw.model.Img;
import com.craw.task.runnable.NameRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 负责下载图片
 */
public class ImgTask implements NameRunnable {

    private static final Logger logger = LoggerFactory.getLogger(ImgTask.class);

    private static final String ROOT_PATH;

    static {
        ROOT_PATH = Common.getPropertiesKey("img.down.path");
        Path path = Paths.get(ROOT_PATH);
        if (Files.notExists(path)) {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                logger.error("img_down 文件夹不存在，并且创建失败，系统退出");
                System.exit(1);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(ImgTask.class.getResource(""));
        System.out.println(ImgTask.class.getClassLoader().getResource(""));
    }

    private final BlockingQueue<Img> imgDownQueue;

    public ImgTask(BlockingQueue<Img> imgDownQueue) {
        this.imgDownQueue = imgDownQueue;
    }

    @Override
    public void run() {
        Common.takeRun(imgDownQueue, getName(), 100, TimeUnit.MILLISECONDS, null, this::down);
    }

    private void down(Img img) {
        String url = img.getImg();
        String fileName = img.getImgId() + ".jpg";
        if (Files.exists(Paths.get(ROOT_PATH + fileName))) {
            return;
        }
        try (FileOutputStream saveStream = new FileOutputStream(ROOT_PATH + fileName)) {
            HttpClientUtil.down(HttpConfig.custom().headers(Common.getHeard().build()).url(url).context(Common.getCookies().getContext()).out(saveStream));
        } catch (IOException e) {
            logger.error("【{}】文件保存异常 fileName={} url={}", getName(), fileName, url, e);
        } catch (HttpProcessException e) {
            logger.error("【{}】文件下载异常 fileName={} url={}", getName(), fileName, url);
        }
    }

    @Override
    public String getName() {
        return "图像下载任务";
    }
}
