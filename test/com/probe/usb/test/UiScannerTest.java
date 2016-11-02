package com.probe.usb.test;


import com.probe.usb.host.bus.Bus;
import com.probe.usb.host.pc.controller.event.ComPortHasPortsEvent;
import com.probe.usb.host.pc.ui.controller.PortScanUiController;
import org.junit.Test;

import javax.swing.*;
import java.util.LinkedList;

public class UiScannerTest {


    @Test
    public void uiScannerTest() throws InterruptedException {

        JComboBox<String> box = new JComboBox<>();
        JToggleButton btn = new JToggleButton();
        JLabel lbl = new JLabel();
        PortScanUiController controller = new PortScanUiController(box, btn, lbl);
        Bus.post(new ComPortHasPortsEvent(new LinkedList<String>()));
        Thread.sleep(1000*5);

    }
}
