package com.probe.usb.host.pc.controller;

import com.probe.usb.host.parser.ProbeUsbParser;
import com.probe.usb.host.parser.processor.PrintProcessor;
import com.probe.usb.host.parser.processor.TablePrintProcessor;
import com.probe.usb.host.pc.Preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutputController {

    public enum OutputChannel {
        RAW_DATA("raw-", ".bin"),
        ACC_DATA("acc-", ".txt"),
        MSG_DATA("msg-", ".txt"),
        ;

        final public String filePrefix, fileSuffix;
        OutputChannel(String filePrefix, String fileSuffix) {
            this.filePrefix = filePrefix;
            this.fileSuffix = fileSuffix;
        }
    };

    private Preferences preferences;
    public OutputController setPreferences(Preferences preferences) {
        this.preferences = preferences;
        setOutputDir(preferences.getOutputDirectory());
        return this;
    }

    public OutputController setParser(ProbeUsbParser parser) {
        parser.addPacketProcessor(tablePrintProcessor);
        parser.addPacketProcessor(msgPrintProcessor);
        return this;
    }

    private String outputDir;
    public void setOutputDir(String outputDir) {
        final String oldDir = this.outputDir;
        this.outputDir = outputDir;
        accWriter.setOutputDir(outputDir);
        rawWriter.setOutputDir(outputDir);
        msgWriter.setOutputDir(outputDir);
        if (oldDir == null || !oldDir.equals(outputDir))
            forceNewFiles();
    }

    public interface FileStatusListener {
        void onFileStatus(OutputChannel chan, String fileName, String fileSize);
    }
    private FileStatusListener fileStatusListener;
    public OutputController setFileStatusListener(FileStatusListener listener) {
        this.fileStatusListener = listener;
        return this;
    }

    public interface Logger { void print(String line); }
    private Logger logger;
    public OutputController setLogger(Logger logger) {
        this.logger = logger;
        return this;
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

    private List<Byte> outputBytes = new ArrayList<>();

    public void addNewByte(int b) {
        outputBytes.add((byte)b);
    }


    private void updateFileStatus(OutputChannel chan, OutputWriter writer) {
        final long
                storedN = counts.get(chan) == null? 0 : counts.get(chan),
                actualN = (writer instanceof TextOutputWriter) ?
                        ((TextOutputWriter)writer).getLineCount() :  writer.getCurrentByteCount();
        final String actualName = writer.getCurrentFileName();

        final boolean fileNameChanged = actualName != null
                    && !actualName.equals(preferences.getLastFileName(chan.name()));
        if (fileNameChanged)
            preferences.saveLastFileName(chan.name(), actualName);

        if (actualName != null && (actualN != storedN || fileNameChanged)) {
            String fname = actualName.replaceFirst(this.outputDir, "");
            final boolean slash = fname.startsWith("/") || fname.startsWith("\\");
            fname = fname.substring(slash? 1: 0);
            fileStatusListener.onFileStatus(chan, fname, Long.toString(actualN));
        }

        counts.put(chan, actualN);
    }

    public void forceNewFiles() {
        accWriter.forceNewFile();
        rawWriter.forceNewFile();
        msgWriter.forceNewFile();
        tick();
    }


    public void tick() {
        accWriter.write(tablePrintProcessor.popResult().getBytes());
        updateFileStatus(OutputChannel.ACC_DATA, accWriter);

        final String line = msgPrintProcessor.popResult();
        msgWriter.write(line.getBytes());
        updateFileStatus(OutputChannel.MSG_DATA, msgWriter);
        if (!line.isEmpty())
            logger.print(line);

        byte[] arrBytes = new byte[outputBytes.size()];
        int i = 0;
        for (Byte b: outputBytes) {
            arrBytes[i] = b;
            i++;
        }
        outputBytes = new ArrayList<>();
        rawWriter.write(arrBytes);
        updateFileStatus(OutputChannel.RAW_DATA, rawWriter);
    }

    public void setChannelEnabled(OutputChannel channel, boolean enabled) {
        OutputWriter ow = (channel == OutputChannel.ACC_DATA)? accWriter :
                (channel == OutputChannel.RAW_DATA) ? rawWriter :
                        (channel == OutputChannel.MSG_DATA)? msgWriter :
                                null;
        if (ow != null)
            ow.setEnabled(enabled);
    }
}
