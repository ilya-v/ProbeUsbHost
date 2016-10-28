package com.probe.usb.host.pc.ui.controller;


import com.probe.usb.host.pc.Preferences;
import com.probe.usb.host.pc.controller.OutputController;
import com.probe.usb.host.pc.controller.OutputController.OutputChannel;

import javax.swing.*;

import java.util.HashMap;

import static com.probe.usb.host.pc.controller.OutputController.OutputChannel.ACC_DATA;
import static com.probe.usb.host.pc.controller.OutputController.OutputChannel.MSG_DATA;
import static com.probe.usb.host.pc.controller.OutputController.OutputChannel.RAW_DATA;

public class OutputSettingsUiController {

    private JButton btnChooseOutputDir;
    private JTextField txtOutputDir;
    private Preferences preferences;

    private JTextField txtRawFile, txtAccFile, txtMessagesFile;
    private JLabel labelRaw, labelAcc, labelMessages;
    private JToggleButton btnRaw, btnAcc, btnMessages;

    private OutputController outputController;
    public OutputSettingsUiController setOutputController(OutputController outputController) {
        this.outputController = outputController;
        return this;
    }

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

        this.txtOutputDir.setText(preferences.getOutputDirectory());
        btnChooseOutputDir.addActionListener(e -> chooseOutputDir());

        channels.put(ACC_DATA, btnAcc.isSelected());
        channels.put(RAW_DATA, btnRaw.isSelected());
        channels.put(MSG_DATA, btnRaw.isSelected());

        btnRaw.addActionListener(e -> aButtonToggled());
    }

    private void chooseOutputDir() {
        final JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fc.showSaveDialog(btnChooseOutputDir) != JFileChooser.APPROVE_OPTION)
            return;

        final String selectedFolder = fc.getSelectedFile().getAbsolutePath();
        txtOutputDir.setText(selectedFolder);
        preferences.saveOutputDirectory(selectedFolder);
        outputController.setOutputDir(selectedFolder);
    }

    public void onFileStatus(OutputChannel chan, String fileName, String fileSize) {
        JTextField txt =
                (chan == ACC_DATA)? txtAccFile :
                (chan == RAW_DATA)? txtRawFile :
                (chan == MSG_DATA)? txtMessagesFile : null;
        if (txt != null)
            txt.setText(fileName);
        JLabel label =
                (chan == ACC_DATA)? labelAcc :
                (chan == RAW_DATA)? labelRaw :
                (chan == MSG_DATA)? labelMessages : null;
        if (label != null)
            label.setText(fileSize);
    }

    private void probeChannel(OutputChannel chan, JToggleButton button) {
        if (!channels.containsKey(chan) || !channels.get(chan).equals(button.isSelected()))
            outputController.setChannelEnabled(chan, button.isSelected());
        channels.put(chan, button.isSelected());
    }

    private void aButtonToggled() {
        probeChannel(ACC_DATA, btnAcc);
        probeChannel(RAW_DATA, btnRaw);
        probeChannel(MSG_DATA, btnMessages);
    }
}
