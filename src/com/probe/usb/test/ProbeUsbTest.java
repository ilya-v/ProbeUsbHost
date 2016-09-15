package com.probe.usb.test;

import com.probe.usb.host.ProbeUsbParser;


class ProbeUsbTest {
    void test() {
        //test_parser_0();
        //test_parser_1();    
        //test_parser_2();
        test_parser_3();
    }
    
    static private class Parser extends ProbeUsbParser {
        
        static String bin(final int b) {
            StringBuilder sb = new StringBuilder("0b");
            for (int i = 7; i >= 0; i--)
                sb.append( ((1<<i) & b & 0xFF) != 0? "1" : "0");
            return sb.toString();
        }
        
        static String hbin(final int b) {
            final String s = bin(b);
            return s.substring(0, 6) + "[" + s.substring(6) + "]";
        }
        
        String result = "";
        String getResult() { final String r = result; result = ""; return r; }
      
        protected void onNewByte(final int b)                      { result += "B " + bin(b) + "\n"; }
        protected void onSync(final int[] bytes, final int nBytesInSync) { result += "Sync: " + bytes.length + " bytes, " + nBytesInSync + " total\n"; }
        protected void onNewFrame(final int b1, final int b2)      {
            result += "F " + bin(b1) + " " + bin(b2) + ":  ";


            int index = 0;
            for (int b: bytes) {
                result += (index % 2 == 0 ? hbin(b) : bin(b)) + " ";
                index ++;
            }
            result += "\n";
        }
        protected void onNewDataPacket(final int[] d)              { result += "D " + bin(d[0]) + bin(d[1]) + bin(d[2]) + bin(d[3]) + "\n"; }
        protected void onNewTimePacket(final int[] d)              { result += "T " + bin(d[0]) + bin(d[1]) + bin(d[2]) + bin(d[3]) + "\n"; }
        protected void onNewSingleFrame(final int b1, final int b2){ result += "S " + bin(b1) + " " + bin(b2) + "\n"; }
        protected void onDropByte(final int b)                     { result += "- " + bin(b) + "\n"; }
        protected void onExpectNewPacket(final FramePacket p)      { result += "==NewPacket==" + p.length() + "\n";}
        protected void onPacketDataByte(final FramePacket p)       { 
            result += "== FB [" + p.length() + "] ";
            for (int i = 0; i < p.getFrameIdxInPacket(); i++)
                result += bin(p.getPacketByte(i)) + " ";
            result += "\n";
        }
    };
    
    private Parser p = new Parser();
  
    private String parse(final int[] bytes) {
        p.result = "";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length/2; i++)
            sb.append(p.addFrame(bytes[2*i], bytes[2*i + 1])).append("\n");
        return sb.toString();
    }
    
    private String parseBytes(final int[] bytes) {
        p.result = "";
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (int b: bytes) {
          sb.append("#").append(i).append(": ").append(p.addByte(b)).append("\n");
          i++;
        }
        return sb.toString();
    }
  
    private void test_parser_0() {
         int bytes[] = {
            0b00000000, 0b00000000, // garbage
            0b11111111, 0b00000000, // garbage
            0b00001111, 0b00000000, // garbage
            0b00011111, 0b00000000, // garbage
            0b00100000, 0b00000000, // T1
            0b00110001, 0b00000001, // T2
            0b01000010, 0b10000000, // AX1
            0b01010011, 0b00000000, // AX2
            0b01100100, 0b01111111, // AY1
            0b01110111, 0b00000000, // AY2, wrong counter
            0b10000110, 0b01111111, // AZ1
            0b10010111, 0b11111111, // AZ2
            0b00000000, 0b00000000, // garbage
            0b11111111, 0b00000000, // garbage
            0b00001111, 0b00000000, // garbage
            0b00011111, 0b00000000, // garbage  
        };
        
        String s = parse(bytes);
        assert !s.contains("1.0 ");
        
        int nGarbageLines = s.length() - s.replace("-1 -1 ", "-1-1 ").length();
        assert nGarbageLines == 8;
    }
  
    private void test_parser_1() {    
        int bytes[] = {
            0b00000000, 0b00000000, // garbage
            0b11111111, 0b00000000, // garbage
            0b00001111, 0b00000000, // garbage
            0b00011111, 0b00000000, // garbage
            0b00100000, 0b00000000, // T1
            0b00110001, 0b00000001, // T2
            0b01000010, 0b10000000, // AX1
            0b01010011, 0b00000000, // AX2
            0b01100100, 0b01111111, // AY1
            0b01110101, 0b00000000, // AY2
            0b10000110, 0b01111111, // AZ1
            0b10010111, 0b11111111, // AZ2
            0b00000000, 0b00000000, // garbage
            0b11111111, 0b00000000, // garbage
            0b00001111, 0b00000000, // garbage
            0b00011111, 0b00000000, // garbage  
        };
    
        String s = parse(bytes);
        assert s.contains("1.0 -24.000000000005862 23.812500000005816 23.999267578130862");
        
        int nGarbageLines = s.length() - s.replace("-1 -1 ", "-1-1 ").length();
        assert nGarbageLines == 8;
    }
    
     private void test_parser_2() {    
        int bytes[] = {
            0b00000000, 0b00000000, // garbage
            0b11111111, 0b00000000, // garbage
            0b00001111, 0b00000000, // garbage
            0b00011111, 0b00000000, // garbage
            0b11000000, 0xF6,       // MSG1: Unix Time First Byte
            0b11010001, 0b00000000, // MSG2: Hi Byte indicator
            0b11100010, 0b00000000, // MSG3: Unix Time byte Hi(3)
            0b11110011, 0b00000001, // MSG4: Unix Time byte (2)
            0b00000000, 0b00000000, // garbage
            0b11111111, 0b00000000, // garbage
            0b00001111, 0b00000000, // garbage
            0b00011111, 0b00000000, // garbage              
            0b11000000, 0xF6,       // MSG1: Unix Time First Byte
            0b11010001, 0b00000001, // MSG2: Lo Byte indicator
            0b11100010, 0b00000000, // MSG3: Unix Time byte (1)
            0b11110011, 0b00000010, // MSG4: Unix Time byte (0)
            0b00000000, 0b00000000, // garbage
            0b11111111, 0b00000000, // garbage
            0b00001111, 0b00000000, // garbage
            0b00011111, 0b00000000, // garbage  
        };
        
        String s = parse(bytes);
        assert p.getUnixTime() == 65538;
    }
    
    private void test_parser_3() {
         int bytes[] = {
            0b00000000, 0b00000000, // garbage
            0b11111111, 0b00000000, // garbage
            0b00001110, 0b00000001, // garbage, but with right counter value
            0b00011111, 0b00000010, // garbage, but with right counter value
            0b00100000, 0b00000000, // T1, counter continued
            0b00110001, 0b00000001, // T2
            0b01000010, 0b10000000, // AX1
            0b01010011, 0b00000000, // AX2
            0b01100100, 0b01111111, // AY1
            0b01110101, 0b00000000, // AY2
            0b10000110, 0b01111111, // AZ1
            0b10010111, 0b11111111, // AZ2
            0b00000000, 0b00000000, // garbage
            0b11111111, 0b00000000, // garbage
            0b00001111, 0b00000000, // garbage
            0b00011110, 0b00000000, // garbage  
            0b00100000, 0b00000000, // T1, counter reset
            0b00110001, 0b00000001, // T2
            0b01000010, 0b10000000, // AX1
            0b01010011, 0b00000000, // AX2
            0b01100100, 0b01111111, // AY1
            0b01110101, 0b00000000, // AY2
            0b10000110, 0b01111111, // AZ1
            0b10010111, 0b11111111, // AZ2
            0b00000000, 0b00000000, // garbage
            0b11111111, 0b00000000, // garbage
            0b00001111, 0b00000000, // garbage
            0b00011110, 0b00000000, // garbage
        };
               
        String s ="";
        try {
            s = parseBytes(bytes);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(p.getResult());            
        }
        System.out.println(p.getResult());
        
        int nGarbageLines = s.length() - s.replace("-1 -1 ", "-1-1 ").length();
        //assert nGarbageLines == 8;
    }
}