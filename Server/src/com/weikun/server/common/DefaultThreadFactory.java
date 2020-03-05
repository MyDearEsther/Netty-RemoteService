package com.weikun.server.common;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author linweikun
 * @date 2019/8/2
 * 默认线程工厂
 */
public class DefaultThreadFactory implements ThreadFactory {
    private static final AtomicInteger POOLNUMBER = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private static final int DEFAULT_PRIORITY = 5;

    public DefaultThreadFactory() {
        SecurityManager var1 = System.getSecurityManager();
        this.group = var1 != null?var1.getThreadGroup():Thread.currentThread().getThreadGroup();
        this.namePrefix = "pool-" + POOLNUMBER.getAndIncrement() + "-thread-";
    }

    @Override
    public Thread newThread(Runnable var1) {
        Thread var2 = new Thread(this.group, var1, this.namePrefix + this.threadNumber.getAndIncrement(), 0L);
        if(var2.isDaemon()) {
            var2.setDaemon(false);
        }

        if(var2.getPriority() != DEFAULT_PRIORITY) {
            var2.setPriority(DEFAULT_PRIORITY);
        }

        return var2;
    }
}