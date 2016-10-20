package com.probe.usb.host.parser;

import com.probe.usb.host.parser.internal.FramePacket;

public class ParserEventListener {
    public void onNewByte(final int b) {}
    public void onSync(final int[] bytes, final int nBytesInSync) {}
    public void onNewFrame(final int b1, final int b2) {}
    public void onNewDataPacket(final int[] packetData) {}
    public void onNewMessagePacket(final int[] packetData) {}
    public void onNewTimePacket(final int[] packetData) {}
    public void onNewSingleFrame(final int b1, final int b2) {}
    public void onExpectNewPacket(final FramePacket p) {}
    public void onPacketDataByte(final FramePacket p) {}

    public void onNewAccRegMessage(final int[] packetData) {}

    public void onNewDeviceStatePacket(final int[] packetData) {}

    public void onNewAccStatePacket(final int[] packetData) {}

    public void onNewPacket(final int[] packetData) {
    }

    public void onGenericMessage(final int[] packetData) {
    }

    public void onNewconfigParamMessage(final int[] packetData) {
    }
    public void onNewUnixTimeMessage(int[] packetData) {
    }
}
