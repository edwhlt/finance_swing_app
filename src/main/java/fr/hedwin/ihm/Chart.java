package fr.hedwin.ihm;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;

public class Chart extends JFrame {

    public Chart(String title) throws HeadlessException {
        super(title);
        // Create dataset
        DefaultCategoryDataset dataset = createDataset();
        // Create chart
        JFreeChart chart = ChartFactory.createLineChart(
                "Solde Chart", // Chart title
                "Date", // X-Axis Label
                "Solde", // Y-Axis Label
                dataset
        );

        ChartPanel panel = new ChartPanel(chart);
        setContentPane(panel);
    }

    private DefaultCategoryDataset createDataset() {
        String series1 = "Solde CC";
        String series2 = "Solde Lydia";

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        dataset.addValue(231.40, series1, "8");
        dataset.addValue(1086.20, series1, "9");
        dataset.addValue(1968.80, series1, "10");
        dataset.addValue(1640.42, series1, "11");
        dataset.addValue(2095.33, series1, "12");
        dataset.addValue(2101.29, series1, "1");

        dataset.addValue(20.66, series2, "9");
        dataset.addValue(27.44, series2, "10");
        dataset.addValue(25.44, series2, "11");
        dataset.addValue(16.44, series2, "12");
        dataset.addValue(18.44, series2, "1");

        return dataset;
    }

}
