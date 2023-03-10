package fr.hedwin.ihm;

import javax.swing.*;

import static fr.hedwin.ihm.IHMP.saveFiles;

public class MenuBar extends JMenuBar {

    private IHMP ihmp;

    public MenuBar(IHMP ihmp) {
        this.ihmp = ihmp;
        JMenu fileMenu = new JMenu("Options");
        JCheckBoxMenuItem filesSave = new JCheckBoxMenuItem("Enregistrer les fichiers à la fermeture");
        filesSave.setState(saveFiles);
        filesSave.addActionListener(e -> {
            saveFiles = false;
        });

        fileMenu.add(filesSave);
        add(fileMenu);
    }
}
