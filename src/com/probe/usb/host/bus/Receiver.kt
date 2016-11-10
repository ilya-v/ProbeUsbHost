package com.probe.usb.host.bus

import com.probe.usb.host.pc.controller.event.ReceiverCreated


abstract class Receiver : Sender {
    init {
        Bus.register(this)
        postEvent(ReceiverCreated(this.javaClass))
    }

    var active = true
        set(value) {
            if (active!=value)
                if (active)
                    Bus.register(this) else Bus.unregister(this)
        }

}