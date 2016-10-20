package com.probe.usb.host.parser.processor;

import com.probe.usb.host.parser.ParserEventListener;
import com.probe.usb.host.parser.internal.DataFormat.PacketType;
import com.probe.usb.host.parser.internal.message.AccRegMessage;
import com.probe.usb.host.parser.internal.message.ConfigParamMessage;
import com.probe.usb.host.parser.internal.message.GenericMessage;
import com.probe.usb.host.parser.internal.message.UnixTimeMessage;
import com.probe.usb.host.parser.internal.message.factory.MessageFactory;

public class PacketProcessor {

    protected ParserEventListener listener = new ParserEventListener();

    public PacketProcessor setListener(ParserEventListener listener) {
        this.listener = listener;
        return this;
    }

    public void processPacket(PacketType packetType, int[] packetData) {
        listener.onNewPacket(packetData);
        if (packetType == PacketType.DataPacket) processDataPacket(packetData);
        else if (packetType == PacketType.MessageFramePacket) processMessagePacket(packetData);
        else if (packetType == PacketType.DeviceStatePacket) processDeviceStatePacket(packetData);
        else if (packetType == PacketType.AccStatePacket) processAccStatePacket(packetData);
        else processSingleFrame(packetData);
    }

    protected void processDataPacket(final int[] packetData) {
        listener.onNewDataPacket(packetData);
    }

    protected void processDeviceStatePacket(int[] packetData) {
        listener.onNewDeviceStatePacket(packetData);
    }

    protected void processAccStatePacket(int[] packetData) {
        listener.onNewAccStatePacket(packetData);
    }

    protected void processSingleFrame(int[] packetData) {
        listener.onNewAccStatePacket(packetData);
    }


    protected void processMessagePacket(final int[] packetData) {
        listener.onNewMessagePacket(packetData);
        final GenericMessage message = MessageFactory.createMessage(packetData);
        if (message.getClass().equals(ConfigParamMessage.class))    processConfigParamMessage((ConfigParamMessage) message);
        else if (message.getClass().equals(UnixTimeMessage.class))  processUnixTimeMessage((UnixTimeMessage) message);
        else if (message.getClass().equals(AccRegMessage.class))    processAccRegMessage((AccRegMessage) message);
        else processGenericMessage(message);
    }

    protected void processGenericMessage(final GenericMessage message) {
        listener.onGenericMessage(message.getPacketData());
    }

    protected void processAccRegMessage(final AccRegMessage message) {
        listener.onNewAccRegMessage(message.getPacketData());
    }

    protected void processConfigParamMessage(ConfigParamMessage message) {
        listener.onNewconfigParamMessage(message.getPacketData());
    }

    protected void processUnixTimeMessage(UnixTimeMessage message) {
        listener.onNewUnixTimeMessage(message.getPacketData());
    }
}
