package com.probe.usb.host.parser.internal;

import com.probe.usb.host.parser.internal.DataFormat.FrameType;

import java.util.List;
import java.util.Objects;

public class Frame {
    public int b1, b2;
    public Frame(final int b1, final int b2) { this.b1 = b2; this.b2 = b2; }
    public Frame(final int ...b) { b1 = b[0]; b1 = b[1]; }
    public Frame(List<Integer> bytes) { b1 = bytes.get(0); b2 = bytes.get(1); }
    public FrameType getType() { return  FrameType.fromValue(DataFormat.extractFrameTypeValue(b1)); }
    int getCounter() { return DataFormat.extractCounter(b1); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Frame)) return false;
        Frame frame = (Frame) o;
        return b1 == frame.b1 &&
                b2 == frame.b2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(b1, b2);
    }
}
