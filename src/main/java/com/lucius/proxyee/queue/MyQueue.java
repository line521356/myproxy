package com.lucius.proxyee.queue;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MyQueue {

    private static ConcurrentHashMap<String,ConcurrentLinkedQueue<String>>  queueConcurrentHashMap = new ConcurrentHashMap<>();

    public static void push(String key,String value){
        ConcurrentLinkedQueue<String> queue = queueConcurrentHashMap.get(key);
        if(queue == null){
            synchronized (MyQueue.class){
                queue = queueConcurrentHashMap.get(key);
                if(queue == null){
                    queue = new ConcurrentLinkedQueue<>();
                    queueConcurrentHashMap.put(key,queue);
                }
            }
        }
        queue.offer(value);
    }

    public static synchronized String pop(String key){
        ConcurrentLinkedQueue<String> queue = queueConcurrentHashMap.get(key);
        if (queue == null){
            return null;
        }
        return queue.poll();
    }

    public static Integer length(String key){
        ConcurrentLinkedQueue<String> queue = queueConcurrentHashMap.get(key);
        return queue.size();
    }

}
