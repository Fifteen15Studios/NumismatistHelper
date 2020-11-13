import items.Coin;
import items.CoinSet;
import items.DatabaseConnection;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

public class AddCoinScreen {

    private JButton cancelButton;
    private JPanel addCoinPanel;
    private JButton OKButton;
    private JTextField coinTypeInput;
    private JTextField denominationInput;
    private JButton pennyButton;
    private JButton nickelButton;
    private JButton dimeButton;
    private JButton quarterButton;
    private JTextField yearInput;
    private JTextField countryInput;
    private JComboBox mintMarkComboBox;
    private JComboBox gradeComboBox;
    private JCheckBox gradedCheckBox;
    private JCheckBox errorCheckBox;
    private JTextField errorTypeInput;
    private JTextArea errorDisplay;
    private JTextField imageObvLocationInput;
    private JButton obvBrowseButton;
    private JButton obvSetButton;
    private JTextField valueInput;
    private JButton obvRemoveButton;
    private JTextField imageRevLocationInput;
    private JButton revBrowseButton;
    private JButton revSetButton;
    private JButton revRemoveButton;
    private JTextArea noteInput;
    private JButton addAnotherButton;
    private JPanel obvPicPanel;
    private JPanel revPicPanel;

    private final JFrame parent;

    private boolean editingSet = false;
    private boolean fromCollection = false;

    private Coin coin;
    private CoinSet set = null;

    private boolean validObvImg = false;
    private boolean validRevImg = false;

    private String obvImageLocation = "";
    private String revImageLocation = "";

    public AddCoinScreen(JFrame parent) {
        this(parent, new Coin());
    }

    public AddCoinScreen(JFrame parent, final Coin coin) {
        this.parent = parent;

        this.coin = coin;

        pennyButton.addActionListener(e -> denominationInput.setText("" + Coin.PENNY));
        nickelButton.addActionListener(e -> denominationInput.setText("" + Coin.NICKEL));
        dimeButton.addActionListener(e -> denominationInput.setText("" + Coin.DIME));
        quarterButton.addActionListener(e -> denominationInput.setText("" + Coin.QUARTER));

        errorCheckBox.addActionListener(e -> errorTypeInput.setEnabled(errorCheckBox.isSelected()));

        for(int i = 0; i < Coin.Companion.getMINT_MARKS().length; i++) {
            mintMarkComboBox.addItem(Coin.Companion.getMINT_MARKS()[i]);
        }

        for(int i = 0; i < Coin.Companion.getCONDITIONS().length; i++) {
            gradeComboBox.addItem(Coin.Companion.getCONDITIONS()[i]);
        }

        obvSetButton.addActionListener(e -> {
            obvImageLocation = imageObvLocationInput.getText();
            addImage(obvImageLocation, true);
        });

        revSetButton.addActionListener(e -> {
            revImageLocation = imageRevLocationInput.getText();
            addImage(revImageLocation, false);
        });

        obvBrowseButton.addActionListener( e -> openFileChooser(true));
        revBrowseButton.addActionListener( e -> openFileChooser(false));

        obvRemoveButton.addActionListener(e -> removeImage(true));
        revRemoveButton.addActionListener(e -> removeImage(false));

        addAnotherButton.addActionListener( e -> {
            if(saveCoin()) {
                errorDisplay.setForeground(Color.GREEN);
                errorDisplay.setText("Coin saved!");

                setCoin(new Coin());

                editingSet = false;
                if(set != null)
                    parent.setTitle("Add Coin to Set");
                else
                    parent.setTitle("Add Coin");
            }
        });

        OKButton.addActionListener(e -> {

            if(saveCoin())
                goHome();
        });
        cancelButton.addActionListener(e -> goHome());

        setInfo();
    }

    private void setInfo() {
        coinTypeInput.setText(coin.getName());
        countryInput.setText(coin.getCountry());
        if(coin.getYear() != 0)
            yearInput.setText("" + coin.getYear());
        else
            yearInput.setText("");
        mintMarkComboBox.setSelectedItem(coin.getMintMark());

        if(coin.getDenomination() != 0.0) {
            // Format the number properly
            // Necessary due to inaccuracies caused by binary decimal calculations
            DecimalFormat format = new DecimalFormat();
            format.applyPattern("0.00");
            String value = format.format(coin.getDenomination());
            denominationInput.setText(value);
        }
        else
            denominationInput.setText("");

        if(coin.getValue() != 0) {
            // Format the number properly
            // Necessary due to inaccuracies caused by binary decimal calculations
            DecimalFormat format = new DecimalFormat();
            format.applyPattern("0.00");
            String value = format.format(coin.getValue());
            valueInput.setText(value);
        }
        else
            valueInput.setText("");

        gradedCheckBox.setSelected(coin.getGraded());
        gradeComboBox.setSelectedItem(coin.getCondition());
        errorCheckBox.setSelected(coin.getError());
        noteInput.setText(coin.getNote());

        // Set images
        if(!coin.getObvImgExt().equals("")) {
            obvImageLocation = coin.getImagePath(true);
            addImage(obvImageLocation, true);
        }
        if(!coin.getRevImgExt().equals("")) {
            revImageLocation = coin.getImagePath(false);
            addImage(revImageLocation, false);
        }
    }

    public JPanel getPanel() {
        return addCoinPanel;
    }

    private boolean saveCoin() {
        coin.setName(coinTypeInput.getText());
        coin.setCountry(countryInput.getText());

        // True if input is invalid
        boolean invalid = false;
        String invalidText = "";

        try {
            coin.setYear(Integer.parseInt(yearInput.getText()));
        }
        catch (NumberFormatException ex) {
            invalid = true;
            invalidText += "Year format is incorrect. Cannot be blank and must be an integer (whole number).";
        }

        if(mintMarkComboBox.getSelectedItem() != null)
            coin.setMintMark(mintMarkComboBox.getSelectedItem().toString());

        try {
            coin.setDenomination(Double.parseDouble(denominationInput.getText()));
        } catch (NumberFormatException ex) {
            invalid = true;
            if (!invalidText.equals(""))
                invalidText += "\n";

            invalidText += "Denomination format incorrect. Cannot be blank and must be a number.";
        }

        // If value box is not empty
        if(!valueInput.getText().equals("")) {
            try {
                coin.setValue(Double.parseDouble(valueInput.getText()));
            } catch (NumberFormatException ex) {
                invalid = true;
                if (!invalidText.equals(""))
                    invalidText += "\n";

                invalidText += "Value format incorrect. Must be a number.";
            }
        }

        if(gradeComboBox.getSelectedItem() != null)
            coin.setCondition(gradeComboBox.getSelectedItem().toString());
        coin.setGraded(gradedCheckBox.isSelected());

        coin.setError(errorCheckBox.isSelected());

        if(coin.getError())
            coin.setErrorType(errorTypeInput.getText());
        else
            coin.setErrorType("");

        coin.setNote(Main.escapeForJava(noteInput.getText()));

        // If some fields contain invalid data
        if(invalid) {
            errorDisplay.setForeground(Color.RED);
            errorDisplay.setText(invalidText);
        }
        else
        {
            int rows = coin.saveToDb(((Main)parent).databaseConnection);

            String successMessage = ((Main)parent).databaseConnection.wasSuccessful(rows);

            if(successMessage.equals(DatabaseConnection.SUCCESS_MESSAGE)){
                errorDisplay.setText("");

                // Add to set if necessary
                if(set != null && !editingSet)
                    set.addCoin(coin);

                String errorMessage = "";

                // Copy the image to a new location, then use that location
                if(validObvImg) {
                    String path = imageObvLocationInput.getText();
                    coin.setObvImgExt(path.substring(path.lastIndexOf('.')));

                    if(Main.copyFile(imageObvLocationInput.getText(),
                            coin.getImagePath(true))) {

                        String sql = "UPDATE Coins SET ObvImgExt=\"" + coin.getObvImgExt() + "\"\n" +
                                "WHERE ID=" + coin.getId() + ";";

                        rows = ((Main) parent).databaseConnection.runUpdate(sql);
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
                    // Delete the file
                    new File(coin.getImagePath(true)).delete();
                    coin.setObvImgExt("");

                    // Remove image extension from database
                    String sql = "UPDATE Coins SET ObvImgExt=null\n" +
                            "WHERE ID=" + coin.getId() + ";";

                    ((Main)parent).databaseConnection.runUpdate(sql);
                }
                if(validRevImg) {
                    String path = imageRevLocationInput.getText();
                    coin.setRevImgExt(path.substring(path.lastIndexOf('.')));

                    if(Main.copyFile(imageRevLocationInput.getText(),
                            coin.getImagePath(false))) {

                        String sql = "UPDATE Coins SET RevImgExt=\"" + coin.getRevImgExt() + "\"\n" +
                                "WHERE ID=" + coin.getId() + ";";

                        rows = ((Main) parent).databaseConnection.runUpdate(sql);
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
                    // Delete the file
                    new File(coin.getImagePath(false)).delete();
                    coin.setRevImgExt("");

                    // Remove image extension from database
                    String sql = "UPDATE Coins SET RevImgExt=null\n" +
                            "WHERE ID=" + coin.getId() + ";";

                    ((Main)parent).databaseConnection.runUpdate(sql);
                }

                if(!errorMessage.equals("")) {
                    invalid = true;
                    errorDisplay.setForeground(Color.RED);
                    errorDisplay.setText(errorMessage);
                }
            }
            else {
                errorDisplay.setText(successMessage);
                invalid = true;
            }
        }

        return !invalid;
    }

    private void goHome() {
        if(set != null) {
            AddSetScreen setScreen = new AddSetScreen(parent, set);
            setScreen.setFromCollection(fromCollection);
            setScreen.setInfo();

            ((Main) parent).changeScreen(setScreen.getPanel(), "");
        }
        else if(fromCollection) {
            CollectionTableScreen collectionTableScreen = new CollectionTableScreen(parent);

            ((Main) parent).changeScreen(collectionTableScreen.getPanel(), "Collection");
        }
        else {
            ((Main) parent).changeScreen(((Main) parent).getPanel(), "");
        }
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
            else {revImageLocation = file.getAbsolutePath();
                imageRevLocationInput.setText(revImageLocation);
            }

            addImage(file.getAbsolutePath(), obverse);
        } else if(returnVal != JFileChooser.CANCEL_OPTION) {
            errorDisplay.setText("Error retrieving file");
        }
    }

    void setEditingSet(boolean editing) {
        this.editingSet = editing;
    }

    public void setSet(CoinSet set) {
        this.set = set;

        setInfo();
    }

    public CoinSet getSet() {
        return set;
    }

    public void setCoin(Coin coin) {
        this.coin = coin;
        setInfo();
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
