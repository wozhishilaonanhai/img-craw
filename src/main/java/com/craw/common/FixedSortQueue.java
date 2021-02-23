package com.craw.common;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Spliterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 固定值的队列
 * 如果队列满了，则自动踢出队列头部
 * 只提供 offer contains size remainingCapacity 方法
 * 并且提供O(1)的判断值存在
 *
 * @param <T>
 */
public class FixedSortQueue<T> extends ArrayBlockingQueue<T> {

    private final LinkedHashSet<T> indexSet;

    private final ReadWriteLock lock;

    public FixedSortQueue(int capacity) {
        super(capacity);
        indexSet = new LinkedHashSet<>();
        this.lock = new ReentrantReadWriteLock();
    }

    @Override
    public boolean offer(T t) {
        lock.writeLock().lock();
        try {
            boolean offer = super.offer(t);
            if (!offer) {
                T poll = super.poll();
                indexSet.remove(poll);
                offer = super.offer(t);
            }
            indexSet.add(t);
            return offer;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean contains(Object o) {
        lock.readLock().lock();
        try {
            return indexSet.contains(o);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean offer(T t, long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T take() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T poll(long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T peek() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return super.size();
    }

    @Override
    public int remainingCapacity() {
        return super.remainingCapacity();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int drainTo(Collection<? super T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int drainTo(Collection<? super T> c, int maxElements) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<T> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Spliterator<T> spliterator() {
        throw new UnsupportedOperationException();
    }
}
