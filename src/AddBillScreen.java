import items.Bill;
import items.DatabaseConnection;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

public class AddBillScreen {

    private JTextField typeInput;
    private JButton cancelButton;
    private JButton OKButton;
    private JTextField countryInput;
    private JTextField yearInput;
    private JTextField denominationInput;
    private JComboBox gradeComboBox;
    private JCheckBox gradedCheckBox;
    private JCheckBox errorCheckBox;
    private JTextField errorTypeInput;
    private JTextField serialNumberInput;
    private JPanel addBillPanel;
    private JTextArea noteInput;
    private JTextField imageObvLocationInput;
    private JButton obvSetButton;
    private JButton obvBrowseButton;
    private JButton obvRemoveButton;
    private JTextField imageRevLocationInput;
    private JButton revSetButton;
    private JButton revBrowseButton;
    private JButton revRemoveButton;
    private JTextField plateSeriesObvInput;
    private JTextField plateSeriesRevInput;
    private JTextField districtInput;
    private JTextField notePositionInput;
    private JTextArea errorDisplay;
    private JLabel fieldTips;
    private JCheckBox starCheck;
    private JTextField valueInput;
    private JButton addAnotherButton;
    private JTextField signaturesInput;
    private JPanel obvPicPanel;
    private JPanel revPicPanel;
    private JTextField seriesLetter;

    private final JFrame parent;

    private boolean editing = false;
    private Bill bill;

    private String obvImageLocation = "";
    private String revImageLocation = "";

    private boolean validObvImg = false;
    private boolean validRevImg = false;

    private boolean fromCollection = false;

    public AddBillScreen(JFrame parent) {
        this.parent = parent;

        setBill(new Bill());

        fieldTips.setText("<HTML><U><B>What are these?</B></U></HTML>");
        fieldTips.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        fieldTips.addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                // Show bill info
                BillInfoDialog infoDialog = new BillInfoDialog(parent);
                infoDialog.setVisible(true);
            }
        });

        errorCheckBox.addActionListener(e -> errorTypeInput.setEnabled(errorCheckBox.isSelected()));
        cancelButton.addActionListener(e -> goHome());

        addAnotherButton.addActionListener( e -> {
            if(saveBill()) {
                errorDisplay.setForeground(Color.GREEN);
                errorDisplay.setText("Bill saved!");

                setBill(new Bill());
                setEditing(false);

                parent.setTitle("Add Bill");
            }
        });

        OKButton.addActionListener( e-> {
            if(saveBill())
                goHome();
        });

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

        for(int i = 0; i < Bill.Companion.getCONDITIONS().length; i++) {
            gradeComboBox.addItem(Bill.Companion.getCONDITIONS()[i]);
        }
    }

    private void setInfo() {
        typeInput.setText(bill.getName());
        countryInput.setText(bill.getCountry());
        if(bill.getYear() != 0)
            yearInput.setText("" + bill.getYear());
        else
            yearInput.setText("");
        serialNumberInput.setText(bill.getSerial());
        if(bill.getDenomination() != 0.0) {
            // Format the number properly
            // Necessary due to inaccuracies caused by binary decimal calculations
            DecimalFormat format = new DecimalFormat();
            format.applyPattern("0.00");
            String value = format.format(bill.getDenomination());
            denominationInput.setText(value);
        }
        else
            denominationInput.setText("");

        if(bill.getValue() != 0.0) {
            // Format the number properly
            // Necessary due to inaccuracies caused by binary decimal calculations
            DecimalFormat format = new DecimalFormat();
            format.applyPattern("0.00");
            String value = format.format(bill.getValue());
            valueInput.setText(value);
        }
        else
            valueInput.setText("");

        plateSeriesObvInput.setText(bill.getPlateSeriesObv());
        plateSeriesRevInput.setText(bill.getPlateSeriesRev());
        districtInput.setText(bill.getDistrict());
        notePositionInput.setText(bill.getNotePosition());
        gradedCheckBox.setSelected(bill.getGraded());
        gradeComboBox.setSelectedItem(bill.getCondition());
        errorCheckBox.setSelected(bill.getError());
        errorTypeInput.setText(bill.getErrorType());
        starCheck.setSelected(bill.getStar());
        noteInput.setText(bill.getNote());

        // Set images
        if(!bill.getObvImgExt().equals("")) {
            obvImageLocation = imageObvLocationInput.getText();
            addImage(obvImageLocation, true);
        }
        if(!bill.getRevImgExt().equals("")) {
            revImageLocation = imageRevLocationInput.getText();
            addImage(revImageLocation, true);
        }
    }

    boolean saveBill() {
        String errorMessage = "";

        bill.setName(typeInput.getText());
        if(countryInput.getText().trim().equals(""))
            errorMessage += "Country must not be blank. If it's unknown or indistinguishable, enter \"Unknown\".";

        bill.setCountry(countryInput.getText());

        try {
            bill.setYear(Integer.parseInt(yearInput.getText()));
        }
        catch (NumberFormatException er) {
            if(!errorMessage.equals(""))
                errorMessage += "\n";
            errorMessage += "Year format is incorrect. Cannot be blank and must be an integer (whole number).";
        }
        String letter = seriesLetter.getText();

        if(letter.length() <= 3)
            bill.setSeriesLetter(letter);
        else {
            if(!errorMessage.equals(""))
                errorMessage += "\n";
            errorMessage += "Series letter has max length of 3.";
        }

        bill.setSerial(serialNumberInput.getText());
        bill.setSignatures(signaturesInput.getText());

        try {
            bill.setDenomination(Double.parseDouble(denominationInput.getText()));
        }
        catch (NumberFormatException ex) {
            if(!errorMessage.equals(""))
                errorMessage += "\n";
            errorMessage += "Denomination format incorrect. Cannot be blank and must be a number.";
        }
        if(!valueInput.getText().equals("")) {
            try {
                bill.setValue(Double.parseDouble(valueInput.getText()));
            } catch (NumberFormatException e) {
                errorMessage += "Value format incorrect. Must be a number or left blank.";
            }
        }

        bill.setPlateSeriesObv(plateSeriesObvInput.getText());
        bill.setPlateSeriesRev(plateSeriesRevInput.getText());
        bill.setDistrict(districtInput.getText());
        bill.setNotePosition(notePositionInput.getText());
        bill.setGraded(gradedCheckBox.isSelected());

        if(gradeComboBox.getSelectedItem() != null)
            bill.setCondition(gradeComboBox.getSelectedItem().toString());

        bill.setError(errorCheckBox.isSelected());

        if(errorCheckBox.isSelected())
            bill.setErrorType(errorTypeInput.getText());

        bill.setStar(starCheck.isSelected());

        bill.setNote(noteInput.getText());

        if(errorMessage.equals("")) {
            int rows =  bill.saveToDb(((Main) parent).databaseConnection, editing);
            String message = ((Main) parent).databaseConnection.wasSuccessful(rows);
            if(!message.equals(DatabaseConnection.SUCCESS_MESSAGE)) {
                errorDisplay.setForeground(Color.RED);
                errorDisplay.setText(message);

                // Copy the image to a new location, then use that location
                if(validObvImg) {
                    String path = imageObvLocationInput.getText();
                    bill.setObvImgExt(path.substring(path.lastIndexOf('.')));

                    if(Main.copyFile(imageObvLocationInput.getText(),
                            bill.getImagePath(true))) {

                        String sql = "UPDATE Bills SET ObvImgExt=\"" + bill.getObvImgExt() + "\"\n" +
                                "WHERE ID=" + bill.getId() + ";";

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
                    new File(bill.getImagePath(true)).delete();
                    bill.setObvImgExt("");

                    // Remove image extension from database
                    String sql = "UPDATE Bills SET ObvImgExt=null\n" +
                            "WHERE ID=" + bill.getId() + ";";

                    ((Main)parent).databaseConnection.runUpdate(sql);
                }
                if(validRevImg) {
                    String path = imageRevLocationInput.getText();
                    bill.setRevImgExt(path.substring(path.lastIndexOf('.')));

                    if(Main.copyFile(imageRevLocationInput.getText(),
                            bill.getImagePath(false))) {

                        String sql = "UPDATE Bills SET RevImgExt=\"" + bill.getRevImgExt() + "\"\n" +
                                "WHERE ID=" + bill.getId() + ";";

                        rows = ((Main) parent).databaseConnection.runUpdate(sql);
                        String extMessage = ((Main) parent).databaseConnection.wasSuccessful(rows);
                        if (!extMessage.equals(DatabaseConnection.SUCCESS_MESSAGE)) {
                            if (!errorMessage.equals(""))
                                errorMessage += "\n";
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
                    new File(bill.getImagePath(false)).delete();
                    bill.setRevImgExt("");

                    // Remove image extension from database
                    String sql = "UPDATE Bills SET RevImgExt=null\n" +
                            "WHERE ID=" + bill.getId() + ";";

                    ((Main)parent).databaseConnection.runUpdate(sql);
                }

                if(!errorMessage.equals("")) {

                    errorDisplay.setForeground(Color.RED);
                    errorDisplay.setText(errorMessage);

                    return false;
                }
            }

            return true;
        }
        else {
            errorDisplay.setForeground(Color.RED);
            errorDisplay.setText(errorMessage);
            return false;
        }
    }

    void setEditing(boolean editing) {
        this.editing = editing;

        if(editing)
            parent.setTitle("Edit Bill");
        else
            parent.setTitle("Add Bill");
    }

    public void setBill(Bill bill) {
        this.bill = bill;

        setInfo();
    }

    public JPanel getPanel() {
        return addBillPanel;
    }

    private void goHome() {

        if(fromCollection) {
            CollectionTableScreen collectionTableScreen = new CollectionTableScreen(parent);
            collectionTableScreen.setTab(2);
            ((Main) parent).changeScreen(collectionTableScreen.getPanel(), "Collection");
        }
        else
            ((Main) parent).changeScreen(((Main) parent).getPanel(), Main.title);
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
