package com.probe.usb.test

import com.probe.usb.host.parser.ProbeUsbParser
import com.probe.usb.host.parser.internal.Frame
import com.probe.usb.host.parser.internal.RobustFrameDetector
import com.probe.usb.host.parser.processor.PrintProcessor
import org.junit.Assert
import org.junit.Test
import java.util.*


class MessageTest {

    val input = arrayOf(0xC1, 0xF4, 0xD2, 0x20, 0xE3, 0x00, 0xF4, 0x37)


    @Test
    fun accRegReadTest() {
        var result1 = ""
        val packetProcessor = PrintProcessor()
        val parser = ProbeUsbParser().addPacketProcessor(packetProcessor)
        for (b in input) {
            parser.addByte(b)
            val parsed = packetProcessor.popResult()
            result1 += parsed
            print(parsed)
        }
        Assert.assertTrue(result1.contains("AccRegMessage REG[32] = 0037 [F4 20 00 37]"))
    }

    @Test
    fun syncOnMessageTest() {

        val frames = ArrayList<Frame>();

        val frameDetector = RobustFrameDetector()
        for (b in input) {
            frameDetector.feedByte(b)
            while (frameDetector.getSyncFramesCount() > 0) {
                val f = frameDetector.popFrame()
                frames.add(f)
            }
        }

        Assert.assertEquals(frames.size, input.size/2)
    }

}