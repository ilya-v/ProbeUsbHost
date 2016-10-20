package com.probe.usb.host.parser.processor;

import com.probe.usb.host.parser.internal.message.AccRegMessage;
import com.probe.usb.host.parser.internal.message.ConfigParamMessage;
import com.probe.usb.host.parser.internal.message.GenericMessage;
import com.probe.usb.host.parser.internal.message.UnixTimeMessage;

import java.util.HashSet;
import java.util.Set;

import static com.probe.usb.host.parser.internal.DataFormat.*;

public class PrintProcessor extends PacketProcessor {

    protected String output = "";

    public String popResult() {
        final String result = output;
        output = "";
        return result;
    }

    public enum OutputElement {
        DataPacket,
        DeviceStatePacket,
        AccStatePacket,
        DanglingPacket,
        AccRegMessage,
        UnixTimeMessage,
        ConfigParamMessage,
        GenericMessage;
    };

    protected  Set<OutputElement> enabledOutputElements = new HashSet<>();
    {
        for (OutputElement oe: OutputElement.values())
            enabledOutputElements.add(oe);
    }

    public PrintProcessor enableOutputOf(final OutputElement e) {
        enabledOutputElements.add(e);
        return this;
    }

    public PrintProcessor disableOutputOf(final OutputElement e) {
        enabledOutputElements.remove(e);
        return this;
    }


    @Override
    protected void processDataPacket(final int[] packetData) {
        super.processDataPacket(packetData);
        if (enabledOutputElements.contains(OutputElement.DataPacket))
            output += dumpPacketData(PacketType.DataPacket, packetData);
    }

    @Override
    protected void processDeviceStatePacket(int[] packetData) {
        super.processDeviceStatePacket(packetData);
        if (enabledOutputElements.contains(OutputElement.DeviceStatePacket))
            output += dumpPacketData(PacketType.DeviceStatePacket, packetData);
    }

    @Override
    protected void processAccStatePacket(int[] packetData) {
        super.processAccStatePacket(packetData);
        if (enabledOutputElements.contains(OutputElement.AccStatePacket))
            output += dumpPacketData(PacketType.AccStatePacket, packetData);
    }

    @Override
    protected void processSingleFrame(final int[] packetData) {
        super.processSingleFrame(packetData);
        if (enabledOutputElements.contains(OutputElement.DanglingPacket))
            output += dumpPacketData(PacketType.DanglingPacket, packetData);
    }

    @Override
    protected void processAccRegMessage(AccRegMessage message) {
        super.processAccRegMessage(message);
        if (enabledOutputElements.contains(OutputElement.AccRegMessage))
            output += dumpMessage(message);
    }

    @Override
    protected void processUnixTimeMessage(UnixTimeMessage message) {
        super.processUnixTimeMessage(message);
        if (enabledOutputElements.contains(OutputElement.UnixTimeMessage))
            output += dumpMessage(message);
    }

    @Override
    protected void processConfigParamMessage(ConfigParamMessage message) {
        super.processConfigParamMessage(message);
        if (enabledOutputElements.contains(OutputElement.ConfigParamMessage))
            output += dumpMessage(message);
    }

    @Override
    protected void processGenericMessage(final GenericMessage message) {
        super.processGenericMessage(message);
        if (enabledOutputElements.contains(OutputElement.GenericMessage))
            output += dumpMessage(message);
    }

    protected String dumpPacketData(PacketType packetType, final int[] packetData) {
        String line = packetType.name() + " ";
        for (int b:  packetData)
            line += String.format("%02X ", b);
        if (line.endsWith(" "))
            line = line.substring(0, line.length() -1);
        return line + "\n";
    }

    protected String dumpMessage(GenericMessage message) {
        return message.toString() + "\n";
    }
}
