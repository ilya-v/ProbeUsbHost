package com.probe.usb.host.parser.internal.message.factory;


import com.probe.usb.host.parser.internal.DataFormat;
import com.probe.usb.host.parser.internal.DataFormat.MessageType;
import com.probe.usb.host.parser.internal.message.AccRegMessage;
import com.probe.usb.host.parser.internal.message.ConfigParamMessage;
import com.probe.usb.host.parser.internal.message.GenericMessage;
import com.probe.usb.host.parser.internal.message.UnixTimeMessage;


public class MessageFactory {
    static public GenericMessage createMessage(final int[] packetData) {
        if (packetData == null || packetData.length != DataFormat.messageLength)
            return new GenericMessage(MessageType.GenericMessage, packetData);
        return
                packetData[0] == MessageType.UnixTimeMessage.getFirstByte()?
                        new UnixTimeMessage(packetData) :
                packetData[0] == MessageType.ConfigParamMessage.getFirstByte()?
                        new ConfigParamMessage(packetData) :
                packetData[0] == MessageType.AccRegMessage.getFirstByte()?
                        new AccRegMessage(packetData) :
                        new GenericMessage(MessageType.GenericMessage, packetData);
    }
}
