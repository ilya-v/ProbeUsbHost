package com.probe.usb.host.pc.plot;


import javax.swing.*;
import java.awt.*;

public class Plot extends JFrame {
    protected PlotPanel plotPanel = new PlotPanel();
    public Plot() {
        add(plotPanel);
        pack();
    }
}
