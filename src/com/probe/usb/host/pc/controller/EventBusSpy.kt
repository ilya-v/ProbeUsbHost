package com.probe.usb.host.pc.controller

import com.google.common.eventbus.Subscribe
import com.probe.usb.host.bus.Receiver
import com.probe.usb.host.pc.controller.event.TickEvent
import com.probe.usb.host.pc.controller.event.UiLogTextCommand

object EventBusSpy : Receiver() {

    var enabled = true

    @Subscribe
    fun onEvent(event : Any) {
        if (event !is TickEvent && event !is UiLogTextCommand)
            System.out.println("Spy received: " + event.toString())
    }


}