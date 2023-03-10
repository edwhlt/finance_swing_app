package fr.hedwin.ihm;

import javax.swing.*;
import java.awt.*;

public class IHMDash extends JPanel {

    public IHMDash(){
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JButton button = new JButton("Chart");
        button.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                Chart example = new Chart("Line Chart Example");
                example.setAlwaysOnTop(true);
                example.pack();
                example.setSize(600, 400);
                example.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                example.setVisible(true);
            });
        });

        add(button, BorderLayout.CENTER);
    }



}
