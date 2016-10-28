package com.probe.usb.test;


import com.probe.usb.host.pc.plot.Plot;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class PlotTest {

    @Test
    public void basicPlotTest() throws InterruptedException {
        Plot p = new Plot();
        p.setVisible(true);

        p.setRealX(0, 10);
        p.setRealY(-10, 10);
        List<Plot.Point> points = new ArrayList<Plot.Point>() { {
            add( new Plot.Point(0, -10));
            add( new Plot.Point(5,  10));
            add( new Plot.Point(10, 10));

        } };
        p.setPlot(0, points);
        p.repaint();

        List<Plot.Point> points2 = new ArrayList<Plot.Point>() { {
            add( new Plot.Point(4, 6));
            add( new Plot.Point(6, 8));
            add( new Plot.Point(8, 9));
        } };

        p.setPlot(1, points2);
        p.repaint();

        Thread.sleep(10*1000);
    }
}
