import items.Bill;
import items.Coin;
import items.CoinSet;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CollectionTableScreen {
    private JButton OKButton;
    private JTabbedPane tabbedPane;
    private JPanel panel;
    private JButton exportButton;

    private final JTable coinsTable;
    private final JTable setsTable;
    private final JTable billsTable;

    JFrame parent;

    private ArrayList<Coin> coins;
    private ArrayList<CoinSet> sets;
    private ArrayList<Bill> bills;

    public CollectionTableScreen(JFrame parent) {

        this.parent = parent;

        JScrollPane scrollPane1 = new JScrollPane();
        JScrollPane scrollPane2 = new JScrollPane();
        JScrollPane scrollPane3 = new JScrollPane();
        coinsTable = new JTable();
        setsTable = new JTable();
        billsTable = new JTable();

        coinsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        setCoinsTable();
        scrollPane1.getViewport().add(coinsTable);

        setSetsTable();
        scrollPane2.getViewport().add(setsTable);

        setBillsTable();
        scrollPane3.getViewport().add(billsTable);

        // Remove the default tab
        tabbedPane.removeTabAt(0);

        tabbedPane.addTab("Coins", scrollPane1);
        tabbedPane.addTab("Sets", scrollPane2);
        tabbedPane.addTab("Bills", scrollPane3);

        OKButton.addActionListener( e-> goHome());

        exportButton.addActionListener( e -> {

            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new ExcelFilter());
            fc.setAcceptAllFileFilterUsed(false);

            int returnVal = fc.showSaveDialog(parent);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();

                String path = file.getAbsolutePath();

                // Force proper extension
                String extension;
                try {
                    extension = path.substring(path.lastIndexOf('.') + 1);
                }
                catch (Exception ex) {
                    extension = "";
                }

                boolean acceptable = false;
                for(String ext : ExcelExtensions.INSTANCE.getACCEPTABLE_EXTENSIONS()) {
                    if (extension.equals(ext)) {
                        acceptable = true;
                        break;
                    }
                }

                if(!acceptable)
                    path += ".xlsx";

                export(path);
            }

        });
    }

    public JPanel getPanel() {
        return panel;
    }

    private void setSetsTable() {
        String[] columnNames = {"ID",
                "Name",
                "Year",
                "Coins",
                "Face Value",
                "Value",
                "Note",};

        try {
            sets = CoinSet.Companion.getSetsFromSql(((Main) parent).databaseConnection, "");
        }
        catch (Exception e) {
            sets = new ArrayList<>();
        }

        // Show coins
        Object[][] data = new Object[sets.size() + 1][columnNames.length];

        int count = 0;

        // for each set found
        for (CoinSet set : sets) {
            String columnData = "";
            // For each column
            for(int j = 0; j < columnNames.length; j++) {
                switch (j) {
                    case 0 -> columnData = "" + set.getId();
                    case 1 -> columnData = set.getName();
                    case 2 -> columnData = "" + set.getYear();
                    case 3 -> columnData = "" + set.getCoins().size();
                    case 4 -> {
                        DecimalFormat format = new DecimalFormat();
                        format.applyPattern("0.00");

                        String value = format.format(set.getFaceValue());

                        columnData = "" + value;
                    }
                    case 5 -> {
                        DecimalFormat format = new DecimalFormat();
                        format.applyPattern("0.00");

                        String value = format.format(set.getValue());

                        columnData = "" + value;
                    }
                    case 6 -> columnData = set.getNote();
                }
                data[count][j] = columnData;
            }
            count++;
        }


        data[count][1] = "<HTML><B>Sets: " + sets.size() + "</B></HTML>";

        int coinCount = 0;

        for(CoinSet set : sets)
        {
            coinCount += set.getCoins().size();
        }
        data[count][3] = "<HTML><B>" + coinCount + "</B></HTML>";

        double totalFaceValue = 0.0;
        for(CoinSet set : sets) {
            totalFaceValue += set.getFaceValue();
        }

        DecimalFormat format = new DecimalFormat();
        format.applyPattern("0.00");

        String value = format.format(totalFaceValue);

        data[count][4] = "<HTML><B>" + value + "</B></HTML>";

        // Add total current value
        double totalValue = 0.0;
        for(CoinSet set : sets) {
            totalValue += set.getValue();
        }

        value = format.format(totalValue);

        data[count][5] = "<HTML><B>" + value + "</B></HTML>";

        for(int i = 0; i < columnNames.length; i++)
            if(data[count][i] == null || data[count][i].equals(""))
                data[count][i] = " ";

        // Set data for table
        DefaultTableModel dataModel = new DefaultTableModel();
        dataModel.setDataVector(data, columnNames);
        setsTable.setModel(dataModel);

        // Make table non-editable, and single selection
        setsTable.setDefaultEditor(Object.class, null);
        setsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add right click listener
        setsTable.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                if(SwingUtilities.isRightMouseButton(e)){

                    int index = setsTable.rowAtPoint(e.getPoint());
                    if (index > -1) {
                        setsTable.setRowSelectionInterval(index, index);
                    }

                    if(index != -1 ) {

                        // Don't error out if totals row selected
                        CoinSet selectedSet;
                        try {
                            selectedSet = sets.get(index);
                        }
                        catch (Exception ex) {
                            return;
                        }

                        // Create right click menu
                        final JPopupMenu coinListRightClickMenu = new JPopupMenu();
                        JMenuItem editSet = new JMenuItem("Edit");
                        editSet.addActionListener(e1 -> {
                            // Show edit set screen
                            AddSetScreen addSetScreen = new AddSetScreen(parent, selectedSet);

                            addSetScreen.setFromCollection(true);
                            ((Main) parent).changeScreen(addSetScreen.getPanel(), "Edit Set");
                        });
                        JMenuItem removeSet = new JMenuItem("Remove from Collection");
                        removeSet.addActionListener(e1 ->{
                            // Show an "Are you sure?" prompt
                            int option = JOptionPane.showConfirmDialog(parent, "Are you sure you want to remove this set?", "Delete Set", JOptionPane.YES_NO_OPTION);

                            if(option == JOptionPane.YES_OPTION)
                            {
                                selectedSet.deleteFromDb(((Main)parent).databaseConnection);
                                setSetsTable();
                            }
                        });
                        coinListRightClickMenu.add(editSet);
                        coinListRightClickMenu.add(removeSet);

                        // Show the menu at the location of the click
                        coinListRightClickMenu.show(setsTable, e.getPoint().x, e.getPoint().y);
                    }
                }
            }
        });

        addTableSort(setsTable, new ArrayList<>(List.of(0, 3, 4, 5)));
        hideTableColumn(setsTable, "ID");
    }

    private void setCoinsTable() {
        String[] columnNames = {"ID",
                "Coin",
                "Denomination",
                "Value",
                "Country",
                "Grade",
                "Error",
                "Note",};

        try {
            coins = Coin.Companion.getCoinsFromSql(((Main) parent).databaseConnection, "");
        }
        catch (Exception ex) {
            coins = new ArrayList<>();
        }

        // Show coins
        Object[][] data = new Object[coins.size()+1][columnNames.length];

        int count = 0;
        // for each coin found
        for (Coin coin : coins) {

            // For each column
            for(int j = 0; j < columnNames.length; j++) {
                String columnData = "";
                switch (j) {
                    case 0 -> columnData = "" +  coin.getId();
                    case 1 -> columnData = coin.toString();
                    case 2 -> {
                        DecimalFormat format = new DecimalFormat();
                        format.applyPattern("0.00");

                        String value = format.format(coin.getDenomination());

                        columnData = "" + value;
                    }
                    case 3 -> {
                        DecimalFormat format = new DecimalFormat();
                        format.applyPattern("0.00");

                        String value = format.format(coin.getValue());

                        columnData = "" + value;
                    }
                    case 4 -> columnData = coin.getCountry();
                    case 5 -> columnData = coin.getCondition();
                    case 6 -> columnData = coin.getErrorType();
                    case 7 -> columnData = coin.getNote();
                }
                data[count][j] = columnData;
            }
            count++;
        }

        data[count][1] = "<HTML><B>Coins: " + coins.size() + "</B></HTML>";

        double totalFaceValue = 0.0;
        for(Coin coin : coins) {
            totalFaceValue += coin.getDenomination();
        }

        DecimalFormat format = new DecimalFormat();
        format.applyPattern("0.00");

        String value = format.format(totalFaceValue);

        data[count][2] = "<HTML><B>" + value + "</B></HTML>";

        // Add total current value
        double totalValue = 0.0;
        for(Coin coin : coins) {
            totalValue += coin.getValue();
        }

        value = format.format(totalValue);

        data[count][3] = "<HTML><B>" + value + "</B></HTML>";

        for(int i = 0; i < columnNames.length; i++)
            if(data[count][i] == null || data[count][i].equals(""))
                data[count][i] = " ";

        // Set data for table
        DefaultTableModel dataModel = new DefaultTableModel();
        dataModel.setDataVector(data, columnNames);
        coinsTable.setModel(dataModel);

        // Make table non-editable, and single selection
        coinsTable.setDefaultEditor(Object.class, null);
        coinsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add right click listener
        coinsTable.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                if(SwingUtilities.isRightMouseButton(e)){

                    int index = coinsTable.rowAtPoint(e.getPoint());
                    if (index > -1) {
                        coinsTable.setRowSelectionInterval(index, index);
                    }

                    if(index != -1 ) {

                        // Don't error out if totals row selected.
                        Coin selectedCoin;
                        try {
                             selectedCoin = coins.get(index);
                        }
                        catch (Exception ex) {
                            return;
                        }

                        // Create right click menu
                        final JPopupMenu coinListRightClickMenu = new JPopupMenu();
                        JMenuItem editCoin = new JMenuItem("Edit");
                        editCoin.addActionListener(e1 -> {
                            // Show edit coin screen
                            AddCoinScreen addCoinScreen = new AddCoinScreen(parent);

                            addCoinScreen.setCoin(selectedCoin);
                            addCoinScreen.setFromCollection(true);
                            ((Main) parent).changeScreen(addCoinScreen.getPanel(), "Edit Coin");
                        });
                        JMenuItem removeCoin = new JMenuItem("Remove from Collection");
                        removeCoin.addActionListener(e1 ->{
                            // Show an "Are you sure?" prompt
                            int option = JOptionPane.showConfirmDialog(parent, "Are you sure you want to remove this coin?", "Delete Coin", JOptionPane.YES_NO_OPTION);

                            if(option == JOptionPane.YES_OPTION)
                            {
                                selectedCoin.deleteFromDb(((Main)parent).databaseConnection);
                                setCoinsTable();
                            }
                        });
                        coinListRightClickMenu.add(editCoin);
                        coinListRightClickMenu.add(removeCoin);

                        // Show the menu at the location of the click
                        coinListRightClickMenu.show(coinsTable, e.getPoint().x, e.getPoint().y);
                    }
                }
            }
        });

        addTableSort(coinsTable, new ArrayList<>(List.of(0, 2,3)));
        hideTableColumn(coinsTable, "ID");
    }

    private void setBillsTable() {
        String[] columnNames = {"ID",
                "Bill",
                "Denomination",
                "Value",
                "Country",
                "Signatures",
                "Grade",
                "Error",
                "Star",
                "Note",};

        try {
            bills = Bill.Companion.getBillsFromSql(((Main) parent).databaseConnection, "");
        }
        catch (Exception e) {
            bills = new ArrayList<>();
        }

        // Show bills
        Object[][] data = new Object[bills.size()+1][columnNames.length];

        int count = 0;
        // for each bill found
        for (Bill bill : bills) {

            // For each column
            for(int j = 0; j < columnNames.length; j++) {
                String columnData = "";
                switch (j) {
                    case 0 -> columnData += bill.getId();
                    case 1 -> columnData = bill.toString();
                    case 2 -> {
                        DecimalFormat format = new DecimalFormat();
                        format.applyPattern("0.00");

                        String value = format.format(bill.getDenomination());

                        columnData = "" + value;
                    }
                    case 3 -> {
                        DecimalFormat format = new DecimalFormat();
                        format.applyPattern("0.00");

                        String value = format.format(bill.getValue());

                        columnData = "" + value;
                    }
                    case 4 -> columnData = bill.getCountry();
                    case 5 -> columnData = bill.getSignatures();
                    case 6 -> columnData = bill.getCondition();
                    case 7 -> columnData = bill.getErrorType();
                    case 8 -> columnData = "" + bill.getStar();
                    case 9 -> columnData = bill.getNote();
                }
                data[count][j] = columnData;
            }
            count++;
        }

        data[count][1] = "<HTML><B>Bills: " + bills.size() + "</B></HTML>";

        double totalFaceValue = 0.0;
        for(Bill bill : bills) {
            totalFaceValue += bill.getDenomination();
        }

        DecimalFormat format = new DecimalFormat();
        format.applyPattern("0.00");

        String value = format.format(totalFaceValue);

        data[count][2] = "<HTML><B>" + value + "</B></HTML>";

        // Add total current value
        double totalValue = 0.0;
        for(Bill bill : bills) {
            totalValue += bill.getValue();
        }

        value = format.format(totalValue);

        data[count][3] = "<HTML><B>" + value + "</B></HTML>";

        // Put space in all empty columns in totals row
        for(int i = 0; i < columnNames.length; i++)
            if(data[count][i] == null || data[count][i].equals(""))
                data[count][i] = " ";

        // Set data for table
        DefaultTableModel dataModel = new DefaultTableModel();
        dataModel.setDataVector(data, columnNames);
        billsTable.setModel(dataModel);

        // Make table non-editable, and single selection
        billsTable.setDefaultEditor(Object.class, null);
        billsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add right click listener
        billsTable.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                if(SwingUtilities.isRightMouseButton(e)){

                    int index = billsTable.rowAtPoint(e.getPoint());
                    if (index > -1) {
                        billsTable.setRowSelectionInterval(index, index);
                    }

                    if(index != -1 ) {

                        // Don't error out if totals row selected
                        Bill selectedBill;
                        try {
                            selectedBill = bills.get(index);
                        }
                        catch (Exception ex) {
                            return;
                        }

                        // Create right click menu
                        final JPopupMenu coinListRightClickMenu = new JPopupMenu();
                        JMenuItem editBill = new JMenuItem("Edit");
                        editBill.addActionListener(e1 -> {
                            // Show edit bill screen
                            AddBillScreen addBillScreen = new AddBillScreen(parent);

                            addBillScreen.setBill(selectedBill);
                            addBillScreen.setFromCollection(true);
                            ((Main) parent).changeScreen(addBillScreen.getPanel(), "Edit Bill");
                        });
                        JMenuItem removeBill = new JMenuItem("Remove from Collection");
                        removeBill.addActionListener(e1 ->{
                            // Show an "Are you sure?" prompt
                            int option = JOptionPane.showConfirmDialog(parent, "Are you sure you want to remove this bill?", "Delete Bill", JOptionPane.YES_NO_OPTION);

                            if(option == JOptionPane.YES_OPTION)
                            {
                                selectedBill.deleteFromDb(((Main)parent).databaseConnection);
                                setBillsTable();
                            }
                        });
                        coinListRightClickMenu.add(editBill);
                        coinListRightClickMenu.add(removeBill);

                        // Show the menu at the location of the click
                        coinListRightClickMenu.show(billsTable, e.getPoint().x, e.getPoint().y);
                    }
                }
            }
        });

        addTableSort(billsTable, new ArrayList<>(List.of(0, 2, 3)));
        hideTableColumn(billsTable, "ID");
    }

    private void goHome() {
        ((Main) parent).changeScreen(((Main) parent).getPanel(), "Coin Collection");
    }

    private void export(String path) {

        try{
            TableModel model;

            switch (tabbedPane.getSelectedIndex()) {
                case 0 -> model = coinsTable.getModel();
                case 1 -> model = setsTable.getModel();
                default -> model = billsTable.getModel();
            }

            File file = new File(path);
            file.createNewFile(); // if file already exists will do nothing

            FileWriter excel = new FileWriter(file);

            for (int i = 0; i < model.getColumnCount(); i++) {
                excel.write(model.getColumnName(i) + "\t");
            }

            excel.write("\n");

            // Don't include the totals row
            for (int i = 0; i < model.getRowCount() -1; i++) {
                for (int j = 0; j < model.getColumnCount(); j++) {
                    if(model.getValueAt(i, j) != null)
                        excel.write(model.getValueAt(i, j).toString() + "\t");
                    else
                        excel.write("\t");
                }
                excel.write("\n");
            }

            excel.close();

        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public void setTab(int index) {
        tabbedPane.setSelectedIndex(index);
    }

    private void hideTableColumn(JTable table, String columnName) {
        table.getColumn(columnName).setMinWidth(0); // Must be set before maxWidth!!
        table.getColumn(columnName).setMaxWidth(0);
        table.getColumn(columnName).setWidth(0);
    }

    private void addTableSort(JTable table, ArrayList<Integer> numberColumns) {

        TableRowSorter sorter = new TableRowSorter();
        table.setRowSorter(sorter);
        sorter.setModel(table.getModel());

        ArrayList<Integer> textColumns = new ArrayList<>();
        textColumns.add(1);
        // Add all non-number rows to the list of text rows
        for(int i = 0; i < table.getColumnCount(); i++)
            if(!numberColumns.contains(i))
                textColumns.add(i);

        // Number based columns
        for(int num : numberColumns) {
            sorter.setComparator(num, (Comparator<String>) (name1, name2) -> {
                try {
                    double one = Double.parseDouble(name1);
                    double two = Double.parseDouble(name2);

                    return Double.compare(one, two);
                } catch (NumberFormatException e) {
                    return 0;
                }
            });
        }

        // Text based columns
        for (int num : textColumns) {
            sorter.setComparator(num, (Comparator<String>) (name1, name2) -> {
                if (name1.equals(" ") || name2.equals(" ") ||
                        name1.startsWith("<HTML>") || name2.startsWith("<HTML>"))
                    return 0;
                else
                    return name1.compareTo(name2);
            });
        }
    }
}
