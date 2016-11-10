package com.probe.usb.host.parser.processor
import com.probe.usb.host.bus.Sender
import com.probe.usb.host.parser.internal.DataFormat
import com.probe.usb.host.pc.controller.event.PlotDataPointEvent

object KPlotProcessor : PacketProcessor(), Sender {

    override fun processDataPacket(packetData: IntArray) {
        super.processDataPacket(packetData)
        val dp = DataFormat.decodeDataPacket(packetData)
        postEvent(PlotDataPointEvent(dp))
    }
}