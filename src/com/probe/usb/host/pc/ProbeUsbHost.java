/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.probe.usb.host.pc;

import com.probe.usb.host.pc.Communicator;
import com.probe.usb.host.pc.ProbeGUI;

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
