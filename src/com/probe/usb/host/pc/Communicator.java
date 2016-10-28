/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.probe.usb.host.pc;

import gnu.io.*;

import java.io.InputStream;
import java.util.*;

public class Communicator {

    interface Receiver {  void handle(byte data); }
    interface Logger { void print(String line);  }
    interface Listener { void connectionStatus(boolean connected); }
    interface PortUpdateListener { void onNewPorts(List<String> ports); }

    private Receiver receiver;
    private Logger logger;
    private Listener listener;
    private PortUpdateListener portUpdateListener;

    public Communicator setReceiver(Receiver receiver) {
        this.receiver = receiver;
        return this;
    }
    public Communicator setLogger(Logger logger) {
        this.logger = logger;
        return this;
    }
    public Communicator setListener(Listener listener) {
        this.listener = listener;
        return this;
    }
    public Communicator setPortUpdateListener(PortUpdateListener portUpdateListener) {
        this.portUpdateListener = portUpdateListener;
        return this;
    }

    private SerialPort serialPort = null;
    private Map<String, CommPortIdentifier> portsMap = new HashMap<>();
    final static int TIMEOUT = 2000;


    private void logException(String message, Exception e) {
        if (logger != null)
            logger.print(getClass().getSimpleName() + ": " + message + ": " + e.toString());
    }


    private List<String> searchForPorts() {
        List<String> portList = new ArrayList<>();
        this.portsMap = new HashMap<>();
        Enumeration ports = CommPortIdentifier.getPortIdentifiers();
        while (ports.hasMoreElements()) {
            CommPortIdentifier curPort = (CommPortIdentifier)ports.nextElement();
            if (curPort.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                portList.add(curPort.getName());
                this.portsMap.put(curPort.getName(), curPort);
            }
        }
        return portList;
    }


    public boolean connect(String portName) {
        try {
            disconnect();
            CommPortIdentifier portId = portsMap.get(portName);
            serialPort = (SerialPort) portId.open("ProbeBoardControlPanel", TIMEOUT);
            serialPort.addEventListener(this::serialEvent);
            serialPort.notifyOnDataAvailable(true);
        } catch (Exception e) {
            logException("Cannot connect to a port " + portName, e);
            disconnect();
            return false;
        }
        if (listener != null)
            listener.connectionStatus(true);
        return true;
    }

    public void disconnect()  {
        if (serialPort == null)
            return;
        try {
            serialPort.removeEventListener();
            serialPort.getInputStream().close();
            serialPort.getOutputStream().close();
            serialPort.close();
        } catch (Exception e) {
            logException("Cannot disconnect from a port " + serialPort.getName(), e);
        }
        serialPort = null;
        listener.connectionStatus(false);
    }

    public void serialEvent(SerialPortEvent evt) {
        if (evt.getEventType() != SerialPortEvent.DATA_AVAILABLE || serialPort == null)
            return;

        List<Integer> bytes = new ArrayList<>();

        try   {
            InputStream is = serialPort.getInputStream();
            while (is.available() > 0) {
                final int b = is.read();
                if (b != -1)
                    bytes.add(b);
            }
        }
        catch (Exception e)  {
            logException("Cannot read data from a serial port", e);
        }

        if (receiver != null)
            for (int b: bytes)
                receiver.handle((byte)b);
    }

    public boolean writeData(byte [] data) {
        if(serialPort == null)
            return false;
        
        try  {
            serialPort.getOutputStream().write(data);
            serialPort.getOutputStream().flush();
        }
        catch (Exception e)   {
            logException("Cannot write data to a serial port", e);
            return false;
        }
        return true;
    }

    public String getPortName() {
        return serialPort == null? "" : serialPort.getName();
    }

    public void onTimerTick() {
        if (serialPort != null)
            return;
        Map<String, CommPortIdentifier> oldPorts = this.portsMap;
        List<String> newPorts = searchForPorts();
        boolean changed = false;
        for (String p: newPorts) {
            if (!oldPorts.containsKey(p)) {
                changed = true;
                break;
            }
        }
        if (changed || newPorts.size() != oldPorts.size())
            portUpdateListener.onNewPorts(newPorts);
    }
}

