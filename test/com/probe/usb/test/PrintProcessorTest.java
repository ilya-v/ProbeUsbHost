package com.probe.usb.test;

import com.probe.usb.host.parser.ProbeUsbParser;
import com.probe.usb.host.parser.processor.PrintProcessor;
import org.junit.Test;

public class PrintProcessorTest {
    static protected int ctr(int c) { return c; }
    static protected int t(int t) { return t<< 4;}

    final int[] input  = {
            t(0b1100) | ctr(0b0000),    0xF4,
            t(0b1101) | ctr(0b0001),    0x10,
            t(0b1110) | ctr(0b0010),    0x01,
            t(0b1111) | ctr(0b0011),    0x02,

            t(0b1100) | ctr(0b0000),    0xF2,
            t(0b1101) | ctr(0b0001),    0x0A,
            t(0b1110) | ctr(0b0010),    0x00,
            t(0b1111) | ctr(0b0011),    0x01,

            t(0b1100) | ctr(0b0000),    0xFF,
            t(0b1101) | ctr(0b0001),    0x10,
            t(0b1110) | ctr(0b0010),    0x00,
            t(0b1111) | ctr(0b0011),    0x01,
    };



    @Test
    public void testMessages() {

        String result1 = "";
        PrintProcessor packetProcessor = new PrintProcessor();
        ProbeUsbParser parser = new ProbeUsbParser().addPacketProcessor(packetProcessor);
        for (int b : input) {
            parser.addByte(b);
            final String parsed = packetProcessor.popResult();
            result1 += parsed;
            System.out.print(parsed);
        }
    }

    @Test
    public void testAccRegMessage() {
        final int[] input = {
                0xC1, 0xF4, 0xD2, 0x20,
                0xE3, 0x00, 0xF4, 0x37,

                0xC5, 0xF4, 0xD6, 0x32,
                0xE7, 0x00, 0xF8, 0x7F

        };

        PrintProcessor packetProcessor = new PrintProcessor();
        ProbeUsbParser parser = new ProbeUsbParser().addPacketProcessor(packetProcessor);
        String result = "";
        for (int b : input) {
            parser.addByte(b);
            final String parsed = packetProcessor.popResult();
            result += parsed;
            System.out.print(parsed);
        }

    }
}
