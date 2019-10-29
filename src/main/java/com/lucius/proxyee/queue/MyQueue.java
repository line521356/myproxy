package com.lucius.proxyee.queue;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MyQueue {

    private static ConcurrentHashMap<String,ConcurrentLinkedQueue<String>>  queueConcurrentHashMap = new ConcurrentHashMap<>();

    private static ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    private static Lock r = rwl.readLock();

    private static Lock w = rwl.writeLock();

    public static void push(String key,String value){
        w.lock();
        try {
            ConcurrentLinkedQueue<String> queue = queueConcurrentHashMap.get(key);
            if (queue == null) {
                queue = new ConcurrentLinkedQueue<>();
                queueConcurrentHashMap.put(key, queue);
            }
            queue.offer(value);
        }finally {
            w.unlock();
        }
    }

    public static String pop(String key){
        r.lock();
        try {
            ConcurrentLinkedQueue<String> queue = queueConcurrentHashMap.get(key);
            if (queue == null) {
                return null;
            }
            return queue.poll();
        }finally {
            r.unlock();
        }
    }


}
