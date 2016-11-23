package com.probe.usb.host.pc.controller;

import com.google.common.eventbus.Subscribe;
import com.probe.usb.host.bus.Receiver;
import com.probe.usb.host.parser.ProbeUsbParser;
import com.probe.usb.host.parser.processor.PrintProcessor;
import com.probe.usb.host.parser.processor.TablePrintProcessor;
import com.probe.usb.host.pc.Preferences;
import com.probe.usb.host.pc.controller.event.*;

import java.util.HashMap;
import java.util.Map;

public class OutputController extends Receiver {

    public enum OutputChannel {
        RAW_DATA("raw-", ".bin"),
        ACC_DATA("acc-", ".txt"),
        MSG_DATA("msg-", ".txt"),;

        final public String filePrefix, fileSuffix;

        OutputChannel(String filePrefix, String fileSuffix) {
            this.filePrefix = filePrefix;
            this.fileSuffix = fileSuffix;
        }
    }

    private Preferences preferences;

    public OutputController setPreferences(Preferences preferences) {
        this.preferences = preferences;
        this.outputDir = preferences.getOutputDirectory();
        return this;
    }

    public OutputController setParser(ProbeUsbParser parser) {
        parser.addPacketProcessor(tablePrintProcessor);
        parser.addPacketProcessor(msgPrintProcessor);
        return this;
    }

    private String outputDir;

    @Subscribe
    public void setOutputDir(UiOutputDirEvent outputDirEvent) {
        final String oldDir = outputDir;
        outputDir = outputDirEvent.getDirectory();
        accWriter.setOutputDir(outputDir);
        rawWriter.setOutputDir(outputDir);
        msgWriter.setOutputDir(outputDir);
        if (oldDir == null || !oldDir.equals(outputDir))
            forceNewFiles(null);
    }

    private Map<OutputChannel, Long> counts = new HashMap<>();

    private TablePrintProcessor tablePrintProcessor = new TablePrintProcessor();

    private PrintProcessor msgPrintProcessor = new PrintProcessor()
            .disableOutputOf(PrintProcessor.OutputElement.DanglingPacket)
            .disableOutputOf(PrintProcessor.OutputElement.DataPacket);

    private OutputWriter accWriter = new TextOutputWriter()
            .setFileName(OutputChannel.ACC_DATA.filePrefix, OutputChannel.ACC_DATA.fileSuffix);


    private OutputWriter rawWriter = new OutputWriter()
            .setFileName(OutputChannel.RAW_DATA.filePrefix, OutputChannel.RAW_DATA.fileSuffix);

    private OutputWriter msgWriter = new TextOutputWriter()
            .setFileName(OutputChannel.MSG_DATA.filePrefix, OutputChannel.MSG_DATA.fileSuffix);


    @Subscribe
    public void addBytes(ComPortDataEvent bytes) {
        byte[] accBytes = tablePrintProcessor.popResult().getBytes();
        if (accBytes.length > 0) {
            accWriter.write(accBytes);
            updateFileStatus(OutputChannel.ACC_DATA, accWriter);
        }

        final String line = msgPrintProcessor.popResult();
        if (!line.isEmpty()) {
            msgWriter.write(line.getBytes());
            postEvent(new InfoLogEvent("Received: " + line));
            updateFileStatus(OutputChannel.MSG_DATA, msgWriter);
        }

        rawWriter.write(bytes.getPortData());
        if (bytes.getPortData().length > 0)
            updateFileStatus(OutputChannel.RAW_DATA, rawWriter);
    }


    private void updateFileStatus(OutputChannel chan, OutputWriter writer) {
        final long
                storedN = counts.get(chan) == null ? 0 : counts.get(chan),
                actualN = (writer instanceof TextOutputWriter) ?
                        ((TextOutputWriter) writer).getLineCount() : writer.getCurrentByteCount();
        final String actualName = writer.getCurrentFileName();

        final boolean fileNameChanged = actualName != null
                && !actualName.equals(preferences.getLastFileName(chan.name()));
        if (fileNameChanged)
            preferences.saveLastFileName(chan.name(), actualName);

        if (actualName != null && (actualN != storedN || fileNameChanged)) {
            String fname = actualName;
            if (actualName.startsWith(this.outputDir))
                fname = actualName.substring(this.outputDir.length());
            final boolean slash = fname.startsWith("/") || fname.startsWith("\\");
            fname = fname.substring(slash ? 1 : 0);
            postEvent(new UiOutputFileStatusCommand(chan, fname, Long.toString(actualN)));
        }

        counts.put(chan, actualN);
    }

    @Subscribe
    public void forceNewFiles(NewDataTrackEvent event) {
        accWriter.forceNewFile();
        rawWriter.forceNewFile();
        msgWriter.forceNewFile();
        updateFileStatus(OutputChannel.ACC_DATA, accWriter);
        updateFileStatus(OutputChannel.MSG_DATA, msgWriter);
        updateFileStatus(OutputChannel.RAW_DATA, rawWriter);
    }


    @Subscribe
    public void setChannelEnabled(UiOutputChanEnabledEvent chanEvent) {
        OutputWriter ow =   (chanEvent.getChan() == OutputChannel.ACC_DATA) ? accWriter :
                            (chanEvent.getChan() == OutputChannel.RAW_DATA) ? rawWriter :
                            (chanEvent.getChan() == OutputChannel.MSG_DATA) ? msgWriter :
                                null;
        if (ow != null)
            ow.setEnabled(chanEvent.getEnabled());
    }
}
