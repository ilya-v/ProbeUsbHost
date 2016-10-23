package com.probe.usb.host.pc;

import com.probe.usb.host.parser.ParserEventListener;

class StatusListener extends ParserEventListener {

    private Listener listener;

    private boolean gotFrames = false;
    private boolean gotBytes = false;
    private ConnectionStatus status = ConnectionStatus.Disconnected;


    public StatusListener(Listener listener) {
        this.listener = listener;
    }

    private void updateStatus(ConnectionStatus newStatus) {
        if (newStatus != status)
            listener.onStatusChange(newStatus);
        status = newStatus;
    }

    public void onDisconnected () {
        updateStatus(ConnectionStatus.Disconnected);
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

    enum ConnectionStatus {
        Disconnected,
        Connected,
        Reading,
        Data,
        Idle
    }

    interface Listener {
        void onStatusChange(ConnectionStatus connectionStatus);
    }
}
