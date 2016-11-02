package com.probe.usb.test;

import org.junit.Test;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ExecTest {

    private Executor executor = Executors.newSingleThreadExecutor();

    @Test
    public void ExecSimpleTest(){

        executor.execute(new Runnable() {
            @Override
            public void run() {

            }
        });

    }
}
