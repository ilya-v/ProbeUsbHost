package com.probe.usb.host.pc.plot;

import javax.swing.*;
import java.awt.*;

public class PlotPanel extends JPanel {

    public PlotPanel() {
        setBorder(BorderFactory.createLineBorder(Color.black));
        setBackground(Color.WHITE);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(250,200);
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

    }
}
