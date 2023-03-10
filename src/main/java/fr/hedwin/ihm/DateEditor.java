package fr.hedwin.ihm;

import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.Arrays;
import java.util.Date;

public class DateEditor extends AbstractCellEditor implements TableCellEditor {
    private JDateChooser dateChooser = new JDateChooser();

    public DateEditor() {
        for (Component component : dateChooser.getJCalendar().getDayChooser().getComponents()) {
            System.out.println(component.getName());
        }
    }

    public Component getTableCellEditorComponent(JTable var1, Object var2, boolean var3, int var4, int var5) {
        Date var6 = null;
        if (var2 instanceof Date) {
            var6 = (Date)var2;
        }

        this.dateChooser.setDate(var6);
        return this.dateChooser;
    }

    public Object getCellEditorValue() {
        return this.dateChooser.getDate();
    }
}
