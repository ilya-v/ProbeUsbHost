package com.probe.usb.host.pc;

import com.probe.usb.host.parser.ParserEventListener;

import java.util.*;

public class PortScanner extends ParserEventListener {

    interface EnabledInfo {
        boolean isEnabled();
    }

    interface Logger {
        void pintLine(String line);
    }

    interface StatusListener {
        void onConnectionStatusChanged(boolean connected, String portName);
    }

    public enum State {
        zero            (false),
        havePorts       (false),
        checkingPort    (true),
        haveData        (true),;

        private boolean connected;
        State(boolean connected) {this.connected = connected; }
        public boolean isConnected() {return connected;}
    }

    final static long delayMsec = 1000;

    private Logger logger;
    private EnabledInfo enabledInfo;
    private Communicator communicator;
    private StatusListener listener;

    private State state = State.zero;

    private Deque<String> ports = new ArrayDeque<>();
    private Date lastFrameTime = new Date(0L);


    public PortScanner(Communicator communicator) {
        this.communicator = communicator;
    }
    public PortScanner setEnabledInfo(EnabledInfo enabledInfo) {
        this.enabledInfo = enabledInfo;
        return this;
    }
    public PortScanner setLogger(Logger logger) {
        this.logger = logger;
        return this;
    }
    public PortScanner setStatusListener(StatusListener listener) {
        this.listener = listener;
        return this;
    }

    private void updateState(State newState) {
        if (state != newState) {
            if (logger != null && (state == State.haveData || newState == State.haveData))
                logger.pintLine(getClass().getSimpleName() + ": " + state + " --> " + newState);
            if (!newState.isConnected())
                communicator.disconnect();
            if (state.isConnected() != newState.isConnected() && listener != null)
                listener.onConnectionStatusChanged(newState.isConnected(), communicator.getPortName());
        }
        state = newState;
    }

    @Override
    public void onNewFrame(final int b1, final int b2) {
        lastFrameTime = new Date();
    }

    public void onTimerTick(List<String> portNames) {
        final boolean isEnabled = enabledInfo != null && enabledInfo.isEnabled();
        if (state == State.zero && !isEnabled)
            return;
        final boolean gotFrames = (lastFrameTime.getTime() + delayMsec > (new Date().getTime()));
        if (state == State.zero) {
            ports.addAll(portNames);
            if (!ports.isEmpty()) {
                updateState(State.havePorts);
                onTimerTick(Collections.emptyList());
            }
        } else if (state == State.havePorts) {
            if (ports.isEmpty() || !isEnabled)
                updateState(State.zero);
            else if (communicator.connect(ports.pollFirst()))
                updateState(State.checkingPort);
            else
                onTimerTick(Collections.emptyList());
        } else if (state == State.checkingPort)
            updateState( !isEnabled? State.zero :       gotFrames? State.haveData :         State.havePorts);
        else if (state == State.haveData)
            updateState( !isEnabled? State.zero :       gotFrames? State.haveData :         State.checkingPort);
    }

}
