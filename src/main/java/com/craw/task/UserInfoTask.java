package com.craw.task;

import com.arronlong.httpclientutil.HttpClientUtil;
import com.arronlong.httpclientutil.common.HttpConfig;
import com.arronlong.httpclientutil.exception.HttpProcessException;
import com.craw.common.Common;
import com.craw.model.User;
import com.craw.task.runnable.NameRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 负责获取其他详细信息
 */
public class UserInfoTask implements NameRunnable {

    private static final Logger logger = LoggerFactory.getLogger(UserInfoTask.class);

    private static final String INFO_URL = "https://weibo.com/p/100505%s/info?mod=pedit_more";

    private final BlockingQueue<User> userInfoQueue;

    public UserInfoTask(BlockingQueue<User> userInfoQueue) {
        Objects.requireNonNull(userInfoQueue);
        this.userInfoQueue = userInfoQueue;
    }

    @Override
    public String getName() {
        return "用户详细信息获取任务";
    }

    @Override
    public void run() {
        Common.takeRun(userInfoQueue, getName(), 200, TimeUnit.MILLISECONDS, null, (user) -> {

        });
    }

    private void getUserInfoData(User user) {
        String url = String.format(INFO_URL, user.getWbUserId());
        try {
            String res = HttpClientUtil.get(HttpConfig.custom().url(url).context(Common.getCookies().getContext()).headers(Common.getHeard().build()));
            parseUserInfoHtml(res);
        } catch (HttpProcessException e) {
            logger.error("【{}】获取用户详细信息地址请求失败， user={}", getName(), user, e);
        }
    }

    private void parseUserInfoHtml(String html) {
    }
}
