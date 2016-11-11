package com.probe.usb.host.pc.controller

import com.google.common.eventbus.Subscribe
import com.probe.usb.host.bus.Receiver
import com.probe.usb.host.pc.controller.event.*
import java.util.*


object KPortScanner : Receiver() {

    enum class State (private val connected: Boolean) {
        zero        (false),
        havePorts   (false),
        checkingPort(true),
        haveData    (true),
        ;

        fun isConnected(): Boolean { return connected; }
    }

    private val delayMsec: Long = 1000

    private var state = State.zero
    private var enabled = false
    private var portNames:      List<String> = ArrayList()
    private var curPort: String? = null
    private var lastFrameTime = Date(0L)

    @Subscribe
    fun onPortScannerEnabled(enabledEvent : PortScannerEnableEvent) {
        enabled = enabledEvent.enabled
    }

    @Subscribe
    fun onPortsUpdate(comPortHasPortsEvent: ComPortHasPortsEvent) {
        this.portNames = comPortHasPortsEvent.ports
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun onNewFrame(parserFrameEvent: ParserFrameEvent) {
        lastFrameTime = Date()
    }

    private fun updateState(newState: State) {
        if (state == newState)
            return
        if (newState.isConnected() != state.isConnected())
            postEvent(ComPortConnectCommand(curPort, newState.isConnected()))
        postEvent(PortStatusEvent(newState))
        state = newState
    }

    private fun nextPort() {
        if (portNames.isEmpty()) {
            curPort = null
            return
        }
        val nextIndex = (portNames.indexOf(curPort) + 1) % portNames.size
        curPort = portNames[nextIndex]
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun onTimerTick(tick: TickEvent) {
        if (!enabled ) {
            updateState(State.zero)
            return
        }

        updateState(
            when (state) {
                State.zero -> {
                    nextPort()
                    if (curPort != null) State.havePorts else State.zero
                }
                State.havePorts -> {
                    nextPort()
                    State.checkingPort
                }
                State.checkingPort, State.haveData -> {
                    val gotFrames = lastFrameTime.time + delayMsec > Date().time
                    if (gotFrames) State.haveData else State.havePorts
                }
            }
        )
    }
}
