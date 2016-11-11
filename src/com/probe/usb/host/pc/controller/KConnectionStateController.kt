package com.probe.usb.host.pc.controller

import com.google.common.eventbus.Subscribe
import com.probe.usb.host.bus.Receiver
import com.probe.usb.host.pc.controller.event.*

object KConnectionStateController : Receiver() {

    private var gotFrames = false
    private var gotBytes = false
    private var status = ConnectionStatus.Disconnected

    @Subscribe
    fun onComPortConnectionEvent(connectionEvent: ComPortConnectionEvent) {
        updateStatus(if (connectionEvent.connectionStatus) ConnectionStatus.Connected else ConnectionStatus.Disconnected)
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun onTimerTick(event: TickEvent) {
        if ((status == ConnectionStatus.Reading || status == ConnectionStatus.Data) && !gotBytes)
            updateStatus(ConnectionStatus.Idle)
        else if (!gotFrames && gotBytes)
            updateStatus(ConnectionStatus.Reading)
        gotFrames = false
        gotBytes = false
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun onNewByte(comPortDataEvent: ComPortDataEvent) {
        if (status != ConnectionStatus.Data)
            updateStatus(ConnectionStatus.Reading)
        gotBytes = true
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun onNewFrame(parserFrameEvent: ParserFrameEvent) {
        updateStatus(ConnectionStatus.Data)
        gotFrames = true
    }


    private fun updateStatus(newStatus: ConnectionStatus) {
        if (newStatus != status) {
            postEvent(newStatus)
            postEvent(DebugLogEvent("New connection state: " + newStatus))
        }
        status = newStatus
    }
}