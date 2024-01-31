package fr.hedwin.ihm.chart;

import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;

import java.awt.*;

public class CustomBarRenderer extends StackedBarRenderer {

    public CustomBarRenderer(){
        setItemMargin(0.04);
        setShadowVisible(false);
        setGradientPaintTransformer(null);
        setBarPainter(new StandardBarPainter());
    }

    @Override
    public Paint getItemPaint(int row, int column) {
        // Customize the colors for positive and negative values
        double value = getPlot().getDataset().getValue(row, column).doubleValue();
        return value >= 0 ? Color.decode("#6AEA53") : Color.decode("#FF3535");
    }
}