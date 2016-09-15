package com.probe.usb.host;

import java.util.List;
import java.util.Date;
import java.util.ArrayList;


class ProbeUsbCommander
{
    protected List<Short> bytes = new ArrayList<Short>(); 
    
    public int popOutputByte() {
      if (bytes.isEmpty())  return -1;
      final int result = bytes.get(0);
      bytes.remove(0);
      return result;
    }
    
    static protected int encodeValue(Object value) {
        if (value instanceof Boolean)
          return (boolean)value? 1 : 0;
        return Integer.parseInt(value.toString());
    }
    
    static protected short nthByte(final long val, final int idx) {
        return (short)( (val >>> (idx * 8)) & 0xFF );
    }
   
    static final short
        writeConfigFirstByte      = 0xF1,
        readConfigFirstByte       = 0xF2,
        accRegWriteFirstByte      = 0xF3,
        accRegReadFirstByte       = 0xF4,
        setTimeHiFirstByte        = 0xF5,
        setTimeLoFirstByte        = 0xF6;
        
    protected void bytesToDevice(final short b1, final short b2, final short b3, final short b4) {
        for (short b: new short[] {b1, b2, b3, b4})
            this.bytes.add(b); 
    }
        
    protected void deviceCommand(short firstByte, final ConfigParamType paramType, final Object value) {
        final int intValue = encodeValue(value);
        bytesToDevice(firstByte, (short)paramType.index, (short)(intValue / 256), (short)(intValue % 256));
    }
    
    protected void deviceWriteConfig(final ConfigParamType paramType, final Object value) {
        deviceCommand(writeConfigFirstByte, paramType, value);
    }
    
    protected void deviceReadConfig(final ConfigParamType paramType) {
        deviceCommand(readConfigFirstByte, paramType, 0);
    }
    
    protected void deviceAccRegWrite(final int regAddress, final int regValue) {
        bytesToDevice(accRegWriteFirstByte, (short)regAddress, (short)0, (short)regValue );
    }
    
    protected void deviceAccRegRead(final int regAddress) {
        bytesToDevice(accRegReadFirstByte, (short)regAddress, (short)0, (short)0);
    }
    
    protected void deviceSetTime(Date datetime) {
        final long unixTime = datetime.getTime() / 1000;
        bytesToDevice(setTimeHiFirstByte, (short)0, nthByte(unixTime, 3), nthByte(unixTime, 2));
        bytesToDevice(setTimeLoFirstByte, (short)0, nthByte(unixTime, 1), nthByte(unixTime, 0));
    }
}