package com.probe.usb.host.pc.ui.controller;

import com.google.common.eventbus.Subscribe;
import com.probe.usb.host.Context;
import com.probe.usb.host.bus.UiReceiver;
import com.probe.usb.host.pc.Preferences;
import com.probe.usb.host.pc.controller.event.OpenRawInputFileCommand;
import com.probe.usb.host.pc.controller.event.UiInputBusyCommand;
import com.probe.usb.host.pc.controller.event.UiInputFileActiveEvent;

import javax.swing.*;
import java.io.File;

public class InputFileUiController extends UiReceiver {

    private JToggleButton uiBtnInputFromFile;
    private JButton uiBtnChooseInputFile;
    private JTextField uiTxtInputDataFile;

    private Preferences preferences;
    public InputFileUiController setPreferences(Preferences preferences) {
        this.preferences = preferences;
        return this;
    }

    public InputFileUiController(JToggleButton btnInputFromFile,
                                 JButton btnChooseInputFile, JTextField txtInputDataFile) {
        this.uiBtnInputFromFile = btnInputFromFile;
        this.uiBtnChooseInputFile = btnChooseInputFile;
        this.uiTxtInputDataFile = txtInputDataFile;

        this.uiBtnChooseInputFile.setEnabled(this.uiBtnInputFromFile.isSelected());

        this.uiBtnInputFromFile.addActionListener(e -> btnInputFromFileEvent());
        this.uiBtnChooseInputFile.addActionListener(e -> btnChooseInputFileEvent());
    }

    private void btnInputFromFileEvent() {
        uiBtnChooseInputFile.setEnabled(uiBtnInputFromFile.isSelected());
        postEvent(new UiInputFileActiveEvent(uiBtnInputFromFile.isSelected()));
        if (!uiBtnInputFromFile.isSelected())
            uiTxtInputDataFile.setText("");
    }

    private void btnChooseInputFileEvent() {
        final JFileChooser fc = new JFileChooser();
        final String itemName = "lastInputDir";
        String lastDir = preferences.getLastFileName(itemName);
        if (lastDir != null)
            fc.setCurrentDirectory(new File(lastDir));
        if (fc.showOpenDialog(uiBtnChooseInputFile) != JFileChooser.APPROVE_OPTION)
            return;
        String path = fc.getSelectedFile().getAbsolutePath();
        preferences.saveLastFileName(itemName, fc.getSelectedFile().getParent());
        uiTxtInputDataFile.setText(path);

        postEvent(new OpenRawInputFileCommand(path));
    }

    @Subscribe
    void onBusy(UiInputBusyCommand busyCommand) {
        Context.invokeUi( () -> {
            uiBtnChooseInputFile.setEnabled(!busyCommand.getBusy());
            uiBtnInputFromFile.setEnabled(!busyCommand.getBusy());
        });
    }

}
