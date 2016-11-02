package com.probe.usb.host.bus;

import com.google.common.eventbus.AsyncEventBus;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class Bus {


    static private boolean commandDelayed = false;

    static public Executor getWorkerThreadExecutor() {
        return workerThreadExecutor;
    }

    static private class SimpleThreadFactory implements ThreadFactory {
        private int counter = 0;
        Thread lastThread = null;
        @Override
        public Thread newThread(Runnable r) {
            lastThread = new Thread(r, "Worker-" + counter++);
            return lastThread;
        }
        public Thread getLastThread() {
            return lastThread;
        }
    };

    static private SimpleThreadFactory threadFactory = new SimpleThreadFactory();

    static private Executor executor = Executors.newSingleThreadExecutor(threadFactory);
    static {
        executor.execute( ()-> {} );
    }

    static private Executor workerThreadExecutor = command -> {
        if (Thread.currentThread().getId() == threadFactory.getLastThread().getId() && !commandDelayed)
            command.run();
        else
            executor.execute(command);
    };

    static private AsyncEventBus bus = new AsyncEventBus(getWorkerThreadExecutor());

    public static void register(Object object) {
        bus.register(object);
    }

    public static void unregister(Object object) {
        bus.unregister(object);
    }

    public static void post(Object event) {
        commandDelayed = false;
        bus.post(event);
    }

    public static void postDelayed(Object event) {
        commandDelayed = true;
        bus.post(event);
    }
}
