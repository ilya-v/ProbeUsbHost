package com.probe.usb.test;


import com.google.common.eventbus.Subscribe;
import com.probe.usb.host.bus.Bus;
import com.probe.usb.host.pc.controller.KPortScanner;
import com.probe.usb.host.pc.controller.event.DebugLogEvent;
import com.probe.usb.host.pc.controller.event.PortScannerEnableEvent;
import org.junit.Test;

public class PortScannerTest {

    private String log = "";

    @Subscribe
    public void onDebugLogEvent(DebugLogEvent ev) {
        log += ev.getMessage() + "\n";
    }


    @Test
    public void portScannerTest() throws InterruptedException {

        Bus.register(KPortScanner.INSTANCE);
        Bus.register(this);
        Bus.post(new PortScannerEnableEvent(true));

        Thread.sleep(5*1000);
        System.out.println(log);

    }
}
