package com.probe.usb.host.commander;

public enum ConfigCommand  {
    writeConfig    (0xF1),
    readConfig     (0xF2),
    accRegWrite    (0xF3),
    accRegRead     (0xF4),
    setTimeHi      (0xF5),
    setTimeLo      (0xF6),
    ;

    final short firstByte;

    ConfigCommand(final int firstByte) {
        this.firstByte = (short) firstByte;
    }

    public short getFirstByte() {
        return firstByte;
    }
}
