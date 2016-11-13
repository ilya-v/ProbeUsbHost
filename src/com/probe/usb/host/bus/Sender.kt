package com.probe.usb.host.bus

import com.probe.usb.host.pc.controller.event.SilentEvent

interface Sender {

    fun postEvent(event : Any) {
        if (event !is SilentEvent)
            System.out.println("Post: " + event + " from " + this.javaClass.simpleName)
        Bus.post(event);
    }

    fun postDelayedEvent(event : Any) {
        System.out.println("Post Delayed: " + event + " from " + this.javaClass.simpleName)
        Bus.postDelayed(event);
    }
}