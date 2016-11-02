package com.probe.usb.host.pc.controller

import com.google.common.eventbus.Subscribe
import com.probe.usb.host.bus.Receiver
import com.probe.usb.host.parser.ParserEventListener
import com.probe.usb.host.parser.ProbeUsbParser
import com.probe.usb.host.pc.controller.event.ComPortDataEvent
import com.probe.usb.host.pc.controller.event.DebugLogEvent
import com.probe.usb.host.pc.controller.event.ParserFrameEvent
import java.util.*


object ParserController : Receiver() {

    private var date = Date(0L)

    private var lastDataEventCounter = -1

    private var parser : ProbeUsbParser? = null
    fun setParser(parser: ProbeUsbParser) {
        this.parser = parser
        this.parser?.setListener(FrameListener())
    }

    @Subscribe
    fun onNewBytes(portData: ComPortDataEvent){

        if (lastDataEventCounter + 1 != portData.sync)
            postEvent(DebugLogEvent("Data packet counter mistmatch: expected "
                    + (lastDataEventCounter + 1) + ", received  " + portData.sync))
        lastDataEventCounter = portData.sync

        fun Byte.toPositiveInt() = toInt() and 0xFF
        for (b in portData.portData)
            parser?.addByte(b.toPositiveInt())
    }

    private class FrameListener : ParserEventListener() {
        override fun onNewFrame(b1: Int, b2: Int) {
            val newDate = Date();
            if (date.time + 1000 < newDate.time) {
                postEvent(ParserFrameEvent())
                date = newDate
            }
        }
    }
}
