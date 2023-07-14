package fr.hedwin.ihm.popup;

import fr.hedwin.ihm.components.DataTable;
import fr.hedwin.objects.*;
import fr.hedwin.sql.DataManager;
import fr.hedwin.sql.dao.DaoFactory;
import fr.hedwin.sql.exceptions.DaoException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static javax.swing.JOptionPane.YES_NO_OPTION;

public class RightClickPopup extends JPopupMenu {


    public RightClickPopup(DataTable<?> table, int rowRightClicked, Consumer<Integer> onDeleteRow, Consumer<Integer> onAddRow) {
        int id = (int) table.getValueAt(rowRightClicked, 0);

        int[] rows = table.getSelectedRows();
        if(rows.length > 1) {
            JMenuItem menu = new JMenuItem("Supprimer toute les lignes selectionnées");
            menu.addActionListener(e -> {
                int[] ids = Arrays.stream(rows).map(r -> (int) table.getValueAt(r, 0)).toArray();
                int result = JOptionPane.showConfirmDialog(this,
                        "Êtes-vous sur de vouloir supprimer la/les transaction(s) "+String.join(", ", IntStream.of(ids).mapToObj(String::valueOf).toArray(String[]::new)),
                        "Suppression d'éléments", YES_NO_OPTION);
                if(result == 0) for(int i=0;i<rows.length;i++){
                    onDeleteRow.accept(rows[i]-i);
                }
            });
            add(menu);
        }

        JMenuItem del = new JMenuItem("Supprimer la ligne");
        del.addActionListener(e -> {
            int result = 0;
            if(id != -1){
                 result = JOptionPane.showConfirmDialog(this,
                        "Êtes-vous sur de vouloir supprimer la transaction "+table.getValueAt(rowRightClicked, 0),
                        "Suppression d'un élément", YES_NO_OPTION);
            }
            if(result == 0) onDeleteRow.accept(rowRightClicked);
        });
        add(del);

        addSeparator();

        if(rows.length > 1){
            JMenuItem add = new JMenuItem("Envoyer les lignes dans la base");
            add.addActionListener(e -> {
                for (int i = 0; i < rows.length; i++) {
                    int idx = (int) table.getValueAt(i, 0);
                    if(idx == -1) onAddRow.accept(i);
                }
            });
            add(add);
        }

        if(id == -1){
            JMenuItem add = new JMenuItem("Envoyer la ligne dans la base");
            add.addActionListener(e -> onAddRow.accept(rowRightClicked));
            add(add);
        }

    }

    public static class PopClickListener extends MouseAdapter {
        private final BiConsumer<MouseEvent, Integer> eventConsumer;

        public PopClickListener(BiConsumer<MouseEvent, Integer> eventConsumer){
            this.eventConsumer = eventConsumer;
        }

        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger())
                doPop(e);
        }

        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger())
                doPop(e);
        }

        private void doPop(MouseEvent e) {
            if(e.getComponent() instanceof DataTable<?> table){
                int r = table.rowAtPoint(e.getPoint());
                //if (r >= 0 && r < table.getRowCount()) table.setRowSelectionInterval(r, r);
                //else table.clearSelection();

                //int rowindex = table.getSelectedRow();
                //if (rowindex < 0) return;
                eventConsumer.accept(e, r);
            }
        }
    }

}
