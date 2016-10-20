package com.probe.usb.host.pc;

import com.probe.usb.host.commander.ConfigParamType;
import com.probe.usb.host.commander.ProbeUsbCommander;
import com.probe.usb.host.parser.ProbeUsbParser;
import com.probe.usb.host.parser.TablePacketProcessor;

import java.awt.Cursor;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;

import static com.probe.usb.host.commander.ConfigCommand.*;
import com.probe.usb.host.parser.ParserEventListener;
import com.probe.usb.host.parser.internal.FramePacket;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.StandardCharsets;
import java.util.prefs.BackingStoreException;
import javax.swing.JOptionPane;
import javax.swing.Timer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author chernov
 */
public class ProbeGUI extends javax.swing.JFrame implements Communicator.Receiver, CheckListener, StatusListener {

    public static ProbeGUI instance;
    
    private Communicator communicator;
    
    private ComboItem selectedCommand;
    private ComboItem selectedArgument;
    
    private ProbeUsbCommander probeCommander = new ProbeUsbCommander();

    private TablePacketProcessor packetProcessor = new TablePacketProcessor();
    private ProbeUsbParser parser = new ProbeUsbParser().addPacketProcessor(packetProcessor);

    private boolean dataFromFile = false;
    
    private final String PREF_OUTPUTDIR_NAME = "preference_outputdir";
    private final String PREF_LASTFILE_NAME = "preference_lastfile";
    private final String PREF_DATAFILEDIR = "preference_datafiledir";
    private final String PREF_CONFIGFILEDIR = "preference_configfiledir";
    
    private String outputDirectory;

    private Map<Integer, String> lastArguments = new HashMap<>();
    
    private TestParserEventListener testEventListener;
    private StatusParserEventListener statusEventListener;
    
    private Deque<String> portsToCheck;
    
    private enum ConnectionStatus {
        Disconnected,
        Connected,
        Reading,
        Data,
        Idle
    }
    
    private ConnectionStatus status = ConnectionStatus.Disconnected;
    
    private int locCounter = 0;
    private boolean fileStarted = false;
    
    private List<String> commands = new ArrayList<String>();
    
    
    public void setCommunicator(Communicator communicator) {
        this.communicator = communicator;
        this.communicator.setReceiver(this);
        
        HashMap ports = this.communicator.getAvailableComPorts();
        for (Object key : new TreeSet(ports.keySet())) {
            cboxPorts.addItem((String)key);
        }
    }
    
    public void handle(byte data) {
        parser.addByte(data);
    }
    
    /**
     * Creates new form ProbeGUI
     */
    private ProbeGUI() {
        initComponents();
        
        getRootPane().setDefaultButton(sendButton);
        
        sendLog.setEditable(false);
        
        testEventListener = new TestParserEventListener(this);
        statusEventListener = new StatusParserEventListener(this);
        
        outputDirectory = getOutputDirectory();
        outputDirDisplay.setText(outputDirectory);
        
        lastSavedFileField.setText(getLastFileName());
        
        Vector modelPacketTypes = new Vector();
        modelPacketTypes.addElement( new ComboItem(writeConfig.getFirstByte(), writeConfig.name() ) );
        modelPacketTypes.addElement( new ComboItem(readConfig.getFirstByte(), readConfig.name()) );
        modelPacketTypes.addElement( new ComboItem(accRegWrite.getFirstByte(), accRegWrite.name() ) );
        modelPacketTypes.addElement( new ComboItem(accRegRead.getFirstByte(), accRegRead.name() ) );
        modelPacketTypes.addElement( new ComboItem(setTimeHi.getFirstByte(), "setTimeHi, setTimeLo" ) );

        cboxCommand.setModel(new DefaultComboBoxModel(modelPacketTypes));
        
        Vector modelArgumentTypes = new Vector();
        
        modelArgumentTypes.addElement( new ComboItem(ConfigParamType.selective_recording_mode.index, "selective_recording_mode" ) );
        modelArgumentTypes.addElement( new ComboItem(ConfigParamType.streaming_mode.index, "streaming_mode" ) );
        modelArgumentTypes.addElement( new ComboItem(ConfigParamType.use_bt_with_usb.index, "use_bt_with_usb" ) );
        modelArgumentTypes.addElement( new ComboItem(ConfigParamType.snooze_detection_time.index, "snooze_detection_time" ) );
        modelArgumentTypes.addElement( new ComboItem(ConfigParamType.snooze_threshold_acceleration.index, "snooze_threshold_acceleration" ) );
        modelArgumentTypes.addElement( new ComboItem(ConfigParamType.activation_threshold_acceleration.index, "activation_threshold_acceleration" ) );
        modelArgumentTypes.addElement( new ComboItem(ConfigParamType.sleep_detection_time.index, "sleep_detection_time" ) );
        modelArgumentTypes.addElement( new ComboItem(ConfigParamType.sleep_threshold_acceleration.index, "sleep_threshold_acceleration" ) );
        modelArgumentTypes.addElement( new ComboItem(ConfigParamType.shock_threshold_acceleration.index, "shock_threshold_acceleration" ) );
        modelArgumentTypes.addElement( new ComboItem(ConfigParamType.rest_threshold_acceleration.index, "rest_threshold_acceleration" ) );
        modelArgumentTypes.addElement( new ComboItem(ConfigParamType.rest_detection_time.index, "rest_detection_time" ) );
        modelArgumentTypes.addElement( new ComboItem(ConfigParamType.accelerometer_status_reg.index, "accelerometer_status_reg" ) );
        modelArgumentTypes.addElement( new ComboItem(ConfigParamType.vcc_voltage.index, "vcc_voltage" ) );
        modelArgumentTypes.addElement( new ComboItem(ConfigParamType.vcc_nominal_voltage.index, "vcc_nominal_voltage" ) );
        modelArgumentTypes.addElement( new ComboItem(ConfigParamType.charging_status.index, "charging_status" ) );
        
        cboxArgumentType.setModel(new DefaultComboBoxModel(modelArgumentTypes));
        
        selectedCommand = (ComboItem)cboxCommand.getSelectedItem();
        selectedArgument = (ComboItem)cboxArgumentType.getSelectedItem();
        
        this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                if(communicator != null && communicator.isConnected())
                    communicator.disconnect();

                writeNewFileToOutputDirectory(packetProcessor.popResult(), fileStarted);
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        cboxPorts = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        cboxCommand = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        sendButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        sendLog = new javax.swing.JTextArea();
        jLabel4 = new javax.swing.JLabel();
        cboxArgumentType = new javax.swing.JComboBox<>();
        txtCommandArg = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        datafileButton = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        outputdirButton = new javax.swing.JButton();
        outputDirDisplay = new javax.swing.JTextField();
        autoButton = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        connectionIndicator = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        lastSavedFileField = new javax.swing.JTextField();
        locLabel = new javax.swing.JLabel();
        selectCommandFileButton = new javax.swing.JButton();
        performCommandsButton = new javax.swing.JButton();

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        cboxPorts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboxPortsActionPerformed(evt);
            }
        });

        jLabel1.setText("COM Port");

        jLabel2.setText("Команда");

        cboxCommand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboxCommandActionPerformed(evt);
            }
        });

        jLabel3.setText("Тип аргумента");

        sendButton.setText("Отправить");
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendButtonActionPerformed(evt);
            }
        });

        sendLog.setColumns(20);
        sendLog.setRows(5);
        jScrollPane2.setViewportView(sendLog);

        jLabel4.setText("Лог отправки:");

        cboxArgumentType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboxArgumentTypeActionPerformed(evt);
            }
        });

        jLabel5.setText("Значение");

        jCheckBox1.setText("Из файла");
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        datafileButton.setText("Выбрать файл ...");
        datafileButton.setEnabled(false);
        datafileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                datafileButtonActionPerformed(evt);
            }
        });

        jLabel6.setText("Папка для сохранения файлов : ");

        outputdirButton.setText("Выбрать ...");
        outputdirButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputdirButtonActionPerformed(evt);
            }
        });

        outputDirDisplay.setEditable(false);

        autoButton.setText("Авто");
        autoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoButtonActionPerformed(evt);
            }
        });

        jLabel7.setText("Статус соединения : ");

        connectionIndicator.setText("disconnected");
        connectionIndicator.setEnabled(false);

        jLabel8.setText("Последний сохраненый файл :");

        lastSavedFileField.setEditable(false);

        locLabel.setText("        ");

        selectCommandFileButton.setText("Открыть файл с командами настройки");
        selectCommandFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectCommandFileButtonActionPerformed(evt);
            }
        });

        performCommandsButton.setText("Выполнить команды");
        performCommandsButton.setEnabled(false);
        performCommandsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                performCommandsButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboxPorts, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(datafileButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cboxCommand, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2))
                                .addGap(20, 20, 20)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cboxArgumentType, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel3)
                                    .addComponent(performCommandsButton))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtCommandArg)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jCheckBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel5))
                                        .addGap(0, 79, Short.MAX_VALUE))))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addGap(24, 24, 24)
                                .addComponent(outputDirDisplay))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(autoButton, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addComponent(jLabel7)
                                    .addGap(18, 18, 18)
                                    .addComponent(connectionIndicator, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel8)
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
                            .addComponent(jLabel4)
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
                            .addComponent(jLabel1)
                            .addComponent(jCheckBox1)
                            .addComponent(datafileButton)
                            .addComponent(autoButton))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(connectionIndicator))
                        .addGap(48, 48, 48))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel5))
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
                    .addComponent(jLabel8)
                    .addComponent(lastSavedFileField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(locLabel))
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void sendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendButtonActionPerformed
        // Отправка команды
        final String argval = txtCommandArg.getText();
        final int fb = selectedCommand.getVal();

        lastArguments.put(fb, argval);

        if(fb == accRegRead.getFirstByte()) {
            int regAdress = Integer.parseInt(argval);
            probeCommander.deviceAccRegRead(regAdress);
        }
        else if (fb == accRegWrite.getFirstByte()) {
            String[] values = argval.split(" ");
            int regAdress = Integer.parseInt(values[0]);
            int regValue = Integer.parseInt(values[1]);
            probeCommander.deviceAccRegWrite(regAdress, regValue);
        }
        else if (fb == readConfig.getFirstByte()) {
            ConfigParamType selected = selectedParamType();
            probeCommander.deviceReadConfig(selected);
        }
        else if (fb == setTimeHi.getFirstByte()) {
            DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.ENGLISH);
            try {
                Date result = df.parse(argval);
                probeCommander.deviceSetTime(result);
            } catch (ParseException ex) {
                addErrorLine("Неверный формат даты");
            }
        }
        else if (fb == writeConfig.getFirstByte()) {
            ConfigParamType selected = selectedParamType();
            try {
                probeCommander.deviceWriteConfig(selected, (Object) argval);
            }  catch (NumberFormatException ex) {
                addErrorLine("Неверный формат aргумента: <" + argval + ">");
            }
        }
        
        int output;
        String line = "";
        List<Byte> bytes = new ArrayList<>();
        
        while((output = probeCommander.popOutputByte()) != -1) {
            bytes.add((byte)output);
            line += String.format("%02X ", output);
        }
        
        if(bytes.size() > 0 && bytes.size() % 4 == 0) {
            byte[] arrBytes = new byte[bytes.size()];
            for (int i =0; i < arrBytes.length; i++)
                arrBytes[i] = bytes.get(i);
            if (communicator != null)
                communicator.writeData(arrBytes);
            addNormalLine(line);
        }
        else if (bytes.size() > 0){
            addErrorLine(line + " cannot be sent");
        }
    }//GEN-LAST:event_sendButtonActionPerformed

    private  ConfigParamType selectedParamType() {
        ConfigParamType cptype = ConfigParamType.acc_odr;
                
        for (ConfigParamType nextType : ConfigParamType.values()) {
            if(selectedArgument.getVal() == nextType.index) {
                cptype = nextType;
                break;
            }
        }

        return cptype;
    }
    
    private void cboxArgumentTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboxArgumentTypeActionPerformed
        // Изменен тип аргумента
        JComboBox comboBox = (JComboBox)evt.getSource();
        selectedArgument = (ComboItem)comboBox.getSelectedItem();
    }//GEN-LAST:event_cboxArgumentTypeActionPerformed

    private void cboxPortsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboxPortsActionPerformed
        // Выбран порт, пробуем подключиться к нему
        String port = String.valueOf(cboxPorts.getSelectedItem());
        
        if(communicator.isConnected()) {
            communicator.disconnect();
            changeStatus(ConnectionStatus.Disconnected);
        }
        
        int result = communicator.connect(port);
        
        if(Communicator.CONNECTION_OK == result) {
            addNormalLine("Connection OK");
            changeStatus(ConnectionStatus.Connected);
            parser.setListener(statusEventListener);
            statusEventListener.startCheck();
        }
        else if(Communicator.CONNECTION_FAILED == result) {
            addErrorLine("FAILED to open " + port);
        }
        else if(Communicator.CONNECTION_CONNECTED == result) {
            addNormalLine(port + " is in use.");
        }
         
        if (communicator.isConnected()) {
            if (communicator.initIOStream()) {
                communicator.initListener();
            }
        }
    }//GEN-LAST:event_cboxPortsActionPerformed

    private void cboxCommandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboxCommandActionPerformed
        // Измененен тип сообщения
        JComboBox comboBox = (JComboBox)evt.getSource();
        selectedCommand = (ComboItem)comboBox.getSelectedItem();
        
        if(selectedCommand.getVal() != writeConfig.getFirstByte() && selectedCommand.getVal() != readConfig.getFirstByte()) {
            cboxArgumentType.setEnabled(false);
        }
        else {
            cboxArgumentType.setEnabled(true);
        }

        if (selectedCommand.getVal() == setTimeHi.getFirstByte())
        {
            String date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.ENGLISH).format(new Date());
            txtCommandArg.setText(date);
        }
        else if (lastArguments.containsKey(selectedCommand.getVal())) {
            txtCommandArg.setText(lastArguments.get(selectedCommand.getVal()));
        }
    }//GEN-LAST:event_cboxCommandActionPerformed

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        dataFromFile = ((JCheckBox)evt.getSource()).isSelected();
        
        writeNewFileToOutputDirectory(packetProcessor.popResult(), fileStarted);
        fileStarted = false;
        
        datafileButton.setEnabled(dataFromFile);
        cboxPorts.setEnabled(!dataFromFile);
        cboxCommand.setEnabled(!dataFromFile);
        cboxArgumentType.setEnabled(!dataFromFile);
        txtCommandArg.setEnabled(!dataFromFile);
        sendButton.setEnabled(!dataFromFile);
        autoButton.setEnabled(!dataFromFile);
        
        if(!dataFromFile) {
            parser.setListener(statusEventListener);
            statusEventListener.startCheck();
        }
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void datafileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_datafileButtonActionPerformed
        // Выбрать файл для чтения
        final JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(ProbeGUI.this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            byte [] fileData;
            
            try {
                fileData = Files.readAllBytes(file.toPath());
            } catch (IOException ex) {
                Logger.getLogger(ProbeGUI.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
            
            parser.setListener(new ParserEventListener());
            packetProcessor.popResult();
            
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            for (int b: fileData) {
                parser.addByte(b);
            }
            
            // Записать результат парсинга в файл
            writeNewFileToOutputDirectory(packetProcessor.popResult(), false);
            setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_datafileButtonActionPerformed

    private void writeNewFileToOutputDirectory(String data, boolean append) {
        if (data.isEmpty())
            return;
        
        File actualFile;
        
        if(!append) {
            String dirName = outputDirectory;
            String fileName = "accel-" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".txt";
            File dir  = new File (dirName);
            actualFile = new File (dir, fileName);
        }
        else {
            actualFile = new File(getLastFileName());
        }

        try {
            if(!append)
                actualFile.createNewFile();
            
            FileWriter fw = new FileWriter(actualFile);
            fw.write(data);
            fw.close();

            if(!append) {
                String fullfilename = actualFile.getAbsolutePath();            
                lastSavedFileField.setText(fullfilename);
                saveLastFileName(fullfilename);
            }
            
            locCounter += data.split("\r\n|\r|\n").length;
            locLabel.setText(Integer.toString(locCounter) + " строк");
        } catch (IOException ex) {
            Logger.getLogger(ProbeGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void outputdirButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputdirButtonActionPerformed
        final JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fc.showSaveDialog(ProbeGUI.this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File folder = fc.getSelectedFile();
            outputDirectory = folder.getPath();
            outputDirDisplay.setText(outputDirectory);
            saveOutputDirectory(outputDirectory);
        }
    }//GEN-LAST:event_outputdirButtonActionPerformed

    private void autoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoButtonActionPerformed
        // Начать сканирование портов и найти из них подходящий
        if(communicator.getAvailableComPorts().size() > 0) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            enableControls(false);
            checkAllPorts();
        }
    }//GEN-LAST:event_autoButtonActionPerformed

    private void selectCommandFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectCommandFileButtonActionPerformed
        // Выбрать файл с коммандами
        String configDir = getConfigFileDirectory();    // предыдущая директория
        commands.clear();
        
        final JFileChooser fc = new JFileChooser(configDir);
        int returnVal = fc.showOpenDialog(ProbeGUI.this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            addNormalLine("CONFIG : " + file.getAbsolutePath());
            
            String path = file.getParent();
            if(path != null) {
                saveConfigFileDirectory(path);
            }
            
            try {
                List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
                
                for(String line : lines) {
                    if(line.contains("CRLF")) {
                        String [] parts = line.split("CRLF");
                        
                        for(String part : parts) {
                            commands.add(part);
                        }
                    }
                    else {
                        commands.add(line);
                    }
                }

                for (int i = 0; i < commands.size(); i++) {
                    if(commands.get(i).contains("//"))
                        commands.set(i, commands.get(i).split("//")[0]);
                }                
            } catch (IOException ex) {
                Logger.getLogger(ProbeGUI.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }  
            
            performCommandsButton.setEnabled(true);
        }
    }//GEN-LAST:event_selectCommandFileButtonActionPerformed

    private void performCommandsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_performCommandsButtonActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        // Послать команды в устройство
        for(String command : commands) {
            String [] byte_codes = command.split(" ");
            
            for(String byte_code : byte_codes) {
                if(byte_code.length() > 0 ) {
                    byte nextByte = Byte.parseByte(byte_code, 16);
                    communicator.writeData(new byte [] {nextByte});
                }
            }
            addNormalLine(command.trim());
        }
        
        performCommandsButton.setEnabled(false);
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_performCommandsButtonActionPerformed

    private void checkAllPorts() {
        communicator.disconnect();
        parser.setListener(testEventListener);        
        portsToCheck = new ArrayDeque<>();
        portsToCheck.addAll(communicator.getAvailableComPorts().keySet());
        checkNextPort();
    }
    
    private void checkNextPort() {
        String port = portsToCheck.peek();
        
        if(port != null) {
            // проверить что:
            // 1. порт открывается и с него поступают данные
            // 2. накопить некоторое количество данных (100 байт) и с помощью нового ProbeUsbParser проверить, что в них есть пакеты фреймов хоть какого-нибудь типа, кроме Dangling/Single Frame.
            // при выполнении условий - вернуть true
            if(communicator.connect(port) != Communicator.CONNECTION_OK)
                onCheckFail();
            
            if (communicator.isConnected()) {
                if (communicator.initIOStream()) {
                    communicator.initListener();
                }
            }

            testEventListener.startCheck();
        }
    }
    
    public void onCheckOK() {
        String port = portsToCheck.pop();
        parser.setListener(statusEventListener);
        statusEventListener.startCheck();
        setCursor(Cursor.getDefaultCursor());
        cboxPorts.setSelectedItem(port);
        enableControls(true);
    }
    
    public void onCheckFail() {
        communicator.disconnect();
        portsToCheck.pop();
        if(portsToCheck.peek() != null) {
            checkNextPort();
        }
        else {
            enableControls(true);
            setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(this, "Устройство не найдено");
        }
    }
    
    private void changeStatus(ConnectionStatus status) {
        if(status == ConnectionStatus.Disconnected) {
            connectionIndicator.setForeground(Color.red);
            connectionIndicator.setText("DISCONNECTED");
            this.status = status;
        }
        else if(status == ConnectionStatus.Connected) {
            connectionIndicator.setForeground(Color.black);
            connectionIndicator.setText("CONNECTED");
            this.status = status;
        }
        else if(status == ConnectionStatus.Reading) {
            if(this.status == ConnectionStatus.Connected) {
                connectionIndicator.setForeground(new Color(0.4f, 0.7f, 0.6f));
                connectionIndicator.setText("READING BYTES ...");
                this.status = status;
            }
        }
        else if(status == ConnectionStatus.Data) {
            connectionIndicator.setForeground(new Color(0.4f, 0.7f, 0.6f));
            connectionIndicator.setText("DATA ...");
            this.status = status;
            
            // Тут сразу скидывать в файл и обновлять счетчик строк
            String result = packetProcessor.popResult();
            if( result.length() > 0) {
                writeNewFileToOutputDirectory(result, fileStarted);
                fileStarted = true;
            }
        }
        else if(status == ConnectionStatus.Idle) {
            if(this.status == ConnectionStatus.Data) {
                connectionIndicator.setForeground(Color.black);
                connectionIndicator.setText("IDLE");
                this.status = status;
            }
        }
    }
    
    public void onStatusReading() {
        changeStatus(ConnectionStatus.Reading);
    }
    
    public void onStatusGotData() {
        changeStatus(ConnectionStatus.Data);
    }
            
    public void onStatusIdle() {
        changeStatus(ConnectionStatus.Idle);
    }

    private void addErrorLine(String line) {
        sendLog.append("ERROR : " + line + "\n");        
    }
    
    private void addNormalLine(String line) {
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
        String defaultValue = System.getProperty("нет");
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
        cboxCommand.setEnabled(enable);
        cboxArgumentType.setEnabled(enable);
        txtCommandArg.setEnabled(enable);
        sendButton.setEnabled(enable);
        autoButton.setEnabled(enable);
    }
        
    /**
     * @param args the command line arguments
     */
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
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ProbeGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ProbeGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ProbeGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ProbeGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        instance = new ProbeGUI();
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                instance.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton autoButton;
    private javax.swing.JComboBox<String> cboxArgumentType;
    private javax.swing.JComboBox<String> cboxCommand;
    private javax.swing.JComboBox<String> cboxPorts;
    private javax.swing.JButton connectionIndicator;
    private javax.swing.JButton datafileButton;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField lastSavedFileField;
    private javax.swing.JLabel locLabel;
    private javax.swing.JTextField outputDirDisplay;
    private javax.swing.JButton outputdirButton;
    private javax.swing.JButton performCommandsButton;
    private javax.swing.JButton selectCommandFileButton;
    private javax.swing.JButton sendButton;
    private javax.swing.JTextArea sendLog;
    private javax.swing.JTextField txtCommandArg;
    // End of variables declaration//GEN-END:variables
}

interface CheckListener {
    public void onCheckOK();
    public void onCheckFail();
}

class TestParserEventListener extends ParserEventListener implements ActionListener {
    
    private CheckListener listener; 
    
    private boolean gotFrames = false;
    
    public TestParserEventListener(CheckListener listener) {
        this.listener = listener;
    }
    
    public void startCheck() {
        gotFrames = false;
        Timer timer = new Timer( 2000, this);
        timer.setRepeats(false);
        timer.start(); 
    }
    
    public void actionPerformed(ActionEvent e) {
        if(gotFrames) {
            listener.onCheckOK();
            return;
        }
                
        listener.onCheckFail();
    }
    
    public void onNewByte(final int b) {
    }
    public void onSync(final int[] bytes, final int nBytesInSync) {}
    public void onNewFrame(final int b1, final int b2) {
        gotFrames = true;
    }
    public void onNewDataPacket(final int[] packetData) {}
    public void onNewTimePacket(final int[] packetData) {}
    public void onNewSingleFrame(final int b1, final int b2) {}
    public void onExpectNewPacket(final FramePacket p) {}
    public void onPacketDataByte(final FramePacket p) {}
}

interface StatusListener {
    public void onStatusReading();
    public void onStatusGotData();
    public void onStatusIdle();
}

class StatusParserEventListener extends ParserEventListener implements ActionListener {
    
    private StatusListener listener; 
    private boolean checking  = false;
    private boolean gotFrames = false;
    
    public StatusParserEventListener(StatusListener listener) {
        this.listener = listener;
    }
    
    public void startCheck() {
        if(!checking) {
            gotFrames = false;
            checking = true;
            Timer timer = new Timer( 1000, this);
            timer.setRepeats(false);
            timer.start(); 
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        if(!gotFrames) {
            listener.onStatusIdle();
        }
        
        checking = false;
        startCheck();
    }
    
    public void onNewByte(final int b) {
        listener.onStatusReading();
    }
    public void onSync(final int[] bytes, final int nBytesInSync) {}
    public void onNewFrame(final int b1, final int b2) {
        listener.onStatusGotData();
        gotFrames = true;
    }
    public void onNewDataPacket(final int[] packetData) {}
    public void onNewTimePacket(final int[] packetData) {}
    public void onNewSingleFrame(final int b1, final int b2) {}
    public void onExpectNewPacket(final FramePacket p) {}
    public void onPacketDataByte(final FramePacket p) {}    
}

class ComboItem {

    private int     val;
    private String  description;

    public ComboItem(int val, String description) {
        this.val = val;
        this.description = description;
    }
    public int getVal() {
    return val;
  }
    public String getDescription() {
    return description;
  }

    @Override
    public String toString() {
    return description;
  }
}
