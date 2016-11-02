package com.probe.usb.host.bus

import com.probe.usb.host.pc.controller.event.UiReceiverCreatedEvent


abstract class UiReceiver : Receiver() {

    init {
        postEvent(UiReceiverCreatedEvent(this.javaClass))
    }
}