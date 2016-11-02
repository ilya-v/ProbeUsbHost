package com.probe.usb.host.pc.ui.controller;


import com.google.common.eventbus.Subscribe;
import com.probe.usb.host.Context;
import com.probe.usb.host.bus.UiReceiver;
import com.probe.usb.host.pc.Preferences;
import com.probe.usb.host.pc.controller.OutputController.OutputChannel;
import com.probe.usb.host.pc.controller.event.UiOutputChanEnabledEvent;
import com.probe.usb.host.pc.controller.event.UiOutputDirEvent;
import com.probe.usb.host.pc.controller.event.UiOutputFileStatusCommand;

import javax.swing.*;
import java.util.HashMap;

import static com.probe.usb.host.pc.controller.OutputController.OutputChannel.*;

public class OutputSettingsUiController extends UiReceiver {

    private JButton btnChooseOutputDir;
    private JTextField txtOutputDir;
    private Preferences preferences;

    private JTextField txtRawFile, txtAccFile, txtMessagesFile;
    private JLabel labelRaw, labelAcc, labelMessages;
    private JToggleButton btnRaw, btnAcc, btnMessages;

    private HashMap<OutputChannel, Boolean> channels = new HashMap<>();

    public OutputSettingsUiController(Preferences preferences,
                                      JButton btnChooseOutputDir,
                                      JTextField txtOutputDir,
                                      JTextField txtRawFile,
                                      JTextField txtAccFile,
                                      JTextField txtMessagesFile,
                                      JLabel labelRaw,
                                      JLabel labelAcc,
                                      JLabel labelMessages,
                                      JToggleButton btnRaw,
                                      JToggleButton btnAcc,
                                      JToggleButton btnMessages
                                      ) {
        this.preferences = preferences;
        this.btnChooseOutputDir = btnChooseOutputDir;
        this.txtOutputDir = txtOutputDir;
        this.txtAccFile = txtAccFile;
        this.txtRawFile = txtRawFile;
        this.txtMessagesFile = txtMessagesFile;
        this.labelAcc = labelAcc;
        this.labelMessages = labelMessages;
        this.labelRaw = labelRaw;

        this.btnRaw = btnRaw;
        this.btnAcc = btnAcc;
        this.btnMessages = btnMessages;

        this.labelMessages = labelMessages;
        this.labelRaw = labelRaw;
        this.labelAcc = labelAcc;

        Context.invokeUi(this::uiUpdateChannels);

        this.txtOutputDir.setText(preferences.getOutputDirectory());
        postEvent(new UiOutputDirEvent(preferences.getOutputDirectory()));
        btnChooseOutputDir.addActionListener(e -> uiChooseOutputDir());
        btnAcc.addActionListener(e -> uiUpdateChannels());
        btnRaw.addActionListener(e -> uiUpdateChannels());
        btnMessages.addActionListener(e -> uiUpdateChannels());
    }

    private void uiChooseOutputDir() {
        final JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fc.showSaveDialog(btnChooseOutputDir) != JFileChooser.APPROVE_OPTION)
            return;

        final String selectedFolder = fc.getSelectedFile().getAbsolutePath();
        txtOutputDir.setText(selectedFolder);
        preferences.saveOutputDirectory(selectedFolder);
        postEvent(new UiOutputDirEvent(selectedFolder));
    }

    @Subscribe
    public void onFileStatus(UiOutputFileStatusCommand fileStatusCommand) {
        final OutputChannel chan = fileStatusCommand.getChan();
        JTextField txt =
                (chan == ACC_DATA)? txtAccFile :
                (chan == RAW_DATA)? txtRawFile :
                (chan == MSG_DATA)? txtMessagesFile : null;
        if (txt != null)
            Context.invokeUi(() -> txt.setText(fileStatusCommand.getFileName()));
        JLabel label =
                (chan == ACC_DATA)? labelAcc :
                (chan == RAW_DATA)? labelRaw :
                (chan == MSG_DATA)? labelMessages : null;
        if (label != null)
            Context.invokeUi(() -> label.setText(fileStatusCommand.getFileSize()));
    }

    private void uiUpdateChannel(OutputChannel chan, JToggleButton button) {
        if (!channels.containsKey(chan) || !channels.get(chan).equals(button.isSelected()))
            postEvent(new UiOutputChanEnabledEvent(chan, button.isSelected()));
        channels.put(chan, button.isSelected());
    }

    private void uiUpdateChannels() {
        uiUpdateChannel(ACC_DATA, btnAcc);
        uiUpdateChannel(RAW_DATA, btnRaw);
        uiUpdateChannel(MSG_DATA, btnMessages);
    }
}
