package com.probe.usb.host.parser.internal;


public class RobustFrameDetector {

    protected int
            frameSize = 2,
            minFramesInSync = 3,
            minDiffSync = 3;

    protected int byteCounter = 0;

    protected FrameDetector[] frameDetectors;
    protected Selector selector;

    protected class Selector {
        private int selected = -1;
        private boolean hasPreference = false;

        boolean hasPreference() { return hasPreference; }
        int getSelected() { return selected; }
        void select() {
            final int
                    maxIdx = getMaxSyncIdx(),
                    maxSyncCount = frameDetectors[maxIdx].getSyncFramesCount(),
                    secondMaxSyncCount = getSecondMaxSyncCount(maxIdx);
            hasPreference =  (secondMaxSyncCount + minDiffSync <= maxSyncCount);
            selected =  hasPreference?  maxIdx      :
                        selected >= 0?  selected    :
                                        getMaxCountIdx();
        }
    };

    public RobustFrameDetector() { init(); }

    protected void init() {
        frameDetectors = new FrameDetector[frameSize];
        for (int i = 0; i < frameDetectors.length; i++)
            frameDetectors[i] = new FrameDetector().setFrameSize(frameSize).setMinFramesInSync(minFramesInSync);
        selector = new Selector();

    }

    public RobustFrameDetector setMinFramesInSync(final int minFramesInSync) {
        this.minFramesInSync = minFramesInSync;
        init();
        return this;
    }

    public RobustFrameDetector setFrameSize(final int frameSize) {
        this.frameSize = frameSize;
        init();
        return this;
    }

    public void resetFrameCounter() {
        frameDetectors[selector.getSelected()].resetFrameCounter();
    }

    public void feedByte(final int b) {
        int idxFrameDetector = 0;
        for (FrameDetector fd: frameDetectors) {
            if (idxFrameDetector <= byteCounter)
                fd.feedByte(b);
            idxFrameDetector ++;
        }
        byteCounter ++;
        selector.select();
    }

    public int getSyncFramesCount(){
        return selector.hasPreference()? frameDetectors[selector.getSelected()].getSyncFramesCount() : 0;
    }

    public int getTotalFramesCount() {
        return frameDetectors[selector.getSelected()].getTotalFramesCount();
    }

    public Frame popFrame() {
        final int popIdx = selector.getSelected();

        Frame result = null;
        int idx = 0;
        for (FrameDetector fd: frameDetectors) {
            if (idx == popIdx)
                result = fd.popFrame();
            else if (fd.getTotalFramesCount() > 0)
                fd.popFrame();
            idx++;
        }
        return result;
    }

    protected int getMaxSyncIdx() {
        int maxSyncCount = 0;
        int maxIdx = 0;

        int idx = 0;
        for (FrameDetector fd : frameDetectors) {
            if (fd.getSyncFramesCount() > maxSyncCount) {
                maxIdx = idx;
                maxSyncCount = fd.getSyncFramesCount();
            }
            idx++;
        }
        return maxIdx;
    }

    protected int getSecondMaxSyncCount(final int maxSyncIdx) {
        int secondMaxSyncCount = 0;
        int idx = 0;
        for (FrameDetector fd : frameDetectors) {
            final int fdSyncCount = fd.getSyncFramesCount();
            if ( fdSyncCount > secondMaxSyncCount && idx != maxSyncIdx ) {
                secondMaxSyncCount = fd.getSyncFramesCount();
            }
            idx++;
        }
        return secondMaxSyncCount;
    }

    protected int getMaxCountIdx() {
        int maxCount = 0;
        int maxDataIdx = 0;

        int idx = 0;
        for (FrameDetector fd : frameDetectors) {
            if (fd.getTotalFramesCount() > maxCount) {
                maxDataIdx = idx;
                maxCount = fd.getTotalFramesCount();
            }
            idx++;
        }
        return maxDataIdx;
    }
}
