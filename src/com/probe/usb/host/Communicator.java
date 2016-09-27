/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.probe.usb.host;

/**
 *
 * @author chernov
 */
import gnu.io.*;
import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TooManyListenersException;

public class Communicator implements SerialPortEventListener
{
    final static int CONNECTION_OK = 1;
    final static int CONNECTION_FAILED = 2;
    final static int CONNECTION_CONNECTED = 3;
    
    //for containing the ports that will be found
    private Enumeration ports = null;
    //map the port names to CommPortIdentifiers
    private HashMap portMap = new HashMap();

    //this is the object that contains the opened port
    private CommPortIdentifier selectedPortIdentifier = null;
    private SerialPort serialPort = null;

    //input and output streams for sending and receiving data
    private InputStream input = null;
    private OutputStream output = null;

    //just a boolean flag that i use for enabling
    //and disabling buttons depending on whether the program
    //is connected to a serial port or not
    private boolean bConnected = false;

    //the timeout value for connecting with the port
    final static int TIMEOUT = 2000;

    //some ascii values for for certain things
    final static int SPACE_ASCII = 32;
    final static int DASH_ASCII = 45;
    final static int NEW_LINE_ASCII = 10;

    //a string for recording what goes on in the program
    private String logText = "";
    private String logFileName = "log.txt";

    //search for all the serial ports
    //pre: none
    //post: adds all the found ports to a combo box on the GUI
    public void searchForPorts()
    {
        ports = CommPortIdentifier.getPortIdentifiers();

        while (ports.hasMoreElements())
        {
            CommPortIdentifier curPort = (CommPortIdentifier)ports.nextElement();

            //get only serial ports
            if (curPort.getPortType() == CommPortIdentifier.PORT_SERIAL)
            {
                //window.cboxPorts.addItem(curPort.getName());
                portMap.put(curPort.getName(), curPort);
            }
        }
    }
    
    public HashMap getAvailableComPorts() {
        return portMap;
    }

    //connect to the selected port in the combo box
    //pre: ports are already found by using the searchForPorts method
    //post: the connected comm port is stored in commPort, otherwise,
    //an exception is generated
    public int connect(String selectedPort)
    {
        //String selectedPort = (String)window.cboxPorts.getSelectedItem();
        selectedPortIdentifier = (CommPortIdentifier)portMap.get(selectedPort);

        CommPort commPort = null;

        try
        {
            //the method below returns an object of type CommPort
            commPort = selectedPortIdentifier.open("ProbeBoardControlPanel", TIMEOUT);
            //the CommPort object can be casted to a SerialPort object
            serialPort = (SerialPort)commPort;

            //for controlling GUI elements
            bConnected = true;

            //logging
            logText = selectedPort + " opened successfully.";
            writeLineToLog(logText);
        }
        catch (PortInUseException e)
        {
            logText = selectedPort + " is in use. (" + e.toString() + ")";
            writeLineToLog(logText);            
            return CONNECTION_CONNECTED;
        }
        catch (Exception e)
        {
            logText = "Failed to open " + selectedPort + "(" + e.toString() + ")";
            writeLineToLog(logText);
            return CONNECTION_FAILED;
        }
        
        return CONNECTION_OK;
    }

    //open the input and output streams
    //pre: an open port
    //post: initialized intput and output streams for use to communicate data
    public boolean initIOStream()
    {
        //return value for whather opening the streams is successful or not
        boolean successful = false;

        try {
            //
            input = serialPort.getInputStream();
            output = serialPort.getOutputStream();
            
            successful = true;
            return successful;
        }
        catch (IOException e) {
            logText = "I/O Streams failed to open. (" + e.toString() + ")";
            writeLineToLog(logText);
            return successful;
        }
    }

    //starts the event listener that knows whenever data is available to be read
    //pre: an open serial port
    //post: an event listener for the serial port that knows when data is recieved
    public void initListener()
    {
        try
        {
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        }
        catch (TooManyListenersException e)
        {
            logText = "Too many listeners. (" + e.toString() + ")";
            writeLineToLog(logText);
        }
    }

    //disconnect the serial port
    //pre: an open serial port
    //post: clsoed serial port
    public void disconnect()
    {
        //close the serial port
        try
        {
            serialPort.removeEventListener();
            serialPort.close();
            input.close();
            output.close();
            bConnected = false;
            //window.keybindingController.toggleControls();

            logText = "Disconnected.";
            writeLineToLog(logText);
        }
        catch (Exception e)
        {
            logText = "Failed to close " + serialPort.getName() + "(" + e.toString() + ")";
            writeLineToLog(logText);
        }
    }

    final public boolean isConnected()
    {
        return bConnected;
    }

    //what happens when data is received
    //pre: serial event is triggered
    //post: processing on the data it reads
    public void serialEvent(SerialPortEvent evt) {
        if (evt.getEventType() == SerialPortEvent.DATA_AVAILABLE)
        {
            try
            {
                byte singleData = (byte)input.read();
                logText = Integer.toHexString(singleData);
                writeLineToLog(logText);
            }
            catch (Exception e)
            {
                logText = "Failed to read data. (" + e.toString() + ")";
                writeLineToLog(logText);
            }
        }
    }

    //method that can be called to send data
    //pre: open serial port
    //post: data sent to the other device
    public void writeData(byte [] data)
    {
        if(!bConnected)
            return;
        
        try
        {
            String toLog = "Отправлено : ";
            
            for(byte next: data) {
                toLog += Integer.toHexString(next) + " ";
            }
            
            writeLineToLog(toLog);
            
            output.write(data);
            output.flush();
        }
        catch (Exception e)
        {
            logText = "Failed to write data. (" + e.toString() + ")";
            writeLineToLog(logText);
        }
    }
    
    public void setCommunicationLogFile(String fileName) {
        this.logFileName = fileName;
    }
    
    private void writeLineToLog(String line) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(logFileName, true));
            pw.println(line);
            pw.close();
        }
        catch (Exception ex) {}
    }
}

