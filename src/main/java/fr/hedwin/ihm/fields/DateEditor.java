package fr.hedwin.ihm.fields;

import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.tableeditors.DateTableEditor;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DateEditor extends DefaultCellEditor {

    private DateTableEditor dateTableEditor;
    public DateEditor() {
        super(new JTextField());
        this.dateTableEditor = new DateTableEditor();
        DatePickerSettings datePickerSettings = dateTableEditor.getDatePickerSettings();
        colors.forEach(datePickerSettings::setColor);

        datePickerSettings.setColorBackgroundWeekdayLabels(Color.decode("#212121"), true);
        datePickerSettings.setColorBackgroundWeekNumberLabels(Color.decode("#212121"), true);
        datePickerSettings.setVisibleClearButton(false);
        dateTableEditor.getDatePicker().getComponentDateTextField().setEnabled(false);
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column){
        dateTableEditor.setCellEditorValue(value);
        return dateTableEditor.getDatePicker();
    }

    public Object getCellEditorValue() {
        return dateTableEditor.getCellEditorValue();
    }

    Map<DatePickerSettings.DateArea, Color> colors = new HashMap<>(){{
        put(DatePickerSettings.DateArea.CalendarBackgroundNormalDates, Color.decode("#1A1A1A"));
        put(DatePickerSettings.DateArea.BackgroundOverallCalendarPanel, Color.decode("#212121"));
        put(DatePickerSettings.DateArea.BackgroundMonthAndYearMenuLabels, Color.decode("#212121"));
        put(DatePickerSettings.DateArea.BackgroundTodayLabel, Color.decode("#212121"));
        put(DatePickerSettings.DateArea.BackgroundClearLabel, Color.decode("#212121"));
        put(DatePickerSettings.DateArea.BackgroundMonthAndYearNavigationButtons, Color.decode("#1A1A1A"));
        put(DatePickerSettings.DateArea.BackgroundTopLeftLabelAboveWeekNumbers, Color.decode("#212121"));
        put(DatePickerSettings.DateArea.CalendarBackgroundSelectedDate, Color.decode("#212121"));
        put(DatePickerSettings.DateArea.CalendarBorderSelectedDate, Color.decode("#292929"));

        put(DatePickerSettings.DateArea.BackgroundCalendarPanelLabelsOnHover, Color.decode("#3F3F3F"));
        put(DatePickerSettings.DateArea.BackgroundTopLeftLabelAboveWeekNumbers, Color.decode("#3F3F3F"));
        put(DatePickerSettings.DateArea.CalendarBackgroundSelectedDate, Color.decode("#3F3F3F"));

        put(DatePickerSettings.DateArea.TextFieldBackgroundValidDate, Color.decode("#212121"));
        put(DatePickerSettings.DateArea.TextFieldBackgroundInvalidDate, Color.decode("#212121"));
        put(DatePickerSettings.DateArea.TextFieldBackgroundDisabled, Color.decode("#212121"));
        put(DatePickerSettings.DateArea.TextFieldBackgroundVetoedDate, Color.decode("#212121"));
    }};

}
