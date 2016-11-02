package com.probe.usb.host.pc.controller;

import com.google.common.eventbus.Subscribe
import com.probe.usb.host.bus.Receiver
import com.probe.usb.host.pc.controller.event.DebugLogEvent
import com.probe.usb.host.pc.controller.event.ErrorLogEvent
import com.probe.usb.host.pc.controller.event.InfoLogEvent
import com.probe.usb.host.pc.controller.event.UiLogTextCommand

object Logger : Receiver() {

    @Subscribe
    fun printLine(line: String) {
        postEvent(UiLogTextCommand(line + "\n"))
    }

    @Subscribe
    fun printDebugLine(event : DebugLogEvent) {
        System.out.println("D: " + event.message)
    }

    @Subscribe
    fun printError(event: ErrorLogEvent) {
        postEvent(UiLogTextCommand("Error: " + event.message + "\n"))
    }

    @Subscribe
    fun printInfo(event: InfoLogEvent) {
        postEvent(UiLogTextCommand(event.message + "\n"))
    }
}

