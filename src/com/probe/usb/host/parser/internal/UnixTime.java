package com.probe.usb.host.parser.internal;

import java.util.Date;

public class UnixTime {
    private int[] unixTimeBytes = {0, 0, 0, 0};

    public int getUnixTime() {
        return (unixTimeBytes[0] << 24) | (unixTimeBytes[1] << 16) | (unixTimeBytes[2] << 8) | (unixTimeBytes[3] << 0);
    }

    public void assignHiBytes(int b1, int b2) {
        unixTimeBytes[0] = b1;
        unixTimeBytes[1] = b2;
    }

    public int assignLoBytes(int b3, int b4) {
        unixTimeBytes[2] = b3;
        unixTimeBytes[3] = b4;
        return getUnixTime();
    }

    public Date getDate() {
        return new Date(getUnixTime() * 1000L);
    }

}
