package com.probe.usb.host.parser.internal;

import java.util.ArrayList;
import java.util.List;

public class FrameDetector {
    protected int
            frameSize = 2,
            minFramesInSync = 3;

    protected List<Integer> bytes = new ArrayList<>();
    protected int nFramesInSync = 0;
    protected int nextCounter = -1;
    protected int nHistoricSync = 0;


    public FrameDetector setMinFramesInSync(final int minFramesInSync) {
        this.minFramesInSync = minFramesInSync;
        return this;
    }

    public FrameDetector setFrameSize(final int frameSize) {
        this.frameSize = frameSize;
        return this;
    }

    public void resetFrameCounter() {
        nextCounter = 0;
    }

    public void feedByte(final int b) {
        bytes.add(b);
        if (bytes.size() % frameSize != 0)
            return;

        final int newCounter = DataFormat.extractCounter( bytes.get(bytes.size() - frameSize) );
        final boolean
                inSync = (nextCounter >= 0) &&  (nextCounter == newCounter),
                haveGrayFrames = nFramesInSync + 1 <  bytes.size() / frameSize;
        if (!inSync)
            bytes.subList(nFramesInSync * frameSize, bytes.size() - frameSize).clear();
        if (inSync && !haveGrayFrames)
            nFramesInSync ++;

        nHistoricSync = (inSync? nHistoricSync + 1 : 0);

        nextCounter = DataFormat.nextCounter(newCounter);

        final int nGrayFrames = bytes.size() / frameSize - nFramesInSync;
        if (nGrayFrames >= minFramesInSync)
            nFramesInSync += nGrayFrames;
    }

    public int getSyncFramesCount() {
        return nFramesInSync;
    }

    public int getHistoricSyncCount() {
        return nHistoricSync;
    }

    public int getTotalFramesCount() {
        return bytes.size()/frameSize;
    }

    public Frame popFrame() {
        if (bytes.size() < frameSize)
            throw new RuntimeException("Cannot pop a frame: no data");
        final Frame frame = new Frame(bytes.subList(0,frameSize));
        bytes.subList(0, frameSize).clear();
        if (nFramesInSync > 0)
            nFramesInSync --;
        return frame;
    }
}
