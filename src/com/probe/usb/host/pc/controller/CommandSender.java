package com.probe.usb.host.pc.controller;

import com.google.common.eventbus.Subscribe;
import com.probe.usb.host.bus.Bus;
import com.probe.usb.host.bus.Receiver;
import com.probe.usb.host.pc.controller.event.*;

import java.util.*;

public class CommandSender extends Receiver {

    @Subscribe
    public void onNewInputBytes(SendDataToDeviceCommand command) {
        String line = "";
        ArrayList<Byte> bytes = new ArrayList<>();
        int i = 0;
        for (int intb : command.getBytes()) {
            byte b = (byte)intb;
            bytes.add(b);
            i++;
            line += String.format("%02X ", Byte.toUnsignedInt(b));
        }
        if (bytes.size() % 4 != 0) {
            postEvent(new ErrorLogEvent(line + " cannot be sent: wrong byte count"));
            return;
        }

        while (!bytes.isEmpty()) {
            commands.add(new byte[]{bytes.get(0), bytes.get(1), bytes.get(2), bytes.get(3)});
            bytes.subList(0, 4).clear();
        }
    }

    private Deque<byte[]> commands = new ArrayDeque<>();
    private String commandBeingSent = null;
    private Date lastCommandTime = new Date(0L);

    @Subscribe
    public void onTimerTick(TickEvent tickEvent) {
        if (lastCommandTime.getTime() + 1000 > new Date().getTime())
            return;

        commandBeingSent = null;
        byte[] bytes = commands.pollFirst();
        if (bytes == null)
            return;

        String logLine = "";
        for (byte b : bytes)
            logLine += String.format("%02X ", b);

        logLine = logLine.trim();
        commandBeingSent = logLine;
        postEvent(new ComPortWriteCommand(bytes));
        postEvent(new InfoLogEvent(logLine));
        lastCommandTime = new Date();
    }

    @Subscribe
    public void onPortWriteFailure(ComPortWriteFailureEvent writeFailure) {
        if (commandBeingSent != null) {
            postEvent(new ErrorLogEvent("Cannot send " + commandBeingSent + ": " + writeFailure.getMessage()));
            commands.clear();
        }
        commandBeingSent = null;
    }

    public boolean parseAndSendBytes(List<String> lines) {

        List<byte[]> newCommands = new ArrayList<>();

        for (String line : lines) {
            final String[] cmdBytes = line.split("//", 1)[0].trim().split("\\s+");
            if (cmdBytes.length == 0)
                continue;

            byte[] bytes = new byte[cmdBytes.length];
            int i = 0;
            for (String b : cmdBytes) {
                try {
                    bytes[i] = Byte.parseByte(b, 16);
                } catch (NumberFormatException e) {
                    Bus.post(getClass().getSimpleName() + ": Error while parsing " + line + ": in " + b);
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
