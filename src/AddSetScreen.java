import items.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

@SuppressWarnings("rawtypes")
public class AddSetScreen extends AddSetItemScreen {

    private JTextField nameInput;
    private JButton cancelButton;
    private JButton OKButton;
    private JTextField yearInput;
    private JTextField valueInput;
    private JLabel faceValueDisplay;
    private JButton addNewButton;
    private JButton addExistingButton;
    private JList coinList;
    private final DefaultListModel listModel;
    private JPanel panel;
    private JTextArea noteInput;
    private JTextArea errorDisplay;
    private JButton saveNewButton;
    private JTextField imageObvLocationInput;
    private JButton obvSetButton;
    private JButton obvBrowseButton;
    private JButton obvRemoveButton;
    private JTextField imageRevLocationInput;
    private JButton revSetButton;
    private JButton revRemoveButton;
    private JButton revBrowseButton;
    private JPanel obvPicPanel;
    private JPanel revPicPanel;
    private JButton setCoinsButton;
    private JButton saveCopyButton;
    private JComboBox locationDropDown;
    private JButton addContainerButton;
    private JScrollPane scrollPane;

    private final Set originalSet;
    private Set set = new Set();

    public AddSetScreen(JFrame parent) {
        this(parent, new Set());
    }

    public AddSetScreen(JFrame parent, Set set) {
        super(parent);

        setReturnTab(TAB_SETS);

        if(set == null)
            set = new Set();

        originalSet = set.copy();

        api = ((Main) parent).api;

        setImageObvLocationInput(imageObvLocationInput);
        setImageRevLocationInput(imageRevLocationInput);

        setObvPicPanel(obvPicPanel);
        setRevPicPanel(revPicPanel);

        listModel = new DefaultListModel();

        // Restrict input
        ((PlainDocument) yearInput.getDocument()).setDocumentFilter(MyDocFilter.Companion.getCurrentYearFilter());
        ((PlainDocument) valueInput.getDocument()).setDocumentFilter(MyDocFilter.Companion.getValueFilter());

        ComboBoxHelper.setContainerList(locationDropDown, api, addContainerButton, parent);

        cancelButton.addActionListener(e -> {
            // Find items that were added to the set and remove them
            ArrayList<SetItem> removeItems = new ArrayList<>();
            for(SetItem item : this.set.getItems()) {
                if(!originalSet.getItems().contains(item))
                    removeItems.add(item);
            }
            for(SetItem item : removeItems)
                this.set.removeItem(item);

            // Find items that were originally in the set but are now missing and re-add them
            for(SetItem item : originalSet.getItems() ) {
                if(!this.set.getItems().contains(item)) {
                    this.set.addItem(item);
                }
            }

            // Set all other properties back to their original values
            setSet(originalSet);
            goHome();
        });
        OKButton.addActionListener(e -> saveSet(getBUTTON_OK()));
        saveNewButton.addActionListener(e -> saveSet(getBUTTON_SAVE_NEW()));
        saveCopyButton.addActionListener( e -> saveSet(getBUTTON_SAVE_COPY()));

        obvSetButton.addActionListener(e -> {
            setObvImageLocation(imageObvLocationInput.getText());
            addImage(getObvImageLocation(), true);
        });

        revSetButton.addActionListener(e -> {
            setRevImageLocation(imageRevLocationInput.getText());
            addImage(getRevImageLocation(), true);
        });


        obvBrowseButton.addActionListener( e -> openFileChooser(true));
        revBrowseButton.addActionListener( e -> openFileChooser(false));

        obvRemoveButton.addActionListener(e -> removeImage(true));
        revRemoveButton.addActionListener(e -> removeImage(false));

        addNewButton.addActionListener(e -> {

            //Create the popup menu
            final JPopupMenu popup = new JPopupMenu();
            popup.add(new JMenuItem(new AbstractAction(Main.getString("addSet_menu_newCoin")) {
                public void actionPerformed(ActionEvent e) {
                    setSetFromInput();
                    showCoinScreen(new Coin());
                }
            }));
            popup.add(new JMenuItem(new AbstractAction(Main.getString("addSet_menu_newBill")) {
                public void actionPerformed(ActionEvent e) {
                    setSetFromInput();
                    showBillScreen(new Bill());
                }
            }));
            popup.add(new JMenuItem(new AbstractAction(Main.getString("addSet_menu_newSet")) {
                public void actionPerformed(ActionEvent e) {
                    setSetFromInput();
                    showSetScreen(new Set());
                }
            }));

            popup.show(addNewButton, 0, addNewButton.getHeight());
        });

        addExistingButton.addActionListener(e -> {
            setSetFromInput();

            JDialog existingItemDialog = new JDialog(parent);
            CollectionTableScreen collectionTableScreen = new CollectionTableScreen(parent, existingItemDialog);
            collectionTableScreen.setSet(this.set);

            String name;

            if(this.set.getName().equals(""))
                name = Main.getString("property_set_toString").toLowerCase();
            else
                name = this.set.getName();

            existingItemDialog.add(collectionTableScreen.getPanel());
            existingItemDialog.pack();
            existingItemDialog.setTitle(MessageFormat.format(Main.getString("addSet_popUp_title"), name));
            existingItemDialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            existingItemDialog.setSize(800,600);
            existingItemDialog.setMinimumSize(new Dimension(400,300));
            existingItemDialog.setLocationRelativeTo(parent);
            existingItemDialog.setModal(true);
            existingItemDialog.setVisible(true);

            // Runs when dialog closed
            if(collectionTableScreen.selectedItem != null)
                getSet().getItems().add(collectionTableScreen.selectedItem);
                //getSet().addItem(collectionTableScreen.selectedItem);
            setInfo();

            // Reset default button, as it seems to get lost after closing the dialog
            parent.getRootPane().setDefaultButton(OKButton);
        });

        coinList.setLayoutOrientation(JList.VERTICAL);

        setSet(set);

        setInfo();

        // Add right click listener
        coinList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(SwingUtilities.isRightMouseButton(e)){

                    // find which item, if any, was clicked
                    int index = coinList.locationToIndex(e.getPoint());

                    if(index != -1 ) {

                        // Find bounds of cell
                        Rectangle selectedCell = coinList.getCellBounds(index, index + 1);
                        int maxX = selectedCell.x + selectedCell.width;
                        int maxY = selectedCell.y + selectedCell.height;

                        // If clicked in a cell
                        if ((e.getX() > selectedCell.getX()) && (e.getX() < maxX) &&
                                (e.getY() > selectedCell.getY()) && (e.getY() < maxY)
                        ) {

                            coinList.setSelectedIndex(index);
                            SetItem selectedItem = getItem(index);

                            // Create right click menu
                            final JPopupMenu coinListRightClickMenu = new JPopupMenu();
                            JMenuItem editCoin = new JMenuItem(Main.getString("addSet_rtClickItem_edit"));
                            editCoin.addActionListener(e1 -> {
                                setSetFromInput();
                                // Show edit screen
                                if(selectedItem instanceof Coin)
                                    showCoinScreen((Coin)selectedItem);
                                else if(selectedItem instanceof Bill)
                                    showBillScreen((Bill) selectedItem);
                                else
                                    showSetScreen((Set) selectedItem);
                            });
                            JMenuItem removeCoin = new JMenuItem(Main.getString("addSet_rtClickItem_remove"));
                            removeCoin.addActionListener(e1 ->{
                                if(removeItem(selectedItem))
                                    setInfo();
                            });
                            coinListRightClickMenu.add(editCoin);
                            coinListRightClickMenu.add(removeCoin);

                            // Show the menu at the location of the click
                            coinListRightClickMenu.show(coinList, e.getPoint().x, e.getPoint().y);
                        }
                    }
                }
            }
        });
        setCoinsButton.addActionListener(e -> setCoinsToYear());

        parent.getRootPane().setDefaultButton(OKButton);
        // Set initial focus
        SwingUtilities.invokeLater(() -> nameInput.requestFocus());
    }

    private void setCoinsToYear() {
        for (SetItem item : set.getItems()) {
            try {
                set.setYear(Integer.parseInt(yearInput.getText()));
                item.setYear(set.getYear());
                setCoinList();
            }
            catch (NumberFormatException ex) {
                errorDisplay.setForeground(Main.COLOR_ERROR);
                errorDisplay.setText(Main.getString("Year cannot be blank."));
            }

        }
    }

    private SetItem getItem(int index) {
        return set.getItems().get(index);
    }

    private boolean removeItem(SetItem item) {
        return set.removeItem(item);
    }

    private void setInfo() {

        nameInput.setText(set.getName());
        if(set.getYear() != Set.YEAR_NONE)
            yearInput.setText("" + set.getYear());
        else
            yearInput.setText("");
        if(set.getValue() != 0.0)
            valueInput.setText("" + set.getValue());
        else
            valueInput.setText("");
        noteInput.setText(set.getNote());

        locationDropDown.setSelectedItem(api.findContainer(set.getContainerId()).getName());

        // Set images
        if(!set.getObvImgPath().equals("")) {
            setObvImageLocation(imageObvLocationInput.getText());
            addImage(getObvImageLocation(), true);
        }
        if(!set.getRevImgPath().equals("")) {
            setRevImageLocation(imageRevLocationInput.getText());
            addImage(getRevImageLocation(), true);
        }

        setCoinList();
        setFaceValueDisplay();
    }

    void setSetFromInput() {
        set.setName(nameInput.getText());

        try {
            set.setYear(Integer.parseInt(yearInput.getText()));
        }
        catch (NumberFormatException ignored) {

        }

        try {
            set.setValue(Double.parseDouble(valueInput.getText()));
        }
        catch (NumberFormatException ignored) {

        }

        set.setNote(Main.escapeForJava(noteInput.getText()));
    }

    private void saveSet(int button) {
        String errorMessage = "";

        if(!nameInput.getText().equals(""))
            set.setName(nameInput.getText());
        else
            errorMessage += Main.getString("error_emptyName");

        if(!yearInput.getText().equals(""))
            try {
                set.setYear(Integer.parseInt(yearInput.getText()));
            }
            catch (NumberFormatException ex) {
                if(!errorMessage.equals(""))
                    errorMessage += "\n";
                errorMessage += Main.getString("error_emptyYear");
            }

        if(!valueInput.getText().equals("")) {
            try {
                set.setValue(Double.parseDouble(valueInput.getText()));
            }
            catch (NumberFormatException ex) {
                if(!errorMessage.equals(""))
                    errorMessage += "\n";

                errorMessage += Main.getString("error_emptyValue");
            }
        }
        else
            set.setValue(0.0);

        set.setNote(Main.escapeForJava(noteInput.getText()));

        if(locationDropDown.getSelectedItem() != null &&
                !locationDropDown.getSelectedItem().equals("")) {
            set.setContainerId(api.findContainer(locationDropDown.getSelectedItem().toString()).getId());
        }

        if(errorMessage.equals("")) {

            errorDisplay.setForeground(Color.BLACK);
            errorDisplay.setText(Main.getString("addSet_message_saving"));
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                String errorMessage = "";
                String returnMessage = "";

                @Override
                protected Void doInBackground() {
                    int rows;
                    try {
                        rows = set.saveToDb(api);
                        returnMessage = api.getSuccessMessage(rows);
                    }
                    catch (IOException ioe) {
                        returnMessage = ioe.getMessage();
                    }

                    return null;
                }

                @Override
                protected void done() {
                    // Check for success
                    if(returnMessage.equals(NumismatistAPI.Companion.getString("db_message_success")) ||
                            returnMessage.equals(NumismatistAPI.Companion.getString("db_message_noChange")) ||
                            returnMessage.equals("")) {

                        if(getValidObvImg()) {
                            try {
                                if(!set.saveObvImage())
                                {
                                    errorMessage += MessageFormat.format(Main.getString("error_savingFile_message"), set.getObvImgPath());
                                }
                            } catch (SecurityException | IOException securityException ) {
                                errorMessage += securityException.getMessage();
                            }
                        }
                        else {
                            try {
                                if(!set.deleteObvImage())
                                {
                                    if(!errorMessage.equals(""))
                                        errorMessage += "\n";

                                    errorMessage += MessageFormat.format(Main.getString("error_deletingFile_message"), set.getObvImgPath());
                                }
                            } catch (SecurityException | IOException securityException ) {
                                if(!errorMessage.equals(""))
                                    errorMessage += "\n";

                                errorMessage += securityException.getMessage();
                            }
                        }

                        if(getValidRevImg()) {
                            try {
                                if(!set.saveRevImage())
                                {
                                    if(!errorMessage.equals(""))
                                        errorMessage += "\n";

                                    errorMessage += MessageFormat.format(Main.getString("error_savingFile_message"), set.getRevImgPath());
                                }
                            } catch (SecurityException | IOException securityException ) {
                                if(!errorMessage.equals(""))
                                    errorMessage += "\n";

                                errorMessage += securityException.getMessage();
                            }
                        }
                        else {
                            try {
                                if(!set.deleteRevImage())
                                {
                                    if(!errorMessage.equals(""))
                                        errorMessage += "\n";

                                    errorMessage += MessageFormat.format(Main.getString("error_deletingFile_message"), set.getRevImgPath());
                                }
                            } catch (SecurityException | IOException securityException ) {
                                if(!errorMessage.equals(""))
                                    errorMessage += "\n";

                                errorMessage += securityException.getMessage();
                            }
                        }

                        if(!errorMessage.equals("")) {
                            errorDisplay.setForeground(Main.COLOR_ERROR);
                            errorDisplay.setText(errorMessage);
                        }
                        else {
                            errorDisplay.setForeground(Main.COLOR_SUCCESS);
                            errorDisplay.setText(MessageFormat.format(Main.getString("addSet_message_saved"), set.toString()));

                            // Add to set if necessary
                            if(getParentSet() != null && !getEditingParent())
                                getParentSet().addItem(set);

                            if(button == getBUTTON_OK()) {
                                set.getRemovedItems().clear();
                                goHome();
                            }
                            else if(button == getBUTTON_SAVE_NEW()) {
                                setSet(new Set());

                                getParent().setTitle(Main.getString("addSet_title_add"));
                            }
                            else if(button == getBUTTON_SAVE_COPY()) {

                                Set newSet = set.copy();
                                newSet.setSet(null);
                                newSet.setId(DatabaseItem.ID_INVALID);
                                newSet.setObvImgPath("");
                                newSet.setRevImgPath("");
                                newSet.setContainerId(DatabaseItem.ID_INVALID);

                                if(getParentSet() != null) {
                                    newSet.setSet(getParentSet());
                                    getParent().setTitle(Main.getString("addSet_title_addToSet"));
                                }
                                else
                                    getParent().setTitle(Main.getString("addSet_title_add"));

                                setSet(newSet);
                            }
                        }
                    }
                    else {
                        errorDisplay.setForeground(Main.COLOR_ERROR);
                        errorDisplay.setText(returnMessage);
                    }
                }
            };

            worker.execute();
        }
        else
        {
            errorDisplay.setForeground(Main.COLOR_ERROR);
            errorDisplay.setText(errorMessage);
        }
    }

    JPanel getPanel() {
        return panel;
    }

    void setFaceValueDisplay() {
        // Format the number properly
        // Necessary due to inaccuracies caused by binary decimal calculations
        DecimalFormat format = new DecimalFormat();
        format.applyPattern("0.00");
        String value = format.format(set.getFaceValue());

        faceValueDisplay.setText("" + value);
    }

    public void setSet(Set set) {
        this.set = set;

        setInfo();
    }

    @SuppressWarnings("unchecked")
    void setCoinList() {
        listModel.removeAllElements();

        for(int i = 0; i< set.getItems().size(); i++ )
            listModel.addElement(set.getItems().get(i));

        coinList.setModel(listModel);
        coinList.invalidate();
    }

    private void showCoinScreen(Coin coin) {
        AddCoinScreen addCoinScreen = new AddCoinScreen(getParent(), coin);
        addCoinScreen.setParentSet(this.set);
        addCoinScreen.setFromCollection(getFromCollection());
        addCoinScreen.setPreviousScreen(this);

        if(set.getItems().contains(coin))
            addCoinScreen.setEditingParent(true);

        String name;

        if(set.getName().equals(""))
            name = Main.getString("property_set_toString");
        else
            name = set.getName();

        if(coin.getId() != 0)
            ((Main) getParent()).changeScreen(addCoinScreen.getPanel(), MessageFormat.format(Main.getString("addCoin_title_editInSet"), name));
        else
            ((Main) getParent()).changeScreen(addCoinScreen.getPanel(), MessageFormat.format(Main.getString("addCoin_title_addToSet"), name));
    }

    private void showBillScreen(Bill bill) {
        AddBillScreen addBillScreen = new AddBillScreen(getParent(), bill);
        addBillScreen.setSet(this.set);
        addBillScreen.setFromCollection(getFromCollection());
        addBillScreen.setPreviousScreen(this);

        if(set.getItems().contains(bill))
            addBillScreen.setEditingParent(true);

        String name;

        if(set.getName().equals(""))
            name = Main.getString("property_set_toString");
        else
            name = set.getName();

        if(bill.getId() != 0)
            ((Main) getParent()).changeScreen(addBillScreen.getPanel(), MessageFormat.format(Main.getString("addBill_title_editInSet"),name));
        else
            ((Main) getParent()).changeScreen(addBillScreen.getPanel(), MessageFormat.format(Main.getString("addBill_title_addToSet"),name));
    }

    private void showSetScreen(Set newSet) {
        AddSetScreen addSetScreen = new AddSetScreen(getParent(), newSet);
        addSetScreen.setParentSet(this.set);
        addSetScreen.setFromCollection(getFromCollection());
        addSetScreen.setPreviousScreen(this);

        if(set.getItems().contains(newSet))
            addSetScreen.setEditingParent(true);

        String newName;

        if(newSet.getName().equals(""))
            newName = Main.getString("property_set_toString");
        else
            newName = newSet.getName();

        if(newSet.getId() != 0) {
            ((Main) getParent()).changeScreen(addSetScreen.getPanel(), MessageFormat.format(Main.getString("addSet_title_edit"), newName));
        }
        else {
            String name = set.getName();
            if(name.equals(""))
                name = Main.getString("property_set_toString");
            String newTitle = MessageFormat.format(Main.getString("addSet_title_addToSet"), name);
            ((Main) getParent()).changeScreen(addSetScreen.getPanel(), newTitle);
        }
    }

    public Set getSet() {
        return set;
    }

    private void createUIComponents() {

        scrollPane = new JScrollPane();
        scrollPane.setBorder(null);

        // Allow resizing of images
        obvPicPanel = new JPanel() {

            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);

                try {
                    Image img = ImageIO.read(new File(getObvImageLocation()));

                    double heightFactor = (float)getHeight() / img.getHeight(this);
                    double widthFactor = (float)getWidth() / img.getWidth(this);

                    double scaleFactor = Math.min(heightFactor, widthFactor);

                    int newHeight = (int)(img.getHeight(this) * scaleFactor);
                    int newWidth = (int)(img.getWidth(this) * scaleFactor);

                    Image scaled;
                    // Only scale image if it's larger than we want
                    if(scaleFactor < 1) {
                        // Scale to new size
                        scaled = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                    }
                    else
                        scaled = img;

                    g.drawImage(scaled, 0, 0, null);
                } catch (IOException ignore) {
                }
            }
        };

        // Allow resizing of images
        revPicPanel = new JPanel() {

            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);

                try {

                    Image img = ImageIO.read(new File(getRevImageLocation()));

                    double heightFactor = (float)getHeight() / img.getHeight(this);
                    double widthFactor = (float)getWidth() / img.getWidth(this);

                    double scaleFactor = Math.min(heightFactor, widthFactor);

                    int newHeight = (int)(img.getHeight(this) * scaleFactor);
                    int newWidth = (int)(img.getWidth(this) * scaleFactor);

                    Image scaled;

                    // Only scale image if it's larger than we want
                    if(scaleFactor < 1) {
                        // Scale to new size
                        scaled = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                    }
                    else
                        scaled = img;

                    g.drawImage(scaled, 0, 0, null);
                } catch (IOException ignore) {
                }
            }
        };
    }
}
