package com.probe.usb.host.parser.internal;

import com.probe.usb.host.parser.ParserEventListener;
import com.probe.usb.host.parser.internal.DataFormat.PacketType;

import java.util.ArrayDeque;
import java.util.Deque;

import static com.probe.usb.host.parser.internal.DataFormat.*;

public class FramePacket {
    protected final PacketType packetType;
    protected ParserEventListener listener = new ParserEventListener();
    protected final FrameType[] typesSequence;
    protected Deque<Frame> frames = new ArrayDeque<>();


    public FramePacket(final PacketType packetType, final FrameType[] typesSequence) {
        this.packetType = packetType;
        this.typesSequence = typesSequence;
    }

    public PacketType getPacketType() { return packetType; }

    public FramePacket setListener(ParserEventListener listener) {
        this.listener = listener;
        return this;
    }

    public int length() {
        return typesSequence != null? typesSequence.length : 1;
    }


    public boolean addFrame(final Frame frame) {
        final boolean
                countersMatch = tailLength() == 0 || nextCounter(frames.getLast().getCounter()) == frame.getCounter(),
                frameTypeMatch = (typesSequence == null) || frame.getType() == typesSequence[tailLength()];
        if (countersMatch && frameTypeMatch) {
            frames.addLast(frame);
            listener.onPacketDataByte(this);
            return true;
        }
        reset();
        return false;
    }

    public int getPacketCount() { return frames.size() / length(); }

    public int[] popPacket() {
        int[] packetData = new int[length()];
        for (int i = 0; i < packetData.length; i++)
            packetData[i] = frames.removeFirst().b2;
        return packetData;
    }

    public void reset() {
        listener.onExpectNewPacket(this);
        while (tailLength() != 0)
            frames.removeLast();
    }

    protected int tailLength() { return frames.size() % length(); }
}
