package fr.hedwin.ihm.importtab;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import fr.hedwin.ihm.components.DataTable;
import fr.hedwin.objects.Tiers;
import fr.hedwin.sql.DataManager;
import fr.hedwin.sql.exceptions.DaoException;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static fr.hedwin.ihm.components.DataTable.column;

public class TiersChoosePanel extends JDialog {
    private final JFrame parent;

    public static Point point;

    public TiersChoosePanel(JFrame parent, DataManager dataManager, String libel, Map<Integer, Tiers> tiers, Consumer<Tiers> onApply, Consumer<Integer> onSkip){
        super(parent, "Choisir le tiers de : "+libel, true);
        this.parent = parent;

        setLayout(new BorderLayout());

        DataTable.DataColumn<Tiers, ?>[] dataColumn = new DataTable.DataColumn[]{
                column(Integer.class, -1, "Id", false, Tiers::getId, null, new DefaultTableCellRenderer(){
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        JComponent jComponent = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        //jComponent.setBackground(jComponent.getBackground().darker());
                        return jComponent;
                    }
                }, null),
                column(String.class, "", "Nom", false, Tiers::getName, (id, name) -> {
                    try {
                        dataManager.updateTiers(id, name);
                    } catch (DaoException e) {
                        e.printStackTrace();
                    }
                }),
                column(String.class, "", "Regex", false, Tiers::getRegex, (id, regex) -> {
                    try {
                        dataManager.updateTiersRegex(id, regex);
                    } catch (DaoException e) {
                        e.printStackTrace();
                    }
                })
        };

        DataTable<Tiers> table = new DataTable<>(dataManager.getTiersMap().values().toArray(Tiers[]::new), dataColumn);

        JToolBar tools = new JToolBar();
        add(tools, BorderLayout.NORTH);

        tools.add(Box.createHorizontalGlue());
        JComboBox<DataTable.DataColumn<Tiers, ?>> columnCombo = new JComboBox<>(dataColumn);
        columnCombo.setSelectedIndex(1);
        JFormattedTextField jFormattedTextField = new JFormattedTextField();

        tools.add(columnCombo);
        tools.add(jFormattedTextField);
        table.initFilter(columnCombo, jFormattedTextField);


        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setViewportView(table);
        add(scrollPane, BorderLayout.CENTER);

        JButton copyLabel = new JButton(new FlatSVGIcon("images/copy_dark.svg"));
        JButton previous = new JButton(new FlatSVGIcon("images/back_dark.svg"));
        JButton skip = new JButton(new FlatSVGIcon("images/forward_dark.svg"));
        JButton skipAll = new JButton(new FlatSVGIcon("images/cancel_dark.svg"));
        JButton create = new JButton(new FlatSVGIcon("images/addRemoteDatasource_dark.svg"));
        JButton send = new JButton(new FlatSVGIcon("images/send_dark.svg"));

        copyLabel.addActionListener(e -> {
            StringSelection selection = new StringSelection(libel);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, null);
        });

        previous.addActionListener(e -> {
            point = getLocation();
            dispose();
            onSkip.accept(-1);
        });
        skip.addActionListener(e -> {
            point = getLocation();
            dispose();
            onSkip.accept(1);
        });
        skipAll.addActionListener(e -> {
            point = getLocation();
            dispose();
            onSkip.accept(Integer.MAX_VALUE);
        });

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                point = getLocation();
                dispose();
                onSkip.accept(Integer.MAX_VALUE);
            }
        });

        create.addActionListener(e -> new CreateTiersFrame(parent, dataManager, libel, ntiers -> {
            tiers.put(ntiers.getId(), ntiers);
            point = getLocation();
            dispose();
            onApply.accept(ntiers);
        }));

        send.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                Tiers tie = tiers.get((int) table.getValueAt(row, 0));
                point = getLocation();
                dispose();
                onApply.accept(tie);
            }else {
                JOptionPane.showMessageDialog(this, "Please select a row.");
            }
        });

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    // Get the selected row
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        Tiers tie = tiers.get((int) table.getValueAt(row, 0));
                        point = getLocation();
                        dispose();
                        onApply.accept(tie);
                    }else {
                        JOptionPane.showMessageDialog(TiersChoosePanel.this, "Please select a row.");
                    }
                }
            }
        });

        tools.add(copyLabel);
        tools.add(Box.createHorizontalGlue());
        tools.add(previous);
        tools.add(skip);
        tools.add(skipAll);
        tools.add(Box.createHorizontalGlue());
        tools.add(create);
        tools.add(Box.createHorizontalGlue());
        tools.add(send);
        pack();

        if(point == null) point = getCenterOfParentFrame();
        setLocation(point);
        setVisible(true);
    }

    private Point getCenterOfParentFrame(){
        int parentX = parent.getLocation().x;
        int parentY = parent.getLocation().y;
        int parentWidth = parent.getWidth();
        int parentHeight = parent.getHeight();

        // Calculating center coordinates and subtracting half the width and height of the frame
        int centerX = parentX + parentWidth / 2 - this.getWidth() / 2;
        int centerY = parentY + parentHeight / 2 - this.getHeight() / 2;
        return new Point(centerX, centerY);
    }

}
