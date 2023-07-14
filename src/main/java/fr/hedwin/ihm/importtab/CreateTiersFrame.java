package fr.hedwin.ihm.importtab;

import fr.hedwin.ihm.components.FormFrame;
import fr.hedwin.objects.Tiers;
import fr.hedwin.sql.DataManager;
import fr.hedwin.sql.exceptions.DaoException;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class CreateTiersFrame extends FormFrame<Tiers> {


    public CreateTiersFrame(JFrame parent, DataManager dataManager, String libel, Consumer<Tiers> tiersFunction){
        super(parent, "Creation d'un nouveau tiers", tiersFunction);
        setLayout(new BorderLayout());
        JTextField name = new JTextField("name");
        name.setText(libel);
        JTextField regex = new JTextField("cm_name");
        regex.setText(libel);
        JButton jButton1 = new JButton("Add"){{
            addActionListener(e -> {
                try {
                    Tiers ntiers = dataManager.addTiers(name.getText(), regex.getText());
                    dispose();
                    tiersFunction.accept(ntiers);
                } catch (DaoException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }};

        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
        listPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        listPane.add(name);
        listPane.add(Box.createRigidArea(new Dimension(0,5)));
        listPane.add(regex);
        listPane.add(Box.createRigidArea(new Dimension(0,10)));
        listPane.add(jButton1);
        add(listPane, BorderLayout.NORTH);
        pack();
        setCenterOfParentFrame();
        setVisible(true);
    }

    private void setCenterOfParentFrame(){
        int parentX = parent.getLocation().x;
        int parentY = parent.getLocation().y;
        int parentWidth = parent.getWidth();
        int parentHeight = parent.getHeight();

        // Calculating center coordinates and subtracting half the width and height of the frame
        int centerX = parentX + parentWidth / 2 - this.getWidth() / 2;
        int centerY = parentY + parentHeight / 2 - this.getHeight() / 2;
        this.setLocation(centerX, centerY);
    }

}
