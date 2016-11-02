package com.probe.usb.host.pc;

import com.probe.usb.host.bus.Bus;
import com.probe.usb.host.commander.ProbeUsbCommander;
import com.probe.usb.host.parser.ProbeUsbParser;
import com.probe.usb.host.parser.processor.PlotProcessor;
import com.probe.usb.host.pc.controller.*;
import com.probe.usb.host.pc.controller.event.ComPortConnectCommand;
import com.probe.usb.host.pc.controller.event.EventDispatcher;
import com.probe.usb.host.pc.controller.event.TickEvent;
import com.probe.usb.host.pc.plot.Plot;
import com.probe.usb.host.pc.ui.UiLookAndFeel;
import com.probe.usb.host.pc.ui.controller.*;
import com.probe.usb.host.pc.ui.frame.ProbeMainWindow;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class ProbeGUI {


    public static ProbeGUI instance;
    public static Plot plot;

    private ProbeMainWindow ui = new ProbeMainWindow();

    {
        EventBusSpy.INSTANCE.setActive(true);
        ComPortController.INSTANCE.setActive(true);
        InputFileController.INSTANCE.setActive(true);
        KConnectionStateController.INSTANCE.setActive(true);
        KPortScanner.INSTANCE.setActive(true);
        LoggerUiController.INSTANCE.setActive(true);
        Logger.INSTANCE.setActive(true);
        ParserController.INSTANCE.setActive(true);
        EventDispatcher.INSTANCE.setActive(true);
    }


    private Preferences preferences = new Preferences();

    private PlotController plotController = new PlotController()
            .setPlotDataListener(new PlotController.PlotDataListener() {
                @Override
                public void setPlotData(List<Plot.Point> px, List<Plot.Point> py, List<Plot.Point> pz, List<Double> verticalLines) {
                    plotUiController.setPlotData(px, py, pz, verticalLines);
                }

                @Override
                public void setPlotBounds(double x0, double y0, double xz, double yz) {
                    plotUiController.setPlotBounds(x0, y0, xz, yz);
                }

                @Override
                public void reset() {
                    plotUiController.reset();
                }
            });

    private PlotProcessor plotProcessor = new PlotProcessor().setPointsListener(plotController::onNewPoint);

    private ProbeUsbParser parser = new ProbeUsbParser().addPacketProcessor(plotProcessor);


    private ProbeUsbCommander probeCommander = new ProbeUsbCommander();

    private CommandSender commandSender = new CommandSender();

    private CommandUiController commandUiController = new CommandUiController(
            ui.cboxCommand, ui.cboxArgumentType, ui.txtCommandArg, ui.sendButton)
            .setCommander(probeCommander);

    private PortScanUiController portScanUiController = new PortScanUiController(
            ui.cboxPorts, ui.autoButton, ui.connectionIndicator);

    private OutputController outputController = new OutputController()
            .setPreferences(preferences)
            .setParser(parser);

    InputFileUiController inputFileUiController = new InputFileUiController(
            ui.btnInputFromFile, ui.btnChooseInputFile, ui.txtInputDataFile)
            .setPreferences(preferences);

    Timer timer = new Timer(1000, e -> {
        Bus.post(new TickEvent());
        plotController.tick();
    });


    private OutputSettingsUiController outputSettingsUiController = new OutputSettingsUiController(
            preferences,
            ui.btnChooseOutputDir,
            ui.txtOutputDir,
            ui.txtRawFile, ui.txtAccFile, ui.txtMessagesFile,
            ui.lblRaw, ui.lblAcc, ui.lblMessages,
            ui.btnRaw, ui.btnAcc, ui.btnMessages);
    {
        ParserController.INSTANCE.setParser(parser);
        LoggerUiController.INSTANCE.setLogTextArea(ui.sendLog);
        timer.start();
    }

    private PlotUiController plotUiController = new PlotUiController(plot,
            ui.plotButton, ui.plotSliderX, ui.plotSliderY)
            .setPlotController(plotController);

    public ProbeGUI() {
        ui.btnChooseCommandFile.addActionListener(this::selectCommandFileButtonActionPerformed);

        ui.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                Bus.post(new ComPortConnectCommand(null, false));
            }
        });
        ui.btnExecuteCommands.addActionListener(this::selectCommandFileButtonActionPerformed);
    }


    private void selectCommandFileButtonActionPerformed(ActionEvent evt) {

        final JFileChooser fc = new JFileChooser(preferences.getConfigFileDirectory());
        if (fc.showOpenDialog(ui) != JFileChooser.APPROVE_OPTION)
            return;

        File file = fc.getSelectedFile();
        Bus.post("CONFIG : " + file.getAbsolutePath());

        if (file.getParent() != null)
            preferences.saveConfigFileDirectory(file.getParent());

        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Bus.post("Error: cannot read file " + file);
            return;
        }
        commandSender.parseAndSendBytes(lines);
    }

    public static void main(String args[]) {
        UiLookAndFeel.init();
        plot = new Plot();
        instance = new ProbeGUI();
    }
}
