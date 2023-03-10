package fr.hedwin.ihm.components;

import fr.hedwin.ihm.listeners.TableCellListener;
import fr.hedwin.ihm.listeners.TransferCellListener;
import fr.hedwin.ihm.popup.RightClickPopup;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static javax.swing.SortOrder.*;

public class DataTable<T> extends JTable {

    private final DataColumn<T,?>[] columns;

    public DataColumn<T, ?>[] getColumns() {
        return columns;
    }

    public static class DataColumn<T, R> {
        private Class<R> type;
        private R defaultValue;
        private String name;
        private boolean editable;
        private Function<T, R> columnValue;
        private BiConsumer<Integer, R> valueSendDataBase;
        private TableCellEditor tableCellEditor;
        private TableCellRenderer tableCellRenderer;

        public DataColumn(Class<R> type, R defaultValue, String name, boolean editable, Function<T, R> columnValue, BiConsumer<Integer, R> valueSendDataBase){
            this.type = type;
            this.defaultValue = defaultValue;
            this.name = name;
            this.editable = editable;
            this.columnValue = columnValue;
            this.valueSendDataBase = valueSendDataBase;
        }

        public DataColumn(Class<R> type, R defaultValue, String name, boolean editable, Function<T, R> columnValue, TableCellEditor tableCellEditor, TableCellRenderer tableCellRenderer, BiConsumer<Integer, R> valueSendDataBase){
            this.type = type;
            this.defaultValue = defaultValue;
            this.name = name;
            this.editable = editable;
            this.columnValue = columnValue;
            this.tableCellEditor = tableCellEditor;
            this.tableCellRenderer = tableCellRenderer;
            this.valueSendDataBase = valueSendDataBase;
        }

        public Class<R> getType() {
            return type;
        }

        public R getColumnValue(T t) {
            return columnValue.apply(t);
        }

        public R getDefaultValue() {
            return defaultValue;
        }

        public String getName() {
            return name;
        }

        public boolean isEditable() {
            return editable;
        }

        public void sendDataBase(int id, Object r) {
            valueSendDataBase.accept(id, (R) r);
        }

    }

    public DataTable(T[][] data, DataColumn<T, ?>[] column){
        this.columns = column;
        setModel(new DefaultTableModel(data, Arrays.stream(column).map(DataColumn::getName).toArray(String[]::new)){
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return column[columnIndex].getType();
            }
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return column[columnIndex].isEditable();
            }
        });
        TableColumnModel cm = getColumnModel();
        for (int i = 0; i < column.length; i++) {
            TableCellEditor tableCellEditor = column[i].tableCellEditor;
            TableCellRenderer tableCellRenderer = column[i].tableCellRenderer;
            if(tableCellEditor != null) cm.getColumn(i).setCellEditor(column[i].tableCellEditor);
            if(tableCellRenderer != null) cm.getColumn(i).setCellRenderer(column[i].tableCellRenderer);
        }
        getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer(){
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JComponent jComponent = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                jComponent.setBackground(Color.decode("#1A1A1A"));
                return jComponent;
            }
        });

        for (int i = 0; i < columns.length; i++) {
            if(columns[i].getType().isAssignableFrom(Double.class)) getColumnModel().getColumn(i).setMaxWidth(70);
            if(columns[i].getType().isAssignableFrom(Integer.class)) getColumnModel().getColumn(i).setMaxWidth(70);
        }

        setAutoCreateRowSorter(true);
        setShowHorizontalLines(true);
    }

    public DataTable(T[] datas, DataColumn<T, ?>[] column){
        this.columns = column;
        Object[][] values = Arrays.stream(datas).map(t -> {
            Object[] objects = new Object[column.length];
            for (int i = 0; i < column.length; i++) {
                objects[i] = column[i].getColumnValue(t);
            }
            return objects;
        }).toArray(Object[][]::new);

        setModel(new DefaultTableModel(values, Arrays.stream(column).map(DataColumn::getName).toArray(String[]::new)){
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return column[columnIndex].getType();
            }
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return column[columnIndex].isEditable();
            }
        });
        TableColumnModel cm = getColumnModel();
        for (int i = 0; i < column.length; i++) {
            TableCellEditor tableCellEditor = column[i].tableCellEditor;
            TableCellRenderer tableCellRenderer = column[i].tableCellRenderer;
            if(tableCellEditor != null) cm.getColumn(i).setCellEditor(column[i].tableCellEditor);
            if(tableCellRenderer != null) cm.getColumn(i).setCellRenderer(column[i].tableCellRenderer);
        }
        getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer(){
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JComponent jComponent = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                jComponent.setBackground(Color.decode("#1A1A1A"));
                return jComponent;
            }
        });

        for (int i = 0; i < columns.length; i++) {
            if(columns[i].getType().isAssignableFrom(Double.class)) getColumnModel().getColumn(i).setMaxWidth(70);
            if(columns[i].getType().isAssignableFrom(Integer.class)) getColumnModel().getColumn(i).setMaxWidth(70);
        }

        setAutoCreateRowSorter(true);
        setShowHorizontalLines(true);
    }

    private JComboBox<DataColumn<T, ?>> comboBox;
    private JTextField filterField;

    public void initFilter(JComboBox<DataColumn<T, ?>> comboBox, JTextField jTextField){
        this.comboBox = comboBox;
        this.filterField = jTextField;

        int idx = this.comboBox.getSelectedIndex();
        this.comboBox.removeAllItems();
        for (DataColumn<T, ?> column : columns) {
            this.comboBox.addItem(column);
        }
        this.comboBox.setSelectedIndex(Math.max(idx, 0));
        this.comboBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> new JLabel(value.getName()));

        filterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filter();

            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

    }

    public void filter(){
        DataColumn<T, ?> dataColumn = (DataColumn<T, ?>) comboBox.getSelectedItem();
        String value = filterField.getText();

        int columnIndex = getColumn(dataColumn.getName()).getModelIndex();

        RowFilter<TableModel, Object> rf;
        try {
            //(?i) to ignore case
            rf = RowFilter.regexFilter("(?i)"+value, columnIndex);
        } catch (java.util.regex.PatternSyntaxException ee) {
            return;
        }
        TableRowSorter<TableModel> tableRowSorter = new TableRowSorter<>(getModel());
        tableRowSorter.setRowFilter(rf);
        setRowSorter(tableRowSorter);
    }

    public void insertRow(int row, T t){
        Object[] ob = new Object[columns.length];
        for (int i = 0; i < columns.length; i++) {
            ob[i] = columns[i].getColumnValue(t);
        }
        ((DefaultTableModel) getModel()).insertRow(row, ob);
    }

    public void insertNewRow(){
        Object[] ob = new Object[columns.length];
        for (int i = 0; i < columns.length; i++) {
            ob[i] = columns[i].getDefaultValue();
        }
        ((DefaultTableModel) getModel()).insertRow(0, ob);
    }

    public void addOnCellChange(Consumer<ActionEvent> action){
        addPropertyChangeListener(new TableCellListener(this, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                action.accept(e);
            }
        }));
    }

    public void addKeyAction(String actionName, KeyStroke key, Consumer<ActionEvent> action){
        getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(key, actionName);
        getActionMap().put(actionName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.accept(e);
            }
        });
    }

    public static <T, R> DataColumn<T, R> column(Class<R> type, R defaultValue, String name, boolean editable, Function<T, R> columnValue, TableCellEditor tableCellEditor, TableCellRenderer tableCellRenderer, BiConsumer<Integer, R> sendToBase){
        return new DataColumn<>(type, defaultValue, name, editable, columnValue, tableCellEditor, tableCellRenderer, sendToBase);
    }

    public static <T, R> DataColumn<T, R> column(Class<R> type, R defaultValue, String name, boolean editable, Function<T, R> columnValue, BiConsumer<Integer, R> sendToBase){
        return new DataColumn<>(type, defaultValue, name, editable, columnValue, sendToBase);
    }


}
