package com.probe.usb.host.parser.internal;

import static com.probe.usb.host.parser.internal.DataFormat.FrameType.*;

public class DataFormat {

    public static final double
            timeScale = 1.0e4,
            accScale  = 5230.0;//1365.333333333;

    static public FrameType[]
            dataFramePacketTypes    = {frameT1, frameT2, frameAX1, frameAX2, frameAY1, frameAY2, frameAZ1, frameAZ2},
            messageFramePacketTypes = {frameMSG1, frameMSG2, frameMSG3, frameMSG4},
            deviceStateFrameTypes   = {frameST},
            accStateFrameTypes      = {frameSA};

    public enum PacketType { DataPacket, MessageFramePacket, DeviceStatePacket, AccStatePacket, DanglingPacket };

    static public int extractCounter(final int b) { return b & 0b00000111; }
    static public int nextCounter(final int counter) { return (counter + 1) & 0b00000111; }
    static public int extractFrameTypeValue(final int b) { return (b >> 4) & 0x0F; }

    public static int decode2sComplement(final int hiByte, final int loByte) {
        return (short)((hiByte << 8) | loByte);
    }

    public enum FrameType {
        frameST   (0b0000),
        frameSA   (0b0001),
        frameT1   (0b0010),
        frameT2   (0b0011),
        frameAX1  (0b0100),
        frameAX2  (0b0101),
        frameAY1  (0b0110),
        frameAY2  (0b0111),
        frameAZ1  (0b1000),
        frameAZ2  (0b1001),
        frameMSG1 (0b1100),
        frameMSG2 (0b1101),
        frameMSG3 (0b1110),
        frameMSG4 (0b1111);

        final int value;
        public int getValue() { return value; }

        FrameType(final int value) {
            this.value = value;
        }

        static public FrameType fromValue(final int value) {
            for (FrameType ft: values())
                if (ft.getValue() == value)
                    return ft;
            return null;
        }
    }

    public enum MessageType {
        UnixTimeMessage(unixTimeMessageFirstByte),
        ConfigParamMessage(configParamMessageFirstByte),
        AccRegMessage(accRegMessageFirstByte),
        GenericMessage(-1),
        ;

        final int firstByte;
        public int getFirstByte() { return  firstByte; }
        MessageType(final int firstByte) { this.firstByte = firstByte; }
    }

    static protected final int
            unixTimeMessageFirstByte = 0xF6,
            configParamMessageFirstByte = 0xF2,
            accRegMessageFirstByte = 0xF4;

    static public final int
            messageLength = 4;

    static public final int
            unixTimeHiSecondByte = 0x00,
            unixTimeLoSecondByte = 0x01;

    static public DataPoint decodeDataPacket(final int[] packetData, double timeScale, double accScale) {
        return new DataPoint(
                (packetData[0] * 0xFF + packetData[1]) / timeScale, //TODO: how is it actually encoded?
                //decode2sComplement(packetData[2], packetData[3]) / accScale,
                //decode2sComplement(packetData[4], packetData[5]) / accScale,
                //decode2sComplement(packetData[6], packetData[7]) / accScale);
                decode2sComplement(packetData[3], packetData[2]) / accScale,
                decode2sComplement(packetData[5], packetData[4]) / accScale,
                decode2sComplement(packetData[7], packetData[6]) / accScale);
    }

    static public int decodeUnixTime(final int[] packetData, UnixTime unixTime) {
        if (packetData.length != 4)
            return -1;
        if (packetData[0] == unixTimeMessageFirstByte && packetData[1] == unixTimeHiSecondByte) {
            unixTime.assignHiBytes(packetData[2], packetData[3]);
            return -1; // time not ready
        }

        if (packetData[0] == unixTimeMessageFirstByte && packetData[1] == unixTimeLoSecondByte) {
            return unixTime.assignLoBytes(packetData[2], packetData[3]);
        }
        return -1;
    }

    static public int decodeIndexInConfigParamMessage(final int[] packetData) {
        return (packetData != null && packetData.length == messageLength
                && packetData[0] == configParamMessageFirstByte)?
                packetData[1] : -1;
    }

    static public int decodeAddressInAccRegMessage(final int[] packetData) {
        return (packetData != null && packetData.length == messageLength
                && packetData[0] == accRegMessageFirstByte)?
                packetData[1] : -1;
    }
}
