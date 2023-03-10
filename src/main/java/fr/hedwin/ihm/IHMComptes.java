package fr.hedwin.ihm;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import fr.hedwin.ihm.components.CheckedComboBox;
import fr.hedwin.ihm.components.DataTable;
import fr.hedwin.ihm.fields.ComboEditor;
import fr.hedwin.ihm.fields.DateEditor;
import fr.hedwin.ihm.fields.SortedComboBoxModel;
import fr.hedwin.ihm.fields.SpinnerEditor;
import fr.hedwin.ihm.listeners.TableCellListener;
import fr.hedwin.ihm.listeners.TransferCellListener;
import fr.hedwin.ihm.popup.RightClickPopup;
import fr.hedwin.objects.*;
import fr.hedwin.sql.DataManager;
import fr.hedwin.sql.dao.DaoFactory;
import fr.hedwin.sql.exceptions.DaoException;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;
import javax.xml.transform.Transformer;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.awt.event.*;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static fr.hedwin.ihm.components.DataTable.column;
import static javax.swing.JOptionPane.YES_NO_OPTION;

public class IHMComptes extends JPanel {

    private DataManager dataManager;
    private DataTable<TransactionTable> table;
    private JScrollPane scrollPane = new JScrollPane();

    private JFormattedTextField filterField;
    private JComboBox<DataTable.DataColumn<TransactionTable, ?>> columnCombo;

    public IHMComptes(DataManager dataManager) throws DaoException, ParseException {
        this.dataManager = dataManager;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JToolBar tools = new JToolBar(JToolBar.HORIZONTAL);
        tools.setLayout(new BoxLayout(tools, BoxLayout.X_AXIS));
        tools.setMargin(new Insets(5, 0, 5, 0));
        tools.setFloatable(false);

        JComboBox<Compte> comboBox = new JComboBox<>(dataManager.getCompteMap().values().toArray(Compte[]::new));
        comboBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> new JLabel(value.getName()));
        comboBox.setMaximumSize(comboBox.getPreferredSize());
        JButton refresh = new JButton(new FlatSVGIcon("images/refresh_dark.svg"));
        JButton add = new JButton(new FlatSVGIcon("images/add_dark.svg"));

        tools.add(refresh);
        tools.add(add);
        tools.add(comboBox);
        tools.add(Box.createRigidArea(new Dimension(50, 0)));

        JTextField from = new JFormattedTextField(new MaskFormatter("####-##-##"));
        JTextField to = new JFormattedTextField(new MaskFormatter("####-##-##"));
        JButton apply = new JButton(new FlatSVGIcon("images/buildAutoReloadChanges_dark.svg"));

        from.setText((LocalDate.now().getYear()-1)+"-08-31");
        from.setMaximumSize(from.getPreferredSize());
        from.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER) to.requestFocus();
            }
        });
        to.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        to.setMaximumSize(to.getPreferredSize());
        to.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER) apply.doClick();
            }
        });
        apply.addActionListener(e -> refresh.doClick());

        JButton properties = new JButton(new FlatSVGIcon("images/properties_dark.svg"));
        JButton unsort = new JButton(new FlatSVGIcon("images/visibilitySort_dark.svg"));
        unsort.addActionListener(e -> {
            int max = scrollPane.getVerticalScrollBar().getMaximum();
            int min = scrollPane.getVerticalScrollBar().getMinimum();
            if(scrollPane.getVerticalScrollBar().getValue() == min) for(int i = 0; i < 1000; i++){
                scrollPane.getVerticalScrollBar().setValue(min + i*(max-min)/1000 );
            }else  for(int i = 0; i < 1000; i++){
                scrollPane.getVerticalScrollBar().setValue(max - i*(max-min)/1000 );
            }
        });

        JComboBox<Integer> rowHeightCombo = new JComboBox<>(new Integer[]{20, 30, 40, 50, 60});
        rowHeightCombo.setMaximumSize(comboBox.getPreferredSize());
        rowHeightCombo.addActionListener(e -> {
            if(table != null) table.setRowHeight((int) rowHeightCombo.getSelectedItem());
        });

        tools.add(new JLabel("Du "));
        tools.add(from);
        tools.add(new JLabel(" au "));
        tools.add(to);
        tools.add(apply);
        tools.add(Box.createRigidArea(new Dimension(50, 0)));
        tools.add(new JLabel("Taille de ligne "));
        tools.add(rowHeightCombo);
        tools.add(Box.createHorizontalGlue());

        tools.add(new JLabel("Filtrer "));
        this.columnCombo = new JComboBox<>();
        columnCombo.setMaximumSize(columnCombo.getPreferredSize());
        tools.add(columnCombo);
        this.filterField = new JFormattedTextField();
        filterField.setMaximumSize(filterField.getPreferredSize());
        tools.add(filterField);
        tools.add(Box.createRigidArea(new Dimension(50, 0)));
        tools.add(unsort);
        tools.add(properties);

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        comboBox.addActionListener(e -> refresh.doClick());
        refresh.addActionListener(e -> {
            content.removeAll();
            content.repaint();
            content.revalidate();
            JProgressBar jProgressBar = new JProgressBar(JProgressBar.HORIZONTAL);
            jProgressBar.setIndeterminate(true);
            jProgressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
            content.add(jProgressBar, BorderLayout.NORTH);
            new Thread(() -> {
                try {
                    updateTable(content, ((Compte) comboBox.getSelectedItem()), from.getText(), to.getText());
                    table.setRowHeight((int) rowHeightCombo.getSelectedItem());
                } catch (DaoException | InterruptedException daoException) {
                    JOptionPane.showMessageDialog(this, daoException.getMessage());
                }
            }).start();
        });
        add.addActionListener(e -> {
            if(table != null){
                double lastSolde = (double) table.getValueAt(0, 7);
                table.insertNewRow();
                table.setValueAt(lastSolde, 0, 7);
            }
        });


        //combo selection appelle updateTable
        comboBox.setSelectedIndex(0);
        rowHeightCombo.setSelectedIndex(1);

        add(tools);
        add(content);
    }

    public void updateTable(JPanel content, Compte compte, String dateFrom, String dateTo) throws DaoException, InterruptedException {
        LinkedHashMap<Integer, TransactionTable> elements = dataManager.getDaoFactory().getTransactionDao()
                .getTableElement(compte, dataManager.getTiersMap(), dataManager.getCategorieMap(), dataManager.getPaymentTypeMap(), dateFrom, dateTo);

        LinkedList<Tiers> remboursement = new LinkedList<>(dataManager.getTiersMap().values());
        remboursement.addFirst(null);

        table = new DataTable<>(elements.values().toArray(TransactionTable[]::new), new DataTable.DataColumn[]{
                column(Integer.class, -1, "Id", false, TransactionTable::getId, null, new DefaultTableCellRenderer(){
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        JComponent jComponent = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        jComponent.setBackground(Color.decode("#1A1A1A"));
                        return jComponent;
                    }
                }, null),
                column(Tiers.class, null, "Tiers", true, TransactionTable::getTiers,
                        new ComboEditor<>(dataManager.getTiersMap().values().toArray(Tiers[]::new), () -> Comparator.comparing(Tiers::getName), Tiers::getName), new DefaultTableCellRenderer(){
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        return super.getTableCellRendererComponent(table, value != null ? ((Tiers) value).getName() : "", isSelected, hasFocus, row, column);
                    }
                }, (id, tiers) -> {
                    try {
                        dataManager.getDaoFactory().getTransactionDao().update(id, "tiers_id", preparedStatement -> {
                            try {
                                preparedStatement.setInt(1, tiers.getId());
                            } catch (SQLException sqlException) {
                                sqlException.printStackTrace();
                            }
                        });
                    } catch (DaoException e) {
                        e.printStackTrace();
                    }
                }),
                column(String.class, "", "Information", true, TransactionTable::getInformation, (id, info) -> {
                    try {
                        dataManager.getDaoFactory().getTransactionDao().update(id, "information", preparedStatement -> {
                            try {
                                preparedStatement.setString(1, info);
                            } catch (SQLException sqlException) {
                                sqlException.printStackTrace();
                            }
                        });
                    } catch (DaoException e) {
                        e.printStackTrace();
                    }
                }),
                column(Tiers.class, null, "Remboursement", true, TransactionTable::getRemboursement,
                        new ComboEditor<>(remboursement.toArray(Tiers[]::new), () -> Comparator.comparing(Tiers::getName), Tiers::getName), new DefaultTableCellRenderer(){
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        return super.getTableCellRendererComponent(table, value != null ? ((Tiers) value).getName() : "", isSelected, hasFocus, row, column);
                    }
                }, (id, rembour) -> {
                    try {
                        dataManager.getDaoFactory().getTransactionDao().update(id, "remboursement", preparedStatement -> {
                            try {
                                if(rembour != null) preparedStatement.setInt(1, rembour.getId());
                                else preparedStatement.setNull(1, java.sql.Types.INTEGER);
                            } catch (SQLException sqlException) {
                                sqlException.printStackTrace();
                            }
                        });
                    } catch (DaoException e) {
                        e.printStackTrace();
                    }
                }),
                column(Categorie.class, null, "Catégorie", true, TransactionTable::getCategorie,
                        new ComboEditor<>(dataManager.getCategorieMap().values().toArray(Categorie[]::new), () -> Comparator.comparing(Categorie::getName), Categorie::getName), new DefaultTableCellRenderer(){
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        return super.getTableCellRendererComponent(table, value != null ? ((Categorie) value).getName() : "", isSelected, hasFocus, row, column);
                    }
                }, (id, categorie) -> {
                    try {
                        dataManager.getDaoFactory().getTransactionDao().update(id, "categorie_id", preparedStatement -> {
                            try {
                                preparedStatement.setInt(1, categorie.getId());
                            } catch (SQLException sqlException) {
                                sqlException.printStackTrace();
                            }
                        });
                    } catch (DaoException e) {
                        e.printStackTrace();
                    }
                }),
                column(PaymentType.class, null, "Mode de payment", true, TransactionTable::getPaymentType,
                        new ComboEditor<>(dataManager.getPaymentTypeMap().values().toArray(PaymentType[]::new), () -> Comparator.comparing(PaymentType::getName), PaymentType::getName), new DefaultTableCellRenderer(){
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        return super.getTableCellRendererComponent(table, value != null ? ((PaymentType) value).getName() : "", isSelected, hasFocus, row, column);
                    }
                }, (id, mdp) -> {
                    try {
                        dataManager.getDaoFactory().getTransactionDao().update(id, "mdp_id", preparedStatement -> {
                            try {
                                preparedStatement.setInt(1, mdp.getId());
                            } catch (SQLException sqlException) {
                                sqlException.printStackTrace();
                            }
                        });
                    } catch (DaoException e) {
                        e.printStackTrace();
                    }
                }),
                column(Double.class, 0.0, "Montant", true, TransactionTable::getMontant, new SpinnerEditor(), new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        JLabel component = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        component.setHorizontalAlignment(SwingConstants.RIGHT);
                        double i = (Double) table.getValueAt(row, column);
                        if(i < 0) component.setForeground(Color.decode("#F8696B"));
                        else if(i > 0) component.setForeground(Color.decode("#63BE7B"));
                        else component.setForeground(Color.decode("#FFEB84"));
                        return component;
                    }
                }, (id, montant) -> {
                    try {
                        dataManager.getDaoFactory().getTransactionDao().update(id, "montant", preparedStatement -> {
                            try {
                                preparedStatement.setDouble(1, montant);
                            } catch (SQLException sqlException) {
                                sqlException.printStackTrace();
                            }
                        });
                    } catch (DaoException e) {
                        e.printStackTrace();
                    }
                }),
                column(Double.class, 0.0, "Solde", false, TransactionTable::getSolde, null, new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        JLabel component = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        component.setHorizontalAlignment(SwingConstants.RIGHT);
                        double i = (Double) table.getValueAt(row, column);
                        if(i < 0) component.setBackground(Color.decode("#F8696B"));
                        else if(i > 0) component.setBackground(Color.decode("#63BE7B"));
                        else component.setBackground(Color.decode("#FFEB84"));
                        component.setForeground(Color.decode("#1a1a1a"));
                        return component;
                    }
                }, null),
                column(LocalDate.class, LocalDate.now(), "Date", true, TransactionTable::getDate, new DateEditor(), new DefaultTableCellRenderer(){
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        return super.getTableCellRendererComponent(table, value != null ? ((LocalDate) value).toString() : "", isSelected, hasFocus, row, column);
                    }
                }, (id, localDate) -> {
                    try {
                        dataManager.getDaoFactory().getTransactionDao().update(id, "date_op", preparedStatement -> {
                            try {
                                preparedStatement.setDate(1, new java.sql.Date(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()).getTime()));
                            } catch (SQLException sqlException) {
                                sqlException.printStackTrace();
                            }
                        });
                    } catch (DaoException e) {
                        e.printStackTrace();
                    }
                }),
        });

        table.setDragEnabled(true);
        table.putClientProperty("FlatLaf.oldTransferHandler", table.getTransferHandler());
        table.setDropMode(DropMode.ON_OR_INSERT);
        table.setTransferHandler(new TransferCellListener<>(table));

        table.addOnCellChange(e -> {
            TableCellListener tcl = (TableCellListener) e.getSource();

            // attention getModel().getValueAt() et getValueAt() ne sont pas équivalent
            // par exemple si la list est triée la ligne tcl.getRow() renvoie la ligne du model (à l'initialisation des données)
            // et non celle réèl visible
            int id = (int) table.getModel().getValueAt(tcl.getRow(), 0);
            if(!Objects.equals(tcl.getNewValue(), tcl.getOldValue()) && id != -1){
                table.getColumns()[tcl.getColumn()].sendDataBase(id, tcl.getNewValue());
            }
        });

        table.addKeyAction("delete", KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), e -> {
            int[] ids = Arrays.stream(table.getSelectedRows()).map(r -> (int) table.getValueAt(r, 0)).toArray();
            int result = JOptionPane.showConfirmDialog(this,
                    "Êtes-vous sur de vouloir supprimer la/les transaction(s) "+String.join(", ", IntStream.of(ids).mapToObj(String::valueOf).toArray(String[]::new)),
                    "Suppression d'éléments", YES_NO_OPTION);
            if(result == 0) for(int r : table.getSelectedRows()) {
                try {
                    dataManager.getDaoFactory().getTransactionDao().delete((int) table.getValueAt(r, 0));
                    ((DefaultTableModel) table.getModel()).removeRow(r);
                } catch (DaoException daoException) {
                    JOptionPane.showMessageDialog(this, daoException.getMessage());
                }
            }
        });
        table.addMouseListener(new RightClickPopup.PopClickListener((e, r) -> {
            RightClickPopup menu = new RightClickPopup(table, r, row -> {
                try {
                    dataManager.getDaoFactory().getTransactionDao().delete((int) table.getValueAt(row, 0));
                    ((DefaultTableModel) table.getModel()).removeRow(r);
                } catch (DaoException daoException) {
                    JOptionPane.showMessageDialog(this, daoException.getMessage());
                }
            }, row -> {
                int id = (int) table.getValueAt(row, 0);
                Tiers tiers = (Tiers) table.getValueAt(row, 1);
                String info = (String) table.getValueAt(row, 2);
                Tiers rembour = (Tiers) table.getValueAt(row, 3);
                Categorie categorie = (Categorie) table.getValueAt(row, 4);
                PaymentType mdp = (PaymentType) table.getValueAt(row, 5);
                double montant = (double) table.getValueAt(row, 6);
                LocalDate date = (LocalDate) table.getValueAt(row, 8);

                Transaction transaction = new Transaction(id, tiers.getId(), info, rembour == null ? 0 : rembour.getId(), compte.getId(), categorie.getId(), mdp.getId(), montant, Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                try {
                    dataManager.getDaoFactory().getTransactionDao().add(transaction);
                    table.setValueAt(transaction.getId(), row, 0);
                } catch (DaoException daoException) {
                    JOptionPane.showMessageDialog(this, daoException.getMessage());
                }
            });
            menu.show(e.getComponent(), e.getX(), e.getY());
        }));

        table.initFilter(columnCombo, filterField);

        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setViewportView(table);

        content.removeAll();
        content.add(scrollPane, BorderLayout.CENTER);
    }

}
