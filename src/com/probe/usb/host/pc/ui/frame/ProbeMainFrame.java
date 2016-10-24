package com.probe.usb.host.pc.ui.frame;


import javax.swing.*;
import javax.swing.text.DefaultCaret;

public class ProbeMainFrame extends JFrame  {
    public javax.swing.JToggleButton autoButton;
    public javax.swing.JToggleButton plotButton;
    public javax.swing.JComboBox<String> cboxPorts;

    public javax.swing.JComboBox<String> cboxArgumentType;
    public javax.swing.JComboBox<String> cboxCommand;
    public javax.swing.JButton connectionIndicator;
    public javax.swing.JButton datafileButton;
    public javax.swing.JCheckBox checkBoxFromDataFile;
    private javax.swing.JLabel labelComPortCaption;
    private javax.swing.JLabel labelCommand;
    private javax.swing.JLabel labelArgType;
    private javax.swing.JLabel labelLogCaption;
    private javax.swing.JLabel labelArgValueCaption;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel labelConnectionStatus;
    private javax.swing.JLabel lastSavedFileLabel;
    private javax.swing.JScrollPane logScrollPane;
    public javax.swing.JTextField lastSavedFileField;
    public javax.swing.JLabel locLabel;
    public javax.swing.JTextField outputDirDisplay;
    public javax.swing.JButton outputdirButton;
    private javax.swing.JButton performCommandsButton;
    public javax.swing.JButton selectCommandFileButton;
    public javax.swing.JButton sendButton;
    public javax.swing.JTextArea sendLog;
    public javax.swing.JTextField txtCommandArg;
    private javax.swing.JCheckBox checkBoxSaveAccOutput;


    {
        cboxPorts = new javax.swing.JComboBox<>();
        labelComPortCaption = new javax.swing.JLabel();
        labelCommand = new javax.swing.JLabel();
        cboxCommand = new javax.swing.JComboBox<>();
        labelArgType = new javax.swing.JLabel();
        sendButton = new javax.swing.JButton();
        logScrollPane = new javax.swing.JScrollPane();
        sendLog = new javax.swing.JTextArea();
        labelLogCaption = new javax.swing.JLabel();
        cboxArgumentType = new javax.swing.JComboBox<>();
        txtCommandArg = new javax.swing.JTextField();
        labelArgValueCaption = new javax.swing.JLabel();
        checkBoxFromDataFile = new javax.swing.JCheckBox();
        datafileButton = new javax.swing.JButton();
        plotButton = new javax.swing.JToggleButton();
        jLabel6 = new javax.swing.JLabel();
        outputdirButton = new javax.swing.JButton();
        outputDirDisplay = new javax.swing.JTextField();
        autoButton = new javax.swing.JToggleButton("Auto Scan Ports", true);
        labelConnectionStatus = new javax.swing.JLabel();
        connectionIndicator = new javax.swing.JButton();
        lastSavedFileLabel = new javax.swing.JLabel();
        lastSavedFileField = new javax.swing.JTextField();
        locLabel = new javax.swing.JLabel();
        selectCommandFileButton = new javax.swing.JButton();
        performCommandsButton = new javax.swing.JButton();
        checkBoxSaveAccOutput = new javax.swing.JCheckBox();

        sendButton.setText("Send Command");
        labelComPortCaption.setText("COM Port");
        labelCommand.setText("Command");
        labelArgType.setText("Argument Type");
        sendLog.setColumns(20);
        sendLog.setRows(5);
        logScrollPane.setViewportView(sendLog);
        labelLogCaption.setText("Log");
        labelArgValueCaption.setText("Value");
        plotButton.setText("Plot");
        performCommandsButton.setText("Execute Commands");
        performCommandsButton.setEnabled(false);
        datafileButton.setText("Choose File...");
        datafileButton.setEnabled(false);
        jLabel6.setText("Directory for output files:");
        outputdirButton.setText("Choose...");
        labelConnectionStatus.setText("Connection Status:");
        connectionIndicator.setText("disconnected");
        checkBoxFromDataFile.setText("Read From File");
        outputDirDisplay.setEditable(false);
        connectionIndicator.setEnabled(false);
        lastSavedFileLabel.setText("Last Saved File:");
        lastSavedFileField.setEditable(false);
        locLabel.setText("        ");
        selectCommandFileButton.setText("Open Command File");
        sendLog.setEditable(false);
        cboxPorts.setEnabled( !autoButton.isSelected() );
        cboxPorts.setEditable(true);
        checkBoxSaveAccOutput.setText("Save Acceleraions");

        DefaultCaret caret = (DefaultCaret)sendLog.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);



        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(22, 22, 22)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(logScrollPane)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(labelComPortCaption, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(cboxPorts, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(autoButton)
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(checkBoxFromDataFile)
                                                .addComponent(datafileButton)
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(plotButton))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(cboxCommand, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(labelCommand))
                                                                .addGap(20, 20, 20)
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(cboxArgumentType, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(labelArgType)
                                                                        .addComponent(performCommandsButton))
                                                                .addGap(18, 18, 18)
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(txtCommandArg)
                                                                        .addGroup(layout.createSequentialGroup()
                                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(checkBoxFromDataFile, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                        .addComponent(labelArgValueCaption))
                                                                                .addGap(0, 79, Short.MAX_VALUE))))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(jLabel6)
                                                                .addGap(24, 24, 24)
                                                                .addComponent(outputDirDisplay))
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                                .addComponent(autoButton, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                                        .addComponent(labelConnectionStatus) ////
                                                                        .addGap(18, 18, 18)
                                                                        .addComponent(connectionIndicator, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(lastSavedFileLabel)
                                                                .addGap(35, 35, 35)
                                                                .addComponent(lastSavedFileField)))
                                                .addGap(18, 18, 18)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(sendButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(locLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                .addGap(95, 95, 95))
                                                        .addComponent(outputdirButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(labelLogCaption)
                                                        .addComponent(selectCommandFileButton, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(19, 19, 19)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(cboxPorts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(labelComPortCaption)
                                                        .addComponent(checkBoxFromDataFile)
                                                        .addComponent(datafileButton)
                                                        .addComponent(autoButton)
                                                        .addComponent(plotButton))
                                                .addGap(18, 18, 18)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(labelConnectionStatus) ////
                                                        .addComponent(connectionIndicator))
                                                //.addComponent(checkBoxSaveAccOutput)
                                                .addGap(48, 48, 48))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(labelCommand)
                                                        .addComponent(labelArgType)
                                                        .addComponent(labelArgValueCaption))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(cboxCommand, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(cboxArgumentType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txtCommandArg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(sendButton))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(selectCommandFileButton)
                                        .addComponent(performCommandsButton))
                                .addGap(33, 33, 33)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel6)
                                        .addComponent(outputDirDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(outputdirButton))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(lastSavedFileLabel)
                                        .addComponent(lastSavedFileField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(locLabel))
                                .addGap(18, 18, 18)
                                .addComponent(labelLogCaption)
                                .addGap(18, 18, 18)
                                .addComponent(logScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                                .addContainerGap())
        );

        pack();

        getRootPane().setDefaultButton(sendButton);

        java.awt.EventQueue.invokeLater(() -> setVisible(true));
    }
}
