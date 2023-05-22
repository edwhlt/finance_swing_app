package fr.hedwin.ihm;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import fr.hedwin.ihm.components.DataTable;
import fr.hedwin.ihm.fields.ComboEditor;
import fr.hedwin.ihm.fields.DateEditor;
import fr.hedwin.ihm.fields.SpinnerEditor;
import fr.hedwin.ihm.listeners.TableCellListener;
import fr.hedwin.ihm.listeners.TransferCellListener;
import fr.hedwin.ihm.popup.RightClickPopup;
import fr.hedwin.objects.*;
import fr.hedwin.sql.DataManager;
import fr.hedwin.sql.dao.DaoFactory;
import fr.hedwin.sql.exceptions.DaoException;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static fr.hedwin.ihm.components.DataTable.column;

public class IHMDatas extends JPanel {

    private DataManager dataManager;
    private DataTable<?> table;

    public IHMDatas(DataManager dataManager) {
        this.dataManager = dataManager;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JToolBar tools = new JToolBar(JToolBar.HORIZONTAL);
        tools.setLayout(new BoxLayout(tools, BoxLayout.X_AXIS));
        tools.setMargin(new Insets(5, 0, 5, 0));
        tools.setFloatable(false);

        JComboBox<String> comboBox = new JComboBox<>(new String[]{"Tiers", "Comptes", "Categories", "Mode de payment"});
        comboBox.setMaximumSize(comboBox.getPreferredSize());
        JButton refresh = new JButton(new FlatSVGIcon("images/refresh_dark.svg"));
        JButton add = new JButton(new FlatSVGIcon("images/add_dark.svg"));

        tools.add(refresh);
        tools.add(add);
        tools.add(comboBox);
        tools.add(Box.createRigidArea(new Dimension(50, 0)));

        JButton properties = new JButton(new FlatSVGIcon("images/properties_dark.svg"));

        JComboBox<Integer> rowHeightCombo = new JComboBox<>(new Integer[]{20, 30, 40, 50, 60});
        rowHeightCombo.setMaximumSize(comboBox.getPreferredSize());
        rowHeightCombo.addActionListener(e -> {
            if(table != null) table.setRowHeight((int) rowHeightCombo.getSelectedItem());
        });

        tools.add(new JLabel("Taille de ligne "));
        tools.add(rowHeightCombo);
        tools.add(Box.createHorizontalGlue());
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
                    updateTable(content, ((String) comboBox.getSelectedItem()));
                    table.setRowHeight((int) rowHeightCombo.getSelectedItem());
                } catch (DaoException | InterruptedException daoException) {
                        JOptionPane.showMessageDialog(null, daoException.getMessage());
                }
            }).start();
        });
        add.addActionListener(e -> {
            if(table != null) table.insertNewRow();
        });


        //combo selection appelle updateTable
        comboBox.setSelectedIndex(0);
        rowHeightCombo.setSelectedIndex(1);

        add(tools);
        add(content);
    }

    public void updateTable(JPanel content, String string) throws DaoException, InterruptedException {
        if(string.equalsIgnoreCase("tiers")) {
            table = new DataTable<>(dataManager.getTiersMap().values().toArray(Tiers[]::new), new DataTable.DataColumn[]{
                    column(Integer.class, -1, "Id", false, Tiers::getId, null, new DefaultTableCellRenderer(){
                        @Override
                        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                            JComponent jComponent = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                            //jComponent.setBackground(jComponent.getBackground().darker());
                            return jComponent;
                        }
                    }, null),
                    column(String.class, "", "Nom", true, Tiers::getName, (id, name) -> {
                        try {
                            dataManager.updateTiers(id, name);
                        } catch (DaoException e) {
                            e.printStackTrace();
                        }
                    }),
                    column(String.class, "", "Regex", true, Tiers::getRegex, (id, regex) -> {
                        try {
                            dataManager.updateTiersRegex(id, regex);
                        } catch (DaoException e) {
                            e.printStackTrace();
                        }
                    })
            });
            table.addKeyAction("delete", KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), e -> {
                for(int r : table.getSelectedRows()) {
                    try {
                        dataManager.deleteTiers((int) table.getValueAt(r, 0));
                        ((DefaultTableModel) table.getModel()).removeRow(r);
                    } catch (DaoException daoException) {
                        JOptionPane.showMessageDialog(null, daoException.getMessage());
                    }
                }
            });
            table.addMouseListener(new RightClickPopup.PopClickListener((e, r) -> {
                RightClickPopup menu = new RightClickPopup(table, r, row -> {
                    try {
                        dataManager.deleteTiers((int) table.getValueAt(r, 0));
                        ((DefaultTableModel) table.getModel()).removeRow(r);
                    } catch (DaoException daoException) {
                        JOptionPane.showMessageDialog(null, daoException.getMessage());
                    }
                }, row -> {
                    String name = (String) table.getValueAt(row, 1);
                    String cm_name = (String) table.getValueAt(row, 2);
                    try {
                        Tiers tiers = dataManager.addTiers(name, cm_name);
                        table.setValueAt(tiers.getId(), row, 0);
                    } catch (DaoException daoException) {
                        JOptionPane.showMessageDialog(null, daoException.getMessage());
                    }
                });
                menu.show(e.getComponent(), e.getX(), e.getY());
            }));
        }else if(string.equalsIgnoreCase("comptes")){
            table = new DataTable<>(dataManager.getCompteMap().values().toArray(Compte[]::new), new DataTable.DataColumn[]{
                    column(Integer.class, -1, "Id", false, Compte::getId, null, new DefaultTableCellRenderer(){
                        @Override
                        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                            JComponent jComponent = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                            //jComponent.setBackground(jComponent.getBackground().darker());
                            return jComponent;
                        }
                    }, null),
                    column(String.class, "", "Nom", true, Compte::getName, (id, name) -> {
                        try {
                            dataManager.updateCompte(id, name);
                        } catch (DaoException e) {
                            e.printStackTrace();
                        }
                    }),
            });
            table.addKeyAction("delete", KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), e -> {
                for(int r : table.getSelectedRows()) {
                    try {
                        dataManager.deleteCompte((int) table.getValueAt(r, 0));
                        ((DefaultTableModel) table.getModel()).removeRow(r);
                    } catch (DaoException daoException) {
                        JOptionPane.showMessageDialog(null, daoException.getMessage());
                    }
                }
            });
            table.addMouseListener(new RightClickPopup.PopClickListener((e, r) -> {
                RightClickPopup menu = new RightClickPopup(table, r, row -> {
                    try {
                        dataManager.deleteCompte((int) table.getValueAt(r, 0));
                        ((DefaultTableModel) table.getModel()).removeRow(r);
                    } catch (DaoException daoException) {
                        JOptionPane.showMessageDialog(null, daoException.getMessage());
                    }
                }, row -> {
                    String name = (String) table.getValueAt(row, 1);
                    try {
                        Compte compte = dataManager.addCompte(name);
                        table.setValueAt(compte.getId(), row, 0);
                    } catch (DaoException daoException) {
                        JOptionPane.showMessageDialog(null, daoException.getMessage());
                    }
                });
                menu.show(e.getComponent(), e.getX(), e.getY());
            }));
        }else if(string.equalsIgnoreCase("categories")){
            table = new DataTable<>(dataManager.getCategorieMap().values().toArray(Categorie[]::new), new DataTable.DataColumn[]{
                    column(Integer.class, -1, "Id", false, Categorie::getId, null, new DefaultTableCellRenderer(){
                        @Override
                        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                            JComponent jComponent = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                            //jComponent.setBackground(jComponent.getBackground().darker());
                            return jComponent;
                        }
                    }, null),
                    column(String.class, "", "Nom", true, Categorie::getName, (id, name) -> {
                        try {
                            dataManager.updateCategorie(id, name);
                        } catch (DaoException e) {
                            e.printStackTrace();
                        }
                    }),
            });
            table.addKeyAction("delete", KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), e -> {
                for(int r : table.getSelectedRows()) {
                    try {
                        dataManager.deleteCategorie((int) table.getValueAt(r, 0));
                        ((DefaultTableModel) table.getModel()).removeRow(r);
                    } catch (DaoException daoException) {
                        JOptionPane.showMessageDialog(null, daoException.getMessage());
                    }
                }
            });
            table.addMouseListener(new RightClickPopup.PopClickListener((e, r) -> {
                RightClickPopup menu = new RightClickPopup(table, r, row -> {
                    try {
                        dataManager.deleteCategorie((int) table.getValueAt(r, 0));
                        ((DefaultTableModel) table.getModel()).removeRow(r);
                    } catch (DaoException daoException) {
                        JOptionPane.showMessageDialog(null, daoException.getMessage());
                    }
                }, row -> {
                    String name = (String) table.getValueAt(row, 1);
                    try {
                        Categorie categorie = dataManager.addCategorie(name);
                        table.setValueAt(categorie.getId(), row, 0);
                    } catch (DaoException daoException) {
                        JOptionPane.showMessageDialog(null, daoException.getMessage());
                    }
                });
                menu.show(e.getComponent(), e.getX(), e.getY());
            }));
        }else{
            table = new DataTable<>(dataManager.getPaymentTypeMap().values().toArray(PaymentType[]::new), new DataTable.DataColumn[]{
                    column(Integer.class, -1, "Id", false, PaymentType::getId, null, new DefaultTableCellRenderer(){
                        @Override
                        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                            JComponent jComponent = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                            //jComponent.setBackground(jComponent.getBackground().darker());
                            return jComponent;
                        }
                    }, null),
                    column(String.class, "", "Nom", true, PaymentType::getName, (id, name) -> {
                        try {
                            dataManager.updatePaymentType(id, name);
                        } catch (DaoException e) {
                            e.printStackTrace();
                        }
                    }),
                    column(String.class, "", "Regex", true, PaymentType::getRegex, (id, regex) -> {
                        try {
                            dataManager.updatePaymentTypeRegex(id, regex);
                        } catch (DaoException e) {
                            e.printStackTrace();
                        }
                    })
            });
            table.addKeyAction("delete", KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), e -> {
                for(int r : table.getSelectedRows()) {
                    try {
                        dataManager.deletePaymentType((int) table.getValueAt(r, 0));
                        ((DefaultTableModel) table.getModel()).removeRow(r);
                    } catch (DaoException daoException) {
                        JOptionPane.showMessageDialog(null, daoException.getMessage());
                    }
                }
            });
            table.addMouseListener(new RightClickPopup.PopClickListener((e, r) -> {
                RightClickPopup menu = new RightClickPopup(table, r, row -> {
                    try {
                        dataManager.deletePaymentType((int) table.getValueAt(r, 0));
                        ((DefaultTableModel) table.getModel()).removeRow(r);
                    } catch (DaoException daoException) {
                        JOptionPane.showMessageDialog(null, daoException.getMessage());
                    }
                }, row -> {
                    String name = (String) table.getValueAt(row, 1);
                    String cm_name = (String) table.getValueAt(row, 2);
                    try {
                        PaymentType paymentType = dataManager.addPaymentType(name, cm_name);
                        table.setValueAt(paymentType.getId(), row, 0);
                    } catch (DaoException daoException) {
                        JOptionPane.showMessageDialog(null, daoException.getMessage());
                    }
                });
                menu.show(e.getComponent(), e.getX(), e.getY());
            }));
        }

        table.setDragEnabled(true);
        table.putClientProperty("FlatLaf.oldTransferHandler", table.getTransferHandler());
        table.setDropMode(DropMode.ON_OR_INSERT);
        table.setTransferHandler(new TransferCellListener<>(table));

        table.addOnCellChange(e -> {
            TableCellListener tcl = (TableCellListener) e.getSource();
            int id = (int) table.getModel().getValueAt(tcl.getRow(), 0);
            if(!Objects.equals(tcl.getNewValue(), tcl.getOldValue()) && id != -1){
                table.getColumns()[tcl.getColumn()].sendDataBase(id, tcl.getNewValue());
            }
        });

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setViewportView(table);

        content.removeAll();
        content.add(scrollPane, BorderLayout.CENTER);
    }


}
