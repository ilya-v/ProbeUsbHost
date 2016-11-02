package com.probe.usb.host.pc.ui.controller

import com.google.common.eventbus.Subscribe
import com.probe.usb.host.Context
import com.probe.usb.host.bus.UiReceiver
import com.probe.usb.host.pc.controller.event.UiLogTextCommand
import javax.swing.JTextArea

object LoggerUiController : UiReceiver() {

    private var sendLog : JTextArea? = null

    fun setLogTextArea(sendLog : JTextArea) {
        this.sendLog = sendLog
    }

    @Subscribe
    fun onNewLogText(text : UiLogTextCommand) {
        Context.invokeUi { sendLog?.append(text.text) }
    }
}