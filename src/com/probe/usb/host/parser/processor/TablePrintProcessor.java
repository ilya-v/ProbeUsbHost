package com.probe.usb.host.parser.processor;

import com.probe.usb.host.parser.internal.DataPoint;
import com.probe.usb.host.parser.internal.UnixTime;
import com.probe.usb.host.parser.internal.message.AccRegMessage;
import com.probe.usb.host.parser.internal.message.GenericMessage;
import com.probe.usb.host.parser.internal.message.UnixTimeMessage;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.probe.usb.host.parser.internal.DataFormat.*;


public class TablePrintProcessor extends PrintProcessor {

    static private String format = "%.6f %.6f %.6f %.6f\n";
    static final private int
        timeHeader                  = -1,
        dataPacketHeader            = -2,
        deviceStatusHeader          = -3,
        accStatusHeader             = -4,
        accRegMessageHeader         = -6,
        configParamMessageHeader    = -7,
        unixTimeMessageHeader       = -8,
        genericMessageHeader        = -9,
        danglingHeader              = -9999;

    static protected final Map<PacketType, Integer> packetHeaders = new HashMap<PacketType, Integer>(){{
        put(PacketType.AccStatePacket,      accStatusHeader);
        put(PacketType.DanglingPacket,      danglingHeader);
        put(PacketType.DeviceStatePacket,   deviceStatusHeader);
        put(PacketType.DataPacket,          dataPacketHeader);
    }};

    static protected final Map<MessageType, Integer> messageHeaders = new HashMap<MessageType, Integer>() {{
        put(MessageType.AccRegMessage,      accRegMessageHeader);
        put(MessageType.ConfigParamMessage, configParamMessageHeader);
        put(MessageType.UnixTimeMessage,    unixTimeMessageHeader);
        put(MessageType.GenericMessage,     genericMessageHeader);
    }};


    {
        disableOutputOf(OutputElement.DataPacket);
    }

    private UnixTime unixTime = new UnixTime();

    @Override
    protected void processDataPacket(final int[] packetData) {
        super.processDataPacket(packetData);
        final DataPoint dp = decodeDataPacket(packetData, timeScale, accScale);
        output += String.format(format, dp.t, dp.ax, dp.ay, dp.az);
    }

    @Override
    protected void processAccRegMessage(AccRegMessage message) {
        super.processAccRegMessage(message);
    }

    @Override
    protected void processUnixTimeMessage(UnixTimeMessage message) {
        super.processUnixTimeMessage(message);
        if (!message.mergeInto(unixTime))
            return;

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

    @Override
    protected String dumpPacketData(PacketType packetType, final int[] packetData) {
        final int header = packetHeaders.get(packetType);
        return dumpBytesToTable(header, packetData);
    }

    @Override
    protected String dumpMessage(GenericMessage message) {
        final int header = messageHeaders.get(message.getType());
        return dumpBytesToTable(header, message.getPacketData());
    }

    protected String dumpBytesToTable(final int header, final int[] bytes) {
        String output = "";
        final int L = bytes.length;
        for (int i = 0; i < L/2; i++)
            output += String.format(format, (double)header, 2.0, (double)bytes[2*i], (double)bytes[2*i + 1]);
        return output + (L % 2 > 0?
                String.format(format, (double)header, 1.0, (double)bytes[L-1], 0.0) : "");
    }
}
