package com.probe.usb.host.pc.plot;


import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Plot extends JFrame {

    private PlotPanel plotPanel = new PlotPanel() {
        @Override
        public void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Plot.this.draw((Graphics2D) graphics, getSize(), getInsets());
        }
    };

    public Plot() {
        add(plotPanel);
        this.setAlwaysOnTop(true);
        pack();
    }

    static private class Line {
        public int color;
        //double

    }


    public int getWidth() {
        return plotPanel.getWidth() - getInsets().left - getInsets().right;
    }
    public int getHeight() {
        return plotPanel.getHeight() - getInsets().top - getInsets().bottom;
    }

    public void addPoint(int color, int x, int y) {

    }


    private Map<Integer, List<Point>> plots = new HashMap<>(0);
    private List<Double> verticalLinesPosX = new ArrayList<>();
    //private List<>


    static public class Point{ double x, y;
        public Point(double x, double y) { this.x = x; this.y = y; }
    }



    private double
            startRealX = 0.0,
            endRealX = 10.0,
            startRealY = -10.0,
            endRealY = 10.0;



    public void draw(Graphics2D graph, Dimension dim, Insets insets) {

        for (int index: plots.keySet()) {
            final int colorIndex = index % colors.length;
            final List<Point> points = plots.get(index);
            if (points.isEmpty())
                continue;

            Color color = colors[colorIndex];
            graph.setColor(color);
            Stroke stroke = new BasicStroke(1f,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            graph.setStroke(stroke);
            Point prev = points.get(0);
            for (Point p: points) {
                graph.drawLine(
                        mapx(prev.x, dim, insets), mapy(prev.y, dim, insets),
                        mapx(p.x, dim, insets), mapy(p.y, dim, insets));
                prev = p;
                if (p.x > dim.width || p.x < 0)
                    break;
            }

            graph.setColor(Color.BLACK);
            for (double realX: verticalLinesPosX) {
                final int x = mapx(realX, dim, insets);
                graph.drawLine(x, insets.top, x, dim.height -insets.bottom);
            }
        }
    }

    public void setRealX(double startRealX, double endRealX) {
        this.startRealX = startRealX;
        this.endRealX = endRealX;
    }

    public void setRealY(double endRealY, double startRealY) {
        this.startRealY = startRealY;
        this.endRealY = endRealY;
    }

    public void setPlot(int colorIndex, List<Point> points) {
        plots.put(colorIndex, points);
    }
    public void verticalLines(List<Double> xPositions) {
        verticalLinesPosX = xPositions;
    }

    public void clearPlots() {
        plots = new HashMap<>();
        verticalLinesPosX = new ArrayList<>();
        repaint();
    }

    private int mapx(double realX, Dimension dim, Insets insets) {
        final int width = dim.width - insets.left - insets.right;
        final int x = (int)((realX - startRealX) * width / (endRealX - startRealX) + insets.left);
        if (x > dim.width || x < 0) {
            int tx = x;
            //logger.print("plot: x = " + x);
        }
        return x;
    }

    private int mapy(double realY, Dimension dim, Insets insets) {
        final int height = dim.height - insets.top - insets.bottom;
        final int y = (int)((realY - startRealY) * height / (endRealY - startRealY) + insets.top);
        return y;
    }
}
