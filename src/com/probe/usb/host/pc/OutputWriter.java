package com.probe.usb.host.pc;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OutputWriter {

    protected String outputDir = "";
    protected String fileNamePrefix = "";
    protected Date lastWriteTime = new Date();
    protected Date lastFlushTime = new Date();
    protected int fileDelayMs = 1000;
    protected int flushDelayMs = 1000;
    protected FileWriter writer;
    protected String currentFileName;

    public OutputWriter setOutputDir(final String outputDir) {
        this.outputDir = outputDir;
        return this;
    }
    public OutputWriter setFileNamePrefix(final String fileNamePrefix) {
        this.fileNamePrefix = fileNamePrefix;
        return this;
    }
    public OutputWriter setMaxDelayBetweenFiles(final int fileDelayMs) {
        this.fileDelayMs = fileDelayMs;
        return this;
    }
    public OutputWriter setFlushDelayMs(final int flushDelayMs) {
        this.flushDelayMs = flushDelayMs;
        return  this;
    }

    public void write(final String data) throws IOException {
        if (data.isEmpty())
            return;
        tick();
        if (writer == null)
            openFile();
        writer.write(data);
        lastWriteTime = new Date();

        if (lastFlushTime.getTime() + flushDelayMs < new Date().getTime()){
            writer.flush();
            lastFlushTime = new Date();
        }
    }

    public void forceNewFile() {
        try {
            closeFile();
        } catch (IOException e) { /* do nothing */}
    }

    public void tick() throws IOException {
        if (lastWriteTime.getTime() + fileDelayMs < new Date().getTime())
            closeFile();

    }

    protected void openFile() throws IOException {
        closeFile();
        currentFileName = makeFileName();
        writer = new FileWriter(new File(currentFileName));
    }

    public String getCurrentFileName() {
        return currentFileName;
    }

    protected void closeFile() throws IOException {
        if (writer == null)
            return;
        writer.close();
        writer = null;
        currentFileName = null;
    }

    protected String makeFileName() {
        String fileName = fileNamePrefix + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".txt";
        final File dir  = new File (outputDir);
        return new File (dir, fileName).getAbsolutePath();
    }
}
