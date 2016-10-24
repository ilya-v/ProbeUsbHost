package com.probe.usb.host.pc.controller;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class CommandSender {

    public interface DataReceiver { boolean writeData(byte[] bytes); }
    private DataReceiver dataReceiver;

    public CommandSender setDataReceiver(DataReceiver dataReceiver) {
        this.dataReceiver = dataReceiver;
        return this;
    }

    public interface Logger { void printLogLine(String line);  }
    private Logger logger;
    public CommandSender setLogger(Logger logger) {
        this.logger = logger;
        return this;
    }

    public interface InputByteSource { int popByte(); }
    private InputByteSource inputByteSource;
    public CommandSender setInputByteSource(InputByteSource inputByteSource) {
        this.inputByteSource = inputByteSource;
        return this;
    }

    public void onNewInputBytes() {
        String line = "";
        ArrayList<Byte> bytes = new ArrayList<>();

        for (int output, i = 0; (output = inputByteSource.popByte()) != -1; i++) {
            bytes.add((byte) output);
            line += String.format("%02X ", Byte.toUnsignedInt(bytes.get(bytes.size()-1))) + (i%4 == 0? "\n":"");
        }
        if (bytes.size() % 4 != 0) {
            logger.printLogLine("Error: " + line + " cannot be sent: wrong byte count");
            return;
        }

        while (!bytes.isEmpty()) {
            commands.add( new byte[] { bytes.get(0), bytes.get(1), bytes.get(2), bytes.get(3) } );
            bytes.subList(0, 4).clear();
        }
    }

    private Deque<byte[]> commands = new ArrayDeque<>();

    public void onTimerTick() {
        byte[] bytes = commands.pollFirst();
        if (bytes == null)
            return;

        String logLine = "";
        for (byte b: bytes)
            logLine += String.format("%02X ", b);

        logLine = logLine.trim();
        if (!dataReceiver.writeData(bytes)) {
            logLine = "Cannot send " + logLine;
            commands.clear();
        }
        logger.printLogLine(logLine);
    }

    public boolean parseAndSendBytes(List<String> lines) {

        List<byte[]> newCommands = new ArrayList<>();

        for (String  line : lines) {
            final String[] cmdBytes = line.split("//", 1)[0].trim().split("\\s+");
            if (cmdBytes.length == 0)
                continue;

            byte[] bytes = new byte[cmdBytes.length];
            int i = 0;
            for (String b: cmdBytes) {
                try {
                    bytes[i] = Byte.parseByte(b, 16);
                }
                catch (NumberFormatException e) {
                    logger.printLogLine(getClass().getSimpleName()
                            +": Error while parsing " + line + ": in " + b);
                    return false;
                }
                i++;
            }
            newCommands.add(bytes);
        }
        commands.addAll(newCommands);
        return true;
    }
}
