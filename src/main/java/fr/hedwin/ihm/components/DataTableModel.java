package fr.hedwin.ihm.components;

import javax.swing.table.DefaultTableModel;
import java.util.HashMap;
import java.util.Map;

public class DataTableModel extends DefaultTableModel {
    private final Map<Integer, Boolean> columnEditable = new HashMap<>();

    public DataTableModel(Object[] columnNames, int rowCount){
        super(columnNames, rowCount);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        Boolean editable = columnEditable.get(column);
        return editable != null && editable;
    }

    public void setColumnEditable(int column, boolean editable) {
        columnEditable.put(column, editable);
    }
}
