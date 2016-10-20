package com.probe.usb.host.parser.internal.message;

import com.probe.usb.host.common.ConfigParamType;
import com.probe.usb.host.parser.internal.DataFormat;

import static com.probe.usb.host.parser.internal.DataFormat.*;

public class ConfigParamMessage extends GenericMessage {

    public ConfigParamMessage(int[] packetData) {
        super(MessageType.ConfigParamMessage, packetData);
    }

    public int getParamIndex() { return decodeIndexInConfigParamMessage(packetData); }

    public String getParamName() {
        String name = "unknown";
        final int idx = getParamIndex();
        for (ConfigParamType p: ConfigParamType.values())
            if (p.index == idx)
                name = p.name();
        return name;
    }

    @Override
    public String getValue() {
        String result = "";
        if (packetData.length >= messageLength)
            result += getParamName() + "(" + getParamIndex() + ") = " + String.format("%02X%02X", packetData[2], packetData[3]);
        return result  + (result.isEmpty()? "" : " ") + super.getValue();
    }
}
