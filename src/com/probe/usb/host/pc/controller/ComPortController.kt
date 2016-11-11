package com.probe.usb.host.pc.controller

import com.google.common.eventbus.Subscribe
import com.probe.usb.host.bus.Bus
import com.probe.usb.host.bus.Receiver
import com.probe.usb.host.pc.controller.event.*
import purejavacomm.CommPortIdentifier
import purejavacomm.SerialPort
import purejavacomm.SerialPortEvent
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


object ComPortController : Receiver() {

    const private val TIMEOUT = 2000
    private const val byteChunkSize = 1024

    private var serialPort: SerialPort? = null
    private var portsMap: MutableMap<String, CommPortIdentifier> = HashMap()

    private var dataEventCounter = 0

    @Subscribe
    fun onConnectionCommand(connectCommand: ComPortConnectCommand) {
        if (connectCommand.connect && connectCommand.portName != null)
            connect(connectCommand.portName)
        else
            disconnect()
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun onComPortsListCommand(comPortsListCommand: ComPortsListCommand) {
        val portNames = searchForPorts()
        postEvent(ComPortHasPortsEvent(portNames))

    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun tick(tickEvent : TickEvent) {
        if (serialPort != null)
            return
        val oldPorts = this.portsMap
        val newPorts = searchForPorts()
        var changed = false
        for (p in newPorts) {
            if (!oldPorts.containsKey(p)) {
                changed = true
                break
            }
        }
        if (changed || newPorts.size != oldPorts.size)
            postEvent(ComPortHasPortsEvent(newPorts))
    }

    fun connect(portName : String) {
        try {
            disconnect()
            val portId = portsMap[portName]
            serialPort = portId!!.open("ProbeBoardControlPanel", TIMEOUT) as SerialPort
            serialPort!!.addEventListener({ this.serialEvent(it) })
            serialPort!!.notifyOnDataAvailable(true)
        } catch (e: Exception) {
            logException("Cannot connect to a port " + portName, e)
            disconnect()
            postEvent(ComPortFailureEvent("Cannot connect to a port " + portName, portName))
            return
        }
        postEvent(ComPortConnectionEvent(portName, true))
    }

    fun disconnect() {
        if (serialPort == null)
            return;
        try {
            serialPort?.removeEventListener()
            serialPort?.getInputStream()?.close()
            serialPort?.getOutputStream()?.close()
            serialPort?.close()
        } catch (e: Exception) {
            logException("Cannot disconnect from a port " + serialPort?.getName(), e)
        }
        postEvent(ComPortConnectionEvent(serialPort?.name, false))
        serialPort = null
    }

    @Subscribe
    fun writeData(command: ComPortWriteCommand) {
        if (serialPort == null) {
            postEvent(ComPortWriteFailureEvent("Com port not open"))
            return
        }

        try {
            serialPort!!.getOutputStream().write(command.portData)
            serialPort!!.getOutputStream().flush()
        } catch (e: Exception) {
            logException("Cannot write data to a serial port", e)
            Bus.post(ComPortWriteFailureEvent(e.toString()))
        }
    }

    private fun logException(message: String, e: Exception) {
        Bus.post(ErrorLogEvent(javaClass.simpleName + ": " + message + ": " + e.toString()))
    }

    private fun searchForPorts(): List<String> {
        val portList = ArrayList<String>()
        this.portsMap = HashMap<String, CommPortIdentifier>()
        val ports = CommPortIdentifier.getPortIdentifiers()
        while (ports.hasMoreElements()) {
            val curPort = ports.nextElement()
            if (curPort.portType == CommPortIdentifier.PORT_SERIAL) {
                portList.add(curPort.name)
                this.portsMap.put(curPort.name, curPort)
            }
        }
        return portList
    }

    private val inSerialEvent  = AtomicBoolean(false)
    /* private var date = Date()
    private var str : FileOutputStream? = null
    private var counter = 0 */

    private fun serialEvent(evt: SerialPortEvent) {
        if (evt.eventType != SerialPortEvent.DATA_AVAILABLE || serialPort == null
                || !inSerialEvent.compareAndSet(false, true))
            return

        try {
            val inputStream = serialPort!!.getInputStream()
            val byteList = ArrayList<Byte>();
            while (inputStream.available() > 0) {
                val b : Int = inputStream.read()
                if (b < 0) break;

                byteList.add(b.toByte())

                /* if (date.time + 2000 < Date().time) {
                    counter ++
                    str?.close()
                    str = null
                }
                if (str == null) {
                    val f = File("/home/user/test/" + counter + ".out")
                    f.createNewFile()
                    str = FileOutputStream(f)
                }
                date = Date()
                str?.write(b) */

                if (byteList.size >= byteChunkSize) {
                    postDelayedEvent(ComPortDataEvent(byteList.toByteArray(), dataEventCounter++))
                    byteList.clear()
                    Thread.sleep(3) // Do not read the COM port too often
                }
            }
            if (byteList.isNotEmpty())
                postDelayedEvent(ComPortDataEvent(byteList.toByteArray(), dataEventCounter++))
        } catch (e: Exception) {
            logException("Cannot read data from a serial port", e)
            postEvent(ComPortReadFailureEvent(e.toString()))
        }

        inSerialEvent.set(false)
    }
}
