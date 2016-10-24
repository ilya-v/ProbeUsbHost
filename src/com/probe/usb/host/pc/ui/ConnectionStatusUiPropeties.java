package com.probe.usb.host.pc.ui;

import com.probe.usb.host.pc.controller.ConnectionStatus;

import java.awt.*;


public enum ConnectionStatusUiPropeties {

    Disconnected(ConnectionStatus.Disconnected, Color.red, "DISCONNECTED"),
    Connected(ConnectionStatus.Connected, Color.black, "PORT OPEN"),
    Reading(ConnectionStatus.Reading, new Color(0.4f, 0.7f, 0.6f), "READING INPUT..."),
    Data(ConnectionStatus.Data, new Color(0.4f, 0.7f, 0.6f), "RECEIVING DATA"),
    Idle(ConnectionStatus.Idle, Color.black, "NO MORE DATA"),
    ;

    final private ConnectionStatus status;
    final private Color color;
    final private String text;

    ConnectionStatusUiPropeties(ConnectionStatus status, Color color, String text) {
        this.status = status;
        this.color = color;
        this.text = text;
    }

    public static Color getColor(ConnectionStatus status) {
        for (ConnectionStatusUiPropeties p: values())
            if (status == p.status)
                return p.color;
        return Color.black;
    }

    public static String getText(ConnectionStatus status) {
        for (ConnectionStatusUiPropeties p: values())
            if (status == p.status)
                return p.text;
        return "";
    }
}
