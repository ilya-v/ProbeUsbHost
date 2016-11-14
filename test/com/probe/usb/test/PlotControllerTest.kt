package com.probe.usb.test

import com.probe.usb.host.pc.controller.KPlotController.timeScaler
import org.junit.Test
import kotlin.test.assertEquals

class PlotControllerTest {

    @Test
    fun timeScalerTest() {

        timeScaler.displayMinTime = 0.0
        timeScaler.displayMaxTime = 10.0
        timeScaler.plotWidthPixels = 100

        var realTime = 0.0

        realTime = 50.0
        timeScaler.onNewRealTimeValue(realTime)
        val dispTime1 = timeScaler.realToDisplayTime(realTime)
        val plotX1 = timeScaler.displayTimeToPlotX(dispTime1)


        realTime = 50.1
        timeScaler.onNewRealTimeValue(realTime)
        val dispTime2 = timeScaler.realToDisplayTime(realTime)
        val plotX2 = timeScaler.displayTimeToPlotX(dispTime2)


        realTime = 50.2
        timeScaler.onNewRealTimeValue(realTime)
        val dispTime3 = timeScaler.realToDisplayTime(realTime)
        val plotX3 = timeScaler.displayTimeToPlotX(dispTime3)


        assertEquals(plotX2 - plotX1, plotX3 - plotX2);

        realTime = 100.0
        timeScaler.onNewRealTimeValue(realTime)
        val dispTime4 = timeScaler.realToDisplayTime(realTime)
        val plotX4 = timeScaler.displayTimeToPlotX(dispTime4)

        assertEquals(plotX4 - plotX3, plotX3 - plotX2);

        realTime = 100.1
        timeScaler.onNewRealTimeValue(realTime)
        val dispTime5 = timeScaler.realToDisplayTime(realTime)
        val plotX5 = timeScaler.displayTimeToPlotX(dispTime5)

        realTime = 100.2
        timeScaler.onNewRealTimeValue(realTime)
        val dispTime6 = timeScaler.realToDisplayTime(realTime)
        val plotX6 = timeScaler.displayTimeToPlotX(dispTime6)

        assertEquals(plotX6 - plotX5, plotX5 - plotX4);


        realTime = 1.0
        timeScaler.onNewRealTimeValue(realTime)
        val dispTime7 = timeScaler.realToDisplayTime(realTime)
        val plotX7 = timeScaler.displayTimeToPlotX(dispTime7)

        assertEquals(plotX7 - plotX6, plotX6 - plotX5);

        realTime = 1.1
        timeScaler.onNewRealTimeValue(realTime)
        val dispTime8 = timeScaler.realToDisplayTime(realTime)
        val plotX8 = timeScaler.displayTimeToPlotX(dispTime8)

        assertEquals(plotX8 - plotX7, plotX7 - plotX6);

        realTime = 1.2
        timeScaler.onNewRealTimeValue(realTime)
        val dispTime9 = timeScaler.realToDisplayTime(realTime)
        val plotX9 = timeScaler.displayTimeToPlotX(dispTime9)

        assertEquals(plotX9 - plotX8, plotX8 - plotX7);
    }

}