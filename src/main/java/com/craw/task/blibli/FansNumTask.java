package com.craw.task.blibli;

import com.craw.common.Common;
import com.craw.model.BlibliUser;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 负责获取其他详细信息
 */
public class FansNumTask extends CheckTask {

    private static final Logger logger = LoggerFactory.getLogger(FansNumTask.class);

    private static final String INFO_URL = "https://api.bilibili.com/x/relation/stat?vmid=%s&jsonp=jsonp";

    private final BlockingQueue<BlibliUser> fansNumQueue;
    private final BlockingQueue<BlibliUser> userStoreQueue;

    public FansNumTask(BlockingQueue<BlibliUser> fansNumQueue, BlockingQueue<BlibliUser> userStoreQueue) {
        Objects.requireNonNull(fansNumQueue);
        this.fansNumQueue = fansNumQueue;
        this.userStoreQueue = userStoreQueue;
    }

    @Override
    public String getName() {
        return "获取粉丝数量任务";
    }

    @Override
    public void run() {
        Common.takeRun(fansNumQueue, getName(), 200, TimeUnit.MILLISECONDS, null, (user) -> {
            String url = String.format(INFO_URL, user.getUserId());
            try {
                String data = getData(url).orElseThrow(Exception::new);
                JsonObject json = checkData(data, user.getUserId());
                if (Objects.isNull(json)){
                    return;
                }
                setUser(json, user);
                while (!userStoreQueue.offer(user, 5, TimeUnit.SECONDS)) {
                    logger.warn("【[]】userStoreQueue 队列已满，等待并停止入队");
                }
            } catch (Exception e) {
                logger.error("【{}】获取粉丝数据失败... url={}", getName(), url, e);
            }
        });
    }

    private void setUser(JsonObject json, BlibliUser user) {
        BlibliUser.FansInfo fansInfo = user.getFansInfo();
        if (Objects.isNull(fansInfo)) {
            fansInfo = new BlibliUser.FansInfo();
        }
        fansInfo.setFansNum(json.get("follower").getAsInt())
                .setAttentionNum(json.get("following").getAsInt());
        user.setFansInfo(fansInfo);
    }

}
