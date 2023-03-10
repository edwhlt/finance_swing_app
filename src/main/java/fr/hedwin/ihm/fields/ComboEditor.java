package fr.hedwin.ihm.fields;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Supplier;

public class ComboEditor<T> extends DefaultCellEditor {

    private JComboBox<T> comboBox;
    private Function<T, String> renderer;

    public ComboEditor(T[] elements, Function<T, String> renderer) {
        super(new JTextField());
        this.renderer = renderer;
        comboBox = new JComboBox<>(elements);
        comboBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> value != null ? new JLabel(renderer.apply(value)) : new JLabel(""));
    }

    public ComboEditor(T[] elements, Supplier<Comparator<T>> sort, Function<T, String> renderer) {
        super(new JTextField());
        this.renderer = renderer;
        comboBox = new JComboBox<>(new SortedComboBoxModel<>(elements, sort.get()));
        comboBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> value != null ? new JLabel(renderer.apply(value)) : new JLabel(""));
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column){
        if(value != null) comboBox.setSelectedItem(value);
        return comboBox;
    }

    public Object getCellEditorValue() {
        return comboBox.getSelectedItem();
    }

}
