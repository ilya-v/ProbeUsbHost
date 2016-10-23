package com.probe.usb.host.parser;

import com.probe.usb.host.parser.internal.*;
import com.probe.usb.host.parser.processor.PacketProcessor;

import java.util.LinkedList;
import java.util.List;

import static com.probe.usb.host.parser.internal.DataFormat.*;

public class ProbeUsbParser
{
    protected ParserEventListener listener = new ParserEventListener();

    protected FramePacket[] framePackets = new FramePacket[] {
        new FramePacket(PacketType.DataPacket, dataFramePacketTypes),
        new FramePacket(PacketType.MessageFramePacket, messageFramePacketTypes),
        new FramePacket(PacketType.DeviceStatePacket, deviceStateFrameTypes),
        new FramePacket(PacketType.AccStatePacket, accStateFrameTypes),
        new FramePacket(PacketType.DanglingPacket, null)
    };

    protected RobustFrameDetector frameDetector = new RobustFrameDetector();
    protected List<PacketProcessor> packetProcessors = new LinkedList<>();
    protected boolean resetFrameCounterAfterEachFramePacket = true;

    public ProbeUsbParser setListener(ParserEventListener listener) {
        this.listener = listener;
        for (FramePacket fp: framePackets)
            fp.setListener(listener);
        return this;
    }
    public ProbeUsbParser addPacketProcessor(PacketProcessor packetProcessor) {
        this.packetProcessors.add(packetProcessor);
        return this;
    }
    public ProbeUsbParser resetFrameCounterAfterEachFramePacket(final boolean flag) {
        this.resetFrameCounterAfterEachFramePacket = flag;
        return this;
    }
    
    public void addByte(final int b) {
        listener.onNewByte(b);
        frameDetector.feedByte(b);
        while (frameDetector.getSyncFramesCount() > 0)
            addFrame(frameDetector.popFrame());
    }
    
    public void addFrame(final Frame frame) {

        listener.onNewFrame(frame.b1, frame.b2);

        FramePacket updatedFramePacket = null;
        for (FramePacket fp: framePackets)
            if (fp.addFrame(frame)) {
                updatedFramePacket = fp;
                break;
            }

        for (FramePacket fp: framePackets)
            while (fp.getPacketCount() > 0) {
                final int[] packetData = fp.popPacket();
                for (PacketProcessor pp : packetProcessors) {
                    pp.processPacket(fp.getPacketType(), packetData);
                    if (resetFrameCounterAfterEachFramePacket)
                        frameDetector.resetFrameCounter();
                }
            }

        if (updatedFramePacket != null)
            for (FramePacket fp: framePackets)
                if (fp != updatedFramePacket)
                    fp.reset();
    }
}
