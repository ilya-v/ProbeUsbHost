package com.probe.usb.host.common;

public enum ConfigCommand  {
    writeConfig    (0xF1, true,  true),
    readConfig     (0xF2, false, true),
    accRegWrite    (0xF3, true,  true),
    accRegRead     (0xF4, false, true),
    setTimeHi      (0xF5, false, true),
    setTimeLo      (0xF6, false, true),
    startRecording (0xFA, false, false),
    stopRecording  (0xFC, false, false)
    ;

    final short     firstByte;
    final boolean   usesSecondByte;
    final boolean   usesLastBytes;

    ConfigCommand(final int firstByte, final boolean usesSecondByte, final boolean usesLastBytes) {
        this.firstByte = (short) firstByte;
        this.usesSecondByte = usesSecondByte;
        this.usesLastBytes = usesLastBytes;
    }

    public short getFirstByte() {
        return firstByte;
    }
    public boolean usesSecondByte() { return usesSecondByte; }
    public boolean usesLastBytes() { return usesLastBytes; }
}
