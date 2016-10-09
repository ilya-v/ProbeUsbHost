package com.probe.usb.host.parser.internal;

import com.probe.usb.host.parser.internal.DataFormat.PacketType;

public interface PacketProcessor {
    void processPacket(final PacketType packetType, final int[] packetData);
}
