package com.probe.usb.host.pc.ui.controller

import com.probe.usb.host.bus.UiReceiver
import com.probe.usb.host.pc.controller.event.UiPlotResizedEvent
import com.probe.usb.host.pc.controller.event.UiPlotSliderEvent
import com.probe.usb.host.pc.ui.frame.PlotFrame
import javax.swing.JSlider
import javax.swing.JToggleButton


object KPlotUiController : UiReceiver() {

    private var btnPlot: JToggleButton? = null
    private var xSlider: JSlider? = null
    private var ySlider: JSlider? = null
    private var btnFitX : JToggleButton? = null
    private var btnFitY : JToggleButton? = null
    private var plot: PlotFrame? = null

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

        this.plot?.onResize = {w, h -> postEvent(UiPlotResizedEvent(w, h))} //{x : Int,  y : Int) -> Unit {postEvent(); } }
    }

}
