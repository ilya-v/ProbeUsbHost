package com.probe.usb.host.parser.internal;

import java.util.Date;

public class UnixTime {
    private int[] unixTimeBytes = {0, 0, 0, 0};

    boolean
            hasLoBytes = false,
            hasHiBytes = false;

    public boolean isComplete() { return hasHiBytes && hasLoBytes; }

    public int getUnixTime() {
        return (unixTimeBytes[0] << 24) | (unixTimeBytes[1] << 16) | (unixTimeBytes[2] << 8) | (unixTimeBytes[3] << 0);
    }

    public void assignHiBytes(int b1, int b2) {
        unixTimeBytes[0] = b1;
        unixTimeBytes[1] = b2;
        hasHiBytes = true;
    }

    public int assignLoBytes(int b3, int b4) {
        unixTimeBytes[2] = b3;
        unixTimeBytes[3] = b4;
        hasLoBytes = true;
        return getUnixTime();
    }

    public Date getDate() {
        return new Date(getUnixTime() * 1000L);
    }

}
