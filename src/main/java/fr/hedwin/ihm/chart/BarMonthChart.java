package fr.hedwin.ihm.chart;

import fr.hedwin.sql.dao.DaoFactory;
import fr.hedwin.sql.exceptions.DaoException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BarMonthChart extends JPanel {


    private final DaoFactory daoFactory;

    public BarMonthChart(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
        setLayout(new BorderLayout());
        // Create dataset
        DefaultCategoryDataset dataset = createDataset();

        JFreeChart chart = ChartFactory.createBarChart(
                "Histogramme des Soldes",
                "Mois",
                "Montant",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        CustomBarRenderer renderer = new CustomBarRenderer();
        plot.setRenderer(renderer);

        // Set tooltips for each bar
        CategoryToolTipGenerator tooltipGenerator = new StandardCategoryToolTipGenerator();
        renderer.setDefaultToolTipGenerator(tooltipGenerator);

        // Customize the domain (x) axis
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        domainAxis.setLabelFont(new Font("Arial", Font.PLAIN, 12));
        //domainAxis.setAxisLinePaint(Color.GREEN); // Set the color of the axis line
        // Customize the range (y) axis
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setLabelFont(new Font("Arial", Font.PLAIN, 12));
        //rangeAxis.setAxisLinePaint(Color.MAGENTA); // Set the color of the axis line

        this.refreshUI.add(() -> {
            domainAxis.setLabelPaint(UIManager.getColor("Label.foreground"));
            domainAxis.setTickLabelPaint(UIManager.getColor("Label.foreground"));
            rangeAxis.setLabelPaint(UIManager.getColor("Label.foreground"));
            rangeAxis.setTickLabelPaint(UIManager.getColor("Label.foreground"));
        });

        this.refreshUI.add(() -> new ChartUIProperties().setOn(chart));
        this.refreshUI();
        ChartPanel chartPanel = new ChartPanel(chart);

        add(chartPanel, BorderLayout.CENTER);
    }

    private final List<Runnable> refreshUI = new ArrayList<>();
    public void refreshUI(){
        this.refreshUI.forEach(Runnable::run);
    }

    private DefaultCategoryDataset createDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        try {
            daoFactory.request("""
                    SELECT CONCAT(YEAR(date_op), '-', LPAD(MONTH(date_op), 2, '0')) AS mois,
                            SUM(IF(montant > 0, montant, 0)) AS solde_positive,
                            SUM(IF(montant < 0, montant, 0)) AS solde_negative
                    FROM transactions
                    WHERE transactions.comptes_id = 2 AND date_op >= '2019-08-01'
                    GROUP BY YEAR(date_op), MONTH(date_op);
                    """, resultSet -> {
                while(resultSet.next()){
                    String mois = resultSet.getString("mois");
                    double soldePositive = resultSet.getDouble("solde_positive");
                    double soldeNegative = resultSet.getDouble("solde_negative");

                    // Add data to the dataset
                    dataset.addValue(soldePositive, "Solde Positif", mois);
                    dataset.addValue(soldeNegative, "Solde Négatif", mois);
                }
                return null;
            });
        } catch (DaoException e) {
            e.printStackTrace();
        }

        return dataset;
    }

}
