import items.Coin;
import items.CoinSet;
import items.DatabaseConnection;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

public class AddSetScreen {

    private JTextField nameInput;
    private JButton cancelButton;
    private JButton OKButton;
    private JTextField yearInput;
    private JTextField valueInput;
    private JLabel faceValueDisplay;
    private JButton addNewCoinButton;
    private JButton addExistingCoinButton;
    private JList coinList;
    private DefaultListModel listModel;
    private JPanel panel;
    private JTextArea noteInput;
    private JTextArea errorDisplay;
    private JButton addAnotherButton;
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
    private JButton anotherSameButton;
    private JButton setCoinsButton;

    private CoinSet set;

    private final JFrame parent;

    private String obvImageLocation = "";
    private String revImageLocation = "";

    private boolean validObvImg = false;
    private boolean validRevImg = false;

    private boolean fromCollection = false;

    public AddSetScreen(JFrame parent, CoinSet set) {
        this.parent = parent;

        listModel = new DefaultListModel();

        cancelButton.addActionListener(e -> goHome());

        OKButton.addActionListener(e -> {

            if(saveSet()) {
                this.set.getRemovedCoins().clear();
                goHome();
            }
        });

        addAnotherButton.addActionListener(e -> {
            if(saveSet()) {
                setSet(new CoinSet());
                errorDisplay.setForeground(Color.GREEN);
                errorDisplay.setText("Set Saved!");

                parent.setTitle("Add Set");
            }
        });

        anotherSameButton.addActionListener( e -> {
            if(saveSet()) {
                CoinSet newSet = this.set.copy();
                setSet(newSet);
                errorDisplay.setForeground(Color.GREEN);
                errorDisplay.setText("Set Saved!");

                parent.setTitle("Add Set");
            }
        });

        obvSetButton.addActionListener(e -> {
            obvImageLocation = imageObvLocationInput.getText();
            addImage(obvImageLocation, true);
        });

        revSetButton.addActionListener(e -> {
            revImageLocation = imageRevLocationInput.getText();
            addImage(revImageLocation, true);
        });


        obvBrowseButton.addActionListener( e -> openFileChooser(true));
        revBrowseButton.addActionListener( e -> openFileChooser(false));

        obvRemoveButton.addActionListener(e -> removeImage(true));
        revRemoveButton.addActionListener(e -> removeImage(false));

        addNewCoinButton.addActionListener( e -> {
            setSetFromInput();
            showCoinScreen(new Coin());
        });

        addExistingCoinButton.addActionListener(e -> {
            setSetFromInput();
            ExistingCoinDialog existingCoinDialog = new ExistingCoinDialog(parent, this.set);
            Coin newCoin = existingCoinDialog.showDialog();

            if(newCoin != null && !this.set.getCoins().contains(newCoin))
                addCoin(newCoin);
        });

        coinList.setLayoutOrientation(JList.VERTICAL);

        if(set == null)
            set = new CoinSet();

        setSet(set);

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

                        // If clicked inside of a cell
                        if ((e.getX() > selectedCell.getX()) && (e.getX() < maxX) &&
                                (e.getY() > selectedCell.getY()) && (e.getY() < maxY)
                        ) {

                            coinList.setSelectedIndex(index);
                            Coin selectedCoin = getCoin(index);

                            // Create right click menu
                            final JPopupMenu coinListRightClickMenu = new JPopupMenu();
                            JMenuItem editCoin = new JMenuItem("Edit");
                            editCoin.addActionListener(e1 -> {
                                setSetFromInput();
                                // Show edit coin screen
                                showCoinScreen(selectedCoin);
                            });
                            JMenuItem removeCoin = new JMenuItem("Remove from Set");
                            removeCoin.addActionListener(e1 ->{
                                removeCoin(selectedCoin);
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
    }

    private void setCoinsToYear() {
        for (Coin coin : set.getCoins()) {
            try {
                set.setYear(Integer.parseInt(yearInput.getText()));
                coin.setYear(set.getYear());
                setCoinList();
            }
            catch (NumberFormatException ex) {
                errorDisplay.setForeground(Color.RED);
                errorDisplay.setText("Year format is incorrect. Must be an integer (whole number)");
            }

        }
    }

    private Coin getCoin(int index) {
        return set.getCoins().get(index);
    }

    private boolean removeCoin(Coin coin) {
        return set.removeCoin(coin);
    }

    public AddSetScreen(JFrame parent) {
        this(parent, new CoinSet());
    }

    void setInfo() {

        nameInput.setText(set.getName());
        if(set.getYear() !=0)
            yearInput.setText("" + set.getYear());
        else
            yearInput.setText("");
        if(set.getValue() != 0.0)
            valueInput.setText("" + set.getValue());
        else
            valueInput.setText("");
        noteInput.setText(set.getNote());

        // Set images
        if(!set.getObvImgExt().equals("")) {
            obvImageLocation = imageObvLocationInput.getText();
            addImage(obvImageLocation, true);
        }
        if(!set.getRevImgExt().equals("")) {
            revImageLocation = imageRevLocationInput.getText();
            addImage(revImageLocation, true);
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

    boolean saveSet() {
        String errorMessage = "";

        if(!nameInput.getText().equals(""))
            set.setName(nameInput.getText());
        else
            errorMessage += "Name cannot be blank.";

        if(!yearInput.getText().equals(""))
            try {
                set.setYear(Integer.parseInt(yearInput.getText()));
            }
            catch (NumberFormatException ex) {
                if(!errorMessage.equals(""))
                    errorMessage += "\n";
                errorMessage += "Year format is incorrect. Must be an integer (whole number) or blank.";
            }

        if(!valueInput.getText().equals("")) {
            try {
                set.setValue(Double.parseDouble(valueInput.getText()));
            }
            catch (NumberFormatException ex) {
                if(!errorMessage.equals(""))
                    errorMessage += "\n";

                errorMessage += "Value format incorrect. Must be a number or blank.";
            }
        }
        else
            set.setValue(0.0);

        set.setNote(Main.escapeForJava(noteInput.getText()));

        if(errorMessage.equals("")) {
            String message = set.saveToDb(((Main)parent).databaseConnection);

            // Check for success
            if(message.equals(DatabaseConnection.SUCCESS_MESSAGE) ||
                message.equals(DatabaseConnection.NO_CHANGE_MESSAGE) ||
                message.equals("")) {
                // Copy the image to a new location, then use that location
                if(validObvImg) {
                    String path = imageObvLocationInput.getText();
                    set.setObvImgExt(path.substring(path.lastIndexOf('.')));

                    if(Main.copyFile(imageObvLocationInput.getText(),
                            set.getImagePath(true))) {

                        String sql = "UPDATE Sets SET ObvImgExt=\"" + set.getObvImgExt() + "\"\n" +
                                "WHERE ID=" + set.getId() + ";";

                        int rows = ((Main) parent).databaseConnection.runUpdate(sql);
                        String extMessage = ((Main) parent).databaseConnection.wasSuccessful(rows);
                        if (!extMessage.equals(DatabaseConnection.SUCCESS_MESSAGE)) {
                            errorMessage += extMessage;
                        }
                    }
                    else {
                        errorMessage = "Problem saving obverse image. Please try again.";
                    }
                }
                else {
                    try {
                        // Delete the file
                        new File(set.getImagePath(true)).delete();
                        set.setObvImgExt("");

                        // Remove image extension from database
                        String sql = "UPDATE Sets SET ObvImgExt=null\n" +
                                "WHERE ID=" + set.getId() + ";";

                        ((Main) parent).databaseConnection.runUpdate(sql);
                    }
                    catch (Exception ignore) {}
                }
                if(validRevImg) {
                    String path = imageRevLocationInput.getText();
                    set.setRevImgExt(path.substring(path.lastIndexOf('.')));

                    if(Main.copyFile(imageRevLocationInput.getText(),
                            set.getImagePath(false))) {

                        String sql = "UPDATE Sets SET RevImgExt=\"" + set.getRevImgExt() + "\"\n" +
                                "WHERE ID=" + set.getId() + ";";

                        int rows = ((Main) parent).databaseConnection.runUpdate(sql);
                        String extMessage = ((Main) parent).databaseConnection.wasSuccessful(rows);
                        if (!extMessage.equals(DatabaseConnection.SUCCESS_MESSAGE)) {
                            if (!errorMessage.equals(""))
                                errorMessage += extMessage;
                        }
                    }
                    else {
                        if (!errorMessage.equals(""))
                            errorMessage += "\n";
                        errorMessage += "Problem saving reverse image. Please try again.";
                    }
                }
                else {
                    try {
                        // Delete the file
                        new File(set.getImagePath(false)).delete();
                        set.setRevImgExt("");

                        // Remove image extension from database
                        String sql = "UPDATE Sets SET RevImgExt=null\n" +
                                "WHERE ID=" + set.getId() + ";";

                        ((Main) parent).databaseConnection.runUpdate(sql);
                    }
                    catch (Exception ignore) {}
                }
                if(!errorMessage.equals("")) {

                    errorDisplay.setForeground(Color.RED);
                    errorDisplay.setText(errorMessage);

                    return false;
                }
            }
            else {
                errorDisplay.setForeground(Color.RED);
                errorDisplay.setText(message);
                return false;
            }
            return true;
        }
        else
        {
            errorDisplay.setForeground(Color.RED);
            errorDisplay.setText(errorMessage);
        }

        return false;
    }

    JPanel getPanel() {
        return panel;
    }

    private void goHome() {
        if(fromCollection) {
            CollectionTableScreen collectionTableScreen = new CollectionTableScreen(parent);
            collectionTableScreen.setTab(1);
            ((Main) parent).changeScreen(collectionTableScreen.getPanel(), "Collection");
        }
        else
            ((Main) parent).changeScreen(((Main) parent).getPanel(), "Coin Collection");
    }

    void addCoin(Coin newCoin) {
        set.addCoin(newCoin);

        setInfo();
    }

    void addImage(String pathToImage, boolean obverse) {

        try {
            ImageIO.read(new File(pathToImage));

            if(obverse) {
                obvImageLocation = pathToImage;
                validObvImg = true;
                // Force it to draw immediately
                obvPicPanel.update(obvPicPanel.getGraphics());
            }
            else {
                revImageLocation = pathToImage;
                validRevImg = true;
                // Force it to draw immediately
                revPicPanel.update(revPicPanel.getGraphics());
            }

            // Clear error text
            errorDisplay.setText("");
        }
        catch (IOException e) {
            errorDisplay.setText("Error opening file");
        }
    }

    void removeImage(boolean obverse) {
        if(obverse) {
            imageObvLocationInput.setText("");
            validObvImg = false;
        }
        else {
            imageRevLocationInput.setText("");
            validRevImg = false;
        }

        errorDisplay.setText("");
    }

    void openFileChooser(boolean obverse) {
        final JFileChooser fc = new JFileChooser();

        ImageFilter imageFilter = new ImageFilter();

        fc.addChoosableFileFilter(imageFilter);
        fc.setFileFilter(imageFilter);
        int returnVal = fc.showOpenDialog(parent);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();

            if(obverse) {
                obvImageLocation = file.getAbsolutePath();
                imageObvLocationInput.setText(obvImageLocation);
            }
            else {
                revImageLocation = file.getAbsolutePath();
                imageRevLocationInput.setText(revImageLocation);
            }

            addImage(file.getAbsolutePath(), obverse);
        } else if(returnVal != JFileChooser.CANCEL_OPTION) {
            errorDisplay.setText("Error retrieving file");
        }
    }

    void setFaceValueDisplay() {
        // Format the number properly
        // Necessary due to inaccuracies caused by binary decimal calculations
        DecimalFormat format = new DecimalFormat();
        format.applyPattern("0.00");
        String value = format.format(set.getFaceValue());

        faceValueDisplay.setText("$" + value);
    }

    public void setSet(CoinSet set) {
        this.set = set;

        setInfo();
    }

    void setCoinList() {
        listModel.removeAllElements();

        for(int i = 0; i< set.getCoins().size(); i++ )
            listModel.addElement(set.getCoins().get(i));

        coinList.setModel(listModel);
        coinList.invalidate();
    }

    private void showCoinScreen(Coin coin) {
        AddCoinScreen addCoinScreen = new AddCoinScreen(parent);
        addCoinScreen.setSet(this.set);
        addCoinScreen.setFromCollection(fromCollection);
        addCoinScreen.setCoin(coin);

        if(set.getCoins().contains(coin))
            addCoinScreen.setEditingSet(true);

        if(coin.getId() != 0)
            ((Main)parent).changeScreen(addCoinScreen.getPanel(), "Edit New Coin in Set");
        else
            ((Main)parent).changeScreen(addCoinScreen.getPanel(), "Add New Coin to Set");
    }

    public CoinSet getSet() {
        return set;
    }

    public void setFromCollection(boolean fromCollection) {
        this.fromCollection = fromCollection;
    }

    private void createUIComponents() {

        // Allow resizing of images
        obvPicPanel = new JPanel() {

            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);

                try {
                    Image img = ImageIO.read(new File(obvImageLocation));

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

                    Image img = ImageIO.read(new File(revImageLocation));

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
