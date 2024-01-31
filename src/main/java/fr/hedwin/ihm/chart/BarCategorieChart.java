package fr.hedwin.ihm.chart;

import fr.hedwin.sql.dao.DaoFactory;
import fr.hedwin.sql.exceptions.DaoException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BarCategorieChart extends JPanel {


    private final DaoFactory daoFactory;

    public BarCategorieChart(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
        setLayout(new BorderLayout());
        // Create dataset
        DefaultCategoryDataset dataset = createDataset();

        JFreeChart chart = ChartFactory.createBarChart(
                "Histogramme des Catégorie",
                "Catégories",
                "Somme",
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
        LogarithmicAxis yAxis = new LogarithmicAxis(plot.getRangeAxis().getLabel());
        yAxis.setLabelFont(new Font("Arial", Font.PLAIN, 12));
        yAxis.setAllowNegativesFlag(true);
        // yAxis.setTickUnit(new NumberTickUnit(1)); // Customize the tick unit as needed
        plot.setRangeAxis(yAxis);
        //yAxis.setAxisLinePaint(Color.MAGENTA); // Set the color of the axis line

        this.refreshUI.add(() -> {
            domainAxis.setLabelPaint(UIManager.getColor("Label.foreground"));
            domainAxis.setTickLabelPaint(UIManager.getColor("Label.foreground"));
            yAxis.setLabelPaint(UIManager.getColor("Label.foreground"));
            yAxis.setTickLabelPaint(UIManager.getColor("Label.foreground"));
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
                    select categorie.name as name, sum(montant) as valeur from transactions
                    LEFT JOIN categorie ON categorie.id = categorie_id
                    where comptes_id = 2 or comptes_id >= 185 and remboursement is null and date_op >= '2019-08-01' group by categorie_id;
                    """, resultSet -> {
                while(resultSet.next()){
                    String categorie = resultSet.getString("name");
                    double solde = resultSet.getDouble("valeur");

                    dataset.addValue(solde, "Solde", categorie);
                }
                return null;
            });
        } catch (DaoException e) {
            e.printStackTrace();
        }

        return dataset;
    }

}
