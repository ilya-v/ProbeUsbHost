package com.probe.usb.host.pc.controller;


import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OutputWriter {

    protected String outputDir = "";
    protected String
            fileNamePrefix = "",
            fileNameSuffix = ".dat";
    protected Date lastWriteTime = new Date();
    protected Date lastFlushTime = new Date();
    protected int fileDelayMs = 1000;
    protected int flushDelayMs = 1000;
    protected String currentFileName;

    protected FileOutputStream fileOutputStream;
    protected long count = 0;

    protected boolean enabled = false;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public OutputWriter setOutputDir(final String outputDir) {
        this.outputDir = outputDir;
        return this;
    }
    public OutputWriter setFileName(final String fileNamePrefix, final String fileNameSuffix) {
        this.fileNamePrefix = fileNamePrefix;
        this.fileNameSuffix = fileNameSuffix;
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

    public boolean write(final byte[] data) {
        if (data == null || data.length == 0 || !enabled)
            return data != null && data.length == 0;
        try {
            tick();
            if (fileOutputStream == null)
                openFile();
            fileOutputStream.write(data);
            lastWriteTime = new Date();
            count += data.length;

            if (lastFlushTime.getTime() + flushDelayMs < new Date().getTime()) {
                fileOutputStream.flush();
                lastFlushTime = new Date();
            }
        } catch (IOException e) {
            return false;
        }
        return true;
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
        fileOutputStream = new FileOutputStream(currentFileName);
        count = 0;
    }

    public String getCurrentFileName() {
        return currentFileName;
    }
    public long getCurrentByteCount() { return count; }

    protected void closeFile() throws IOException {
        if (fileOutputStream == null)
            return;
        fileOutputStream.flush();
        fileOutputStream.close();
        fileOutputStream = null;
        currentFileName = null;
    }

    protected String makeFileName() {
        String fileName = fileNamePrefix
                + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date())
                + fileNameSuffix;
        final File dir  = new File (outputDir);
        return new File (dir, fileName).getAbsolutePath();
    }
}
