package fr.hedwin.ihm.fields;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Supplier;

public class ComboEditor<T> extends DefaultCellEditor {

    private final DefaultComboBoxModel<T> comboBoxModel;
    private final JComboBox<T> comboBox;
    private final Function<T, String> renderer;

    public ComboEditor(T[] elements, Function<T, String> renderer) {
        super(new JTextField());
        this.renderer = renderer;
        this.comboBoxModel = new DefaultComboBoxModel<>(elements);
        comboBox = new JComboBox<>(comboBoxModel);
        comboBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> value != null ? new JLabel(renderer.apply(value)) : new JLabel(""));
    }

    public ComboEditor(T[] elements, Comparator<T> sort, Function<T, String> renderer) {
        super(new JTextField());
        this.renderer = renderer;
        this.comboBoxModel = new SortedComboBoxModel<>(elements, sort);
        comboBox = new JComboBox<>(comboBoxModel);
        comboBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> value != null ? new JLabel(renderer.apply(value)) : new JLabel(""));
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column){
        if(value != null) comboBox.setSelectedItem(value);
        return comboBox;
    }

    public Object getCellEditorValue() {
        return comboBox.getSelectedItem();
    }

    public JComboBox<T> getComboBox() {
        return comboBox;
    }

    public DefaultComboBoxModel<T> getComboBoxModel() {
        return comboBoxModel;
    }
}
