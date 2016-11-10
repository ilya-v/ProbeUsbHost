package com.probe.usb.host.pc.ui.controller

import com.google.common.eventbus.Subscribe
import com.probe.usb.host.Context
import com.probe.usb.host.bus.UiReceiver
import com.probe.usb.host.pc.controller.event.*
import com.probe.usb.host.pc.ui.frame.PlotFrame
import java.util.*
import javax.swing.JSlider
import javax.swing.JToggleButton


object KPlotUiController : UiReceiver() {

    private var btnPlot: JToggleButton? = null
    private var xSlider: JSlider? = null
    private var ySlider: JSlider? = null
    private var btnFitX : JToggleButton? = null
    private var btnFitY : JToggleButton? = null
    private var plot: PlotFrame? = null

    private var lastPlotPoints = HashMap<Int, Pair<Int, Int>>()


    fun init(plot: PlotFrame, btnPlot: JToggleButton, xSlider: JSlider, ySlider: JSlider,
             btnFitX : JToggleButton, btnFitY: JToggleButton) {
        this.btnPlot = btnPlot
        this.xSlider = xSlider
        this.ySlider = ySlider
        this.plot = plot

        this.btnPlot?.addActionListener { evt -> plot.isVisible = this.btnPlot!!.isSelected }
        val sliderEvent  : () -> Unit = { postEvent(UiPlotSliderEvent(
                xSlider.value / xSlider.maximum.toDouble(),
                ySlider.value / ySlider.maximum.toDouble())) }
        this.xSlider?.addChangeListener  { evt -> sliderEvent() }
        this.xSlider?.addChangeListener  { evt -> sliderEvent() }

        this.btnFitX = btnFitX
        this.btnFitY = btnFitY

        this.plot?.onResize = {w, h -> postEvent(UiPlotResizedEvent(w, h))}
        //{x : Int,  y : Int) -> Unit {postEvent(); } }
        plotClear(UiPlotClearCommand())
    }

    @Subscribe
    fun plotData(plotPoints: UiPlotPointsCommand) {
        for (pt in plotPoints.points)  {
            if (!lastPlotPoints.containsKey(plotPoints.plotIndex))
                lastPlotPoints[plotPoints.plotIndex] = pt;
            val lpt = lastPlotPoints[plotPoints.plotIndex]

            Context.invokeUi {
                plot?.addLine(PlotFrame.Line(lpt!!.first, lpt.second, pt.first, pt.second, plotPoints.plotIndex))
            }
            lastPlotPoints[plotPoints.plotIndex] = pt
        }
    }

    @Subscribe
    fun plotTimeBreak(timeBreak : UiPlotTimeBreakCommand) {
        Context.invokeUi {
            plot?.addLine(PlotFrame.Line(timeBreak.x, 0, timeBreak.x, plot!!.plotHeight))
        }
    }

    @Subscribe
    fun plotClear(plotClear : UiPlotClearCommand) {
        Context.invokeUi { plot?.clear() }
    }

    @Subscribe
    fun plotHorizontalGrid(grid: UiPlotHorizontalGridCommand) {
        Context.invokeUi {
            var y = grid.y0
            while (y < plot?.plotHeight ?: 0) {
                plot!!.addLine(PlotFrame.Line(0, y, plot!!.plotWidth, y))
                y += grid.dy
            }
            y = grid.y0
            while (y > 0) {
                plot!!.addLine(PlotFrame.Line(0, y, plot!!.plotWidth, y))
                y -= grid.dy
            }
        }
    }

}
