package com.probe.usb.host.bus

import com.probe.usb.host.pc.controller.event.ReceiverCreated

abstract class Receiver protected constructor() {
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

    fun postEvent(event : Any) {
        System.out.println("Post: " + event + " from " + this.javaClass.simpleName)
        Bus.post(event);
    }

    fun postDelayedEvent(event : Any) {
        System.out.println("Post Delayed: " + event + " from " + this.javaClass.simpleName)
        Bus.postDelayed(event);
    }
}