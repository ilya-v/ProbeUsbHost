package com.probe.usb.host.pc;

import com.probe.usb.host.commander.ConfigParamType;
import com.probe.usb.host.commander.ProbeUsbCommander;
import com.probe.usb.host.parser.ProbeUsbParser;
import com.probe.usb.host.parser.TablePacketProcessor;
import org.apache.commons.lang.ArrayUtils;

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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author chernov
 */
public class ProbeGUI extends javax.swing.JFrame implements Communicator.Receiver{

    public static ProbeGUI instance;
    
    private Communicator communicator;
    
    private ComboItem selectedCommand;
    private ComboItem selectedArgument;
    
    private ProbeUsbCommander probeCommander = new ProbeUsbCommander();

    private TablePacketProcessor packetProcessor = new TablePacketProcessor();
    private ProbeUsbParser parser = new ProbeUsbParser().addPacketProcessor(packetProcessor);

    private boolean dataFromFile = false;
    
    private final String PREF_OUTPUTDIR_NAME = "preference_outputdir";
    
    private String outputDirectory;

    private Map<Integer, String> lastArguments = new HashMap<>();
    
    
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
        
        outputDirectory = getOutputDirectory();
        outputDirDisplay.setText(outputDirectory);
        
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

                writeNewFileToOutputDirectory(packetProcessor.popResult());
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cboxPorts, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(171, 171, 171)))
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(datafileButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addGap(24, 24, 24)
                                .addComponent(outputDirDisplay))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(cboxCommand, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cboxArgumentType, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel3))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jCheckBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtCommandArg, javax.swing.GroupLayout.PREFERRED_SIZE, 241, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel5))))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(sendButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(outputdirButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cboxPorts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jCheckBox1)
                    .addComponent(datafileButton))
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtCommandArg, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cboxCommand, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cboxArgumentType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(sendButton)))
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(outputdirButton)
                    .addComponent(outputDirDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
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
            if (communicator != null)
                communicator.writeData(ArrayUtils.toPrimitive(bytes.toArray(new Byte[bytes.size()])));
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
        
        if(communicator.isConnected())
            communicator.disconnect();
        
        int result = communicator.connect(port);
        
        if(Communicator.CONNECTION_OK == result) {
            addNormalLine("Connection OK");
        }
        else if(Communicator.CONNECTION_FAILED == result) {
            addErrorLine("FAILED to open " + port);
        }
        else if(Communicator.CONNECTION_CONNECTED == result) {
            addNormalLine(port + " is in use.");
        }
         
        if (communicator.isConnected())
        {
            if (communicator.initIOStream())
            {
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
        
        writeNewFileToOutputDirectory(packetProcessor.popResult());
        
        datafileButton.setEnabled(dataFromFile);
        cboxPorts.setEnabled(!dataFromFile);
        cboxCommand.setEnabled(!dataFromFile);
        cboxArgumentType.setEnabled(!dataFromFile);
        txtCommandArg.setEnabled(!dataFromFile);
        sendButton.setEnabled(!dataFromFile);
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
            
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            for (int b: fileData) {
                parser.addByte(b);
            }
            
            // Записать результат парсинга в файл
            writeNewFileToOutputDirectory(packetProcessor.popResult());
            setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_datafileButtonActionPerformed

    private void writeNewFileToOutputDirectory(String data) {
        if (data.isEmpty())
            return;
        String dirName = outputDirectory;
        String fileName = "accel-" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".txt";
        File dir  = new File (dirName);
        File actualFile = new File (dir, fileName);

        try {
            actualFile.createNewFile();
            FileWriter fw = new FileWriter(actualFile);
            fw.write(data);
            fw.close();
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

    private void addErrorLine(String line) {
        sendLog.append("ERROR : " + line + "\n");        
    }
    
    private void addNormalLine(String line) {
        sendLog.append(line + "\n");        
    }

    private String getOutputDirectory() {
        Preferences prefs = Preferences.userNodeForPackage(ProbeGUI.class);
        String defaultValue = System.getProperty("user.home");;
        return prefs.get(PREF_OUTPUTDIR_NAME, defaultValue);
    }
    
    private void saveOutputDirectory(String outputDirectory) {
        Preferences prefs = Preferences.userNodeForPackage(ProbeGUI.class);
        prefs.put(PREF_OUTPUTDIR_NAME, outputDirectory);
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
    private javax.swing.JComboBox<String> cboxArgumentType;
    private javax.swing.JComboBox<String> cboxCommand;
    private javax.swing.JComboBox<String> cboxPorts;
    private javax.swing.JButton datafileButton;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField outputDirDisplay;
    private javax.swing.JButton outputdirButton;
    private javax.swing.JButton sendButton;
    private javax.swing.JTextArea sendLog;
    private javax.swing.JTextField txtCommandArg;
    // End of variables declaration//GEN-END:variables
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
