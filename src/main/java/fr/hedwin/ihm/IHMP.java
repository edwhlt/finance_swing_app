package fr.hedwin.ihm;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import fr.hedwin.sql.DataManager;
import fr.hedwin.sql.dao.DaoFactory;
import fr.hedwin.sql.exceptions.DaoException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;

import static com.formdev.flatlaf.FlatClientProperties.*;

public class IHMP extends JFrame {

    public static IHMP INSTANCE;
    public static boolean saveFiles = true;

    public IHMP(DaoFactory daoFactory) throws DaoException, ParseException {
        super("Gestion finance personnelle");
        INSTANCE = this;
        DataManager dataManager = new DataManager(this, daoFactory);

        //dataManager.exportToCSV("test.csv", "select 96, remboursement, information, categorie_id, 3, mdp_id, -montant, date_op from transactions where tiers_id = 5 and date_op <= '2020-08-01' order by date_op desc");

        //Image icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/icon.png"));
        Image icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/logo_finance.png"));
        setIconImage(icon);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if(saveFiles){
                    dataManager.exportToCSV("tiers.csv", "select * from tiers");
                    dataManager.exportToCSV("categories.csv", "select * from categorie");
                    dataManager.exportToCSV("mdps.csv", "select * from mdp");
                    dataManager.exportToCSV("comptes.csv", "select * from comptes");
                    dataManager.exportToCSV("transactions.csv", "select * from transactions");
                }
                dispose();
            }
        });
        setJMenuBar(new MenuBar(this));
        setPreferredSize(new Dimension(1280, 720));
        setLayout(new BorderLayout());

        JTabbedPane onglets = new JTabbedPane();
        //onglets.putClientProperty( TABBED_PANE_TAB_ICON_PLACEMENT, SwingConstants.TOP );
        //onglets.putClientProperty( TABBED_PANE_TAB_AREA_ALIGNMENT, TABBED_PANE_ALIGN_CENTER );
        //onglets.putClientProperty( TABBED_PANE_TAB_WIDTH_MODE, TABBED_PANE_TAB_WIDTH_MODE_EQUAL );
        //onglets.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        onglets.addTab("Tableau de bord", new FlatSVGIcon("images/addToDictionary_dark.svg"), new IHMDash(daoFactory));
        onglets.addTab("Comptes bancaires", new FlatSVGIcon("images/DataTables.svg"), new IHMComptes(dataManager));
        onglets.addTab("Données", new FlatSVGIcon("images/dataSchema.svg"), new IHMDatas(dataManager));
        onglets.addTab("Scripts", new FlatSVGIcon("images/scripting_script_dark.svg"), new IHMScript(this, daoFactory));
        onglets.addTab("Import", new FlatSVGIcon("images/import_dark.svg"), new IHMImport(this, dataManager));
        add(onglets, BorderLayout.CENTER);

        setResizable(true);
        pack();
        setLocationRelativeTo(null);
    }

}
