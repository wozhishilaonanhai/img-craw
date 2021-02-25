package com.craw.common;

import com.craw.model.User;

public class UserFilter {

    public static boolean isRubbishUser(User user) {
        int wbNum = user.getWbNum();
        int attentionNum = user.getAttentionNum();
        int fansNum = user.getFansNum();

        // 关注人数超过指定值
        if (attentionNum > Integer.parseInt(Common.getPropertiesKey("rubbish.attentionNumMax"))) {
            return true;
        }

        // 微博数量等于0
        if (wbNum == 0) {
            return true;
        }

        // 如果关注人数超过200并且超过粉丝数的2倍
        if (attentionNum > 200 && attentionNum > (fansNum * 2)) {
            return true;
        }

        return false;
    }

}
