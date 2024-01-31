package fr.hedwin.ihm.chart;

import fr.hedwin.sql.dao.DaoFactory;
import fr.hedwin.sql.exceptions.DaoException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.RingPlot;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import javax.swing.*;
import java.awt.*;

public class PieATBChart extends JPanel {


    private final DaoFactory daoFactory;

    public PieATBChart(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
        setLayout(new BorderLayout());
        // Create dataset
        PieDataset<String> dataset = createDataset();

        JFreeChart chart = ChartFactory.createRingChart(
                "Répartition des transactions",  // Titre du graphique
                dataset,                        // Dataset
                true,                           // Légende
                true,
                false);

        RingPlot plot = (RingPlot) chart.getPlot();
        plot.setSectionDepth(0.30);
        plot.setLabelGenerator(null);

        new ChartUIProperties().setOn(chart);
        ChartPanel chartPanel = new ChartPanel(chart);

        add(chartPanel, BorderLayout.CENTER);
    }

    private PieDataset<String> createDataset() {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();

        try {
            daoFactory.request("""
                    SELECT
                        CASE
                            WHEN montant > 0 THEN 'recette'
                            WHEN montant < 0 THEN 'dépense'
                            ELSE 'équilibre' -- If the sum is zero
                        END AS categorie,
                        SUM(ABS(montant)) AS montant_total -- Absolute value for the total amount
                    FROM transactions
                    WHERE transactions.tiers_id = 647
                    group by categorie;
                    """, resultSet -> {
                while(resultSet.next()){
                    String categorie = resultSet.getString("categorie");
                    double montantTotal = resultSet.getDouble("montant_total");
                    dataset.setGroup(new DatasetGroup());
                    dataset.setValue(categorie, montantTotal);
                }
                return null;
            });
        } catch (DaoException e) {
            e.printStackTrace();
        }

        return dataset;
    }


    private static class CustomBarRenderer extends StackedBarRenderer {

        public CustomBarRenderer(){
            setItemMargin(0.04);
            setShadowVisible(false);
            setGradientPaintTransformer(null);
        }

        @Override
        public Paint getItemPaint(int row, int column) {
            // Customize the colors for positive and negative values
            double value = getPlot().getDataset().getValue(row, column).doubleValue();
            return value >= 0 ? Color.GREEN : Color.RED;
        }
    }

}
