package com.probe.usb.host.pc.ui.controller;


import com.probe.usb.host.pc.controller.ConnectionStatus;
import com.probe.usb.host.pc.ui.ConnectionStatusUiPropeties;

import javax.swing.*;
import java.util.*;

public class PortScanUiController {
    private JComboBox<String> cboxPorts;
    private JToggleButton autoButton;
    private JLabel connectionIndicator;

    Set<String> portNames = new HashSet<>();

    public interface Logger { void print(String line);  }
    private Logger logger;
    public PortScanUiController setLogger(Logger logger) {
        this.logger = logger;
        return this;
    }
    public interface ConnectionCommandListener { void setConnection(String port, boolean status); }
    private ConnectionCommandListener connectionCommandListener;
    public PortScanUiController setConnectionCommandListener(ConnectionCommandListener listener) {
        this.connectionCommandListener = listener;
        return this;
    }

    public PortScanUiController(JComboBox<String> cboxPorts, JToggleButton autoButton, JLabel connectionIndicator) {
        this.cboxPorts = cboxPorts;
        this.autoButton = autoButton;
        this.connectionIndicator = connectionIndicator;
        cboxPorts.addActionListener(e -> onPortsCboxEvent());
        autoButton.addActionListener(e -> onAutoButtonEvent());
    }

    public void setEnabled(final boolean enabled) {
        if (!enabled && cboxPorts.isEnabled()) {
            connectionCommandListener.setConnection(null, false);
            cboxPorts.setSelectedItem("");
        }
        cboxPorts.setEnabled(enabled);
        autoButton.setEnabled(enabled);
        autoButton.setSelected(enabled && autoButton.isSelected());
    }

    public void onConnectionStatusChanged(ConnectionStatus status) {
        connectionIndicator.setForeground(ConnectionStatusUiPropeties.getColor(status));
        connectionIndicator.setText(ConnectionStatusUiPropeties.getText(status));
    }

    public void onAutoButtonEvent() {
        cboxPorts.setEnabled( !autoButton.isSelected() );
        final String portName = (String) cboxPorts.getSelectedItem();
        connectionCommandListener.setConnection(portName,  !autoButton.isSelected() && !portName.isEmpty());
    }

    public void onPortsCboxEvent() {
        if (autoButton.isSelected() || !autoButton.isEnabled())
            return;
        connectionCommandListener.setConnection(null, false);
        if (cboxPorts.getSelectedItem() == null)
            return;
        String port = String.valueOf(cboxPorts.getSelectedItem());
        if (!port.isEmpty())
            connectionCommandListener.setConnection(port, true);
    }

    public void onConnectedEvent(boolean connected, String portName) {
        cboxPorts.setSelectedItem(connected? portName : "");
    }

    public void onPortsUpdateEvent(List<String> portNames) {

        if (cboxPorts.isPopupVisible() || this.portNames.containsAll(portNames))
            return;

        this.portNames = new HashSet<>(portNames);

        String[] modelPorts = portNames.toArray(new String[portNames.size() + 1]);
        modelPorts[portNames.size()] = "";
        cboxPorts.setModel(new DefaultComboBoxModel<>(modelPorts));

        final String portSelection = (String) (cboxPorts.getSelectedItem());
        if (this.portNames.contains(portSelection))
            cboxPorts.setSelectedItem(portSelection);
    }
}

