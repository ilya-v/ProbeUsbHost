package com.probe.usb.host.pc.controller

import com.google.common.eventbus.Subscribe
import com.probe.usb.host.bus.Receiver
import com.probe.usb.host.pc.controller.event.*
import java.util.*
import kotlin.concurrent.fixedRateTimer

object KPlotController : Receiver() {

    class PointControllerTick : SilentEvent()

    private val timerPeriodMs = 250L

    private var shouldRedrawPlot = true
    private var shouldCleanPlot = true

    private val maxDisplayTimeSpan = 30
    private val maxAccAmplitude = 25
    private val accGridStep = 1.0

    private data class RealPoint(val t: Double, val y: Double)

    private val ax = ArrayList<RealPoint>()
    private val ay = ArrayList<RealPoint>()
    private val az = ArrayList<RealPoint>()
    private val gaps = ArrayList<Double>()

    init {
        fixedRateTimer(period = timerPeriodMs) { postEvent(PointControllerTick()) }
    }

    @Subscribe
    fun onPlotDataPoint(dataPointEvent: PlotDataPointEvent) {
        val dp = dataPointEvent.dp

        timeScaler.onNewRealTimeValue(dp.t)
        shouldRedrawPlot = true

        val displayTime = timeScaler.realToDisplayTime(dp.t)

        ax.add(RealPoint(displayTime, dp.ax))
        ay.add(RealPoint(displayTime, dp.ay))
        az.add(RealPoint(displayTime, dp.az))
        if (timeScaler.timeGap)
            gaps.add(displayTime)

        plotMonitor.onDataPoint(displayTime, dp.ax)
        plotMonitor.onDataPoint(displayTime, dp.ay)
        plotMonitor.onDataPoint(displayTime, dp.az)

        modeController.onPlotUpdate()
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun onPointControllerTick(tick: PointControllerTick) {
        updatePlot()
    }

    @Subscribe
    fun onUiPlotResized(event: UiPlotResizedEvent) {
        if (timeScaler.plotWidthPixels == event.w && accScaler.plotHeightPixels == event.h)
            return
        shouldRedrawPlot = true
        shouldCleanPlot = true
        timeScaler.plotWidthPixels = event.w
        accScaler.plotHeightPixels = event.h
        updatePlot()
    }

    @Subscribe
    fun onUiPlotFitYModeEvent(event: UiPlotFitYModeEvent) {
        modeController.fitY = event.fitY
        modeController.onPlotUpdate()
    }

    @Subscribe
    fun onUiPlotFitXModeEvent(event: UiPlotFitXModeEvent) {
        modeController.fitX = event.fitX
        modeController.onPlotUpdate()
    }

    @Subscribe
    fun onUiPlotSliderEvent(event: UiPlotSliderEvent) {

        //event.kx

    }

    private fun realToMappedPoints(rps: ArrayList<RealPoint>): Array<Pair<Int, Int>> {
        val mappedPoints = Array(rps.size, { i -> Pair(0, 0) })
        var i = 0
        while (i < rps.size) {
            val rp = rps[i]
            val mp = Pair(timeScaler.displayTimeToPlotX(rp.t), accScaler.accToPlotY(rp.y))
            mappedPoints[i] = mp
            i++
        }
        return mappedPoints
    }

    private fun realToMappedTimeBreaks(realBreaks: ArrayList<Double>): Array<Int> {
        val mappedBreaks = Array(realBreaks.size, { i -> 0 })
        var i = 0
        while (i < mappedBreaks.size) {
            mappedBreaks[i] = timeScaler.displayTimeToPlotX(realBreaks[i])
            i++
        }
        return mappedBreaks
    }

    fun updatePlot()  {
        if (!shouldRedrawPlot || !accScaler.isInitialized || !timeScaler.isInitialized)
            return

        val plotCommands : MutableList<Any> = ArrayList()
        plotCommands.add(UiPlotClearCommand())

        val gridStepY = accScaler.accIntervalToPlotYInterval(accGridStep)
        val gridY0 = accScaler.accToPlotY(0.0)

        val textX1 = 5
        val displayText1 = String.format("%.2fs", timeScaler.displayMinTime)
        val textX2 = -5
        val displayText2 = String.format("%.2fs", timeScaler.displayMaxTime)

        var pa = if (ax.isEmpty()) RealPoint(0.0,0.0) else ax[0]
        for (a in ax) {
            if (a.t < pa.t)
                System.out.println("" + a.t)
            pa = a
        }

        plotCommands.add(UiPlotHorizontalGridCommand(gridStepY, gridY0))
        plotCommands.add(UiPlotTextCommand(textX1, gridY0, displayText1))
        plotCommands.add(UiPlotTextCommand(textX2, gridY0, displayText2))
        plotCommands.add(UiPlotPointsCommand(0, realToMappedPoints(ax)))
        plotCommands.add(UiPlotPointsCommand(1, realToMappedPoints(ay)))
        plotCommands.add(UiPlotPointsCommand(2, realToMappedPoints(az)))
        plotCommands.add(UiPlotRefreshCommand())

        //postEvent(UiPlotTimeBreakCommand(realToMappedTimeBreaks(gaps)))

        postEvent(UiPlotCommand(plotCommands))

        shouldRedrawPlot = false
        shouldCleanPlot = false
    }

    fun removeOldValues(displayMinTime: Double) {
        fun removeOld(acc : ArrayList<RealPoint>) {
            var i = 0
            while (i < acc.size && acc[i].t < displayMinTime)
                i++
            acc.subList(0, i).clear()
        }
        removeOld(ax)
        removeOld(ay)
        removeOld(az)
    }


    object modeController {

        var fitY = false
        var fitX = false

        fun onPlotUpdate() {

            var reset = false

            if (fitY) {
                if (accScaler.displayMaxAcc < plotMonitor.maxAcc) {
                    accScaler.displayMaxAcc = plotMonitor.maxAcc
                    reset = true
                }
                if (accScaler.displayMinAcc > plotMonitor.minAcc) {
                    accScaler.displayMinAcc = plotMonitor.minAcc
                    reset = true
                }
            }

            if (fitX) {
                if (timeScaler.displayMaxTime < plotMonitor.maxDisplayTime) {
                    timeScaler.displayMaxTime = plotMonitor.maxDisplayTime
                    reset = true
                }

                if (timeScaler.displayMinTime > plotMonitor.minDisplayTime) {
                    timeScaler.displayMinTime = plotMonitor.minDisplayTime
                    reset = true
                }
            }

            if (!fitX) {
                if (timeScaler.displayMaxTime < plotMonitor.maxDisplayTime) {
                    val displayspan = timeScaler.displayMaxTime - timeScaler.displayMinTime
                    timeScaler.displayMinTime = timeScaler.displayMaxTime
                    timeScaler.displayMaxTime = timeScaler.displayMinTime + displayspan
                    reset = true
                }
            }


            if (reset) {
                removeOldValues(timeScaler.displayMinTime)
                plotMonitor.reset()
            }

            shouldRedrawPlot = shouldRedrawPlot || reset
            shouldCleanPlot = shouldCleanPlot || reset

        }
    }


    object timeScaler {
        var displayMinTime: Double = 0.0
        var displayMaxTime: Double = 1.0
        var plotWidthPixels: Int = 0
        var timeGap = false

        val isInitialized: Boolean
            get() =  plotWidthPixels > 0

        private var displayTimeOffset: Double = 0.0

        private var recentRealTime: Double = 0.0
        private var recentRealTimeSet = false;

        private var recentRealTimeStep: Double = 0.0
        private var recentRealTimeStepSet = false

        private val eps = 1.0e-6

        fun reset() {
            recentRealTimeSet = false
            recentRealTimeStepSet = false
        }

        fun onNewRealTimeValue(realTime: Double) {
            timeGap = !recentRealTimeSet
                    || !recentRealTimeStepSet
                    || Math.abs(realTime - recentRealTime - recentRealTimeStep) > eps

            if (timeGap) {
                if (!recentRealTimeSet && !recentRealTimeStepSet) {
                    displayTimeOffset = realTime
                } else if (recentRealTimeSet && !recentRealTimeStepSet) {
                    recentRealTimeStep = realTime - recentRealTime
                    recentRealTimeStepSet = true
                } else if (!recentRealTimeSet && recentRealTimeStepSet) {
                } else if (recentRealTimeSet && recentRealTimeStepSet) {
                    displayTimeOffset = realTime - recentRealTimeStep - (recentRealTime - displayTimeOffset)
                    recentRealTimeStepSet = false
                }
            }
            recentRealTime = realTime
            recentRealTimeSet = true
        }

        fun realToDisplayTime(realTime: Double) = realTime - displayTimeOffset

        fun displayTimeToPlotX(displayTime: Double) =
                Math.round((displayTime - displayMinTime) * plotWidthPixels / (displayMaxTime - displayMinTime)).toInt()
    }


    object accScaler {
        var displayMinAcc: Double = -10.0
        var displayMaxAcc: Double = 10.0
        var plotHeightPixels = 0

        val isInitialized : Boolean
            get() = plotHeightPixels > 0

        fun accToPlotY(acc: Double) =
                Math.round((acc - displayMinAcc) * plotHeightPixels / (displayMaxAcc - displayMinAcc)).toInt()

        fun accIntervalToPlotYInterval(da : Double) =
                Math.round(da * plotHeightPixels / (displayMaxAcc - displayMinAcc)).toInt()
    }


    object plotMonitor {
        var minDisplayTime = Double.MAX_VALUE
        var maxDisplayTime = Double.MIN_VALUE
        var minAcc = Double.MAX_VALUE
        var maxAcc = Double.MIN_VALUE

        fun reset() {
            minDisplayTime = Double.MAX_VALUE
            maxDisplayTime = Double.MIN_VALUE
            minAcc = Double.MAX_VALUE
            maxAcc = Double.MIN_VALUE
        }

        fun onDataPoint(displayTime: Double, acc: Double) {
            minDisplayTime = Math.min(displayTime, minDisplayTime)
            maxDisplayTime = Math.max(displayTime, maxDisplayTime)
            minAcc = Math.min(displayTime, minAcc)
            maxAcc = Math.max(displayTime, maxAcc)
        }
    }
}
