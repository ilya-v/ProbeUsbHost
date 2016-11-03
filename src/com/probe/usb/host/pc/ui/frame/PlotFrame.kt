package com.probe.usb.host.pc.ui.frame

import com.probe.usb.host.pc.plot.PlotPanel
import java.awt.*
import java.util.*
import javax.swing.JFrame

object PlotFrame : JFrame() {

    private val plotPanel = object : PlotPanel() {
        override fun paintComponent(graphics: Graphics) {
            super.paintComponent(graphics)
            this@PlotFrame.draw(graphics as Graphics2D, size, insets)
        }
    }

    init {
        add(plotPanel)
        this.isAlwaysOnTop = true
        pack()
    }

    data class Line(val x1 : Int, val y1 : Int, val x2 : Int, val y2 : Int,
                            val color: Int = 0, val width: Float = 1.0f);

    private data class Text(val x : Int, val y : Int, val text: String);

    private val colors = arrayOf(Color.BLACK, Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.CYAN)

    private var lines : MutableList<Line> = ArrayList()
    private var strings : MutableList<Text> = ArrayList()

    private var plotWidth = 0
    private var plotHeight = 0

    var onResize : ((width : Int, height: Int) -> Unit)? = null

    private fun draw(graph : Graphics2D, size : Dimension, insets : Insets) {

        val newWidth = size.width - insets.left - insets.right
        val newHeight = size.height - insets.top - insets.bottom
        if (newWidth != plotWidth || newHeight != plotHeight)
            onResize?.invoke(newWidth, newHeight)
        plotWidth = newWidth
        plotHeight = newHeight

        var strokeWidth  = 1f
        fun stroke(width: Float) = BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        graph.stroke = stroke(strokeWidth)
        for (line in lines) {
            graph.color = colors[line.color % colors.size]
            if (strokeWidth != line.width) {
                strokeWidth = line.width
                graph.stroke = stroke(strokeWidth)
            }
            graph.drawLine( line.x1 + insets.left, line.y1 + insets.top,
                            line.x2 + insets.left, line.y2 + insets.top)
        }

        graphics.setFont(Font("Times New Roman", Font.PLAIN, 12))
        for (text in strings) {
            graphics.drawString(text.text, text.x, text.y)
        }
    }

    fun clear() =  lines.clear()

    fun addLine(line : Line)  = lines.add(line)

    fun addText(x : Int, y : Int, text : String)  = strings.add(Text(x, y, text))

}