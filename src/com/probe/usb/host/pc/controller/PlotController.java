package com.probe.usb.host.pc.controller;

import com.google.common.eventbus.Subscribe;
import com.probe.usb.host.bus.Receiver;
import com.probe.usb.host.parser.internal.DataPoint;
import com.probe.usb.host.pc.controller.event.NewDataTrackEvent;
import com.probe.usb.host.pc.plot.Plot;

import java.util.ArrayList;
import java.util.List;

public class PlotController extends Receiver {

    public interface PlotDataListener {
        void setPlotData(List<Plot.Point> px, List<Plot.Point> py, List<Plot.Point> pz, List<Double> verticalLines);
        void setPlotBounds(double x0, double y0, double xz, double yz);
        void reset();
    }
    private PlotDataListener plotDataListener;

    public PlotController setPlotDataListener(PlotDataListener plotDataListener) {
        this.plotDataListener = plotDataListener;
        return this;
    }

    final static private double
            maxTimeGap = 0.5,
            maxTimeSpan = 10.0;

    private double
            plotTimeMin = Double.MAX_VALUE,
            plotTimeMax = Double.MIN_VALUE;

    private double tspan = 1.0;

    private double
            plotYmin = -10,
            plotYmax = 10;

    private double timeOffset = 0;
    private double recentTime = 0;
    private boolean recentTimeSet = false;

    private boolean shouldUpdatePlot = false;

    private List<Plot.Point>
            px = new ArrayList<>(),
            py = new ArrayList<>(),
            pz = new ArrayList<>();

    private List<Double> timeBreaks = new ArrayList<>();

    public void tick() {
        if (!shouldUpdatePlot)
            return;

        plotDataListener.setPlotBounds(0, plotYmin, tspan, plotYmax);
        plotDataListener.setPlotData(px, py, pz, timeBreaks);

        shouldUpdatePlot = false;
    }

    public void onNewPoint(DataPoint point) {
        shouldUpdatePlot = true;

        final boolean haveTimeGap = !recentTimeSet || point.t <= recentTime || point.t > recentTime + maxTimeGap;

        if (haveTimeGap){
            timeOffset = point.t + timeOffset - recentTime;
        }

        recentTime = point.t;
        recentTimeSet = true;

        final double plotTime = point.t - timeOffset;
        px.add(new Plot.Point(plotTime, point.ax));
        py.add(new Plot.Point(plotTime, point.ay));
        pz.add(new Plot.Point(plotTime, point.az));

        if (haveTimeGap && plotTime > 1.0e-10)
            timeBreaks.add(plotTime);

        plotTimeMin = Math.min(plotTimeMin, plotTime);
        plotTimeMax = Math.max(plotTimeMax, plotTime);
    }

    @Subscribe
    public void resetPlot(NewDataTrackEvent event) {
        px.clear();
        py.clear();
        pz.clear();
        plotDataListener.reset();
        shouldUpdatePlot = false;
        plotTimeMin = Double.MAX_VALUE;
        plotTimeMax = Double.MIN_VALUE;
        recentTimeSet = false;
        recentTime = 0;
        timeOffset = 0;
    }

    public void setTimeSpanRatio(double ratio) {
        ratio = Math.max(0.001, ratio);
        ratio = Math.min(1.0, ratio);
        this.tspan = plotTimeMax * ratio;
        shouldUpdatePlot = true;
    }

    /*
    private void adjustScale() {
        if (scaleAdjusted)
            return;;
        double
                xmin =  Double.MAX_VALUE,
                xmax =  Double.MIN_VALUE,
                ymin =  Double.MAX_VALUE,
                ymax =  Double.MIN_VALUE;
        for (List<Plot.Point> points : plots.values())
            for (Plot.Point p: points) {
                xmin = Math.min(xmin, p.x);
                xmax = Math.max(xmax, p.x);
                ymin = Math.min(ymin, p.y);
                ymax = Math.max(ymax, p.y);
            }
        if (xmin <= xmax && ymin <= ymax) {
            setRealX(xmin, xmax);
            setRealY(ymin, ymax);
            if (logger!=null)
                logger.print("Rescaled: X: " + xmin + ", " + xmax + "; Y: " + ymin + ", " + ymax);
        }
        scaleAdjusted = true;
    }
    */
}
