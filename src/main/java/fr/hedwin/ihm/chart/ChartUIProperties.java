package fr.hedwin.ihm.chart;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.RingPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;

import javax.swing.*;
import java.awt.*;

public class ChartUIProperties {

    public void setOn(JFreeChart chart){
        chart.setBackgroundPaint(UIManager.getColor("Panel.background"));
        chart.getPlot().setBackgroundPaint(UIManager.getColor("Panel.background"));
        chart.setBorderVisible(false);
        chart.getPlot().setOutlineVisible(false);

        LegendTitle legend = chart.getLegend();
        legend.setBackgroundPaint(UIManager.getColor("Panel.background"));
        legend.setItemPaint(UIManager.getColor("Label.foreground"));

        // Optional: Change the chart's title font and paint (color)
        TextTitle title = chart.getTitle();
        title.setPaint(UIManager.getColor("Label.foreground"));
        title.setFont(new Font("Arial", Font.PLAIN, 16));
    }

}
