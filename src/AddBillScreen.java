import items.*;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;

public class AddBillScreen extends AddSetItemScreen {

    private JTextField typeInput;
    private JButton cancelButton;
    private JButton OKButton;
    private JTextField yearInput;
    private JTextField denominationInput;
    private AutoComboBox gradeComboBox;
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
    private JTextArea errorDisplay;
    private JCheckBox starCheck;
    private JTextField valueInput;
    private JButton saveNewButton;
    private JTextField signaturesInput;
    private JPanel obvPicPanel;
    private JPanel revPicPanel;
    private JTextField seriesLetter;
    private AutoComboBox countryInput;
    private AutoComboBox currencyInput;
    private JButton saveCopyButton;
    private JComboBox locationDropDown;
    private JButton addContainerButton;
    private JScrollPane scrollPane;

    private Bill bill;

    boolean editingSet = false;

    public AddBillScreen(JFrame parent) {
        this(parent, new Bill());
    }

    public AddBillScreen(JFrame parent, final Bill bill) {
        super(parent);

        setReturnTab(TAB_BILLS);

        setImageObvLocationInput(imageObvLocationInput);
        setImageRevLocationInput(imageRevLocationInput);

        setObvPicPanel(obvPicPanel);
        setRevPicPanel(revPicPanel);

        api = ((Main) parent).api;

        api.setCountryListener(new NumismatistAPI.CountryListener() {
            @Override
            public void countryListChanged(@NotNull ArrayList<Country> countries) {
                //Update Country and Currency lists without changing selection
                Object curCountry = countryInput.getSelectedItem();
                Object curCurrency = currencyInput.getSelectedItem();
                ComboBoxHelper.setupBoxes(countryInput, yearInput, currencyInput, errorDisplay, api);
                if(curCountry != null && !curCountry.equals(""))
                    countryInput.setSelectedItem(curCountry);
                if(curCurrency != null && !curCurrency.equals(""))
                    currencyInput.setSelectedItem(curCurrency);
            }
        });

        this.bill = bill;

        errorCheckBox.addActionListener(e -> errorTypeInput.setEnabled(errorCheckBox.isSelected()));
        cancelButton.addActionListener(e -> goHome());

        ComboBoxHelper.setContainerList(locationDropDown, api, addContainerButton, parent);
        ComboBoxHelper.setupBoxes( countryInput, yearInput, currencyInput, errorDisplay, api);

        // Restrict input
        ((PlainDocument) denominationInput.getDocument()).setDocumentFilter(MyDocFilter.Companion.getDenominationFilter());
        ((PlainDocument) yearInput.getDocument()).setDocumentFilter(MyDocFilter.Companion.getCurrentYearFilter());
        ((PlainDocument) valueInput.getDocument()).setDocumentFilter(MyDocFilter.Companion.getValueFilter());

        MyLetterFilter letterFilter = new MyLetterFilter();
        letterFilter.setMaxLetters(1);
        ((PlainDocument) seriesLetter.getDocument()).setDocumentFilter(letterFilter);

        saveNewButton.addActionListener(e -> saveBill(getBUTTON_SAVE_NEW()));
        saveCopyButton.addActionListener(e -> saveBill(getBUTTON_SAVE_COPY()));
        OKButton.addActionListener( e-> saveBill(getBUTTON_OK()));

        obvSetButton.addActionListener(e -> {
            setObvImageLocation(imageObvLocationInput.getText());
            addImage(getObvImageLocation(), true);
        });

        revSetButton.addActionListener(e -> {
            setRevImageLocation(imageRevLocationInput.getText());
            addImage(getRevImageLocation(), false);
        });

        obvBrowseButton.addActionListener( e -> openFileChooser(true));
        revBrowseButton.addActionListener( e -> openFileChooser(false));

        obvRemoveButton.addActionListener(e -> removeImage(true));
        revRemoveButton.addActionListener(e -> removeImage(false));

        for(int i = 0; i < Bill.Companion.getCONDITIONS().length; i++) {
            gradeComboBox.addItem(Bill.Companion.getCONDITIONS()[i]);
        }
        parent.getRootPane().setDefaultButton(OKButton);
        // Set initial focus
        SwingUtilities.invokeLater(() -> countryInput.requestFocus());

        setInfo();
    }

    private void setInfo() {
        typeInput.setText(bill.getName());
        String countryName = Country.Companion.getCountry(api.getCountries(), bill.getCountryName()).getName();

        if(!countryName.isBlank())
            ((JTextField)countryInput.getEditor().getEditorComponent()).setText(countryName);

        if(bill.getYear() != 0)
            yearInput.setText("" + bill.getYear());
        else
            yearInput.setText("");

        // Must happen after country and year are set
        try {
            ComboBoxHelper.setCurrency( countryInput, yearInput, currencyInput, api.getCountries());
        }
        catch (NumberFormatException nfe) {
            errorDisplay.setText(Main.getString("error_invalidDate"));
        }

        if(!bill.getCurrency().getNameAbbr().equals("")) {

            ((JTextField) currencyInput.getEditor().getEditorComponent()).setText(
                    api.findCurrency(bill.getCurrency().getNameAbbr()).getName());
        }

        locationDropDown.setSelectedItem(api.findContainer(bill.getContainerId()).getName());

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

        gradedCheckBox.setSelected(bill.getGraded());
        gradeComboBox.setSelectedItem(bill.getCondition());
        errorCheckBox.setSelected(bill.getError());
        errorTypeInput.setText(bill.getErrorType());
        seriesLetter.setText(bill.getSeriesLetter());
        starCheck.setSelected(bill.getReplacement());
        noteInput.setText(bill.getNote());

        // Set images
        if(!bill.getObvImgPath().equals("")) {
            setObvImageLocation(imageObvLocationInput.getText());
            addImage(getObvImageLocation(), true);
        }
        if(!bill.getRevImgPath().equals("")) {
            setRevImageLocation(imageRevLocationInput.getText());
            addImage(getRevImageLocation(), true);
        }
    }

    private void saveBill(int button) {
        String errorMessage = "";

        bill.setName(typeInput.getText());

        // Set country name
        if(countryInput.getSelectedIndex() != -1) {
            bill.setCountryName((countryInput).ids[countryInput.getSelectedIndex()]);
        }
        else {
            if(!errorMessage.equals(""))
                errorMessage += "\n";
            errorMessage += Main.getString("error_emptyCountry");
        }

        // Set currency abbreviation
        if(currencyInput.getSelectedIndex() != -1) {
            bill.getCurrency().setNameAbbr((currencyInput).ids[currencyInput.getSelectedIndex()]);
        }
        else {
            if(!errorMessage.equals(""))
                errorMessage += "\n";
            errorMessage += Main.getString("error_emptyCurrency");
        }

        try {
            bill.setYear(Integer.parseInt(yearInput.getText()));
        }
        catch (NumberFormatException er) {
            if(!errorMessage.equals(""))
                errorMessage += "\n";
            errorMessage += Main.getString("error_emptyYear");
        }
        String letter = seriesLetter.getText();

        if(letter.length() <= 3)
            bill.setSeriesLetter(letter);
        else {
            if(!errorMessage.equals(""))
                errorMessage += "\n";
            errorMessage += Main.getString("error_seriesLetterLength");
        }

        bill.setSerial(serialNumberInput.getText());
        bill.setSignatures(signaturesInput.getText());

        if(locationDropDown.getSelectedItem() != null &&
                !locationDropDown.getSelectedItem().equals("")) {
            bill.setContainerId(api.findContainer(locationDropDown.getSelectedItem().toString()).getId());
        }

        try {
            bill.setDenomination(Double.parseDouble(denominationInput.getText()));
        }
        catch (NumberFormatException ex) {
            if(!errorMessage.equals(""))
                errorMessage += "\n";
            errorMessage += Main.getString("error_emptyDenomination");
        }
        if(!valueInput.getText().equals("")) {
            try {
                bill.setValue(Double.parseDouble(valueInput.getText()));
            } catch (NumberFormatException e) {
                if(!errorMessage.equals(""))
                    errorMessage += "\n";
                errorMessage += Main.getString("error_emptyValue");
            }
        }

        if(!imageObvLocationInput.getText().equals("" )&& !(new File(imageObvLocationInput.getText())).exists())
            errorMessage += MessageFormat.format(Main.getString("error_fileNotFound"), imageObvLocationInput.getText()) + "\n";
        if(!imageRevLocationInput.getText().equals("") && !(new File(imageRevLocationInput.getText())).exists())
            errorMessage += MessageFormat.format(Main.getString("error_fileNotFound"), imageRevLocationInput.getText()) + "\n";

        bill.setGraded(gradedCheckBox.isSelected());

        if(gradeComboBox.getSelectedItem() != null)
            bill.setCondition(gradeComboBox.getSelectedItem().toString());

        bill.setError(errorCheckBox.isSelected());

        if(errorCheckBox.isSelected())
            bill.setErrorType(errorTypeInput.getText());

        bill.setReplacement(starCheck.isSelected());

        bill.setNote(Main.escapeForJava(noteInput.getText()));

        if(errorMessage.equals("")) {

            errorDisplay.setForeground(Color.BLACK);
            errorDisplay.setText(Main.getString("addBill_message_saving"));
            SwingWorker<Void, Void> worker = new SwingWorker<>() {

                int rows = 0;
                String returnMessage = "";
                String errorMessage = "";

                @Override
                protected Void doInBackground() {
                    rows = bill.saveToDb(api);
                    returnMessage = api.getSuccessMessage(rows);

                    if(returnMessage.equals(NumismatistAPI.Companion.getString("db_message_success"))) {
                        try {
                            if(!bill.saveObvImage())
                            {
                                errorMessage += MessageFormat.format(Main.getString("error_savingFile_message"), bill.getObvImgPath());
                            }
                        } catch (SecurityException | IOException securityException ) {
                            errorMessage += securityException.getMessage();
                        }
                        try {
                            if(!bill.saveRevImage())
                            {
                                errorMessage += MessageFormat.format(Main.getString("error_savingFile_message"), bill.getRevImgPath());
                            }
                        } catch (SecurityException | IOException securityException ) {
                            errorMessage += securityException.getMessage();
                        }
                    }

                    return  null;
                }

                @Override
                protected void done() {
                    if(returnMessage.equals(NumismatistAPI.Companion.getString("db_message_success"))) {
                        if(!errorMessage.equals("")) {
                            errorDisplay.setForeground(Main.COLOR_ERROR);
                            errorDisplay.setText(errorMessage);
                        }
                        else {
                            errorDisplay.setForeground(Main.COLOR_SUCCESS);
                            errorDisplay.setText(
                                    MessageFormat.format(Main.getString("addBill_message_saved"),
                                            bill.toString()));

                            // Add to set if necessary
                            if(getParentSet() != null && !editingSet)
                                getParentSet().addItem(bill);

                            if(button == getBUTTON_OK()) {
                                goHome();
                            }
                            else if (button == getBUTTON_SAVE_NEW()) {

                                setBill(new Bill());

                                if(getParentSet() != null)
                                    getParent().setTitle(Main.getString("addBill_title_addToSet"));
                                else
                                    getParent().setTitle(Main.getString("addBill_title_add"));
                            }
                            else if (button == getBUTTON_SAVE_COPY()) {
                                Bill newBill = bill.copy();
                                newBill.setSet(null);
                                newBill.setId(DatabaseItem.ID_INVALID);
                                newBill.setObvImgPath("");
                                newBill.setRevImgPath("");
                                newBill.setContainerId(DatabaseItem.ID_INVALID);

                                if(getParentSet() != null) {
                                    newBill.setSet(getParentSet());
                                    getParent().setTitle(Main.getString("addBill_title_addToSet"));
                                }
                                else
                                    getParent().setTitle(Main.getString("addBill_title_add"));

                                setBill(newBill);
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
        else {
            errorDisplay.setForeground(Main.COLOR_ERROR);
            errorDisplay.setText(errorMessage);
        }
    }

    public void setBill(Bill bill) {
        this.bill = bill;

        setInfo();
    }

    public JPanel getPanel() {
        return addBillPanel;
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

    public void setSet(Set set) {
        this.setParentSet(set);
    }
}
