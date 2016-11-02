package com.probe.usb.host;

import javax.swing.*;

public class Context {

    public static void invokeUi(Runnable doRun) {
        if (SwingUtilities.isEventDispatchThread())
            doRun.run();
        SwingUtilities.invokeLater(doRun);
    }
}
