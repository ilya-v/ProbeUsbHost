package com.probe.usb.host.parser;

import com.probe.usb.host.parser.internal.*;
import com.probe.usb.host.parser.internal.DataFormat.PacketType;
import java.util.Calendar;


public class TablePacketProcessor implements PacketProcessor {

    static private String format = "%.6f %.6f %.6f %.6f\n";
    static final private int
        timeHeader = -1,
        timeBytesHeader = -2,
        statusHeader = -3,
        accStatusHeader = -4,
        danglingHeader = -9999;

    private UnixTime unixTime = new UnixTime();

    private String output = "";

    private ParserEventListener listener = new ParserEventListener();

    private String processDataPacket(final int[] packetData) {
        listener.onNewDataPacket(packetData);
        final DataPoint dp = DataFormat.decodeDataPacket(packetData, DataFormat.timeScale, DataFormat.accScale);
        return String.format(format, dp.t, dp.ax, dp.ay, dp.az);
    }

    private String processUnixTimePacket(final int[] packetData) {
        listener.onNewTimePacket(packetData);
        final int utime = DataFormat.decodeUnixTime(packetData, unixTime);
        if (utime < 0)
            return dumpPacketData(timeBytesHeader, packetData);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(unixTime.getDate());
        final int
                dmy = calendar.get(Calendar.DAY_OF_MONTH) * 1000000
                    + calendar.get(Calendar.MONTH) * 10000
                    + calendar.get(Calendar.YEAR),
                hms = calendar.get(Calendar.HOUR_OF_DAY) * 10000
                    + calendar.get(Calendar.MINUTE) * 100
                    + calendar.get(Calendar.SECOND);
        return String.format(format, (double)timeHeader, 2.0, (double)dmy, (double)hms);
    }

    private String processDeviceStatePacket(int[] packetData) {
        return dumpPacketData(statusHeader, packetData);
    }

    private String processAccStatePacket(int[] packetData) {
        return dumpPacketData(accStatusHeader, packetData);
    }

    private String processSingleFrame(final int[] packetData) {
        return dumpPacketData(danglingHeader, packetData);
    }

    private String dumpPacketData(final int header, final int[] packetData) {
        String output = "";
        for (int i = 0; i < packetData.length/2; i++)
            output += String.format(format, header, 2.0, (double)packetData[2*i], (double)packetData[2*i + 1]);
        return output + (packetData.length % 2 > 0?
                            String.format(format, (double)header, 1.0, (double)packetData[packetData.length-1], 0.0) : "");
    }

    @Override
    public void processPacket(PacketType packetType, int[] packetData) {
        output +=   (packetType == PacketType.DataPacket)       ?   processDataPacket(packetData) :
                    (packetType == PacketType.UnixTimePacket)   ?   processUnixTimePacket(packetData) :
                    (packetType == PacketType.DeviceStatePacket)?   processDeviceStatePacket(packetData) :
                    (packetType == PacketType.AccStatePacket)   ?   processAccStatePacket(packetData) :
                                                                    processSingleFrame(packetData);
    }

    public String popResult() {
        final String result = output;
        output = "";
        return result;
    }
}
