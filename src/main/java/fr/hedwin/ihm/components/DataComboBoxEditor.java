package fr.hedwin.ihm.components;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class DataComboBoxEditor<T> implements ComboBoxEditor {
    private final JComboBox<T> comboBox;
    private final JTextField textField;
    private final SortedComboBoxModel<T> sortedComboBoxModel;

    public DataComboBoxEditor(SortedComboBoxModel<T> sortedComboBoxModel, JComboBox<T> comboBox) {
        this.comboBox = comboBox;
        this.sortedComboBoxModel = sortedComboBoxModel;
        this.textField = (JTextField) comboBox.getEditor().getEditorComponent();

        // Add a key listener to handle text field events
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterComboBoxItems();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterComboBoxItems();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterComboBoxItems();
            }

            private void filterComboBoxItems() {
                String filterText = textField.getText().toLowerCase();

                if (filterText.isEmpty()) {
                    sortedComboBoxModel.resetFilter();
                } else {
                    sortedComboBoxModel.setFilter(filterText);
                }
            }
        });
    }

    @Override
    public Component getEditorComponent() {
        return textField;
    }

    @Override
    public void setItem(Object anObject) {
        comboBox.setSelectedItem(anObject);
    }

    @Override
    public T getItem() {
        return (T) comboBox.getSelectedItem();
    }

    @Override
    public void selectAll() {
        textField.selectAll();
    }

    @Override
    public void addActionListener(ActionListener l) {
        textField.addActionListener(l);
    }

    @Override
    public void removeActionListener(ActionListener l) {
        textField.removeActionListener(l);
    }
}
