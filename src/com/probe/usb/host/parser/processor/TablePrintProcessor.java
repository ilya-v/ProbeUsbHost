package com.probe.usb.host.parser.processor;

import com.probe.usb.host.parser.internal.*;
import com.probe.usb.host.parser.internal.message.AccRegMessage;
import com.probe.usb.host.parser.internal.message.UnixTimeMessage;

import java.util.Calendar;


public class TablePrintProcessor extends PrintProcessor {

    static private String format = "%.6f %.6f %.6f %.6f\n";
    static final private int
        timeHeader = -1,
        timeBytesHeader = -2,
        statusHeader = -3,
        accStatusHeader = -4,
        danglingHeader = -9999;

    private UnixTime unixTime = new UnixTime();



    @Override
    protected void processDataPacket(final int[] packetData) {
        super.processDataPacket(packetData);
        final DataPoint dp = DataFormat.decodeDataPacket(packetData, DataFormat.timeScale, DataFormat.accScale);
        output += String.format(format, dp.t, dp.ax, dp.ay, dp.az);
    }

    @Override
    protected void processDeviceStatePacket(int[] packetData) {
        super.processDeviceStatePacket(packetData);
        output += dumpPacketData(statusHeader, packetData);
    }

    @Override
    protected void processAccStatePacket(int[] packetData) {
        super.processAccStatePacket(packetData);
        output += dumpPacketData(accStatusHeader, packetData);
    }

    @Override
    protected void processSingleFrame(final int[] packetData) {
        super.processSingleFrame(packetData);
        output += dumpPacketData(danglingHeader, packetData);
    }

    @Override
    protected void processAccRegMessage(AccRegMessage message) {
        super.processAccRegMessage(message);
    }

    @Override
    protected void processUnixTimeMessage(UnixTimeMessage message) {
        super.processUnixTimeMessage(message);

        final int utime = DataFormat.decodeUnixTime(message.getPacketData(), unixTime);
        if (utime < 0) {
            output += dumpPacketData(timeBytesHeader, message.getPacketData());
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(unixTime.getDate());
        final int
                dmy = calendar.get(Calendar.DAY_OF_MONTH) * 1000000
                + calendar.get(Calendar.MONTH) * 10000
                + calendar.get(Calendar.YEAR),
                hms = calendar.get(Calendar.HOUR_OF_DAY) * 10000
                        + calendar.get(Calendar.MINUTE) * 100
                        + calendar.get(Calendar.SECOND);
        output += String.format(format, (double)timeHeader, 2.0, (double)dmy, (double)hms);
    }


    private String dumpPacketData(final int header, final int[] packetData) {
        String output = "";
        for (int i = 0; i < packetData.length/2; i++)
            output += String.format(format, header, 2.0, (double)packetData[2*i], (double)packetData[2*i + 1]);
        return output + (packetData.length % 2 > 0?
                            String.format(format, (double)header, 1.0, (double)packetData[packetData.length-1], 0.0) : "");
    }


}
