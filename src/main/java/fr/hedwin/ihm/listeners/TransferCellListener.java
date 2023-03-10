package fr.hedwin.ihm.listeners;

import fr.hedwin.ihm.components.DataTable;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.io.IOException;

public class TransferCellListener<T> extends TransferHandler {

    private final DataTable<T> jTable;
    public TransferCellListener(DataTable<T> jTable) {
        this.jTable = jTable;
    }

    protected Transferable createTransferable(JComponent c) {
        DataTable<T> dataTable = (DataTable<T>) c;
        if(dataTable.getSelectionModel().isSelectionEmpty() || !dataTable.getColumns()[dataTable.getSelectedColumn()].isEditable()) return null;
        return new StringSelection(dataTable.getSelectedRow()+":"+dataTable.getSelectedColumn());
    }

    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    public boolean canImport(TransferSupport support) {
        JTable.DropLocation dl = (JTable.DropLocation) support.getDropLocation();
        boolean sameColumn = false;
        try {
        String[] cell = ((String) support.getTransferable().getTransferData(DataFlavor.stringFlavor)).split(":");
        sameColumn = jTable.getColumnClass(Integer.parseInt(cell[1])) == jTable.getColumnClass(dl.getColumn());
        } catch (UnsupportedFlavorException | IOException e) {
        e.printStackTrace();
        }
        boolean b = support.getComponent() == jTable && support.isDrop() && sameColumn;
        jTable.setCursor(b ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
        return b;
    }

    public boolean importData(TransferSupport support) {
        JTable target = (JTable) support.getComponent();
        JTable.DropLocation dl = (JTable.DropLocation) support.getDropLocation();
        target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        try {
            String[] cell = ((String) support.getTransferable().getTransferData(DataFlavor.stringFlavor)).split(":");
            Object v = jTable.getValueAt(Integer.parseInt(cell[0]), Integer.parseInt(cell[1]));
            jTable.setValueAt(v, dl.getRow(), dl.getColumn());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    protected void exportDone(JComponent c, Transferable t, int act) {
        if ((act == TransferHandler.MOVE) || (act == TransferHandler.NONE)) {
            jTable.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

}
