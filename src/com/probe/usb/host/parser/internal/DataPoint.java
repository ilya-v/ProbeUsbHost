package com.probe.usb.host.parser.internal;


public class DataPoint {
    public double t, ax, ay, az;

    public DataPoint(double t, double ax, double ay, double az) {
        this.t = t;
        this.ax = ax;
        this.ay = ay;
        this.az = az;
    }
}
