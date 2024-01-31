package fr.hedwin.ihm;

import fr.hedwin.ihm.chart.BarATBChart;
import fr.hedwin.ihm.chart.BarCategorieChart;
import fr.hedwin.ihm.chart.BarMonthChart;
import fr.hedwin.ihm.chart.PieATBChart;
import fr.hedwin.sql.dao.DaoFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class IHMDash extends JPanel {

    public IHMDash(DaoFactory daoFactory){
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        BarMonthChart barMonthChart = new BarMonthChart(daoFactory);
        BarCategorieChart barCategorieChart = new BarCategorieChart(daoFactory);

        BarATBChart barATBChart = new BarATBChart(daoFactory);
        PieATBChart pieATBChart = new PieATBChart(daoFactory);

        MenuBar.addRepaintOnRefresh(barMonthChart::refreshUI, barATBChart::refreshUI, pieATBChart::refreshUI);

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));
        jPanel.add(barATBChart);
        jPanel.add(pieATBChart);


        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("Statistiques Compte Courant"){{
            setFont(new Font("Arial", Font.BOLD, 24));
        }});
        panel.add(barMonthChart);
        panel.add(barCategorieChart);
        panel.add(new JLabel("ATB Clothing"){{
            setFont(new Font("Arial", Font.BOLD, 24));
        }});
        panel.add(jPanel);

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                barMonthChart.setPreferredSize(new Dimension(scrollPane.getViewport().getWidth(), 400));
                barMonthChart.revalidate();
                barCategorieChart.setPreferredSize(new Dimension(scrollPane.getViewport().getWidth(), 400));
                barCategorieChart.revalidate();
                jPanel.setPreferredSize(new Dimension(scrollPane.getViewport().getWidth(), 400));
                jPanel.revalidate();
            }
        });

        add(scrollPane);
    }

}
