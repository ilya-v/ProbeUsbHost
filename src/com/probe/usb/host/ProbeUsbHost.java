/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.probe.usb.host;

import gnu.io.CommPortIdentifier;
import java.util.Enumeration;
import java.util.HashMap;

/**
 *
 * @author chernov
 */
public class ProbeUsbHost {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Communicator communicator = new Communicator();
        communicator.searchForPorts();
        
        ProbeGUI.main(args);
        
        ProbeGUI.instance.setCommunicator(communicator);
    }
    
}
