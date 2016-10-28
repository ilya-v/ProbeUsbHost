package com.probe.usb.host.pc.ui.controller;

import com.probe.usb.host.pc.Preferences;

import javax.swing.*;
import java.io.File;

public class InputFileUiController {

    private JToggleButton btnInputFromFile;
    private JButton btnChooseInputFile;
    private JTextField txtInputDataFile;

    private Preferences preferences;
    public InputFileUiController setPreferences(Preferences preferences) {
        this.preferences = preferences;
        return this;
    }

    public interface StateListener {  void onActiveStatusChanged(boolean active); }
    private StateListener stateListener;

    public interface OpenFileListener{ void onInputFileOpen(String fileName); }
    private OpenFileListener openFileListener;

    public InputFileUiController setStateListener(StateListener stateListener) {
        this.stateListener = stateListener;
        return this;
    }

    public InputFileUiController setOpenFileListener(OpenFileListener openFileListener) {
        this.openFileListener = openFileListener;
        return this;
    }

    public InputFileUiController(JToggleButton btnInputFromFile,
                                 JButton btnChooseInputFile, JTextField txtInputDataFile) {
        this.btnInputFromFile = btnInputFromFile;
        this.btnChooseInputFile = btnChooseInputFile;
        this.txtInputDataFile = txtInputDataFile;

        this.btnChooseInputFile.setEnabled(this.btnInputFromFile.isSelected());

        btnInputFromFile.addActionListener(e -> btnInputFromFileEvent());
        btnChooseInputFile.addActionListener(e -> btnChooseInputFileEvent());
    }

    private void btnInputFromFileEvent() {
        btnChooseInputFile.setEnabled(btnInputFromFile.isSelected());
        stateListener.onActiveStatusChanged(btnInputFromFile.isSelected());
        if (!btnInputFromFile.isSelected())
            txtInputDataFile.setText("");
    }

    private void btnChooseInputFileEvent() {
        final JFileChooser fc = new JFileChooser();
        final String itemName = "lastInputDir";
        String lastDir = preferences.getLastFileName(itemName);
        if (lastDir != null)
            fc.setCurrentDirectory(new File(lastDir));
        if (fc.showOpenDialog(btnChooseInputFile) != JFileChooser.APPROVE_OPTION)
            return;
        String path = fc.getSelectedFile().getAbsolutePath();
        preferences.saveLastFileName(itemName, fc.getSelectedFile().getParent());
        txtInputDataFile.setText(path);
        openFileListener.onInputFileOpen(path);
    }

}
