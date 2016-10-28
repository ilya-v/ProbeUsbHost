package com.probe.usb.host.pc.controller;

import com.probe.usb.host.parser.ParserEventListener;
import com.probe.usb.host.pc.Communicator;

import java.util.*;

public class PortScanner extends ParserEventListener {

    public interface EnabledInfo {
        boolean isEnabled();
    }

    public interface Logger {
        void pintLine(String line);
    }

    public interface StatusListener {
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

    private Deque<String> portsToScan = new ArrayDeque<>();
    private List<String> portNames = new ArrayList<>();
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

    public void onPortsUpdate(List<String> portNames) {
        this.portNames = portNames;
    }

    public void onTimerTick() {
        final boolean isEnabled = enabledInfo != null && enabledInfo.isEnabled();
        if (state == State.zero && !isEnabled)
            return;
        final boolean gotFrames = (lastFrameTime.getTime() + delayMsec > (new Date().getTime()));
        if (state == State.zero) {
            if (!portNames.isEmpty()) {
                portsToScan = new ArrayDeque<>(portNames);
                updateState(State.havePorts);
                onTimerTick();
            }
        } else if (state == State.havePorts) {
            if (portsToScan.isEmpty() || !isEnabled)
                updateState(State.zero);
            else if (communicator.connect(portsToScan.pollFirst()))
                updateState(State.checkingPort);
            else
                onTimerTick();
        } else if (state == State.checkingPort)
            updateState( !isEnabled? State.zero :       gotFrames? State.haveData :         State.havePorts);
        else if (state == State.haveData)
            updateState( !isEnabled? State.zero :       gotFrames? State.haveData :         State.checkingPort);
    }
}
