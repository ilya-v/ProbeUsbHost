package com.probe.usb.host.pc.controller.event

import com.probe.usb.host.parser.internal.DataPoint
import com.probe.usb.host.pc.controller.KPortScanner
import com.probe.usb.host.pc.controller.OutputController


data class UiReceiverCreatedEvent(val clazz : Class<*>)
data class ReceiverCreated(val clazz: Class<*>)

class TickEvent()

data class ErrorLogEvent(val message : String)
data class InfoLogEvent(val message: String)
data class DebugLogEvent(val message : String)

data class UiLogTextCommand(val text: String)

class ComPortsListCommand
data class ComPortHasPortsEvent(val ports: List<String>)
data class ComPortConnectCommand(val portName: String?, val connect: Boolean)
data class ComPortConnectionEvent(val portName: String?, val connectionStatus: Boolean)
data class ComPortDataEvent(val portData: ByteArray, val sync : Int) {
    override fun toString(): String = this.javaClass.simpleName + "[" + portData.size + "]"
}

data class ComPortWriteCommand(val portData: ByteArray)
data class ComPortFailureEvent(val message: String, val port: String)
data class ComPortWriteFailureEvent(val message: String)
data class ComPortReadFailureEvent(val message: String)

class ParserFrameEvent

data class UiInputFileActiveEvent(val active: Boolean)
data class UiInputBusyCommand(val busy: Boolean)
data class OpenRawInputFileCommand(val fileName : String)
class MoreInputRawDataEvent

class NewDataTrackEvent

data class SendDataToDeviceCommand(val bytes: IntArray)

data class PortStatusEvent(val state : KPortScanner.State)
data class PortScannerEnableEvent(val enabled : Boolean)
data class UiPortScanEnabledCommand(val enabled: Boolean)

data class UiCommandEnableCommand(val enabled: Boolean)

data class UiOutputFileStatusCommand(val chan : OutputController.OutputChannel,
                                     val fileName : String,
                                     val fileSize : String)
data class UiOutputChanEnabledEvent(val chan : OutputController.OutputChannel, val enabled: Boolean)
data class UiOutputDirEvent(val directory : String)

data class UiPlotResizedEvent(val w : Int, val h: Int)
data class UiPlotSliderEvent(val kx : Double, val ky : Double)


data class PlotDataPointEvent(val dp : DataPoint)
data class UiPlotPointsCommand(val plotIndex : Int, val points : Array<Pair<Int, Int>>)
data class UiPlotTimeBreakCommand(val x: Int)
data class UiPlotHorizontalGridCommand(val dy: Int, val y0 : Int)
class UiPlotClearCommand