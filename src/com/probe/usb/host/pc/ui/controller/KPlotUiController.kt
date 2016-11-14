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
        this.ySlider?.addChangeListener  { evt -> sliderEvent() }

        this.btnFitX = btnFitX
        this.btnFitY = btnFitY

        this.btnFitY!!.addActionListener { postEvent( UiPlotFitYModeEvent(this.btnFitY!!.isSelected()) ) }
        this.btnFitX!!.addActionListener { postEvent( UiPlotFitXModeEvent(this.btnFitX!!.isSelected()) ) }

        this.plot?.onResize = {w, h -> postEvent(UiPlotResizedEvent(w, h))}
        //{x : Int,  y : Int) -> Unit {postEvent(); } }
        plotClear(UiPlotClearCommand())
    }

    @Subscribe
    fun plotData(plotPoints: UiPlotPointsCommand) {

        val linesToPlot = ArrayList<PlotFrame.Line>()

        var lpt = if (plotPoints.points.isNotEmpty()) plotPoints.points[0] else Pair(0,0)
        for (pt in plotPoints.points)  {
            linesToPlot.add(PlotFrame.Line(lpt.first, lpt.second, pt.first, pt.second, plotPoints.plotIndex))
            lpt = pt
        }

        Context.invokeUi {
            for (line in linesToPlot)
                plot?.addLine(line)
        }
    }

    @Subscribe
    fun plotTimeBreak(timeBreak : UiPlotTimeBreakCommand) {
        Context.invokeUi {
            for (tbx in timeBreak.breaks)
            plot?.addLine(PlotFrame.Line(tbx, 0, tbx, plot!!.plotHeight))
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun plotClear(plotClear : UiPlotClearCommand) {
        Context.invokeUi { plot?.clear() }
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun plotRefresh(event: UiPlotRefreshCommand) {
        Context.invokeUi { plot?.refresh() }
    }

    @Subscribe
    fun plotHorizontalGrid(grid: UiPlotHorizontalGridCommand) {
        Context.invokeUi {
            var y = grid.y0
            val x1 = Int.MIN_VALUE
            val x2 = Int.MAX_VALUE

            plot!!.addLine(PlotFrame.Line(x1, y, x2, y, -1, 2.0f))
            while (y < plot?.plotHeight ?: 0) {
                plot!!.addLine(PlotFrame.Line(x1, y, x2, y, -1, 0.5f))
                y += grid.dy
            }
            y = grid.y0
            while (y > 0) {
                plot!!.addLine(PlotFrame.Line(x1, y, x2, y, -1, 0.5f))
                y -= grid.dy
            }
        }
    }

    @Subscribe
    fun plotText(text: UiPlotTextCommand) {
        Context.invokeUi {
            plot?.addText(text.x, text.y, text.text)
        }
    }

    @Subscribe
    fun onUiPlotCommand(commands: UiPlotCommand) {
        Context.invokeUi {
            for (c in commands.commands)
                when (c) {
                    is UiPlotTextCommand -> plotText(c)
                    is UiPlotHorizontalGridCommand -> plotHorizontalGrid(c)
                    is UiPlotRefreshCommand -> plotRefresh(c)
                    is UiPlotClearCommand -> plotClear(c)
                    is UiPlotTimeBreakCommand -> plotTimeBreak(c)
                    is UiPlotPointsCommand -> plotData(c)
                }
        }
    }
}
