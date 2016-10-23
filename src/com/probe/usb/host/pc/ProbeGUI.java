package com.probe.usb.host.pc;

import com.probe.usb.host.commander.ProbeUsbCommander;
import com.probe.usb.host.parser.ParserEventListener;
import com.probe.usb.host.parser.ProbeUsbParser;
import com.probe.usb.host.parser.processor.PrintProcessor;
import com.probe.usb.host.parser.processor.PrintProcessor.OutputElement;
import com.probe.usb.host.parser.processor.TablePrintProcessor;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.text.DefaultCaret;

import com.probe.usb.host.pc.StatusListener.ConnectionStatus;
import com.probe.usb.host.pc.plot.Plot;
import com.probe.usb.host.pc.ui.CommandUiControl;

import java.awt.event.ActionEvent;
import java.nio.charset.StandardCharsets;

public class ProbeGUI extends javax.swing.JFrame {

    public static ProbeGUI instance;
    public static JFrame graph;

    private javax.swing.JToggleButton autoButton;
    private javax.swing.JToggleButton plotButton;
    private javax.swing.JComboBox<String> cboxPorts;


    private Logger logger = new Logger() {
        @Override
        void printLine(String line) { printToWindowLog(line);}
    };

    private TablePrintProcessor packetProcessor = new TablePrintProcessor();

    private PrintProcessor printProcessor = new PrintProcessor()
            .disableOutputOf(OutputElement.DanglingPacket)
            .disableOutputOf(OutputElement.DataPacket);

    private StatusListener statusEventListener = new StatusListener(this::changeStatus);

    private ParserEventListener bytesAndFramesListener = new ParserEventListener() {
        @Override public void onNewByte(final int b) {
            statusEventListener.onNewByte(b);
            portScanner.onNewByte(b);
        }
        @Override public void onNewFrame(final int b1, final int b2) {
            statusEventListener.onNewFrame(b1, b2);
            portScanner.onNewFrame(b1, b2);
        }
    };

    private ProbeUsbParser parser = new ProbeUsbParser()
            .setListener(bytesAndFramesListener)
            .addPacketProcessor(packetProcessor)
            .addPacketProcessor(printProcessor);

    private Communicator communicator = new Communicator()
            .setLogger(logger::printLine)
            .setListener(statusEventListener::onConnectionStatus)
            .setReceiver(b -> {
                parser.addByte(Byte.toUnsignedInt(b));
                printToWindowLog( printProcessor.popResult() );
                writeNewFileToOutputDirectory();
            });

    private ProbeUsbCommander probeCommander = new ProbeUsbCommander();

    private OutputWriter outputWriter = new OutputWriter()
            .setFileNamePrefix("accel-")
            .setOutputDir( getOutputDirectory() );

    private PortScanner portScanner = new PortScanner(communicator)
            .setLogger(logger::printLine)
            .setEnabledInfo( () -> autoButton != null && autoButton.isSelected())
            .setStatusListener( (connected, portName) -> {
                cboxPorts.setSelectedItem(connected? portName : "");
            });

    private CommandUiControl commandUiControl;

    private final String PREF_OUTPUTDIR_NAME = "preference_outputdir";
    private final String PREF_LASTFILE_NAME = "preference_lastfile";
    private final String PREF_DATAFILEDIR = "preference_datafiledir";
    private final String PREF_CONFIGFILEDIR = "preference_configfiledir";
    
    private String outputDirectory;

    private ConnectionStatus connectionStatus = ConnectionStatus.Disconnected;

    private Timer timer = new Timer(1000, e -> {
        List<String> portNames = new ArrayList<>();
        if (connectionStatus == ConnectionStatus.Disconnected) {
            portNames = communicator.searchForPorts();
            String[] modelPorts = portNames.toArray(new String[portNames.size() + 1]);
            modelPorts[portNames.size()] = "";

            if (!cboxPorts.isPopupVisible()) {
                final String portSelection = (String) (cboxPorts.getSelectedItem());
                cboxPorts.setModel(new DefaultComboBoxModel<>(modelPorts));
                cboxPorts.setSelectedItem(portSelection);
            }
        }
        portScanner.onTimerTick(portNames);
        statusEventListener.onTimerTick();
        sendNextCommandLine();
    } );
    {
        timer.start();
    }

    private int locCounter = 0;
    
    private Deque<byte[]> commands = new ArrayDeque<>();


    private ProbeGUI() {
        initComponents();


        outputDirectory = getOutputDirectory();
        outputDirDisplay.setText(outputDirectory);
        
        lastSavedFileField.setText(getLastFileName());

        this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                communicator.disconnect();
            }
        });
    }

    private void initComponents() {

        cboxPorts = new javax.swing.JComboBox<>();
        labelComPortCaption = new javax.swing.JLabel();
        labelCommand = new javax.swing.JLabel();
        cboxCommand = new javax.swing.JComboBox<>();
        labelArgType = new javax.swing.JLabel();
        sendButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
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
        jScrollPane2.setViewportView(sendLog);
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


        checkBoxFromDataFile.addActionListener(this::checkBoxFromDataFileActionPerformed);

        commandUiControl = new CommandUiControl(cboxCommand, cboxArgumentType, txtCommandArg, sendButton)
                .setCommander(probeCommander)
                .setCommandListener(this::onCommandAdded);

        cboxPorts.addActionListener(this::cboxPortsActionPerformed);

        datafileButton.addActionListener(this::datafileButtonActionPerformed);
        autoButton.addActionListener(this::autoButtonActionPerformed);

        outputdirButton.addActionListener(this::outputdirButtonActionPerformed);
        plotButton.addActionListener(this::plotButtonActionPerformed);
        selectCommandFileButton.addActionListener(this::selectCommandFileButtonActionPerformed);
        performCommandsButton.addActionListener(this::performCommandsButtonActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
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
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();

        getRootPane().setDefaultButton(sendButton);
    }

    private void onCommandAdded(ProbeUsbCommander probeCommander) {

        int output;
        String line = "";
        ArrayList<Byte> bytes = new ArrayList<>();

        while ((output = probeCommander.popOutputByte()) != -1) {
            bytes.add((byte) output);
            line += String.format("%02X ", Byte.toUnsignedInt(bytes.get(bytes.size()-1)));
        }

        if (bytes.size() > 0 && bytes.size() % 4 == 0) {
            byte[] arrBytes = new byte[bytes.size()];
            for (int i = 0; i < arrBytes.length; i++)
                arrBytes[i] = bytes.get(i);
            if (communicator != null)
                communicator.writeData(arrBytes);
            printToWindowLog(line);
        } else if (bytes.size() > 0) {
            addErrorLine(line + " cannot be sent");
        }
    }


    private void cboxPortsActionPerformed(ActionEvent evt) {
        if (autoButton.isSelected() || checkBoxFromDataFile.isSelected())
            return;

        if (!communicator.getPortName().isEmpty())
            printToWindowLog("Disconnected from " + communicator.getPortName());
        communicator.disconnect();

        if (cboxPorts.getSelectedItem() == null)
            return;

        String port = String.valueOf(cboxPorts.getSelectedItem());

        if (!port.isEmpty() && communicator.connect(port))
            printToWindowLog("Connected to " + communicator.getPortName());
    }


    private void checkBoxFromDataFileActionPerformed(ActionEvent evt) {
        final boolean dataFromFile = ((JCheckBox) evt.getSource()).isSelected();
        datafileButton.setEnabled(dataFromFile);
        cboxPorts.setEnabled(!dataFromFile);
        commandUiControl.setEnabled(!dataFromFile && connectionStatus != ConnectionStatus.Disconnected);
        autoButton.setEnabled(!dataFromFile);
        autoButton.setSelected(!dataFromFile && autoButton.isSelected());
        if (dataFromFile) {
            if (!communicator.getPortName().isEmpty())
                printToWindowLog("Disconnected from " + communicator.getPortName());
            communicator.disconnect();
        }
    }

    private void autoButtonActionPerformed(ActionEvent evt) {
        cboxPorts.setEnabled( !autoButton.isSelected() );
        if (autoButton.isSelected()) {
            if (!communicator.getPortName().isEmpty())
                printToWindowLog("Disconnected from " + communicator.getPortName());
            communicator.disconnect();
        }
    }

    private void datafileButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_datafileButtonActionPerformed
        // Выбрать файл для чтения
        final JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(ProbeGUI.this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            byte [] fileData;
            
            try {
                fileData = Files.readAllBytes(file.toPath());
            } catch (IOException ex) {
                logger.printLine(ex.toString());
                return;
            }

            for (int b: fileData) {
                parser.addByte(b);
            }

            outputWriter.forceNewFile();
            writeNewFileToOutputDirectory();
        }
    }//GEN-LAST:event_datafileButtonActionPerformed

    private void writeNewFileToOutputDirectory() {
        final String data = packetProcessor.popResult();
        if (data.isEmpty())
            return;
        try {
            outputWriter.write(data);
        }
        catch (IOException e) {
            logger.printLine(e.toString());
        }

        if (outputWriter.getCurrentFileName() == null)
            return;

        final String lastFileName = lastSavedFileField.getText();
        if (!lastFileName.equals(outputWriter.getCurrentFileName())) {
            lastSavedFileField.setText(outputWriter.getCurrentFileName());
            saveLastFileName(outputWriter.getCurrentFileName());
            locCounter = 0;
        }

        locCounter += data.split("\r\n|\r|\n").length;
        locLabel.setText(Integer.toString(locCounter) + " lines");
    }

    
    private void outputdirButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_outputdirButtonActionPerformed
        final JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fc.showSaveDialog(ProbeGUI.this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File folder = fc.getSelectedFile();
            outputDirectory = folder.getPath();
            outputDirDisplay.setText(outputDirectory);
            saveOutputDirectory(outputDirectory);
            outputWriter.setOutputDir(outputDirectory);
        }
    }//GEN-LAST:event_outputdirButtonActionPerformed

    private void plotButtonActionPerformed(ActionEvent evt) {
        graph.setVisible(plotButton.isSelected());
    }

    private void selectCommandFileButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_selectCommandFileButtonActionPerformed
        String configDir = getConfigFileDirectory();    // предыдущая директория
        
        final JFileChooser fc = new JFileChooser(configDir);
        int returnVal = fc.showOpenDialog(ProbeGUI.this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            printToWindowLog("CONFIG : " + file.getAbsolutePath());
            
            String path = file.getParent();
            if(path != null) {
                saveConfigFileDirectory(path);
            }
            
            try {
                List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
                for (String  line : lines) {
                    final String[] cmdBytes = line.split("//", 1)[0].trim().split("\\s+");
                    if (cmdBytes.length > 0) {
                        byte[] bytes = new byte[cmdBytes.length];
                        int i = 0;
                        for (String b: cmdBytes) {
                            bytes[i] = Byte.parseByte(b, 16);
                            i++;
                        }
                        commands.add(bytes);
                    }
                }
            } catch (Exception ex) {
                logger.printLine(ex.toString());
                commands.clear();
                return;
            }  
            
            performCommandsButton.setEnabled(true);
        }
    }//GEN-LAST:event_selectCommandFileButtonActionPerformed

    private void performCommandsButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_performCommandsButtonActionPerformed
        performCommandsButton.setEnabled(false);
        sendNextCommandLine();
    }//GEN-LAST:event_performCommandsButtonActionPerformed

    private void sendNextCommandLine() {
        String logLine = "";
        byte[] bytes = commands.pollFirst();
        if (bytes == null)
            return;
        for (byte b: bytes) {
            communicator.writeData(new byte [] {b});
            logLine += Integer.toHexString(Byte.toUnsignedInt(b)) + " ";
        }
        printToWindowLog(logLine);
    }

    private void changeStatus(ConnectionStatus status) {
        connectionStatus = status;
        commandUiControl.setEnabled( !checkBoxFromDataFile.isSelected() && status != ConnectionStatus.Disconnected );
        if(status == ConnectionStatus.Disconnected) {
            connectionIndicator.setForeground(Color.red);
            connectionIndicator.setText("DISCONNECTED");
        }
        else if(status == ConnectionStatus.Connected) {
            connectionIndicator.setForeground(Color.black);
            connectionIndicator.setText("PORT OPEN");
        }
        else if(status == ConnectionStatus.Reading) {
            connectionIndicator.setForeground(new Color(0.4f, 0.7f, 0.6f));
            connectionIndicator.setText("READING INPUT...");
        }
        else if(status == ConnectionStatus.Data) {
            connectionIndicator.setForeground(new Color(0.4f, 0.7f, 0.6f));
            connectionIndicator.setText("RECEIVING DATA");
        }
        else if(status == ConnectionStatus.Idle) {
            connectionIndicator.setForeground(Color.black);
            connectionIndicator.setText("IDLE");
        }
    }

    private void addErrorLine(String line) {
        sendLog.append("ERROR : " + line + "\n");        
    }
    
    private void printToWindowLog(String line) {
        if (line.isEmpty()) return;
        sendLog.append(line + "\n");        
    }

    private String getOutputDirectory() {
        Preferences prefs = Preferences.userNodeForPackage(ProbeGUI.class);
        String defaultValue = System.getProperty("user.home");
        return prefs.get(PREF_OUTPUTDIR_NAME, defaultValue);
    }
    
    private void saveOutputDirectory(String outputDirectory) {
        Preferences prefs = Preferences.userNodeForPackage(ProbeGUI.class);
        prefs.put(PREF_OUTPUTDIR_NAME, outputDirectory);
    }
    
    private String getLastFileName() {
        Preferences prefs = Preferences.userNodeForPackage(ProbeGUI.class);
        String defaultValue = System.getProperty("none");
        return prefs.get(PREF_LASTFILE_NAME, defaultValue);        
    }
    
    private void saveLastFileName(String fileName) {
        Preferences prefs = Preferences.userNodeForPackage(ProbeGUI.class);
        prefs.put(PREF_LASTFILE_NAME, fileName); 
    }
    
    private String getConfigFileDirectory() {
        Preferences prefs = Preferences.userNodeForPackage(ProbeGUI.class);
        String defaultValue = System.getProperty("user.home");
        return prefs.get(PREF_CONFIGFILEDIR, defaultValue);        
    }
    
    private void saveConfigFileDirectory(String dir) {
        Preferences prefs = Preferences.userNodeForPackage(ProbeGUI.class);
        prefs.put(PREF_CONFIGFILEDIR, dir);         
    }
    
    private void enableControls(boolean enable) {
        cboxPorts.setEnabled(enable);
        autoButton.setEnabled(enable);
        commandUiControl.setEnabled(enable);
    }
        

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException | InstantiationException ex) {
            java.util.logging.Logger.getLogger(ProbeGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        instance = new ProbeGUI();
        graph = new Plot();

        java.awt.EventQueue.invokeLater(() -> instance.setVisible(true));
    }

    private javax.swing.JComboBox<String> cboxArgumentType;
    private javax.swing.JComboBox<String> cboxCommand;
    private javax.swing.JButton connectionIndicator;
    private javax.swing.JButton datafileButton;
    private javax.swing.JCheckBox checkBoxFromDataFile;
    private javax.swing.JLabel labelComPortCaption;
    private javax.swing.JLabel labelCommand;
    private javax.swing.JLabel labelArgType;
    private javax.swing.JLabel labelLogCaption;
    private javax.swing.JLabel labelArgValueCaption;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel labelConnectionStatus;
    private javax.swing.JLabel lastSavedFileLabel;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField lastSavedFileField;
    private javax.swing.JLabel locLabel;
    private javax.swing.JTextField outputDirDisplay;
    private javax.swing.JButton outputdirButton;
    private javax.swing.JButton performCommandsButton;
    private javax.swing.JButton selectCommandFileButton;
    private javax.swing.JButton sendButton;
    private javax.swing.JTextArea sendLog;
    private javax.swing.JTextField txtCommandArg;

    private javax.swing.JCheckBox checkBoxSaveAccOutput;
    // End of variables declaration//GEN-END:variables
}
