package com.probe.usb.host.pc.ui.controller;


import com.probe.usb.host.pc.controller.PlotController;
import com.probe.usb.host.pc.plot.Plot;

import javax.swing.*;
import java.util.List;

public class PlotUiController {

    private JToggleButton btnPlot;
    private JSlider xSlider;
    private JSlider ySlider;
    private Plot plot;

    private PlotController plotController;

    public PlotUiController(Plot plot, JToggleButton btnPlot, JSlider xSlider, JSlider ySlider) {
        this.btnPlot = btnPlot;
        this.xSlider = xSlider;
        this.ySlider = ySlider;
        this.plot = plot;

        this.btnPlot.addActionListener(evt -> plot.setVisible(this.btnPlot.isSelected()));
        this.xSlider.addChangeListener(evt -> onXSliderChange());
    }

    public PlotUiController setPlotController(PlotController plotController) {
        this.plotController = plotController;
        return this;
    }

    public void setPlotBounds(double x0, double y0, double xz, double yz) {
        plot.setRealX(x0, xz);
        plot.setRealY(y0, yz);
    }

    public void setPlotData(List<Plot.Point> px, List<Plot.Point> py, List<Plot.Point> pz, List<Double> verticalLines) {
        plot.setPlot(0, px);
        plot.setPlot(1, py);
        plot.setPlot(2, pz);
        plot.verticalLines(verticalLines);
        plot.repaint();
    }

    public void onXSliderChange() {
        plotController.setTimeSpanRatio(xSlider.getValue() / (double) xSlider.getMaximum());
    }

    public void onySliderChange() {
        //plotController.
    }

    public void reset() {
        plot.clearPlots();
    }
}
