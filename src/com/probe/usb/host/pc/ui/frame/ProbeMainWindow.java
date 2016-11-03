package com.probe.usb.host.pc.ui.frame;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;

public class ProbeMainWindow extends JFrame {
    public JComboBox<String> cboxPorts;
    public JToggleButton autoButton;
    public JToggleButton btnInputFromFile;
    public JButton btnChooseInputFile;
    public JTextField txtInputDataFile;
    public JToggleButton btnRaw;
    public JToggleButton btnAcc;
    public JToggleButton btnMessages;
    public JComboBox<String> cboxCommand;
    public JComboBox<String> cboxArgumentType;
    public JTextField txtCommandArg;
    public JButton btnChooseCommandFile;
    public JTextField txtCommandFile;
    public JButton btnExecuteCommands;
    public JButton sendButton;
    public JButton btnChooseOutputDir;
    public JTextField txtRawFile;
    public JTextField txtAccFile;
    public JTextField txtMessagesFile;
    public JTextField txtOutputDir;
    public JTextArea sendLog;
    public JToggleButton plotButton;
    public JLabel connectionIndicator;
    public JPanel mainWindow;
    public JLabel lblAcc;
    public JToggleButton btnPlotPictures;
    public JTextField txtPictures;
    public JLabel lblRaw;
    public JLabel lblMessages;
    public JLabel lblPictures;
    public JSlider plotSliderX;
    public JSlider plotSliderY;
    private JToggleButton fitXToggleButton;
    private JToggleButton fitYToggleButton;
    private JToggleButton btnTrigger;
    private JSlider plotSliderTrig;

    public void createUIComponents() {
        // TODO: place custom component creation code here
    }

    public ProbeMainWindow() {
        super("ProbeUsbHost");

        setContentPane(mainWindow);
        pack();
        setVisible(true);


        DefaultCaret caret = (DefaultCaret)sendLog.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    }

    @Override
    public void paint(Graphics g) {
        final Dimension
                d = getSize(),
                m = getMinimumSize(),
                n = new Dimension(Math.max(m.width, d.width), Math.max(m.height, d.height));

        if (d.width < m.width || d.height < m.height)
            setSize(n);
        else
            super.paint(g);
    }
}
