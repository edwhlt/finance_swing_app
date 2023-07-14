package fr.hedwin.ihm;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import fr.hedwin.ihm.components.DataComboBoxEditor;
import fr.hedwin.ihm.components.DataTableModel;
import fr.hedwin.ihm.fields.ComboEditor;
import fr.hedwin.ihm.fields.SortedComboBoxModel;
import fr.hedwin.ihm.importtab.TiersChoosePanel;
import fr.hedwin.objects.*;
import fr.hedwin.sql.DataManager;
import fr.hedwin.sql.exceptions.DaoException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static fr.hedwin.ihm.components.DataTable.column;

public class IHMImport  extends JPanel {

    private final Map<Integer, Tiers> allTiers;
    private final Map<Integer, PaymentType> allMdps;
    private IHMP ihmp;
    private DataManager dataManager;
    private final JPanel left = new JPanel(new BorderLayout());

    public IHMImport(IHMP ihmp, DataManager dataManager) throws DaoException {
        this.ihmp = ihmp;
        this.dataManager = dataManager;
        this.allTiers = dataManager.getTiersMap();
        this.allMdps = dataManager.getPaymentTypeMap();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JToolBar tools = new JToolBar(JToolBar.HORIZONTAL);
        tools.setLayout(new BoxLayout(tools, BoxLayout.X_AXIS));
        tools.setMargin(new Insets(5, 0, 5, 0));
        tools.setFloatable(false);


        JButton reset = new JButton(new FlatSVGIcon("images/resetView_dark.svg"));
        JButton jButton = new JButton(new FlatSVGIcon("images/add_dark.svg"));
        jButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV File", "csv");
            fileChooser.setFileFilter(filter);
            int result = fileChooser.showOpenDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                reset.doClick();
                loadCSVFile(selectedFile, tools);
            }
        });
        reset.addActionListener(e -> {
            tools.removeAll();
            tools.add(reset);
            tools.add(jButton);
            tools.add(Box.createHorizontalGlue());
            left.removeAll();
        });

        tools.add(reset);
        tools.add(jButton);
        tools.add(Box.createHorizontalGlue());

        add(tools, BorderLayout.NORTH);
        add(left, BorderLayout.CENTER);
    }

    private void loadCSVFile(File file, JToolBar tools) {
        String[] headers;
        List<String[]> rowsCSV;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            headers = reader.readLine().split(";");
            rowsCSV = reader.lines().map(l -> l.split(";")).toList();
            reader.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading CSV file: " + e.getMessage());
            return;
        }
        List<String[]> rows = rowsCSV.stream().toList();

        DataTableModel defaultTableModel = new DataTableModel(headers, 0);
        for (String[] row : rows) defaultTableModel.addRow(row);
        JTable table = new JTable(defaultTableModel);
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setViewportView(table);
        left.add(scrollPane, BorderLayout.CENTER);

        JComboBox<String> headComboBox = new JComboBox<>(headers);
        tools.add(headComboBox);

        JComboBox<String> mdpSQLColumn = new JComboBox<>(new String[]{"CB_OR_PCS(v)", "GET_MDP(v)"});
        tools.add(mdpSQLColumn);

        // Process button
        JButton processButton = new JButton(new FlatSVGIcon("images/compile_dark.svg"));
        tools.add(processButton);

        tools.add(Box.createHorizontalGlue());

        tools.add(new JLabel("Compte "));
        Compte[] comptes = dataManager.getCompteOrderMap().values().toArray(Compte[]::new);
        JComboBox<Compte> comboBox = new JComboBox<>(comptes);
        comboBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> new JLabel(value.getName()));
        comboBox.setSelectedIndex(comptes.length-1);
        tools.add(comboBox);

        tools.add(new JLabel("Montant "));
        JComboBox<String> montant = new JComboBox<>(headers);
        tools.add(montant);

        tools.add(new JLabel("Date "));
        JComboBox<String> dateColumn = new JComboBox<>(headers);
        tools.add(dateColumn);

        JComboBox<String> tiersIdColumn = new JComboBox<>();
        tools.add(new JLabel("Tiers "));
        tools.add(tiersIdColumn);

        JComboBox<String> mdpIdColumn = new JComboBox<>();
        tools.add(new JLabel("Mdp "));
        tools.add(mdpIdColumn);

        // Process button

        tools.add(Box.createHorizontalGlue());

        processButton.addActionListener(e -> {
            processTiers(tools, headComboBox, mdpSQLColumn, comboBox, table, scrollPane, defaultTableModel, tiersIdColumn, dateColumn, montant, mdpIdColumn);
        });
    }


    public void processTiers(
            JToolBar tools,
            JComboBox<String> headComboBox,
            JComboBox<String> mdpSQLColumn,
            JComboBox<Compte> comboBox,
            JTable table,
            JScrollPane scrollPane,
            DataTableModel defaultTableModel,
            JComboBox<String> tiersIdColumn,
            JComboBox<String> dateColumn,
            JComboBox<String> montant,
            JComboBox<String> mdpIdColumn
    ){
        String columnName = (String) headComboBox.getSelectedItem();

        // INDEX ROW JTABLE | LIBELLE
        Map<Integer, String> libelles = IntStream.range(0, table.getRowCount())
                .boxed()
                .collect(Collectors.toMap(i -> i, i -> {
                    return table.getValueAt(i, defaultTableModel.findColumn(columnName)).toString();
                }));

        StringJoiner sj = new StringJoiner(" UNION ALL SELECT ", "SELECT ", "");
        libelles.values().stream()
                .map(value -> "'" + value.replace("'", "\\'") + "'")
                .forEach(value -> {
                    if (sj.length() <= 7) sj.add(value + " AS v");
                    else sj.add(value);
                });

        String mdpTypeFunction = (String) mdpSQLColumn.getSelectedItem();
        String query = "SELECT v, GET_TIERS(v), "+mdpTypeFunction+" FROM (" + sj + ") AS t";
        System.out.println(query);

        // INDEX JTABLE | TiersId Found
        Map<Integer, Integer> rsIds;
        // INDEX JTABLE | mDPiD Found
        Map<Integer, Integer> mdpId = new HashMap<>();
        try {
            rsIds = dataManager.getDaoFactory().request(query, resultSet -> {
                NavigableMap<Integer, Integer> map = new TreeMap<>();
                while (resultSet.next()){
                    int id = resultSet.getInt("GET_TIERS(v)");
                    int idMdp = resultSet.getInt(mdpTypeFunction);
                    String lib = resultSet.getString("v");
                    List<Integer> idx = libelles.entrySet()
                            .stream()
                            .filter(entry -> Objects.equals(entry.getValue(), lib))
                            .map(Map.Entry::getKey).toList();
                    idx.forEach(index -> {
                        map.put(index, id);
                        mdpId.put(index, idMdp);
                    });
                }
                return map;
            });
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error executing SQL query: " + ex.getMessage());
            ex.printStackTrace();
            return;
        }

        fr.hedwin.ihm.fields.SortedComboBoxModel<Tiers> defaultComboBoxModel = new SortedComboBoxModel<>(
                allTiers.values().stream().toArray(Tiers[]::new), Comparator.comparing(Tiers::getName));
        JComboBox<Tiers> comboBoxTiers = new JComboBox<>(defaultComboBoxModel);
        /*ComboBoxEditor editor = new DataComboBoxEditor<>(defaultComboBoxModel, comboBoxTiers);
        comboBoxTiers.setEditor(editor);*/
        fr.hedwin.ihm.fields.SortedComboBoxModel<PaymentType> defaultComboBoxModelMdp = new SortedComboBoxModel<>(
                allMdps.values().stream().toArray(PaymentType[]::new), Comparator.comparing(PaymentType::getName));
        JComboBox<PaymentType> comboBoxMdp = new JComboBox<>(defaultComboBoxModelMdp);

        String columnId = "Tiers";
        if(defaultTableModel.findColumn(columnId) == -1) {
            defaultTableModel.addColumn(columnId);
            int columnTiersIndex = defaultTableModel.findColumn(columnId);
            table.getColumnModel().getColumn(columnTiersIndex)
                    .setCellEditor(new ComboEditor<>(allTiers.values().stream().toArray(Tiers[]::new), Comparator.comparing(Tiers::getName), Tiers::getName));
            table.getColumnModel().getColumn(columnTiersIndex).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    return super.getTableCellRendererComponent(table, value != null ? ((Tiers) value).getName() : "", isSelected, hasFocus, row, column);
                }
            });
            defaultTableModel.setColumnEditable(columnTiersIndex, true);
        }
        int columnTiersIndex = defaultTableModel.findColumn(columnId);

        String columnIdMdp = "Mode de paiement";
        if(defaultTableModel.findColumn(columnIdMdp) == -1) {
            defaultTableModel.addColumn(columnIdMdp);
            int columnMdpIndex = defaultTableModel.findColumn(columnIdMdp);
            table.getColumnModel().getColumn(columnMdpIndex)
                    .setCellEditor(new ComboEditor<PaymentType>(allMdps.values().stream().toArray(PaymentType[]::new), Comparator.comparing(PaymentType::getName), PaymentType::getName));
            table.getColumnModel().getColumn(columnMdpIndex).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    return super.getTableCellRendererComponent(table, value != null ? ((PaymentType) value).getName() : "", isSelected, hasFocus, row, column);
                }
            });
            defaultTableModel.setColumnEditable(columnMdpIndex, true);
        }
        int columnMdpIndex = defaultTableModel.findColumn(columnIdMdp);

        String[] columnNames = Arrays.stream(Collections.list(table.getColumnModel().getColumns()).toArray(TableColumn[]::new))
                .map(TableColumn::getHeaderValue)
                .map(Object::toString)
                .toArray(String[]::new);
        tiersIdColumn.setModel(new DefaultComboBoxModel<>(columnNames));
        mdpIdColumn.setModel(new DefaultComboBoxModel<>(columnNames));

        rsIds.forEach((idx, tiers_id) -> {
            table.setValueAt(allTiers.get(tiers_id), idx, columnTiersIndex);
        });
        mdpId.forEach((idx, mdp_id) -> {
            table.setValueAt(allMdps.get(mdp_id), idx, columnMdpIndex);
        });

        Integer[][] idx = rsIds.entrySet()
                .stream()
                .filter(entry -> entry.getValue() == 360)
                .map(entry -> libelles.entrySet()
                        .stream()
                        .filter(entryLibel -> Objects.equals(entryLibel.getValue(), libelles.get(entry.getKey())))
                        .map(Map.Entry::getKey).sorted(Comparator.comparingInt(Integer::intValue))
                        .toArray(Integer[]::new))
                .toList().stream()
                .map(Arrays::asList)  // Convert arrays to List
                .distinct()
                .map(l -> l.toArray(new Integer[0]))
                .toArray(Integer[][]::new);


        tiersChoose(0, idx, libelles, table, columnTiersIndex, scrollPane);

        JOptionPane.showMessageDialog(this, "Tous les tiers ont été passé !");

        JButton send = new JButton(new FlatSVGIcon("images/commit_dark.svg"));
        send.addActionListener(e -> {
            List<Transaction> transactions = new ArrayList<>();

            for (int i = 0; i < table.getRowCount(); i++) {
                int clmTiers = defaultTableModel.findColumn((String) tiersIdColumn.getSelectedItem());
                int clmAmount =  defaultTableModel.findColumn((String) montant.getSelectedItem());
                int clmDate = defaultTableModel.findColumn((String) dateColumn.getSelectedItem());
                int clmMdp = defaultTableModel.findColumn((String) mdpIdColumn.getSelectedItem());

                Tiers nt;
                try{
                    nt = (Tiers) table.getValueAt(i, clmTiers);
                }catch (ClassCastException ex){
                    JOptionPane.showMessageDialog(this, "Vous devez choisir un colonne de tiers pour les tiers !");
                    return;
                }
                PaymentType paymentType;
                try{
                    paymentType = (PaymentType) table.getValueAt(i, clmMdp);
                }catch (ClassCastException ex){
                    JOptionPane.showMessageDialog(this, "Vous devez choisir un colonne de mdp pour les mdps !");
                    return;
                }
                double amount;
                try{
                    amount = Double.parseDouble(table.getValueAt(i, clmAmount).toString());
                }catch (NumberFormatException ex){
                    JOptionPane.showMessageDialog(this, "Vous devez choisir un colonne de montant correcte !");
                    return;
                }
                try {
                    Date date = new SimpleDateFormat("dd/MM/yyyy").parse(table.getValueAt(i, clmDate).toString());
                    Transaction transaction = new Transaction(
                            -1, nt.getId(), null, null, ((Compte) comboBox.getSelectedItem()).getId(),
                            40, paymentType.getId(), amount, 1, date
                    );
                    transactions.add(transaction);
                } catch (ParseException ex) {
                    JOptionPane.showMessageDialog(this, "Vous devez choisir un colonne de date au format (jj/MM/yyyy) pour les dates !");
                    return;
                }
            }

            try {
                dataManager.getDaoFactory().getTransactionDao().add(transactions);
            }catch (DaoException ex) {
                throw new RuntimeException(ex);
            }
        });
        tools.add(send);
    }


    public void tiersChoose(int idx,
                            Integer[][] integers,
                            Map<Integer, String> libel, JTable table,
                            int columnTiersIndex,
                            JScrollPane scrollPane
    ){
        String label = libel.get(integers[idx][0]);

        System.out.println(Arrays.toString(integers[idx]));

        Rectangle cellRect = table.getCellRect(integers[idx][0], 0, true);
        Rectangle visibleRect = scrollPane.getViewport().getViewRect();
        int scrollY = cellRect.y - visibleRect.height / 2 + cellRect.height / 2;
        if(scrollY > 0) scrollPane.getViewport().setViewPosition(new Point(0, scrollY));

        table.removeRowSelectionInterval(0, table.getRowCount()-1);
        for (int i : integers[idx]) table.addRowSelectionInterval(i, i);

        new TiersChoosePanel(
                (JFrame) SwingUtilities.getWindowAncestor(this), dataManager, label, allTiers, nTiers -> {
            for (int index : integers[idx]) {
                table.setValueAt(nTiers, index, columnTiersIndex);
            }

            if(idx < integers.length-1){
                tiersChoose(idx + 1, integers, libel, table, columnTiersIndex, scrollPane);
            }
        }, (nb) -> {
            if(nb >= Integer.MAX_VALUE) return;
            int nidx = Math.max(idx+nb, 0);
            if(nidx < integers.length)
                tiersChoose(nidx, integers, libel, table, columnTiersIndex, scrollPane);
        });
    }


}
