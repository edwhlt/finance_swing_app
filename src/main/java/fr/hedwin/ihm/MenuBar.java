package fr.hedwin.ihm;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.*;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import fr.hedwin.Main;

import javax.swing.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

        JMenu menu = new JMenu("Themes");

        JMenuItem edwin = new JMenuItem("Edwin Dark");
        edwin.addActionListener(e -> changeTheme("edwin-red-dark.theme.json"));
        menu.add(edwin);
        JMenuItem macos = new JMenuItem("MacOS Dark");
        macos.addActionListener(e -> changeTheme(new FlatMacDarkLaf()));
        menu.add(macos);

        for (FlatAllIJThemes.FlatIJLookAndFeelInfo info : FlatAllIJThemes.INFOS) {
            JMenuItem t = new JMenuItem(info.getName());
            t.addActionListener(e -> changeThemeClass(info.getClassName()));
            menu.add(t);
        }

        add(fileMenu);
        add(menu);
    }

    private void changeTheme(String jsonFile){
        FlatLaf fl = null;
        try {
            fl = IntelliJTheme.createLaf(Objects.requireNonNull(Main.class.getClassLoader().getResourceAsStream(jsonFile)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        changeTheme(fl);
    }

    private static final List<Runnable> REFRESH_PAINT_REGISTER = new ArrayList<>();
    public static void addRepaintOnRefresh(Runnable... runnables){
        REFRESH_PAINT_REGISTER.addAll(List.of(runnables));
    }

    private void changeTheme(LookAndFeel lookAndFeel){
        try {
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        SwingUtilities.updateComponentTreeUI(SwingUtilities.getWindowAncestor(this));
        REFRESH_PAINT_REGISTER.forEach(Runnable::run);
    }

    private void changeThemeClass(String className){
        try {
            UIManager.setLookAndFeel(className);
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
        SwingUtilities.updateComponentTreeUI(SwingUtilities.getWindowAncestor(this));
        REFRESH_PAINT_REGISTER.forEach(Runnable::run);
    }

}
