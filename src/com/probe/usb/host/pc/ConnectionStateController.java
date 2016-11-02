package com.probe.usb.host.pc;

import com.google.common.eventbus.Subscribe;
import com.probe.usb.host.bus.Receiver;
import com.probe.usb.host.pc.controller.ConnectionStatus;
import com.probe.usb.host.pc.controller.event.ComPortConnectionEvent;
import com.probe.usb.host.pc.controller.event.ComPortDataEvent;
import com.probe.usb.host.pc.controller.event.ParserFrameEvent;
import com.probe.usb.host.pc.controller.event.TickEvent;

public class ConnectionStateController extends Receiver {

    private boolean gotFrames = false;
    private boolean gotBytes = false;
    private ConnectionStatus status = ConnectionStatus.Disconnected;

    @Subscribe
    public void onComPortConnectionEvent(ComPortConnectionEvent connectionEvent) {
        updateStatus(connectionEvent.getConnectionStatus()? ConnectionStatus.Connected : ConnectionStatus.Disconnected);
    }

    @Subscribe
    public void onTimerTick(TickEvent event) {
        if ( (status == ConnectionStatus.Reading || status == ConnectionStatus.Data) && !gotBytes)
            updateStatus(ConnectionStatus.Idle);
        else if (!gotFrames && gotBytes)
            updateStatus(ConnectionStatus.Reading);
        gotFrames = false;
        gotBytes = false;
    }

    @Subscribe
    public void onNewByte(ComPortDataEvent comPortDataEvent) {
        if (status != ConnectionStatus.Data)
            updateStatus(ConnectionStatus.Reading);
        gotBytes = true;
    }

    @Subscribe
    public void onNewFrame(ParserFrameEvent parserFrameEvent) {
        updateStatus(ConnectionStatus.Data);
        gotFrames = true;
    }

    private void updateStatus(ConnectionStatus newStatus) {
        if (newStatus != status)
            postEvent(newStatus);
        status = newStatus;
    }
}
