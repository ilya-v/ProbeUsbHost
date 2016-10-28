package com.probe.usb.host.pc.controller;


import java.io.IOException;

public class TextOutputWriter extends OutputWriter {


    protected int lineCount = 0;

    @Override
    public boolean write(final byte[] data)  {

        if (!super.write(data))
            return false;

        final String line = new String(data);

        for (int i = 0; i < line.length(); i++)
            if (line.charAt(i) == '\n')
                lineCount++;
        return true;
    }

    public int getLineCount() {
        return lineCount;
    }

}
