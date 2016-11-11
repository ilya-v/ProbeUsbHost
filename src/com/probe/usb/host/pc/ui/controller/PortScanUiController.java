package com.probe.usb.host.pc.ui.controller;


import com.google.common.eventbus.Subscribe;
import com.probe.usb.host.bus.UiReceiver;
import com.probe.usb.host.pc.controller.ConnectionStatus;
import com.probe.usb.host.pc.controller.event.*;
import com.probe.usb.host.pc.ui.ConnectionStatusUiPropeties;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.*;
import java.util.List;

public class PortScanUiController extends UiReceiver {
    private JComboBox<String> cboxPorts;
    private JToggleButton autoButton;
    private JLabel connectionIndicator;

    private Set<String> portNames = new HashSet<>();

    private void connectionCommand(String port, boolean connect) {
        postEvent(new ComPortConnectCommand(port, connect));
    }

    public PortScanUiController(JComboBox<String> cboxPorts, JToggleButton autoButton, JLabel connectionIndicator) {
        this.cboxPorts = cboxPorts;
        this.autoButton = autoButton;
        this.connectionIndicator = connectionIndicator;
        cboxPorts.addActionListener(e -> onPortsCboxEvent());
        cboxPorts.addPopupMenuListener(new PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                adjustCboxPorts((String)cboxPorts.getSelectedItem(), false);
            }
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                adjustCboxPorts((String)cboxPorts.getSelectedItem(), false);
            }
            @Override public void popupMenuCanceled(PopupMenuEvent e) {
                adjustCboxPorts((String)cboxPorts.getSelectedItem(), false);
            }
        });

        autoButton.addActionListener(e -> {
            postEvent(new PortScannerEnableEvent(autoButton.isSelected()));});
        autoButton.addActionListener(e->onAutoButtonEvent());

        postEvent(new ComPortsListCommand());
    }

    @Subscribe
    public void setEnabled(UiPortScanEnabledCommand event) {
        setEnabled(event.getEnabled());
    }

    public void setEnabled(final boolean enabled) {
        if (!enabled && cboxPorts.isEnabled()) {
            cboxPorts.setSelectedItem("");
            postEvent(new ComPortConnectCommand(null,false));
        }
        cboxPorts.setEnabled(enabled);
        autoButton.setEnabled(enabled);
        autoButton.setSelected(enabled && autoButton.isSelected());
        postEvent(new PortScannerEnableEvent(enabled && autoButton.isSelected()));
    }

    private void setStrikethrough(boolean strikethrough) {
        Map<TextAttribute, Object> attributes = new HashMap<>();
        attributes.putAll( cboxPorts.getFont().getAttributes());
        if (strikethrough)
            attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
        else
            attributes.remove(TextAttribute.STRIKETHROUGH);
        cboxPorts.setFont(Font.getFont(attributes));
    }

    @Subscribe
    public void onConnectionStatusChanged(ConnectionStatus status) {
        connectionIndicator.setForeground(ConnectionStatusUiPropeties.getColor(status));
        connectionIndicator.setText(ConnectionStatusUiPropeties.getText(status));
    }

    private void onAutoButtonEvent() {
        cboxPorts.setEnabled( !autoButton.isSelected() );
        final String portName = (String) cboxPorts.getSelectedItem();
        connectionCommand(portName, !autoButton.isSelected() && !portName.isEmpty());
    }

    private void onPortsCboxEvent() {
        adjustCboxPorts((String)cboxPorts.getSelectedItem(), false);
        if (autoButton.isSelected() || !autoButton.isEnabled())
            return;
        connectionCommand(null, false);
        if (cboxPorts.getSelectedItem() == null)
            return;
        String port = String.valueOf(cboxPorts.getSelectedItem());
        if (!port.isEmpty())
            connectionCommand(port, true);
    }

    private void adjustCboxPorts(final String portName, boolean strikethrough) {
        if (!portName.equals(String.valueOf(cboxPorts.getSelectedItem())))
            cboxPorts.setSelectedItem(portName);
        setStrikethrough(strikethrough);
    }

    @Subscribe
    public void onConnectedEvent(ComPortConnectionEvent event) {
        adjustCboxPorts(event.getConnectionStatus()? event.getPortName() : "", false);
    }

    @Subscribe
    public void onConnectionFailureEvent(ComPortFailureEvent event) {
        adjustCboxPorts(event.getPort(), true);
    }

    @Subscribe
    public void onPortsUpdateEvent(ComPortHasPortsEvent event) {
        List<String> portNames = event.getPorts();
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

