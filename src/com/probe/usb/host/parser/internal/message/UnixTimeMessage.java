package com.probe.usb.host.parser.internal.message;

import com.probe.usb.host.parser.internal.DataFormat;
import com.probe.usb.host.parser.internal.UnixTime;

import java.text.SimpleDateFormat;

import static com.probe.usb.host.parser.internal.DataFormat.MessageType;

public class UnixTimeMessage extends GenericMessage {

    public UnixTimeMessage(int[] packetData) {
        super(MessageType.UnixTimeMessage, packetData);

    }

    public boolean mergeInto(UnixTime unixTime) {
        DataFormat.decodeUnixTime(packetData, unixTime);
        return unixTime.isComplete();
    }
}
