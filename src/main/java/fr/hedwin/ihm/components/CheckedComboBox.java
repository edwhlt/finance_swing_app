package fr.hedwin.ihm.components;

import javax.accessibility.Accessible;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboPopup;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class CheckedComboBox<E> extends JComboBox<E> {
    private boolean keepOpen;
    private transient ActionListener listener;

    public CheckedComboBox() {
        super();
    }
    protected CheckedComboBox(ComboBoxModel<E> aModel) {
        super(aModel);
    }
    protected CheckedComboBox(E[] m) {
        super(m);
    }
    @Override public Dimension getPreferredSize() {
        return new Dimension(200, 20);
    }
    @Override public void updateUI() {
        setRenderer(null);
        removeActionListener(listener);
        super.updateUI();
        listener = e -> {
            if ((e.getModifiers() & InputEvent.MOUSE_EVENT_MASK) != 0) {
                updateItem(getSelectedIndex());
                keepOpen = true;
            }
        };
        addActionListener(listener);
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "checkbox-select");
        getActionMap().put("checkbox-select", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                Accessible a = getAccessibleContext().getAccessibleChild(0);
                if (a instanceof BasicComboPopup) {
                    BasicComboPopup pop = (BasicComboPopup) a;
                    updateItem(pop.getList().getSelectedIndex());
                }
            }
        });
    }
    private Map<Integer, Boolean> selected = new HashMap<>();

    private void updateItem(int index) {
        if (isPopupVisible()) {
            E item = getItemAt(index);
            if(!selected.containsKey(index)) selected.put(index, false);
            selected.replace(index, !selected.get(index));
            removeItemAt(index);
            insertItemAt(item, index);
            setSelectedItem(item);
        }
    }

    @Override public void setPopupVisible(boolean v) {
        if (keepOpen) {
            keepOpen = false;
        } else {
            super.setPopupVisible(v);
        }
    }
}