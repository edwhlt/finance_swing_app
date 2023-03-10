package fr.hedwin.ihm;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import fr.hedwin.Main;
import fr.hedwin.ihm.components.DataTable;
import fr.hedwin.sql.dao.DaoFactory;
import fr.hedwin.sql.exceptions.DaoException;
import jdk.jfr.ContentType;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.Arrays;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.BiConsumer;

import static com.formdev.flatlaf.FlatClientProperties.*;
import static fr.hedwin.ihm.components.DataTable.column;

public class IHMScript extends JPanel {

    private IHMP ihmp;
    private DaoFactory daoFactory;
    private Map<String, Map<Integer, Object[]>> objects;

    public IHMScript(IHMP ihmp, DaoFactory daoFactory) throws DaoException {
        this.ihmp = ihmp;
        this.daoFactory = daoFactory;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JToolBar tools = new JToolBar(JToolBar.HORIZONTAL);
        tools.setLayout(new BoxLayout(tools, BoxLayout.X_AXIS));
        tools.setMargin(new Insets(5, 0, 5, 0));
        tools.setFloatable(false);

        JButton jButton = new JButton(new FlatSVGIcon("images/refresh_dark.svg"));

        File fileRoot = new File("requests/");
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new FileNode(fileRoot));
        TreeModel treeModel = new DefaultTreeModel(root);
        JTree tree = new JTree(treeModel);
        tree.setPreferredSize(new Dimension(300, Integer.MAX_VALUE));
        tree.setShowsRootHandles(true);
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                JTree jTree = (JTree) e.getComponent();
                if(e.isPopupTrigger()){
                    JPopupMenu jPopupMenu = new JPopupMenu();
                    JMenuItem jMenuItem = new JMenuItem("Ajouter un fichier");
                    jMenuItem.addActionListener(evt -> System.out.println(Arrays.toString(jTree.getClosestPathForLocation(e.getX(), e.getY()).getPath())));
                    jPopupMenu.add(jMenuItem);
                    if(tree.getSelectionPath() != null){
                        JMenuItem edit = new JMenuItem("Modifier");
                        edit.addActionListener(evt -> onEdit(tree));
                        jPopupMenu.add(edit);
                    }
                    jPopupMenu.show(jTree, e.getX(), e.getY());
                    return;
                }

                if(e.getClickCount() == 2){
                    jButton.doClick();
                }
            }
        });
        createChildren(fileRoot, root);

        JPanel result = new JPanel(new BorderLayout());
        result.setPreferredSize(new Dimension(700, 100));
        JTabbedPane onglets = new JTabbedPane();
        onglets.setTabLayoutPolicy( JTabbedPane.SCROLL_TAB_LAYOUT );
        onglets.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        onglets.putClientProperty( TABBED_PANE_TAB_CLOSABLE, true );
        onglets.putClientProperty( TABBED_PANE_TAB_CLOSE_TOOLTIPTEXT, "Close" );
        onglets.putClientProperty( TABBED_PANE_TAB_CLOSE_CALLBACK, (BiConsumer<JTabbedPane, Integer>) (tabPane, tabIndex) -> onglets.remove(tabIndex));
        result.add(onglets, BorderLayout.CENTER);

        jButton.addActionListener(e -> {
            String link = String.join("/", Arrays.stream(tree.getSelectionPath().getPath()).map(Object::toString).toArray(String[]::new));
            File file = new File(link);
            if(!file.isFile()) return;
            onglets.removeAll();
            try {
                objects = daoFactory.getTransactionDao().readSQLFile(link);
                objects.forEach((k, v) -> {
                    DataTable.DataColumn<Object, ?>[] columns = new DataTable.DataColumn[v.get(-1).length];
                    for (int i = 0; i < v.get(-1).length; i++) {
                        columns[i] = column(String.class, null, (String) v.get(-1)[i], false, Object::toString, null);
                    }
                    DataTable<Object> table = new DataTable<Object>(v.entrySet().stream().filter(entry -> entry.getKey() >= 0).map(Map.Entry::getValue).toArray(Object[][]::new), columns);
                    JScrollPane scrollPane = new JScrollPane();
                    scrollPane.setBorder(BorderFactory.createEmptyBorder());
                    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
                    scrollPane.setViewportView(table);
                    onglets.addTab(k, scrollPane);
                });
            } catch (DaoException daoException) {
                daoException.printStackTrace();
            }
        });
        tools.add(jButton);
        tools.add(Box.createHorizontalGlue());

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setLeftComponent(new JPanel(new BorderLayout()){{add(tree, BorderLayout.CENTER);}});
        split.setRightComponent(result);
        split.setDividerLocation(.5);

        add(tools);
        add(new JPanel(new BorderLayout()){{add(split, BorderLayout.CENTER);}});
    }

    public void onEdit(JTree tree){
        String link = String.join("/", Arrays.stream(tree.getSelectionPath().getPath()).map(Object::toString).toArray(String[]::new));
        File file = new File(link);
        if(!file.isFile()) return;

        JDialog jDialog = new JDialog(ihmp, true);
        jDialog.setLayout(new BoxLayout(jDialog.getContentPane(), BoxLayout.Y_AXIS));

        RSyntaxTextArea textArea = new RSyntaxTextArea(50, 200);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        textArea.setCodeFoldingEnabled(true);
        RTextScrollPane sp = new RTextScrollPane(textArea);
        sp.setBorder(BorderFactory.createEmptyBorder());

        try {
            Theme theme = Theme.load(Main.class.getClassLoader().getResourceAsStream("sql_synthax_theme.xml"));
            theme.apply(textArea);
        } catch (IOException ioe) { // Never happens
            ioe.printStackTrace();
        }

        StringJoiner sj = new StringJoiner("\n");

        try(BufferedReader fileReader = new BufferedReader(new FileReader(file))){
            fileReader.lines().forEach(sj::add);
        }catch (IOException ex){
            ex.printStackTrace();
        }
        textArea.setText(sj.toString());

        JToolBar jToolBar = new JToolBar(JToolBar.HORIZONTAL);
        jToolBar.setLayout(new BoxLayout(jToolBar, BoxLayout.X_AXIS));
        JButton save = new JButton(new FlatSVGIcon("images/menu-saveall_dark.svg"));
        save.addActionListener(event -> {
            try(FileWriter fileWriter = new FileWriter(file)){
                fileWriter.write(textArea.getText());
            }catch (IOException ex){
                ex.printStackTrace();
            }
            jDialog.dispose();
        });
        jToolBar.add(save);
        jToolBar.add(Box.createHorizontalGlue());

        jDialog.add(jToolBar);
        jDialog.add(sp);
        jDialog.pack();
        jDialog.setLocationRelativeTo(null);
        jDialog.setVisible(true);
    }

    public void createChildren(File fileRoot, DefaultMutableTreeNode root){
        File[] files = fileRoot.listFiles();
        if (files != null) for (File file : files) {
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(new FileNode(file));
            root.add(childNode);
            if (file.isDirectory()) {
                createChildren(file, childNode);
            }
        }
    }

    public static class FileNode {
        private File file;

        public FileNode(File file) {
            this.file = file;
        }

        @Override
        public String toString() {
            String name = file.getName();
            if (name.equals("")) {
                return file.getAbsolutePath();
            } else {
                return name;
            }
        }
    }


}
