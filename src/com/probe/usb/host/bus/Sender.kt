package com.probe.usb.host.bus

interface Sender {

    fun postEvent(event : Any) {
        System.out.println("Post: " + event + " from " + this.javaClass.simpleName)
        Bus.post(event);
    }

    fun postDelayedEvent(event : Any) {
        System.out.println("Post Delayed: " + event + " from " + this.javaClass.simpleName)
        Bus.postDelayed(event);
    }
}