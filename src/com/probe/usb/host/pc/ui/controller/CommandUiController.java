package com.probe.usb.host.pc.ui.controller;

import com.probe.usb.host.commander.ProbeUsbCommander;
import com.probe.usb.host.common.ConfigCommand;
import com.probe.usb.host.common.ConfigParamType;
import com.probe.usb.host.parser.internal.message.AccLis331Register;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.probe.usb.host.common.ConfigCommand.*;

public class CommandUiController {

    public interface CommandListener {
        void onCommandAdded(ProbeUsbCommander commander);
    }

    private JComboBox<String> cboxCommand;
    private JComboBox<String> cboxArgType;
    private JTextField txtArg;
    private JButton btnSend;
    private ProbeUsbCommander probeCommander;
    private CommandListener commandListener;

    private Map<ConfigCommand, String> lastArguments = new HashMap<>();

    final static  private CmdComboItem[] modelPacketTypes = {
            new CmdComboItem(writeConfig),
            new CmdComboItem(readConfig),
            new CmdComboItem(accRegWrite),
            new CmdComboItem(accRegRead),
            new CmdComboItem(setTimeHi, "setTimeHi, setTimeLo" ),
            new CmdComboItem(startRecording),
            new CmdComboItem(stopRecording),
    };

    final static private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.ENGLISH);

    public CommandUiController(JComboBox<String> cboxCommand, JComboBox<String> cboxArgType, JTextField txtArg, JButton btnSend) {
        this.cboxCommand = cboxCommand;
        this.cboxArgType = cboxArgType;
        this.txtArg = txtArg;
        this.btnSend = btnSend;

        cboxCommand.setModel(new DefaultComboBoxModel(modelPacketTypes));

        cboxCommand.addActionListener(this::cboxCommandActionPerformed);
        btnSend.addActionListener(this::sendButtonActionPerformed);
        cboxArgType.addActionListener(this::cboxArgTypeActionPerformed);
        txtArg.getDocument().addDocumentListener(new DocumentListener() {
            @Override  public void insertUpdate(DocumentEvent e)    { onTxtArgChanged(); }
            @Override  public void removeUpdate(DocumentEvent e)    { onTxtArgChanged(); }
            @Override  public void changedUpdate(DocumentEvent e)   { onTxtArgChanged(); }
        });
        setEnabled(cboxCommand.isEnabled());
    }

    public CommandUiController setCommander(ProbeUsbCommander commander) {
        this.probeCommander = commander;
        return this;
    }

    public CommandUiController setCommandListener(CommandListener commandListener) {
        this.commandListener = commandListener;
        return this;
    }

    private void adjustArgTypeModel() {
        final ConfigCommand command = ((CmdComboItem)cboxCommand.getSelectedItem()).getCommand();
        final DefaultComboBoxModel model =
                command.equals(writeConfig)?    new DefaultComboBoxModel(ConfigParamType.getWritable()):
                command.equals(readConfig)?     new DefaultComboBoxModel(ConfigParamType.values()) :
                command.equals(accRegWrite)?    new DefaultComboBoxModel(AccLis331Register.getWritable()) :
                command.equals(accRegRead)?     new DefaultComboBoxModel(AccLis331Register.values()):
                                                new DefaultComboBoxModel();
        cboxArgType.setModel(model);
    }

    private boolean useArgFromSelection() {
        return getSelectedCommand().equals(readConfig) || getSelectedCommand().equals(accRegRead);
    }

    public void setEnabled(final boolean enabled) {
        cboxCommand.setEnabled(enabled);
        cboxArgType.setEnabled(enabled && (getSelectedCommand().usesSecondByte() || useArgFromSelection()) );
        txtArg.setEnabled(enabled && getSelectedCommand().usesLastBytes() && !useArgFromSelection());
        btnSend.setEnabled(enabled && validateArgument());
        adjustArgTypeModel();
    }

    private ConfigCommand getSelectedCommand() {
        return ((CmdComboItem) cboxCommand.getSelectedItem()).getCommand();
    }

    private boolean validateArgument() {
        try {
            parseArgument();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private int parseArgument() {

        if (!getSelectedCommand().usesLastBytes())
            return 0;

        if (useArgFromSelection()) {
            final Object selectedArg = cboxArgType.getSelectedItem();
            if (selectedArg instanceof AccLis331Register)
                return ((AccLis331Register) selectedArg).getAddr();
            if (selectedArg instanceof ConfigParamType)
                return ((ConfigParamType) selectedArg).index;
            throw new IllegalArgumentException(selectedArg.toString());
        }

        if (getSelectedCommand().equals(setTimeHi)) {
            try {
                return (int) dateFormat.parse(txtArg.getText()).getTime() / 1000; // unix time
            } catch (ParseException e) { /*do nothing*/ }
        }

        String value = txtArg.getText().toLowerCase();
        int base;
        if (value.contains("0x") || value.contains("h")) {
            value = value.replaceAll("0x", "").replaceAll("h", "").replaceAll("\\s+","");
            base = 16;
        }
        else if (value.contains("b")) {
            value =  value.replaceAll("0b", "").replaceAll("b", "").replaceAll("\\s+","");
            base = 2;
        }
        else {
            value = value.trim();
            base = 10;
        }
        return Integer.parseInt(value, base);
    }

    private void cboxCommandActionPerformed(ActionEvent evt) {
        setEnabled(cboxCommand.isEnabled());

        final ConfigCommand selectedCommand = ((CmdComboItem) cboxCommand.getSelectedItem()).getCommand();
        final String argument =
                selectedCommand.equals(setTimeHi)?
                        new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.ENGLISH).format(new Date()) :
                lastArguments.containsKey(selectedCommand)?
                        lastArguments.get(selectedCommand) :
                "";
        txtArg.setText(argument);
    }

    private void cboxArgTypeActionPerformed(ActionEvent evt) {}

    private void onTxtArgChanged() {
        btnSend.setEnabled(cboxCommand.isEnabled() && validateArgument());
    }

    private void sendButtonActionPerformed(ActionEvent evt) {

        if (!validateArgument())
            return;
        final int argument = parseArgument();
        lastArguments.put(getSelectedCommand(), txtArg.getText());

        if (getSelectedCommand().equals(setTimeHi))
            probeCommander.deviceSetTime(argument);
        else if (getSelectedCommand().equals(accRegRead))
            probeCommander.deviceAccRegRead(((AccLis331Register)cboxArgType.getSelectedItem()).getAddr());
        else if (getSelectedCommand().equals(accRegWrite))
            probeCommander.deviceAccRegWrite(((AccLis331Register)cboxArgType.getSelectedItem()).getAddr(), argument);
        else if (getSelectedCommand().equals(startRecording))
            probeCommander.deviceStartRecording();
        else if (getSelectedCommand().equals(stopRecording))
            probeCommander.deviceStopRecording();
        else
            probeCommander.deviceCommand(getSelectedCommand().getFirstByte(),
                    (ConfigParamType) cboxArgType.getSelectedItem(), argument);

        commandListener.onCommandAdded(probeCommander);
    }
}
