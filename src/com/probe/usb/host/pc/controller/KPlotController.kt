package com.probe.usb.host.pc.controller

import com.google.common.eventbus.Subscribe
import com.probe.usb.host.bus.Receiver
import com.probe.usb.host.pc.controller.event.PlotDataPointEvent
import com.probe.usb.host.pc.controller.event.UiPlotClearCommand
import com.probe.usb.host.pc.controller.event.UiPlotPointsCommand
import com.probe.usb.host.pc.controller.event.UiPlotResizedEvent
import java.util.*
import kotlin.concurrent.fixedRateTimer

object KPlotController :  Receiver() {

    val timerPeriodMs = 250L
    val maxTimeGap = 0.5f

    class PointControllerTick

    private var shouldRedrawPlot = true

    private var plotTimeMin = java.lang.Double.MAX_VALUE
    private var plotTimeMax = java.lang.Double.MIN_VALUE

    private var tspan = 1.0

    private val plotYmin = -10.0
    private val plotYmax = 10.0

    private var timeOffset = 0.0
    private var recentTime = 0.0
    private var recentTimeSet = false

    private var plotStartTime = 0.0
    private var plotEndTime = 0.0
    private var plotStartAcc = -10.0
    private var plotEndAcc = 10.0

    private var plotWidth = 0
    private var plotHeight = 0

    data class RealPoint(val t : Double, val y : Double)

    private val ax = ArrayList<RealPoint>()
    private val ay = ArrayList<RealPoint>()
    private val az = ArrayList<RealPoint>()
    private val tt = ArrayList<Int>()


    init {
        fixedRateTimer (period = timerPeriodMs) { postEvent(PointControllerTick()) }
    }

    @Subscribe
    fun onPlotDataPoint(dataPointEvent : PlotDataPointEvent) {
        val dp = dataPointEvent.dp
        shouldRedrawPlot = true

        val haveTimeGap = !recentTimeSet || dp.t <= recentTime || dp.t > recentTime + maxTimeGap

        if (haveTimeGap) {
            timeOffset = dp.t + timeOffset - recentTime
        }

        recentTime = dp.t
        recentTimeSet = true

        val plotTime = dp.t - timeOffset
        ax.add(RealPoint(plotTime, dp.ax))
        ay.add(RealPoint(plotTime, dp.ay))
        az.add(RealPoint(plotTime, dp.az))


        if (haveTimeGap && plotTime > 1.0e-10)
            tt.add(mapTime(plotTime))

        plotTimeMin = Math.min(plotTimeMin, plotTime)
        plotTimeMax = Math.max(plotTimeMax, plotTime)

    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun onPointControllerTick(tick: PointControllerTick) {
        updatePlot()
    }

    @Subscribe
    fun onUiPlotResized(event : UiPlotResizedEvent) {
        if (plotWidth == event.w && plotHeight == event.h)
            return
        shouldRedrawPlot = true
        plotWidth = event.w
        plotHeight = event.h
        updatePlot()
    }

    private fun realToMappedPoints(rps : ArrayList<RealPoint>) : Array<Pair<Int,Int>> {
        val mappedPoints = Array<Pair<Int,Int>>(rps.size, {i -> Pair<Int,Int>(0,0)})
        var i = 0
        while (i < rps.size) {
            val rp = rps[i]
            val mp = Pair<Int,Int>(mapTime(rp.t), mapAcc(rp.y))
            mappedPoints[i] = mp
            i++
        }
        return mappedPoints
    }

    fun updatePlot() {
        if (!shouldRedrawPlot)
            return

        postEvent(UiPlotClearCommand())

        postEvent(UiPlotPointsCommand(0, realToMappedPoints(ax)))
        postEvent(UiPlotPointsCommand(1, realToMappedPoints(ay)))
        postEvent(UiPlotPointsCommand(2, realToMappedPoints(az)))

        shouldRedrawPlot = false

    }


    private fun mapTime(realTime: Double): Int {
        val plotTime = realTime - timeOffset
        return ((plotTime - plotStartTime) * plotWidth / (plotEndTime - plotStartTime)).toInt()
    }

    private fun mapAcc(acc: Double) : Int {
        return ((acc - plotStartAcc) * plotHeight / (plotEndAcc - plotStartAcc)).toInt()
    }



}