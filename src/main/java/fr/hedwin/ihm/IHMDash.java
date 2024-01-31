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

        MenuBar.addRepaintOnRefresh(barMonthChart::refreshUI, barATBChart::refreshUI, pieATBChart::refreshUI);

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));
        jPanel.add(barATBChart);
        jPanel.add(pieATBChart);


        add(new JLabel("Statistiques Compte Courant"){{
            setFont(new Font("Arial", Font.BOLD, 24));
        }});
        add(barMonthChart);
        add(new JLabel("ATB Clothing"){{
            setFont(new Font("Arial", Font.BOLD, 24));
        }});
        add(jPanel);

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

}
