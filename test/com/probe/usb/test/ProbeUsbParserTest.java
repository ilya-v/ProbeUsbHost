package com.probe.usb.test;

import com.probe.usb.host.parser.ProbeUsbParser;
import com.probe.usb.host.parser.processor.TablePrintProcessor;
import com.probe.usb.host.parser.internal.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ProbeUsbParserTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testCapture()  throws IOException {

        final byte[]
                capture = getCapture(),
                segment = Arrays.copyOfRange(capture, 1, capture.length);

        FrameDetector fd = new FrameDetector();
        List<Frame> frames = getFrames(fd, segment);
        int counter = -1;

        for (Frame frame: frames) {
            final int curCounter = DataFormat.extractCounter(frame.b1);
            final boolean countersMatch = counter < 0 || curCounter == DataFormat.nextCounter(counter);
            assertTrue(countersMatch);
            counter = curCounter;
        }

        assertEquals(fd.getTotalFramesCount(), 0);
    }

    @Test
    public void testFrameSequence() throws IOException {
        final byte[]
                capture = getCapture(),
                segment = Arrays.copyOfRange(capture, 1, capture.length);

        FrameDetector fd = new FrameDetector();
        List<Frame> frames = getFrames(fd, segment);
        for (Frame frame: frames)
            System.out.println(frame.getType());
    }

    @Test
    public void testRobustFrameDetector2() throws IOException {
        final byte[]  capture = getCapture();

        byte[] segment = new byte[capture.length*2];
        int i = 0;
        final int N = 111;
        for (byte b : capture) {
            if (i % N == 0)
                segment[i++] = 0;
            segment[i++] = b;
        }


        RobustFrameDetector fd = new RobustFrameDetector();

        List<Frame> frames = new ArrayList<>();

        for (byte b: segment) {
            fd.feedByte( Byte.toUnsignedInt(b) );
            while (fd.getSyncFramesCount() > 0)
                frames.add(fd.popFrame());
        }

        for (Frame frame: frames)
            System.out.println(frame.getType());
    }

    @Test
    public void testRobustFrameDetector() throws IOException {
        final byte[]
                segment1 = getCapture(),
                segment2 = Arrays.copyOfRange(segment1, 1, segment1.length);

        RobustFrameDetector fd1 = new RobustFrameDetector();
        FrameDetector       fd2 = new FrameDetector();

        List<Frame>
                frames1 = new ArrayList<>(),
                frames2 = new ArrayList<>();

        for (byte b: segment1) {
            fd1.feedByte( Byte.toUnsignedInt(b) );
            while (fd1.getSyncFramesCount() > 0)
                frames1.add(fd1.popFrame());
        }
        while (fd1.getTotalFramesCount() > 0)
            frames1.add(fd1.popFrame());

        for (byte b: segment2) {
            fd2.feedByte( Byte.toUnsignedInt(b) );
            while (fd2.getSyncFramesCount() > 0)
                frames2.add(fd2.popFrame());
        }
        while (fd2.getTotalFramesCount() > 0)
            frames2.add(fd2.popFrame());

        for (int i = 0; i < frames1.size() && i < frames2.size(); i++)
            assertEquals(frames1.get(i), frames2.get(i));

        assertEquals( frames1.size(), frames2.size());
    }

    protected byte[] getCapture() throws IOException {
        final String capturePath = "/cap18.bin";
        InputStream input = this.getClass().getResourceAsStream(capturePath);
        final int nBytes = this.getClass().getResource(capturePath).getFile().length();
        byte[] bytes = new byte[nBytes];

        for (int i = 0, b = 0; (b = input.read()) != -1; i++)
            bytes[i] = (byte)b;

        return bytes;
    }

    protected List<Frame> getFrames(FrameDetector fd, byte[] segment) {
        List<Frame> frames = new ArrayList<>();

        for (byte b: segment) {
            fd.feedByte( Byte.toUnsignedInt(b) );
            while (fd.getSyncFramesCount() > 0)
                frames.add(fd.popFrame());
        }
        return frames;
    }

    @Test
    public void testProbeUsbParser1() throws IOException {
        final byte[] capture = getCapture();

        String result1 = "";
        {
            TablePrintProcessor packetProcessor = new TablePrintProcessor();
            ProbeUsbParser parser = new ProbeUsbParser().addPacketProcessor(packetProcessor);
            for (byte b : capture) {
                parser.addByte(Byte.toUnsignedInt(b));
                final String parsed = packetProcessor.popResult();
                result1 += parsed;
                System.out.print(parsed);
            }
        }

        String result2 = "";
        {
            TablePrintProcessor packetProcessor = new TablePrintProcessor();
            ProbeUsbParser parser = new ProbeUsbParser().addPacketProcessor(packetProcessor);
            for (byte b : capture) {
                parser.addByte(Byte.toUnsignedInt(b));
            }
            result2 = packetProcessor.popResult();
        }

        assertEquals(result1, result2);
    }


}