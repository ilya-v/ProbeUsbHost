package com.probe.usb.host.parser.internal.message;

import com.probe.usb.host.parser.internal.DataFormat;
import com.probe.usb.host.parser.internal.DataFormat.MessageType;

public class AccRegMessage extends GenericMessage {

    public AccRegMessage(int[] packetData) {
        super(MessageType.AccRegMessage, packetData);
    }

    public int getRegisterAddress() {
        return DataFormat.decodeAddressInAccRegMessage(packetData);
    }

    @Override
    public String getValue() {
        String result = "";
        if (packetData.length >= DataFormat.messageLength)
            result += "REG[" + getRegisterAddress() + "] = " + String.format("%02X%02X", packetData[2], packetData[3]);
        return result  + (result.isEmpty()? "" : " ") + super.getValue();
    }
}
