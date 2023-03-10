package fr.hedwin.ihm.fields;

import javax.swing.*;
import java.awt.*;

public class SpinnerEditor extends DefaultCellEditor {

    private DoubleSpinner sp;
    private JSpinner.DefaultEditor defaultEditor;
    private JTextField text;
    // Initialise le spinner
    public SpinnerEditor() {
        super(new JTextField());
        sp = new DoubleSpinner();
        defaultEditor = ((DoubleSpinner.DefaultEditor)sp.getEditor());
        text = defaultEditor.getTextField();
    }
    // Prépare le composant spinner et retourne-le
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column){
        sp.setValue(value);
        return sp;
    }
    // Renvoie la valeur actuelle du spinners
    public Object getCellEditorValue() {
        return sp.getValue();
    }
}
