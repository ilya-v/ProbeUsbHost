package com.probe.usb.host.parser.internal.message;

import com.probe.usb.host.parser.internal.DataFormat;
import com.probe.usb.host.parser.internal.UnixTime;

import java.text.SimpleDateFormat;

import static com.probe.usb.host.parser.internal.DataFormat.MessageType;

public class UnixTimeMessage extends GenericMessage {

    UnixTime unixTime = new UnixTime();
    boolean
            hasLoBytes = false,
            hasHiBytes = false;

    public UnixTimeMessage(int[] packetData) {
        super(MessageType.UnixTimeMessage, packetData);
        final int decodedTime = DataFormat.decodeUnixTime(packetData, unixTime);
        if (decodedTime < 0)    hasHiBytes = true;
        else                    hasLoBytes = true;
    }

    public boolean merge(UnixTimeMessage message) {
        if (message.packetData.length < DataFormat.messageLength)
            return hasHiBytes && hasLoBytes;
        if (message.hasLoBytes)
            unixTime.assignLoBytes(message.packetData[2], message.packetData[3]);
        if (message.hasHiBytes)
            unixTime.assignHiBytes(message.packetData[2], message.packetData[2]);
        hasLoBytes = message.hasLoBytes || hasLoBytes;
        hasHiBytes = message.hasHiBytes || hasHiBytes;
        return hasHiBytes && hasLoBytes;
    }

    @Override
    public String getValue() {
        String result = "";
        if (hasHiBytes && hasLoBytes)
            result += new SimpleDateFormat("yyyy-MM-dd hh.mm.ss ").format(unixTime.getDate());
        return result + super.getValue();
    }

    public UnixTime unixTime() { return unixTime; }
    public boolean isTimeComplete() { return hasHiBytes && hasLoBytes; }
}
