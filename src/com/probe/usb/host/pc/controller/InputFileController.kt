package com.probe.usb.host.pc.controller

import com.google.common.eventbus.Subscribe
import com.probe.usb.host.bus.Bus
import com.probe.usb.host.bus.Receiver
import com.probe.usb.host.pc.controller.event.*
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*


object InputFileController : Receiver() {

    val chunkSize = 1024
    var dataPos = 0
    var fileData : ByteArray? = null
    var dataEventCounter = 0


    @Subscribe
    fun onOpenRawInputFile(openRawInputFileCommand: OpenRawInputFileCommand) {
        postEvent(UiInputBusyCommand(true))
        try {
            dataPos = 0
            fileData = Files.readAllBytes(Paths.get(openRawInputFileCommand.fileName))
        } catch (ex: IOException) {
            Bus.post(ex.toString())
            postEvent(UiInputBusyCommand(false))
            return
        }
        postEvent(MoreInputRawDataEvent())
    }


    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun postNextChunk(event: MoreInputRawDataEvent) {
        if (fileData == null || dataPos >= fileData!!.size){
            fileData = null
            dataPos = 0
            postEvent(UiInputBusyCommand(false))
            postEvent(NewDataTrackEvent())
            return
        }
        val newPos = Math.min(fileData!!.size, dataPos + chunkSize)
        postDelayedEvent(ComPortDataEvent(Arrays.copyOfRange(fileData, dataPos, newPos), dataEventCounter++) )
        dataPos = newPos
        postDelayedEvent(MoreInputRawDataEvent())
    }
}