package com.probe.usb.host.pc.ui;

import com.probe.usb.host.common.ConfigCommand;

class CmdComboItem {
    private ConfigCommand command;
    private String name;

    CmdComboItem(ConfigCommand command) {
        this(command, command.name());
    }

    CmdComboItem(ConfigCommand command, String name) {
        this.command = command;
        this.name = name;
    }

    ConfigCommand getCommand() {
        return command;
    }

    @Override
    public String toString() {
        return name;
    }
}
