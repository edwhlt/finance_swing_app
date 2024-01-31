package fr.hedwin.ihm;

import fr.hedwin.ihm.chart.BarATBChart;
import fr.hedwin.ihm.chart.BarMonthChart;
import fr.hedwin.ihm.chart.PieATBChart;
import fr.hedwin.sql.dao.DaoFactory;

import javax.swing.*;
import java.awt.*;

public class IHMDash extends JPanel {

    public IHMDash(DaoFactory daoFactory){
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        BarMonthChart barMonthChart = new BarMonthChart(daoFactory);

        BarATBChart barATBChart = new BarATBChart(daoFactory);
        PieATBChart pieATBChart = new PieATBChart(daoFactory);

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));
        jPanel.add(barATBChart);
        jPanel.add(pieATBChart);

        add(barMonthChart);
        add(jPanel);
    }

}
