package com.probe.usb.host.parser.internal.message;


import java.util.ArrayList;
import java.util.List;

public enum AccLis331Register {
    CTRL_REG1(      true,   0x20),
    CTRL_REG2(      true,   0x21),
    CTRL_REG3(      true,   0x22),
    CTRL_REG4(      true,   0x23),
    CTRL_REG5(      true,   0x24),
    HP_FILTER_RESET(false,  0x25),
    REFERENCE(      true,   0x26),
    STATUS_REG(     false,  0x27),
    OUT_X_L(        false,  0x28),
    OUT_X_H(        false,  0x29),
    OUT_Y_L(        false,  0x2A),
    OUT_Y_H(        false,  0x2B),
    OUT_Z_L(        false,  0x2C),
    OUT_Z_H(        false,  0x2D),
    INT1_CFG(       true,   0x30),
    INT1_SOURCE(    false,  0x31),
    INT1_THS(       true,   0x32),
    INT1_DURATION(  true,   0x33),
    INT2_CFG(       true,   0x34),
    INT2_SOURCE(    false,  0x35),
    INT2_THS(       true,   0x36),
    INT2_DURATION(  true,   0x37),
    ;

    final int addr;
    boolean writable;

    AccLis331Register(boolean writable, int addr) {
        this.writable = writable;
        this.addr = addr;
    }

    public int getAddr() {
        return addr;
    }

    public boolean isWritable() {
        return writable;
    }

    public static AccLis331Register[] getWritable() {
        List<AccLis331Register> registers = new ArrayList<>();
        for (AccLis331Register r: values())
            if (r.isWritable())
                registers.add(r);
        return registers.toArray(new AccLis331Register[registers.size()]);
    }

    @Override
    public String toString() {
        return name() + " (" + Integer.toHexString(getAddr()) + ")";
    }
}
