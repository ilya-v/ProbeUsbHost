package com.probe.usb.host.pc;

import com.probe.usb.host.parser.ParserEventListener;
import com.probe.usb.host.pc.controller.ConnectionStatus;

public class StatusListener extends ParserEventListener {

    private ConnectionStatusListener connectionStatusListener;

    private boolean gotFrames = false;
    private boolean gotBytes = false;
    private ConnectionStatus status = ConnectionStatus.Disconnected;


    public StatusListener setConnectionStatusListener(ConnectionStatusListener connectionStatusListener) {
        this.connectionStatusListener = connectionStatusListener;
        return this;
    }

    private void updateStatus(ConnectionStatus newStatus) {
        if (newStatus != status)
            connectionStatusListener.onStatusChange(newStatus);
        status = newStatus;
    }

    public void onConnectionStatus(boolean connected) {
        updateStatus(connected? ConnectionStatus.Connected : ConnectionStatus.Disconnected);
    }

    public void onTimerTick() {
        if ( (status == ConnectionStatus.Reading || status == ConnectionStatus.Data) && !gotBytes)
            updateStatus(ConnectionStatus.Idle);
        else if (!gotFrames && gotBytes)
            updateStatus(ConnectionStatus.Reading);
        gotFrames = false;
        gotBytes = false;
    }

    public void onNewByte(final int b) {
        if (status != ConnectionStatus.Data)
            updateStatus(ConnectionStatus.Reading);
        gotBytes = true;
    }
    public void onNewFrame(final int b1, final int b2) {
        updateStatus(ConnectionStatus.Data);
        gotFrames = true;
    }

    interface ConnectionStatusListener {
        void onStatusChange(ConnectionStatus connectionStatus);
    }
}
