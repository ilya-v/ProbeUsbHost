package com.probe.usb.host.parser.internal.message;

import com.probe.usb.host.parser.internal.DataFormat;

public class GenericMessage {
    protected final int[] packetData;
    protected final DataFormat.MessageType type;

    public GenericMessage(final DataFormat.MessageType type, final int[] packetData) {
        this.type = type;
        this.packetData = packetData;
    }

    public int[] getPacketData() {
        return packetData;
    }

    public DataFormat.MessageType getType() {
        return type;
    }


    public String getTitle() {
        return getType().name();
    }

    public String getValue() {
        String result = "[";
        if (packetData != null)
            for (int b : packetData)
                result += String.format("%02X ", b);
        if (result.endsWith(" "))
            result = result.substring(0, result.length() - 1);
        result += "]";
        return result;
    }

    @Override
    public String toString() {
        return getTitle() + " " + getValue();
    }
}
