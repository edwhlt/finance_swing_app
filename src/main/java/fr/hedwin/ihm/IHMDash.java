package fr.hedwin.ihm;

import javax.swing.*;
import java.awt.*;

public class IHMDash extends JPanel {

    public IHMDash(){
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        Chart example = new Chart();

        add(example, BorderLayout.NORTH);
    }



}
