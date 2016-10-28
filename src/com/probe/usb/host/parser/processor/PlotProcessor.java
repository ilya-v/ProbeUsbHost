package com.probe.usb.host.parser.processor;


import com.probe.usb.host.parser.internal.DataPoint;

import static com.probe.usb.host.parser.internal.DataFormat.accScale;
import static com.probe.usb.host.parser.internal.DataFormat.decodeDataPacket;
import static com.probe.usb.host.parser.internal.DataFormat.timeScale;

public class PlotProcessor extends PacketProcessor {

    public interface PointsListener { void onNewPoint(DataPoint point); }
    private PointsListener listener;
    public PlotProcessor setPointsListener(PointsListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    protected void processDataPacket(final int[] packetData) {
        super.processDataPacket(packetData);
        final DataPoint dp = decodeDataPacket(packetData, timeScale, accScale);
        if (listener != null)
            listener.onNewPoint(dp);
    }
}
