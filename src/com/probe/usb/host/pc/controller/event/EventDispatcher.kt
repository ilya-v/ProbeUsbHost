package com.probe.usb.host.pc.controller.event

import com.google.common.eventbus.Subscribe
import com.probe.usb.host.bus.Receiver

object EventDispatcher : Receiver() {

    @Subscribe
    fun onUiInputFileActivated(event: UiInputFileActiveEvent) {
        postEvent(UiPortScanEnabledCommand(!event.active))
        postEvent(UiCommandEnableCommand(!event.active))
    }
}