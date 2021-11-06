import items.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

// TODO: Add tree view - especially for sets and/or container view.
//  Build custom tree view
public class CollectionTableScreen {
    private JButton OKButton;
    private JTabbedPane tabbedPane;
    private JPanel panel;
    private JButton exportButton;
    private JButton cancelButton;

    private final MyTable coinsTable;
    private final MyTable coinsTotalTable;
    private final MyTable setsTable;
    private final MyTable setsTotalTable;
    private final MyTable billsTable;
    private final MyTable billsTotalTable;

    private final JFrame parent;

    private ArrayList<Coin> coins;
    private ArrayList<Set> sets;
    private ArrayList<Bill> bills;

    private Set set = null;

    public SetItem selectedItem = null;

    private ActionListener okListener;

    private final NumismatistAPI api;

    public CollectionTableScreen(JFrame parent) {

        this.parent = parent;

        api = ((Main) parent).api;

        // Add Panels
        JPanel coinsPanel = new JPanel();
        JPanel setsPanel = new JPanel();
        JPanel billsPanel = new JPanel();
        coinsPanel.setLayout(new BoxLayout(coinsPanel, BoxLayout.PAGE_AXIS));
        setsPanel.setLayout(new BoxLayout(setsPanel, BoxLayout.PAGE_AXIS));
        billsPanel.setLayout(new BoxLayout(billsPanel, BoxLayout.PAGE_AXIS));

        // Add scroll panes
        JScrollPane coinsScrollPane = new JScrollPane();
        JScrollPane setsScrollPane = new JScrollPane();
        JScrollPane billsScrollPane = new JScrollPane();

        // Setup tables
        coinsTable = new MyTable();
        coinsTotalTable = new MyTable();
        setsTable = new MyTable();
        setsTotalTable = new MyTable();
        billsTable = new MyTable();
        billsTotalTable = new MyTable();

        coinsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        setsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        billsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        setCoinsTable();
        coinsScrollPane.getViewport().add(coinsTable);

        setSetsTable();
        setsScrollPane.getViewport().add(setsTable);

        setBillsTable();
        billsScrollPane.getViewport().add(billsTable);

        coinsPanel.add(coinsScrollPane);
        coinsPanel.add(coinsTotalTable);
        tabbedPane.addTab(Main.getString("viewColl_tab_coins"), coinsPanel);
        setsPanel.add(setsScrollPane);
        setsPanel.add(setsTotalTable);
        tabbedPane.addTab(Main.getString("viewColl_tab_sets"), setsPanel);
        billsPanel.add(billsScrollPane);
        billsPanel.add(billsTotalTable);
        tabbedPane.addTab(Main.getString("viewColl_tab_bills"), billsPanel);

        okListener = e -> goHome();

        OKButton.addActionListener(okListener);

        // Resize columns to fit content
        coinsTable.resizeColumns();
        coinsTotalTable.resizeColumns();
        setsTotalTable.resizeColumns();
        setsTotalTable.resizeColumns();
        billsTable.resizeColumns();
        billsTotalTable.resizeColumns();

        exportButton.addActionListener( e -> {

            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new CsvFilter());
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
                    path += ".csv";

                export(path);
            }

        });

        api.setCoinListener(new NumismatistAPI.CoinListener() {
            @Override
            public void coinListRetrievedFromFb(@NotNull ArrayList<Coin> coins) {
                setCoinsTable();
            }
        });
        api.setBillListener(new NumismatistAPI.BillListener() {
            @Override
            public void billListRetrievedFromFb(@NotNull ArrayList<Bill> bills) {
                setBillsTable();
            }
        });
        api.setSetListener(new NumismatistAPI.SetListener() {
            @Override
            public void setListRetrievedFromFb(@NotNull ArrayList<Set> sets) {
                setSetsTable();
            }
        });

        parent.getRootPane().setDefaultButton(OKButton);
    }

    public CollectionTableScreen(JFrame parent, JDialog dialog) {
        this(parent);

        exportButton.setVisible(false);
        cancelButton.setVisible(true);

        cancelButton.addActionListener(e -> {
                selectedItem = null;
                dialog.setVisible(false);
        });

        // Remove old listener
        OKButton.removeActionListener(okListener);

        dialog.getRootPane().setDefaultButton(OKButton);

        //create and add new listener
        okListener = e -> {

            Object idString;

            if(tabbedPane.getSelectedIndex() == 0) {

                if(coinsTable.getSelectedRow() == -1) {
                    JOptionPane.showMessageDialog(parent, Main.getString("viewColl_error_noSelectionMessage"),
                            Main.getString("viewColl_error_noSelectionTitle"), JOptionPane.ERROR_MESSAGE);
                }
                else {
                    idString = coinsTable.getValueAt(coinsTable.getSelectedRow(), 0);
                    // If didn't select empty row
                    if (idString != null) {
                        int id = Integer.parseInt(idString.toString());
                        for (Coin coin : coins)
                            if (coin.getId() == id)
                                selectedItem = coin;
                    }
                    dialog.setVisible(false);
                }
            }
            else if(tabbedPane.getSelectedIndex() == 1) {
                if(setsTable.getSelectedRow() == -1) {
                    JOptionPane.showMessageDialog(parent, Main.getString("viewColl_error_noSelectionMessage"),
                            Main.getString("viewColl_error_noSelectionTitle"), JOptionPane.ERROR_MESSAGE);
                }
                else {
                    idString = setsTable.getValueAt(setsTable.getSelectedRow(), 0);
                    // If didn't select empty row
                    if (idString != null) {
                        int id = Integer.parseInt(idString.toString());
                        for (Set selectedSet : sets)
                            if (selectedSet.getId() == id)
                                selectedItem = selectedSet;
                    }
                    dialog.setVisible(false);
                }
            }
            else if(tabbedPane.getSelectedIndex() == 2) {
                if(billsTable.getSelectedRow() == -1) {
                    JOptionPane.showMessageDialog(parent, Main.getString("viewColl_error_noSelectionMessage"),
                            Main.getString("viewColl_error_noSelectionTitle"), JOptionPane.ERROR_MESSAGE);
                }
                else {
                    idString = billsTable.getValueAt(billsTable.getSelectedRow(), 0);
                    // If didn't select empty row
                    if (idString != null) {
                        int id = Integer.parseInt(idString.toString());
                        for (Bill bill : bills)
                            if (bill.getId() == id)
                                selectedItem = bill;
                    }
                    dialog.setVisible(false);
                }
            }
        };
        OKButton.addActionListener(okListener);
    }

    public JPanel getPanel() {
        return panel;
    }

    private void setSetsTable() {
        String[] columnNames = {NumismatistAPI.Companion.getString("property_set_id"),
                NumismatistAPI.Companion.getString("property_set_year"),
                NumismatistAPI.Companion.getString("property_set_name"),
                NumismatistAPI.Companion.getString("property_set_items"),
                NumismatistAPI.Companion.getString("property_set_faceValue"),
                NumismatistAPI.Companion.getString("property_set_value"),
                NumismatistAPI.Companion.getString("property_set_note"),
                NumismatistAPI.Companion.getString("property_set_container")};

        String[] totalsColumns = {NumismatistAPI.Companion.getString("property_set_totals"),
                NumismatistAPI.Companion.getString("property_set_totals_sets"),
                NumismatistAPI.Companion.getString("property_set_totals_items"),
                NumismatistAPI.Companion.getString("property_set_totals_faceValue"),
                NumismatistAPI.Companion.getString("property_set_totals_value")};

        try {
            sets = api.getSets();
            if(set != null) {
                sets.remove(set);

                ArrayList<Set> setsInSet = new ArrayList<>();
                for (SetItem item : set.getItems()) {
                    if (item instanceof Set)
                        setsInSet.add((Set) item);
                }
                sets.removeAll(setsInSet);
            }
        }
        catch (Exception e) {
            sets = new ArrayList<>();
        }

        DecimalFormat format = new DecimalFormat();
        format.applyPattern("0.00");

        // Show coins
        Object[][] data = new Object[sets.size()][columnNames.length];
        Object[][] totalsData = new Object[1][totalsColumns.length];

        int count = 0;

        double totalFaceValue = 0.0;
        double totalValue = 0.0;
        int itemCount = 0;

        // for each set found
        for (Set set : sets) {
            String columnData = "";

            // For each column
            for(int j = 0; j < columnNames.length; j++) {
                switch (j) {
                    case 0:
                        columnData = "" + set.getId();
                        break;
                    case 1:
                        columnData = "" + set.getYear();
                        break;
                    case 2:
                        columnData = set.getName();
                        break;
                    case 3:
                        // Add to total for all sets
                        itemCount += set.getItems().size();

                        columnData = "" + set.getItems().size();
                        break;
                    case 4:
                        totalFaceValue += set.getFaceValue();

                        columnData = format.format(set.getFaceValue());
                        break;
                    case 5:
                        double value;

                        // If no value set, use face value
                        if(set.getValue() == 0.0)
                            value = set.getFaceValue();
                        else
                            value = set.getValue();

                        totalValue += value;

                        columnData = format.format(value);
                        break;
                    case 6:
                        columnData = set.getNote();
                        break;
                    case 7:
                        columnData = api.findContainer(set.getContainerId()).getName();
                        break;
                }
                data[count][j] = columnData;
            }
            count++;
        }

        // Put totals in the final row
        totalsData[0][0] = "<HTML><B>" + NumismatistAPI.Companion.getString("property_set_totals") + "</B></HTML>";
        totalsData[0][1] = "<HTML><B>" + MessageFormat.format(NumismatistAPI.Companion.getString("property_set_totals_sets"),
                sets.size()) + "</B></HTML>";
        totalsData[0][2] = "<HTML><B>" + MessageFormat.format(NumismatistAPI.Companion.getString("property_set_totals_items"),
                itemCount) + "</B></HTML>";
        totalsData[0][3] = "<HTML><B>" + MessageFormat.format(NumismatistAPI.Companion.getString("property_set_totals_faceValue"),
                format.format(totalFaceValue)) + "</B></HTML>";
        totalsData[0][4] = "<HTML><B>" + MessageFormat.format(NumismatistAPI.Companion.getString("property_set_totals_value"),
                format.format(totalValue)) + "</B></HTML>";

        // Set data for table
        DefaultTableModel dataModel = new DefaultTableModel();
        dataModel.setDataVector(data, columnNames);
        setsTable.setModel(dataModel);

        DefaultTableModel totalDataModel = new DefaultTableModel();
        totalDataModel.setDataVector(totalsData, totalsColumns);
        setsTotalTable.setModel(totalDataModel);

        // Make table non-editable, and single selection
        setsTable.setDefaultEditor(Object.class, null);
        setsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setsTotalTable.setEnabled(false);

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
                        Set selectedSet = null;
                        try {
                            // Find the ID of the set that has been clicked
                            int ID = Integer.parseInt((String)setsTable.getValueAt(index, 0));
                            // Find the set that has been clicked
                            for(Set set : sets)
                                if(set.getId() == ID) {
                                    selectedSet = set;
                                    break;
                                }
                        }
                        catch (Exception ex) {
                            return;
                        }

                        if(selectedSet == null)
                            return;

                        final Set finalSet = selectedSet;

                        // Create right click menu
                        final JPopupMenu setListRightClickMenu = new JPopupMenu();
                        JMenuItem copy = new JMenuItem(Main.getString("viewColl_rtClick_copySet"));
                        // Create copy item
                        copy.addActionListener(e1 -> {
                            // Show edit set screen
                            AddSetScreen addSetScreen;
                            addSetScreen = new AddSetScreen(parent, finalSet.copy());

                            addSetScreen.setFromCollection(true);
                            ((Main) parent).changeScreen(addSetScreen.getPanel(), Main.getString("addSet_title_add"));
                        });
                        JMenuItem editSet = new JMenuItem(Main.getString("viewColl_rtClick_editSet"));
                        editSet.addActionListener(e1 -> {
                            // Show edit set screen
                            AddSetScreen addSetScreen = new AddSetScreen(parent, finalSet);

                            String name;
                            if(finalSet.getName().equals(""))
                                name = NumismatistAPI.Companion.getString("property_set_toString");
                            else
                                name = finalSet.getName();

                            addSetScreen.setFromCollection(true);
                            ((Main) parent).changeScreen(addSetScreen.getPanel(), MessageFormat.format(Main.getString("addSet_title_edit"), name));
                        });
                        JMenuItem removeSet = new JMenuItem(Main.getString("viewColl_rtClick_removeSet"));
                        removeSet.addActionListener(e1 ->{
                            // Show an "Are you sure?" prompt
                            int option = JOptionPane.showConfirmDialog(parent,
                                    Main.getString("viewColl_dialog_removeSetMessage"),
                                    Main.getString("viewColl_dialog_removeSetTitle"),
                                    JOptionPane.YES_NO_OPTION);
                            String errorMessage = "";

                            if(option == JOptionPane.YES_OPTION)
                            {
                                try {
                                    if(finalSet.removeFromDb(api)) {
                                        if (!finalSet.deleteObvImage()) {
                                            errorMessage += MessageFormat.format(Main.getString("error_deletingFile_message"), finalSet.getObvImgPath());
                                        }
                                        if (!finalSet.deleteRevImage()) {
                                            errorMessage += MessageFormat.format(Main.getString("error_deletingFile_message"), finalSet.getRevImgPath());
                                        }
                                    }
                                } catch (SecurityException | IOException | SQLException securityException) {
                                    errorMessage += securityException.getMessage();
                                }

                                if(!errorMessage.equals("")) {
                                    JOptionPane.showMessageDialog(parent, errorMessage,
                                            Main.getString("error_genericTitle"), JOptionPane.ERROR_MESSAGE);
                                }
                                setSetsTable();
                            }
                        });
                        setListRightClickMenu.add(editSet);
                        setListRightClickMenu.add(copy);
                        setListRightClickMenu.add(removeSet);

                        // Show the menu at the location of the click
                        setListRightClickMenu.show(setsTable, e.getPoint().x, e.getPoint().y);
                    }
                }
            }
        });

        setsTable.addSort(new ArrayList<>(List.of(0, 1, 3)), new ArrayList<>(List.of(4, 5)));
        setsTable.hideColumn(NumismatistAPI.Companion.getString("property_set_id"));
    }

    private void setCoinsTable() {

        String[] columnNames = {NumismatistAPI.Companion.getString("property_coin_id"),
                NumismatistAPI.Companion.getString("property_coin_toString"),
                NumismatistAPI.Companion.getString("property_coin_denomination"),
                NumismatistAPI.Companion.getString("property_coin_value"),
                NumismatistAPI.Companion.getString("property_coin_country"),
                NumismatistAPI.Companion.getString("property_coin_grade"),
                NumismatistAPI.Companion.getString("property_coin_error"),
                NumismatistAPI.Companion.getString("property_coin_note"),
                NumismatistAPI.Companion.getString("property_coin_container")};

        String[] totalColumns = {NumismatistAPI.Companion.getString("property_coin_totals"),
                NumismatistAPI.Companion.getString("property_coin_totals_coins"),
                NumismatistAPI.Companion.getString("property_coin_totals_faceValue"),
                NumismatistAPI.Companion.getString("property_coin_totals_value")};

        try {
            coins = api.getCoins();

            // If adding to a set
            if(set != null) {

                ArrayList<Coin> coinsInSet = new ArrayList<>();

                // Remove all coins already in the set
                for (SetItem item : set.getItems()) {
                    if (item instanceof Coin)
                        coinsInSet.add((Coin) item);
                }

                coins.removeAll(coinsInSet);
            }
        }
        catch (Exception ex) {
            coins = new ArrayList<>();
        }

        // For formatting the numbers
        DecimalFormat format = new DecimalFormat();
        format.applyPattern("0.00");

        DecimalFormat halfCentFormat = new DecimalFormat();
        halfCentFormat.applyPattern("0.000");

        // Show coins
        Object[][] data = new Object[coins.size()][columnNames.length];
        Object[][] totalData = new Object[1][totalColumns.length];

        double totalFaceValue = 0.0;
        double totalValue = 0.0;

        boolean halfCentIncluded = false;

        int count = 0;
        // for each coin found
        for (Coin coin : coins) {

            // Format denomination with currency symbol
            Currency currency = coin.getCurrency();

            // For each column
            for(int j = 0; j < columnNames.length; j++) {
                String columnData = "";

                String string;
                Double value;
                String formattedValue;

                switch (j) {
                    case 0:
                        columnData = "" +  coin.getId();
                        break;
                    // Coin label - Year, Mint Marl, Type
                    case 1:
                        columnData = coin.toString();
                        break;
                    // Denomination
                    case 2:

                        if(coin.getDenomination() == Coin.HALF_PENNY) {
                            formattedValue = halfCentFormat.format(coin.getDenomination());
                            halfCentIncluded = true;
                        }
                        else
                            formattedValue = format.format(coin.getDenomination());

                        totalFaceValue += coin.getDenomination();

                        if(currency.getSymbolBefore())
                            string = currency.getSymbol() + formattedValue;
                        else
                            string = formattedValue + currency.getSymbol();

                        columnData = string;
                        break;
                    // Value
                    case 3:

                        if(coin.getValue() == 0.0) {
                            value = coin.getDenomination();

                            if(coin.getDenomination() == Coin.HALF_PENNY) {
                                formattedValue = halfCentFormat.format(value);
                            }
                            else {
                                formattedValue = format.format(value);
                            }
                        }
                        else {
                            value = coin.getValue();
                            formattedValue = format.format(value);
                        }

                        totalValue += value;

                        if(currency.getSymbolBefore())
                            string = currency.getSymbol() + formattedValue;
                        else
                            string = formattedValue + currency.getSymbol();

                        columnData = string;
                        break;
                    case 4:
                        columnData = coin.getCountryName();
                        break;
                    case 5:
                        columnData = coin.getCondition();
                        break;
                    case 6:
                        columnData = coin.getErrorType();
                        break;
                    case 7:
                        columnData = coin.getNote();
                        break;
                    case 8:
                        columnData = api.findContainer(coin.getContainerId()).getName();
                        break;
                }
                data[count][j] = columnData;
            }
            count++;
        }

        // If collection includes half cents, add 3rd point of precision
        String faceValue;
        if(halfCentIncluded)
            faceValue = halfCentFormat.format(totalFaceValue);
        else
            faceValue = format.format(totalFaceValue);

        totalData[0][0] = "<HTML><B>" + NumismatistAPI.Companion.getString("property_coin_totals") + "</B></HTML>";
        totalData[0][1] = "<HTML><B>" + MessageFormat.format(NumismatistAPI.Companion.getString("property_coin_totals_coins"),
                coins.size()) + "</B></HTML>";
        totalData[0][2] = "<HTML><B>" + MessageFormat.format(NumismatistAPI.Companion.getString("property_coin_totals_faceValue"),
                faceValue) + "</B></HTML>";
        totalData[0][3] = "<HTML><B>" + MessageFormat.format(NumismatistAPI.Companion.getString("property_coin_totals_value"),
                format.format(totalValue)) + "</B></HTML>";

        // Set data for table
        DefaultTableModel dataModel = new DefaultTableModel();
        dataModel.setDataVector(data, columnNames);
        coinsTable.setModel(dataModel);

        DefaultTableModel totalDataModel = new DefaultTableModel();
        totalDataModel.setDataVector(totalData, totalColumns);
        coinsTotalTable.setModel(totalDataModel);

        // Make table non-editable, and single selection
        coinsTable.setDefaultEditor(Object.class, null);
        coinsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        coinsTotalTable.setEnabled(false);

        // TODO: Allow showing and hiding of columns - but do so in MyTable
        /*coinsTable.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(SwingUtilities.isRightMouseButton(e)) {
                    //int column = coinsTable.columnAtPoint(e.getPoint());

                    // Create right click menu
                    final JPopupMenu coinHeaderRightClickMenu = new JPopupMenu();

                    // Add select columns item
                    JMenuItem headings = new JMenuItem("Select Columns");
                    headings.addActionListener( e1 -> {

                    });

                    coinHeaderRightClickMenu.add(headings);

                    coinHeaderRightClickMenu.show(coinsTable.getTableHeader(), e.getPoint().x, e.getPoint().y);
                }
                else
                    super.mouseClicked(e);
            }
        });*/

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
                        Coin selectedCoin = null;
                        try {
                            int ID = Integer.parseInt((String)coinsTable.getValueAt(index, 0));
                            // Find the set that has been clicked
                            for(Coin coin : coins)
                                if(coin.getId() == ID) {
                                    selectedCoin = coin;
                                    break;
                                }
                        }
                        catch (Exception ex) {
                            return;
                        }

                        if(selectedCoin == null)
                            return;

                        final Coin finalCoin = selectedCoin;

                        // Create right click menu
                        final JPopupMenu coinListRightClickMenu = new JPopupMenu();
                        // Add copy item
                        JMenuItem copy = new JMenuItem(Main.getString("viewColl_rtClick_copyCoin"));
                        copy.addActionListener( e1 -> {
                            // Show edit coin screen
                            AddCoinScreen addCoinScreen = new AddCoinScreen(parent, finalCoin.copy());

                            addCoinScreen.setFromCollection(true);
                            ((Main) parent).changeScreen(addCoinScreen.getPanel(), Main.getString("addCoin_title_add"));
                        });
                        JMenuItem editCoin = new JMenuItem(Main.getString("viewColl_rtClick_editCoin"));
                        editCoin.addActionListener(e1 -> {
                            // Show edit coin screen
                            AddCoinScreen addCoinScreen = new AddCoinScreen(parent, finalCoin);

                            addCoinScreen.setFromCollection(true);
                            ((Main) parent).changeScreen(addCoinScreen.getPanel(), Main.getString("addCoin_title_edit"));
                        });
                        JMenuItem removeCoin = new JMenuItem(Main.getString("viewColl_rtClick_removeCoin"));
                        removeCoin.addActionListener(e1 ->{
                            // Show an "Are you sure?" prompt
                            int option = JOptionPane.showConfirmDialog(parent,
                                    Main.getString("viewColl_dialog_removeCoinMessage"),
                                    Main.getString("viewColl_dialog_removeCoinTitle"),
                                    JOptionPane.YES_NO_OPTION);
                            String errorMessage = "";

                            if(option == JOptionPane.YES_OPTION)
                            {
                                try {
                                    if(finalCoin.removeFromDb(api)) {
                                        if (!finalCoin.deleteObvImage()) {
                                            errorMessage += MessageFormat.format(Main.getString("error_deletingFile_message"), finalCoin.getObvImgPath());
                                        }
                                        if (!finalCoin.deleteRevImage()) {
                                            errorMessage += MessageFormat.format(Main.getString("error_deletingFile_message"), finalCoin.getRevImgPath());
                                        }
                                    }
                                } catch (SecurityException | IOException | SQLException securityException) {
                                    errorMessage += securityException.getMessage();
                                }

                                if(!errorMessage.equals("")) {
                                    JOptionPane.showMessageDialog(parent, errorMessage,
                                            Main.getString("error_genericTitle"), JOptionPane.ERROR_MESSAGE);
                                }
                                setCoinsTable();
                            }
                        });
                        coinListRightClickMenu.add(editCoin);
                        coinListRightClickMenu.add(copy);
                        coinListRightClickMenu.add(removeCoin);

                        // Show the menu at the location of the click
                        coinListRightClickMenu.show(coinsTable, e.getPoint().x, e.getPoint().y);
                    }
                }
            }
        });

        coinsTable.addSort(new ArrayList<>(List.of(0)), new ArrayList<>(List.of(2,3)));
        coinsTable.hideColumn(NumismatistAPI.Companion.getString("property_coin_id"));
    }

    private void setBillsTable() {
        String[] columnNames = {NumismatistAPI.Companion.getString("property_bill_id"),
                NumismatistAPI.Companion.getString("property_bill_toString"),
                NumismatistAPI.Companion.getString("property_bill_denomination"),
                NumismatistAPI.Companion.getString("property_bill_value"),
                NumismatistAPI.Companion.getString("property_bill_country"),
                NumismatistAPI.Companion.getString("property_bill_serial"),
                NumismatistAPI.Companion.getString("property_bill_signatures"),
                NumismatistAPI.Companion.getString("property_bill_grade"),
                NumismatistAPI.Companion.getString("property_bill_error"),
                NumismatistAPI.Companion.getString("property_bill_replacement"),
                NumismatistAPI.Companion.getString("property_bill_note"),
                NumismatistAPI.Companion.getString("property_bill_container")};

        String[] totalColumns = {NumismatistAPI.Companion.getString("property_bill_totals"),
                NumismatistAPI.Companion.getString("property_bill_totals_bills"),
                NumismatistAPI.Companion.getString("property_bill_totals_faceValue"),
                NumismatistAPI.Companion.getString("property_bill_totals_value"),
                NumismatistAPI.Companion.getString("property_bill_totals_replacements")};

        try {
            bills = api.getBills();
            if(set != null) {
                ArrayList<Bill> billsInSet = new ArrayList<>();
                for (SetItem item : set.getItems()) {
                    if (item instanceof Bill)
                        billsInSet.add((Bill) item);
                }
                bills.removeAll(billsInSet);
            }
        }
        catch (Exception e) {
            bills = new ArrayList<>();
        }

        // For formatting the numbers
        DecimalFormat format = new DecimalFormat();
        format.applyPattern("0.00");

        // Show bills
        Object[][] data = new Object[bills.size()][columnNames.length];
        Object[][] totalData = new Object[1][totalColumns.length];

        int count = 0;

        double totalFaceValue = 0.0;
        double totalValue = 0.0;
        int totalReplacements = 0;

        // for each bill found
        for (Bill bill : bills) {

            // Format denomination with currency symbol
            Currency currency = api.findCurrency(bill.getCurrency().getNameAbbr());

            double value;

            // For each column
            for(int j = 0; j < columnNames.length; j++) {
                String columnData = "";
                switch (j) {
                    case 0:
                        columnData += bill.getId();
                        break;
                    case 1:
                        columnData = bill.toString();
                        break;
                    case 2:
                        String valueString = format.format(bill.getDenomination());
                        totalFaceValue += bill.getDenomination();

                        if(currency.getSymbolBefore())
                            valueString = currency.getSymbol() + valueString;
                        else
                            valueString = valueString + currency.getSymbol();
                        columnData = "" + valueString;
                        break;
                    case 3:
                        String string;

                        if(bill.getValue() == 0.0)
                            value = bill.getDenomination();
                        else
                            value = bill.getValue();

                        totalValue += value;

                        if(currency.getSymbolBefore())
                            string = currency.getSymbol() + format.format(value);
                        else
                            string = format.format(value) + currency.getSymbol();

                        columnData = "" + string;
                        break;
                    case 4:
                        columnData = bill.getCountryName();
                        break;
                    case 5:
                        columnData = bill.getSerial();
                        break;
                    case 6:
                        columnData = bill.getSignatures();
                        break;
                    case 7:
                        columnData = bill.getCondition();
                        break;
                    case 8:
                        columnData = bill.getErrorType();
                        break;
                    case 9:

                        if(bill.getReplacement()) {
                            totalReplacements++;
                            columnData = "X";
                        }
                        break;
                    case 10:
                        columnData = bill.getNote();
                        break;
                    case 11:
                        columnData = api.findContainer(bill.getContainerId()).getName();
                        break;
                }
                data[count][j] = columnData;
            }
            count++;
        }

        // Show totals
        totalData[0][0] = "<HTML><B>" + NumismatistAPI.Companion.getString("property_bill_totals") + "</B></HTML>";
        totalData[0][1] = "<HTML><B>" + MessageFormat.format(NumismatistAPI.Companion.getString("property_bill_totals_bills"),
                bills.size()) + "</B></HTML>";
        totalData[0][2] = "<HTML><B>" + MessageFormat.format(NumismatistAPI.Companion.getString("property_bill_totals_replacements"),
                totalReplacements) + "</B></HTML>";
        totalData[0][3] = "<HTML><B>" + MessageFormat.format(NumismatistAPI.Companion.getString("property_bill_totals_faceValue"),
                format.format(totalFaceValue)) + "</B></HTML>";
        totalData[0][4] = "<HTML><B>" + MessageFormat.format(NumismatistAPI.Companion.getString("property_bill_totals_value"),
                format.format(totalValue)) + "</B></HTML>";

        // Set data for table
        DefaultTableModel dataModel = new DefaultTableModel();
        dataModel.setDataVector(data, columnNames);
        billsTable.setModel(dataModel);

        DefaultTableModel totalDataModel = new DefaultTableModel();
        totalDataModel.setDataVector(totalData, totalColumns);
        billsTotalTable.setModel(totalDataModel);

        // Make table non-editable, and single selection
        billsTable.setDefaultEditor(Object.class, null);
        billsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        billsTotalTable.setEnabled(false);

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
                        Bill selectedBill = null;
                        try {
                            int ID = Integer.parseInt((String)billsTable.getValueAt(index, 0));
                            // Find the set that has been clicked
                            for(Bill bill : bills)
                                if(bill.getId() == ID) {
                                    selectedBill = bill;
                                    break;
                                }
                        }
                        catch (Exception ex) {
                            return;
                        }

                        if(selectedBill == null)
                            return;

                        final Bill finalBill = selectedBill;

                        // Create right click menu
                        final JPopupMenu billListRightClickMenu = new JPopupMenu();
                        JMenuItem copy = new JMenuItem(Main.getString("viewColl_rtClick_copyBill"));
                        copy.addActionListener(e1 -> {
                            // Show edit bill screen
                            AddBillScreen addBillScreen = new AddBillScreen(parent, finalBill.copy());

                            addBillScreen.setFromCollection(true);
                            ((Main) parent).changeScreen(addBillScreen.getPanel(), Main.getString("addBill_title_add"));
                        });
                        JMenuItem editBill = new JMenuItem(Main.getString("viewColl_rtClick_editBill"));
                        editBill.addActionListener(e1 -> {
                            // Show edit bill screen
                            AddBillScreen addBillScreen = new AddBillScreen(parent, finalBill);

                            addBillScreen.setFromCollection(true);
                            ((Main) parent).changeScreen(addBillScreen.getPanel(), Main.getString("addBill_title_edit"));
                        });
                        JMenuItem removeBill = new JMenuItem(Main.getString("viewColl_rtClick_removeBill"));
                        removeBill.addActionListener(e1 ->{
                            // Show an "Are you sure?" prompt
                            int option = JOptionPane.showConfirmDialog(parent,
                                    Main.getString("viewColl_dialog_removeBillMessage"),
                                    Main.getString("viewColl_dialog_removeBillTitle"),
                                    JOptionPane.YES_NO_OPTION);

                            if(option == JOptionPane.YES_OPTION)
                            {
                                String errorMessage = "";
                                try {
                                    if(finalBill.removeFromDb(api)) {
                                        if (!finalBill.deleteObvImage()) {
                                            errorMessage += MessageFormat.format(Main.getString("error_deletingFile_message"), finalBill.getObvImgPath());
                                        }
                                        if (!finalBill.deleteRevImage()) {
                                            errorMessage += MessageFormat.format(Main.getString("error_deletingFile_message"), finalBill.getRevImgPath());
                                        }
                                    }
                                } catch (SecurityException | IOException | SQLException securityException) {
                                    errorMessage += securityException.getMessage();
                                }

                                if(!errorMessage.equals("")) {
                                    JOptionPane.showMessageDialog(parent, errorMessage,
                                            Main.getString("error_genericTitle"), JOptionPane.ERROR_MESSAGE);
                                }

                                setBillsTable();
                            }
                        });
                        billListRightClickMenu.add(editBill);
                        billListRightClickMenu.add(copy);
                        billListRightClickMenu.add(removeBill);

                        // Show the menu at the location of the click
                        billListRightClickMenu.show(billsTable, e.getPoint().x, e.getPoint().y);
                    }
                }
            }
        });

        billsTable.addSort(new ArrayList<>(List.of(0)), new ArrayList<>(List.of(2,3)));
        billsTable.hideColumn(NumismatistAPI.Companion.getString("property_bill_id"));
    }

    private void goHome() {
        ((Main) parent).changeScreen(((Main) parent).getPanel(), "");
    }

    private void export(String path) {

        SwingWorker<Void, Void> worker = new SwingWorker<>() {

            String error = "";

            @Override
            protected Void doInBackground(){
                try{
                    TableModel model;
                    JTable table;

                    switch (tabbedPane.getSelectedIndex()) {
                        case 0: table = coinsTable;
                            break;
                        case 1: table = setsTable;
                            break;
                        default: table = billsTable;
                            break;
                    }

                    model = table.getModel();

                    File file = new File(path);

                    // If file already exists
                    if(!file.createNewFile()) {
                        int result = JOptionPane.showConfirmDialog(parent,
                                Main.getString("warning_file_exists_message"),
                                path,JOptionPane.YES_NO_OPTION);
                        if(result == JOptionPane.NO_OPTION) {
                            error = Main.getString("warning_file_write_cancelled");
                            return null;
                        }
                    }

                    FileWriter csv = new FileWriter(file);

                    // Output column names
                    for (int i = 0; i < model.getColumnCount(); i++) {
                        // Don't output hidden columns
                        if(table.getColumn(table.getColumnName(i)).getWidth() != 0) {
                            // Using getModelIndex in case columns were moved
                            csv.write(model.getColumnName(table.getColumn(table.getColumnName(i)).getModelIndex()) + ",");
                        }
                    }

                    csv.write("\n");

                    // Output data 1 row at a time
                    for (int i = 0; i < model.getRowCount(); i++) {
                        for (int j = 0; j < model.getColumnCount(); j++) {
                            // Don't output hidden columns
                            if(table.getColumn(table.getColumnName(j)).getWidth() != 0) {
                                if (model.getValueAt(i, j) != null)
                                    // Using getModelIndex in case columns were moved
                                    csv.write(model.getValueAt(i, table.getColumn(table.getColumnName(j)).getModelIndex()) + ",");
                                else
                                    csv.write(",");
                            }
                        }
                        csv.write("\n");
                    }

                    csv.close();

                } catch(IOException e){
                    error = e.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                if(!error.equals("")) {
                    JOptionPane.showMessageDialog(parent, error, Main.getString("error_fileSave"), JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        Main.showBackgroundPopup(parent,
                Main.getString("export_file_saving_message"),
                Main.getString("export_file_saving_title"), worker);
    }

    public void setTab(int index) {
        tabbedPane.setSelectedIndex(index);
    }

    public void setSet(Set set) {
        this.set = set;

        // Remove this set, and any items in the set, from the list
        setCoinsTable();
        setBillsTable();
        setSetsTable();
    }

    public Set getSet() {
        return set;
    }
}
