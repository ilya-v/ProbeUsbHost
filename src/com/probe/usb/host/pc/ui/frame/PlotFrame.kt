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

    data class Text(val x : Int, val y : Int, val text: String);

    private val colors = arrayOf(Color.BLACK, Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.CYAN,
            Color.LIGHT_GRAY)

    private var lines : MutableList<Line> = ArrayList()
    private var strings : MutableList<Text> = ArrayList()

    var plotWidth   = 0
    var plotHeight  = 0

    var onResize : ((width : Int, height: Int) -> Unit)? = null

    private fun draw(graph : Graphics2D, size : Dimension, insets : Insets) {

        val newWidth = size.width - insets.left - insets.right
        val newHeight = size.height - insets.top - insets.bottom
        val resized = newWidth != plotWidth || newHeight != plotHeight
        plotWidth = newWidth
        plotHeight = newHeight

        var strokeWidth  = 1f
        fun stroke(width: Float) = BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        graph.stroke = stroke(strokeWidth)
        for (line in lines) {
            graph.color = colors[(line.color + colors.size) % colors.size]
            if (strokeWidth != line.width) {
                strokeWidth = line.width
                graph.stroke = stroke(strokeWidth)
            }
            var (x1, y1, x2, y2) = line
            if (x1 == Int.MIN_VALUE) x1 = 0
            if (x2 == Int.MAX_VALUE) x2 = plotWidth
            if (y1 == Int.MIN_VALUE) y1 = 0
            if (y2 == Int.MAX_VALUE) y2 = plotHeight
            graph.drawLine(x1 + insets.left, y1 + insets.top, x2 + insets.left, y2 + insets.top)
        }

        graph.setFont(Font("Times New Roman", Font.PLAIN, 12))
        graph.color = colors[0]
        for (text in strings) {
            val x = if (text.x >= 0) text.x + insets.left
                    else plotWidth + text.x - insets.right - graph.fontMetrics.stringWidth(text.text)
            graph.drawString(text.text, x, text.y + insets.top)
        }

        if (resized)
            onResize?.invoke(newWidth, newHeight)
    }

    fun refresh() = plotPanel.repaint()
    fun clear() {
        lines.clear()
        strings.clear()
    }
    fun addLine(line : Line)  = lines.add(line)
    fun addText(x : Int, y : Int, text : String)  = strings.add(Text(x, y, text))
}