package com.probe.usb.host.pc;

import com.probe.usb.host.commander.ProbeUsbCommander;
import com.probe.usb.host.parser.ParserEventListener;
import com.probe.usb.host.parser.ProbeUsbParser;
import com.probe.usb.host.parser.processor.PlotProcessor;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import javax.swing.*;
import javax.swing.Timer;

import com.probe.usb.host.pc.controller.CommandSender;
import com.probe.usb.host.pc.controller.OutputController;
import com.probe.usb.host.pc.controller.PlotController;
import com.probe.usb.host.pc.plot.Plot;
import com.probe.usb.host.pc.ui.LookAndFeel;
import com.probe.usb.host.pc.ui.controller.*;
import com.probe.usb.host.pc.controller.PortScanner;
import com.probe.usb.host.pc.ui.frame.ProbeMainWindow;

import java.awt.event.ActionEvent;
import java.nio.charset.StandardCharsets;

public class ProbeGUI {

    public static ProbeGUI instance;
    public static Plot plot;

    private ProbeMainWindow ui = new ProbeMainWindow();

    private Preferences preferences = new Preferences();

    private Logger logger = new Logger() {
        @Override
        void printLine(String line) { printToWindowLog(line);}
    };


    private StatusListener statusEventListener = new StatusListener();

    private ParserEventListener bytesAndFramesListener = new ParserEventListener() {
        @Override public void onNewByte(final int b) {
            statusEventListener.onNewByte(b);
            portScanner.onNewByte(b);
            outputController.addNewByte(b);
        }
        @Override public void onNewFrame(final int b1, final int b2) {
            statusEventListener.onNewFrame(b1, b2);
            portScanner.onNewFrame(b1, b2);
        }
    };


    private PlotController plotController = new PlotController()
            .setPlotDataListener(new PlotController.PlotDataListener() {
                @Override
                public void setPlotData(List<Plot.Point> px, List<Plot.Point> py, List<Plot.Point> pz, List<Double> verticalLines) {
                    plotUiController.setPlotData(px, py, pz, verticalLines);
                }
                @Override  public void setPlotBounds(double x0, double y0, double xz, double yz) {
                    plotUiController.setPlotBounds(x0, y0, xz, yz);
                }
                @Override public void reset() {
                    plotUiController.reset();
                }
            });

    private PlotProcessor plotProcessor = new PlotProcessor().setPointsListener(plotController::onNewPoint);

    private ProbeUsbParser parser = new ProbeUsbParser()
            .setListener(bytesAndFramesListener)
            .addPacketProcessor(plotProcessor)
            ;

    private Communicator communicator = new Communicator()
            .setLogger(logger::printLine)
            .setListener(statusEventListener::onConnectionStatus)
            .setReceiver(b -> {
                parser.addByte(Byte.toUnsignedInt(b));
            });

    private ProbeUsbCommander probeCommander = new ProbeUsbCommander();

    private CommandSender commandSender = new CommandSender()
            .setLogger(logger::printLine)
            .setInputByteSource(probeCommander::popOutputByte)
            .setDataReceiver(communicator::writeData);

    private CommandUiController commandUiController = new CommandUiController(
                ui.cboxCommand, ui.cboxArgumentType, ui.txtCommandArg, ui.sendButton)
            .setCommander(probeCommander)
            .setCommandListener(cmd -> { commandSender.onNewInputBytes(); });

    private PortScanUiController portScanUiController = new PortScanUiController(
                ui.cboxPorts, ui.autoButton, ui.connectionIndicator)
            .setLogger(logger::printLine)
            .setConnectionCommandListener( (port, action) -> {
                if(action) communicator.connect(port); else communicator.disconnect();});

    private PortScanner portScanner = new PortScanner(communicator)
            .setLogger(logger::printLine)
            .setEnabledInfo( () ->  ui.autoButton.isSelected())
            .setStatusListener( portScanUiController::onConnectedEvent );

    private OutputController outputController = new OutputController()
            .setPreferences(preferences)
            .setParser(parser)
            .setLogger(logger::printLine);

    class Feeder implements Runnable {
        byte[] bytes;
        int pos = 0;
        final int chunkSize = 1000;
        public Feeder(byte[] bytes) {
            this.bytes = bytes;
        }
        @Override
        public void run() {
            final int posStop = Math.min(pos + chunkSize, bytes.length);
            while (pos < posStop) {
                parser.addByte(Byte.toUnsignedInt(bytes[pos]));
                pos ++;
            }

            if (pos < bytes.length)
                SwingUtilities.invokeLater(this);
        }

    }

    InputFileUiController inputFileUiController = new InputFileUiController(
                ui.btnInputFromFile, ui.btnChooseInputFile, ui.txtInputDataFile)
            .setPreferences(preferences)
            .setStateListener(active -> {
                commandUiController.setEnabled(!active);
                portScanUiController.setEnabled(!active);
            })
            .setOpenFileListener(filePath -> {
                byte [] fileData = new byte[0];
                try {
                    fileData = Files.readAllBytes(Paths.get(filePath));
                } catch (IOException ex) { logger.printLine(ex.toString()); }

                SwingUtilities.invokeLater(new Feeder(fileData));

                /*for (int b: fileData) {
                    parser.addByte(b);
                }*/
                plotController.resetPlot();
                outputController.forceNewFiles();
            });

    Timer timer = new Timer(1000, e -> {
        communicator.onTimerTick();
        portScanner.onTimerTick();
        statusEventListener.onTimerTick();
        commandSender.onTimerTick();
        outputController.tick();
        plotController.tick();
    });


    private OutputSettingsUiController outputSettingsUiController = new OutputSettingsUiController(
            preferences,
            ui.btnChooseOutputDir,
            ui.txtOutputDir,
            ui.txtRawFile, ui.txtAccFile, ui.txtMessagesFile,
            ui.lblRaw, ui.lblAcc, ui.lblMessages,
            ui.btnRaw, ui.btnAcc, ui.btnMessages
            )
        .setOutputController( outputController );

    {
        communicator.setPortUpdateListener(ports -> {
            portScanUiController.onPortsUpdateEvent(ports);
            portScanner.onPortsUpdate(ports);
        });

        statusEventListener.setConnectionStatusListener(status ->
            portScanUiController.onConnectionStatusChanged(status));

        outputController.setFileStatusListener(outputSettingsUiController::onFileStatus);

        plot.setLogger(logger::printLine);
        timer.start();
    }

    private PlotUiController plotUiController = new PlotUiController(plot,
            ui.btnPlotPictures, ui.plotSliderX, ui.plotSliderY)
            .setPlotController(plotController);

    public ProbeGUI() {
        ui.plotButton.addActionListener(evt -> plot.setVisible(ui.plotButton.isSelected()) );
        ui.btnChooseCommandFile.addActionListener(this::selectCommandFileButtonActionPerformed);

        ui.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                communicator.disconnect();
            }
        });
        ui.btnExecuteCommands.addActionListener(this::selectCommandFileButtonActionPerformed);
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

    private void addErrorLine(String line) {
        ui.sendLog.append("ERROR : " + line + "\n");
    }
    
    private void printToWindowLog(String line) {
        if (line.isEmpty()) return;
        ui.sendLog.append(line + "\n");
    }
        

    public static void main(String args[]) {
        LookAndFeel.init();
        plot = new Plot();
        instance = new ProbeGUI();
    }

}
