package fr.hedwin;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.extras.FlatInspector;
import com.formdev.flatlaf.extras.FlatUIDefaultsInspector;
import fr.hedwin.ihm.IHMP;
import fr.hedwin.sql.dao.DaoFactory;
import fr.hedwin.sql.exceptions.DaoException;

import javax.swing.*;
import java.text.ParseException;

public class Main {

    private static final DaoFactory daoFactory = DaoFactory.getInstance();

    public static void main(String[] args) throws DaoException, ParseException {
        try{
            IntelliJTheme.install(Main.class.getClassLoader().getResourceAsStream("edwin-red-dark.theme.json"));
            FlatInspector.install("ctrl shift alt X");
            FlatUIDefaultsInspector.install("ctrl shift alt Y");
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);

        }catch(Exception ex){
            JOptionPane.showMessageDialog(null, "Erreur chargement de thème !");
        }

        IHMP ihmp = new IHMP(daoFactory);
        ihmp.setVisible(true);
    }

}
