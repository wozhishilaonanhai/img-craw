package com.craw;

import com.craw.common.FixedSortQueue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public final class ShareStore {
    // 每个线程最大页数可能不一样
    private static final ThreadLocal<Integer> currentPageMaxSize = ThreadLocal.withInitial(() -> 6);
    // 当前粉丝总数量
    private static final AtomicInteger currentCount = new AtomicInteger(0);

    // 全局粉丝数据 为了用于过滤重复，大小并不等于currentCount
    private static final BlockingQueue<String> allFansSet = new FixedSortQueue<>(1000);

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

    public static void currentCountIncrementAndGet() {
        currentCount.incrementAndGet();
    }

    public static int currentCountGet() {
        return currentCount.get();
    }
}
