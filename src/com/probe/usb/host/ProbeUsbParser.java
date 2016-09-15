package com.probe.usb.host;

import java.lang.StringBuilder;
import java.util.ArrayList;

public class ProbeUsbParser
{
    static protected final boolean inArray(final int val, final int[] array) {
        for (int item: array)
            if (item == val)  return true;
        return false;
    }
    
    static protected final double 
        timeScale = 1.0,  // TODO: specify the constant
        accScale  = 1365.333333333;
  
    static protected final int 
        frameST    = 0b0000,
        frameSA    = 0b0001,
        frameT1    = 0b0010,
        frameT2    = 0b0011,
        frameAX1   = 0b0100,
        frameAX2   = 0b0101,
        frameAY1   = 0b0110,
        frameAY2   = 0b0111,
        frameAZ1   = 0b1000,
        frameAZ2   = 0b1001,
        frameMSG1  = 0b1100,
        frameMSG2  = 0b1101,
        frameMSG3  = 0b1110,
        frameMSG4  = 0b1111;
        
    static protected final int
        unixTimeFirstByte = 0xF6;
    
    static protected final int 
        unixTimeHiSecondByte = 0x00,
        unixTimeLoSecondByte = 0x01;
        
    static protected final int packetsInSyncRequired = 3;
    
    static protected enum PacketStatus {
       FRAME_REJECTED,
       FRAME_ACCEPTED,
       PACKET_ASSEMBLED
    };
        
    protected class FramePacket {
        protected final int[] typesSequence;
        protected int packetData[];
        protected int frameIdxInPacket = 0;

        public int length() {
            return typesSequence == null? 0: typesSequence.length;
        }

        public int getFrameIdxInPacket() { return  frameIdxInPacket; }
        public int getPacketByte(final int index) { return packetData[index]; }
        
        public FramePacket(final int[] typesSequence) {
            this.typesSequence = typesSequence;
            expectNew();
        }
        
        public void expectNew() {
            onExpectNewPacket(this);
            frameIdxInPacket = 0;
            packetData = new int[ typesSequence.length ];
        }
        
        PacketStatus addFrame(final int frameType, final int dataByte, final boolean frameCounterMatch) {
            PacketStatus result = PacketStatus.FRAME_REJECTED;
            if (frameIdxInPacket >= typesSequence.length)
                expectNew();

            final int expectedFrameType = typesSequence[frameIdxInPacket];
            if (frameType == expectedFrameType && (frameCounterMatch || frameIdxInPacket == 0)) {
                packetData[frameIdxInPacket] = dataByte;
                frameIdxInPacket ++;
                onPacketDataByte(this);
                result = PacketStatus.FRAME_ACCEPTED;
            }
            else 
                expectNew();
            return (frameIdxInPacket >= typesSequence.length)? PacketStatus.PACKET_ASSEMBLED : result;
        }
        
        final int[] getData() {
            return packetData;
        }
    }
        
    static protected int[] dataFramePacketTypes = {frameT1, frameT2, frameAX1, frameAX2, frameAY1, frameAY2, frameAZ1, frameAZ2};
    static protected int[] unixTimePacketTypes = {frameMSG1, frameMSG2, frameMSG3, frameMSG4};
    
    protected FramePacket 
        dataFramePacket      = new FramePacket(dataFramePacketTypes),
        unixTimeFramePacket  = new FramePacket(unixTimePacketTypes);
        
    protected long evenSyncCount = 0;
    protected long oddSyncCount = 0;
    protected int evenCounter = -1;
    protected int oddCounter = -1;
        
    private int expectedFrameCounter = 0;
    private int[] unixTimeBytes = {0, 0, 0, 0};
    
    private int unixTime = 0;
    
    protected ArrayList<Integer> bytes = new ArrayList<Integer>();
    
    static private int[] toArray(ArrayList<Integer> li) {
        int[] ints = new int[li.size()];
        int i = 0;
        for (Integer n : li)
            ints[i++] = n;
        return ints;    
    }
    
    public int getUnixTime() { return unixTime; }
    
    static private int extractCounter(final int b) { return b & 0x0F; }
    
    static private class SyncDetector
    {
        int offset = 0, size = 2;
        int byteCount = 0;
        int expectedCounter = -1;
        int packetsInSync = 0;
        
        SyncDetector(final int offset, final int size) {
            this.offset = offset;
            this.size = size;  
        }
        int feedByte(final int b) {
            final int counter = extractCounter(b);
            if ((byteCount + offset) % size == 0) {
                packetsInSync =  (expectedCounter == counter)? (packetsInSync + 1) : 0;
                expectedCounter = (counter + 1) & 0x0F;
            }
            byteCount ++;            
            return packetsInSync;
        }
        boolean gotLastPacketByte() { return (byteCount + offset) % size == 0; }
    }
    
    private SyncDetector
        evenDetector = new SyncDetector(0, 2),
        oddDetector  = new SyncDetector(1, 2);
    
    public String addByte(final int b) {
        String outputString = "";
        onNewByte(b);        
        
        final int 
            evenPacketsInSync = evenDetector.feedByte(b),
            oddPacketsInSync  = oddDetector.feedByte(b);

        final int nPacketsInSync = Math.max(evenPacketsInSync, oddPacketsInSync);
        final boolean gotLastByte = (nPacketsInSync == evenPacketsInSync)?
                evenDetector.gotLastPacketByte() : oddDetector.gotLastPacketByte();

        bytes.add(b);
            
        if (nPacketsInSync >= packetsInSyncRequired) {
            final int nBytesInSync = 2 * nPacketsInSync - (gotLastByte? 0 : 1);
            onSync(toArray(bytes), nBytesInSync);
            final int idxEndBytesToRemove = Math.max(0, bytes.size() - nBytesInSync);
            bytes.subList(0, idxEndBytesToRemove).clear();
            while (bytes.size() >= 2) {
                outputString = addFrame(bytes.get(0), bytes.get(1));
                bytes.remove(0);
                bytes.remove(0);
            }
        }
        return outputString;
    }
    
    public String addFrame(final int firstByte, final int dataByte)
    {
        onNewFrame(firstByte, dataByte);
        final int frameType = (firstByte >>> 4) & 0x0F;
        final int frameCounter = firstByte & 0x0F;
        
        String outputString = "";
        
        final PacketStatus 
            dataFrameStatus = dataFramePacket.addFrame(frameType, dataByte, frameCounter == expectedFrameCounter),
            timeFrameStatus = unixTimeFramePacket.addFrame(frameType, dataByte, frameCounter == expectedFrameCounter);
            
        if (dataFrameStatus == PacketStatus.PACKET_ASSEMBLED)
            outputString = processDataPacket(dataFramePacket.getData());
        else if (timeFrameStatus == PacketStatus.PACKET_ASSEMBLED)
            unixTime = processUnixTimePacket(unixTimeFramePacket.getData());
        else if (dataFrameStatus == PacketStatus.FRAME_REJECTED && timeFrameStatus == PacketStatus.FRAME_REJECTED)
            outputString = processSingleFrame(firstByte, dataByte);   

        expectedFrameCounter = (frameCounter + 1) & 0x0F;
        return outputString;
    }
    
    static private int decode2sComplement(final int hiByte, final int loByte) {
        return (short)((hiByte << 8) | loByte);
    }
    
    private String processDataPacket(final int[] packetData)
    {
        onNewDataPacket(packetData);
        final double 
            t  = (packetData[0] * 0xFF + packetData[1]) / timeScale,
            ax = decode2sComplement(packetData[2], packetData[3]) / accScale,
            ay = decode2sComplement(packetData[4], packetData[5]) / accScale,
            az = decode2sComplement(packetData[6], packetData[7]) / accScale;
         StringBuilder sb = new StringBuilder();
         sb.append(t).append(" ").append(ax).append(" ").append(ay).append(" ").append(az);
         return sb.toString();
    }
    
    private int processUnixTimePacket(final int[] packetData)
    {
        onNewTimePacket(packetData);
        
        if (packetData[0] == unixTimeFirstByte && packetData[1] == unixTimeHiSecondByte) {
            unixTimeBytes[0] = packetData[2];
            unixTimeBytes[1] = packetData[3];
            unixTimeBytes[2] = 0;
            unixTimeBytes[3] = 0;
            return 0;
        }
      
        if (packetData[0] == unixTimeFirstByte && packetData[1] == unixTimeLoSecondByte) {
            unixTimeBytes[2] = packetData[2];
            unixTimeBytes[3] = packetData[3];
            return (unixTimeBytes[0] << 24) | (unixTimeBytes[1] << 16) | (unixTimeBytes[2] << 8) | (unixTimeBytes[3] << 0);
        }
        return 0;
    }
    
    static private boolean intInArray(final int x, final int[] arr) {
        for(int t: arr)
            if (t == x) return true;
        return false;
    }
    
    private String processSingleFrame(final int b1, final int b2) {
        onNewSingleFrame(b1, b2);
        return intInArray(b1 >> 4, dataFramePacketTypes)? 
                "" : 
                "-1 -1 " + Integer.toString(b1) + " " + Integer.toString(b2);
    }
    
    protected void onNewByte(final int b) {}
    protected void onSync(final int[] bytes, final int nBytesInSync) {}
    protected void onNewFrame(final int b1, final int b2) {}
    protected void onNewDataPacket(final int[] packetData) {}
    protected void onNewTimePacket(final int[] packetData) {}
    protected void onNewSingleFrame(final int b1, final int b2) {}
    protected void onExpectNewPacket(final FramePacket p) {}
    protected void onPacketDataByte(final FramePacket p) {}
}