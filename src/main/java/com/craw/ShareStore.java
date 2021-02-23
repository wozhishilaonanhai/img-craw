package com.craw;

import com.craw.common.FixedSortQueue;

import java.util.LinkedHashSet;
import java.util.concurrent.atomic.AtomicInteger;

public class ShareStore {

    private static final AtomicInteger currentPageMaxSize = new AtomicInteger(10000);

    // 全局粉丝数据 为了用于过滤重复
    private static final FixedSortQueue<String> allFansSet = new FixedSortQueue<>(1000);

    public static void setCurrentPageMaxSize(int val) {
        currentPageMaxSize.set(val);
    }

    public static int getCurrentPageMaxSize() {
        return currentPageMaxSize.get();
    }

    public static boolean isContainsFans(String userId) {
        return allFansSet.contains(userId);
    }

    public static void addFans(String userId) {
        allFansSet.offer(userId);
    }
}
