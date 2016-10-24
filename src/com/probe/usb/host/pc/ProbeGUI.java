package com.probe.usb.host.pc;

import com.probe.usb.host.commander.ProbeUsbCommander;
import com.probe.usb.host.parser.ParserEventListener;
import com.probe.usb.host.parser.ProbeUsbParser;
import com.probe.usb.host.parser.processor.PrintProcessor;
import com.probe.usb.host.parser.processor.PrintProcessor.OutputElement;
import com.probe.usb.host.parser.processor.TablePrintProcessor;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.Timer;;

import com.probe.usb.host.pc.controller.CommandSender;
import com.probe.usb.host.pc.controller.ConnectionStatus;
import com.probe.usb.host.pc.plot.Plot;
import com.probe.usb.host.pc.ui.*;
import com.probe.usb.host.pc.ui.LookAndFeel;
import com.probe.usb.host.pc.ui.frame.ProbeMainFrame;

import java.awt.event.ActionEvent;
import java.nio.charset.StandardCharsets;

public class ProbeGUI {

    public static ProbeGUI instance;
    public static JFrame graph;

    private Preferences preferences = new Preferences();

    private Logger logger = new Logger() {
        @Override
        void printLine(String line) { printToWindowLog(line);}
    };

    private TablePrintProcessor packetProcessor = new TablePrintProcessor();

    private PrintProcessor printProcessor = new PrintProcessor()
            .disableOutputOf(OutputElement.DanglingPacket)
            .disableOutputOf(OutputElement.DataPacket);

    private StatusListener statusEventListener = new StatusListener(this::onConnectionStatusChanged);

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
            .setOutputDir( preferences.getOutputDirectory() );

    private ProbeMainFrame ui = new ProbeMainFrame();

    private PortScanner portScanner = new PortScanner(communicator)
            .setLogger(logger::printLine)
            .setEnabledInfo( () ->  ui.autoButton.isSelected())
            .setStatusListener( (connected, portName) -> {
                ui.cboxPorts.setSelectedItem(connected? portName : "");
            });

    private CommandSender commandSender = new CommandSender()
            .setLogger(logger::printLine)
            .setInputByteSource(probeCommander::popOutputByte)
            .setDataReceiver(communicator::writeData);

    private CommandUiControl commandUiControl = new CommandUiControl(
            ui.cboxCommand, ui.cboxArgumentType, ui.txtCommandArg, ui.sendButton)
            .setCommander(probeCommander)
            .setCommandListener(cmd -> { commandSender.onNewInputBytes(); });

    private ConnectionStatus connectionStatus = ConnectionStatus.Disconnected;

    private Timer timer = new Timer(1000, e -> {
        List<String> portNames = new ArrayList<>();
        if (connectionStatus == ConnectionStatus.Disconnected) {
            portNames = communicator.searchForPorts();
            String[] modelPorts = portNames.toArray(new String[portNames.size() + 1]);
            modelPorts[portNames.size()] = "";

            if (!ui.cboxPorts.isPopupVisible()) {
                final String portSelection = (String) (ui.cboxPorts.getSelectedItem());
                ui.cboxPorts.setModel(new DefaultComboBoxModel<>(modelPorts));
                ui.cboxPorts.setSelectedItem(portSelection);
            }
        }
        portScanner.onTimerTick(portNames);
        statusEventListener.onTimerTick();
        commandSender.onTimerTick();
    } );
    {
        timer.start();
    }

    public ProbeGUI() {

        ui.checkBoxFromDataFile.addActionListener(this::checkBoxFromDataFileActionPerformed);
        ui.cboxPorts.addActionListener(this::cboxPortsActionPerformed);
        ui.datafileButton.addActionListener(this::datafileButtonActionPerformed);
        ui.autoButton.addActionListener(this::autoButtonActionPerformed);
        ui.outputdirButton.addActionListener(this::outputdirButtonActionPerformed);
        ui.plotButton.addActionListener(this::plotButtonActionPerformed);
        ui.selectCommandFileButton.addActionListener(this::selectCommandFileButtonActionPerformed);
        ui.outputDirDisplay.setText(preferences.getOutputDirectory());
        ui.lastSavedFileField.setText(preferences.getLastFileName());

        ui.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                communicator.disconnect();
            }
        });
    }


    private void cboxPortsActionPerformed(ActionEvent evt) {
        if (ui.autoButton.isSelected() || ui.checkBoxFromDataFile.isSelected())
            return;

        if (!communicator.getPortName().isEmpty())
            printToWindowLog("Disconnected from " + communicator.getPortName());
        communicator.disconnect();

        if (ui.cboxPorts.getSelectedItem() == null)
            return;

        String port = String.valueOf(ui.cboxPorts.getSelectedItem());

        if (!port.isEmpty() && communicator.connect(port))
            printToWindowLog("Connected to " + communicator.getPortName());
    }


    private void checkBoxFromDataFileActionPerformed(ActionEvent evt) {
        final boolean dataFromFile = ((JCheckBox) evt.getSource()).isSelected();
        ui.datafileButton.setEnabled(dataFromFile);
        ui.cboxPorts.setEnabled(!dataFromFile);
        commandUiControl.setEnabled(!dataFromFile && connectionStatus != ConnectionStatus.Disconnected);
        ui.autoButton.setEnabled(!dataFromFile);
        ui.autoButton.setSelected(!dataFromFile && ui.autoButton.isSelected());
        if (dataFromFile) {
            if (!communicator.getPortName().isEmpty())
                printToWindowLog("Disconnected from " + communicator.getPortName());
            communicator.disconnect();
        }
    }

    private void autoButtonActionPerformed(ActionEvent evt) {
        ui.cboxPorts.setEnabled( !ui.autoButton.isSelected() );
        if (ui.autoButton.isSelected()) {
            if (!communicator.getPortName().isEmpty())
                printToWindowLog("Disconnected from " + communicator.getPortName());
            communicator.disconnect();
        }
    }

    private void datafileButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_datafileButtonActionPerformed
        // Выбрать файл для чтения
        final JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(ui);

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

        if (!ui.lastSavedFileField.getText().equals(outputWriter.getCurrentFileName())) {
            ui.lastSavedFileField.setText(outputWriter.getCurrentFileName());
            preferences.saveLastFileName(outputWriter.getCurrentFileName());
        }

        ui.locLabel.setText("" + outputWriter.getCurrentLineCount() + " lines");
    }

    
    private void outputdirButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_outputdirButtonActionPerformed
        final JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fc.showSaveDialog(ui);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File folder = fc.getSelectedFile();
            final String outputDirectory = folder.getPath();
            ui.outputDirDisplay.setText(outputDirectory);
            preferences.saveOutputDirectory(outputDirectory);
            outputWriter.setOutputDir(outputDirectory);
        }
    }//GEN-LAST:event_outputdirButtonActionPerformed

    private void plotButtonActionPerformed(ActionEvent evt) {
        graph.setVisible(ui.plotButton.isSelected());
    }

    private void selectCommandFileButtonActionPerformed(ActionEvent evt) {
        
        final JFileChooser fc = new JFileChooser(preferences.getConfigFileDirectory());
        if (fc.showOpenDialog(ui) != JFileChooser.APPROVE_OPTION)
            return;

        File file = fc.getSelectedFile();
        printToWindowLog("CONFIG : " + file.getAbsolutePath());

        if(file.getParent() != null)
            preferences.saveConfigFileDirectory(file.getParent());

        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            addErrorLine("Cannot read file " + file);
            return;
        }
        commandSender.parseAndSendBytes(lines);
    }

    private void onConnectionStatusChanged(ConnectionStatus status) {
        connectionStatus = status;
        commandUiControl.setEnabled( !ui.checkBoxFromDataFile.isSelected() && status != ConnectionStatus.Disconnected );
        ui.connectionIndicator.setForeground(ConnectionStatusUiPropeties.getColor(status));
        ui.connectionIndicator.setText(ConnectionStatusUiPropeties.getText(status));
    }

    private void addErrorLine(String line) {
        ui.sendLog.append("ERROR : " + line + "\n");
    }
    
    private void printToWindowLog(String line) {
        if (line.isEmpty()) return;
        ui.sendLog.append(line + "\n");
    }


    
    private void enableControls(boolean enable) {
        ui.cboxPorts.setEnabled(enable);
        ui.autoButton.setEnabled(enable);
        commandUiControl.setEnabled(enable);
    }
        

    public static void main(String args[]) {
        LookAndFeel.init();
        instance = new ProbeGUI();
        graph = new Plot();
    }
}
