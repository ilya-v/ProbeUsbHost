package com.probe.usb.host.commander;

import com.probe.usb.host.common.ConfigParamType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.probe.usb.host.common.ConfigCommand.*;


public class ProbeUsbCommander
{
    protected List<Short> bytes = new ArrayList<>();
    
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

        
    protected void bytesToDevice(final short b1, final short b2, final short b3, final short b4) {
        for (short b: new short[] {b1, b2, b3, b4})
            this.bytes.add(b); 
    }
        
    public void deviceCommand(short firstByte, final ConfigParamType paramType, final Object value) {
        final int intValue = encodeValue(value);
        bytesToDevice(firstByte, (short)paramType.index, (short)(intValue / 256), (short)(intValue % 256));
    }
    
    public void deviceWriteConfig(final ConfigParamType paramType, final Object value) {
        deviceCommand(writeConfig.getFirstByte(), paramType, value);
    }
    
    public void deviceReadConfig(final ConfigParamType paramType) {
        deviceCommand(readConfig.getFirstByte(), paramType, 0);
    }
    
    public void deviceAccRegWrite(final int regAddress, final int regValue) {
        bytesToDevice(accRegWrite.getFirstByte(), (short)regAddress, (short)regValue, (short)0);
    }
    
    public void deviceAccRegRead(final int regAddress) {
        bytesToDevice(accRegRead.getFirstByte(), (short)regAddress, (short)0, (short)0);
    }

    public void deviceSetTime(final int unixTime) {
        bytesToDevice(setTimeHi.getFirstByte(), (short)0, nthByte(unixTime, 3), nthByte(unixTime, 2));
        bytesToDevice(setTimeLo.getFirstByte(), (short)0, nthByte(unixTime, 1), nthByte(unixTime, 0));
    }
    
    public void deviceSetTime(Date datetime) {
        deviceSetTime((int)(datetime.getTime() / 1000));
    }

    public void deviceStartRecording() {
        bytesToDevice(startRecording.getFirstByte(), (short)0, (short)0, (short)0);
    }

    public void deviceStopRecording() {
        bytesToDevice(stopRecording.getFirstByte(), (short)0, (short)0, (short)0);
    }
}