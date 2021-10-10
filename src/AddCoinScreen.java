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
import java.util.Arrays;

public class AddCoinScreen extends AddSetItemScreen {

    private JButton cancelButton;
    private JPanel addCoinPanel;
    private JButton OKButton;
    private JTextField coinTypeInput;
    private JTextField denominationInput;
    private JButton pennyButton;
    private JButton nickelButton;
    private JButton dimeButton;
    private JButton quarterButton;
    private JButton halfButton;
    private JButton dollarButton;
    private AutoComboBox currencyInput;
    private JTextField yearInput;
    private AutoComboBox countryInput;
    private AutoComboBox mintMarkComboBox;
    private AutoComboBox gradeComboBox;
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
    private JButton saveNewButton;
    private JPanel obvPicPanel;
    private JPanel revPicPanel;
    private JButton saveCopyButton;
    private JComboBox locationDropDown;
    private JButton addContainerButton;
    private JScrollPane scrollPane;

    private Coin coin;

    public AddCoinScreen(JFrame parent) {
        this(parent, new Coin());
    }

    public AddCoinScreen(JFrame parent, final Coin startCoin) {
        super(parent);

        setReturnTab(TAB_COINS);

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

        this.coin = startCoin;

        // Used to format numbers
        DecimalFormat format = new DecimalFormat();
        format.applyPattern("0.00");

        pennyButton.addActionListener(e -> denominationInput.setText(format.format(Coin.PENNY)));
        nickelButton.addActionListener(e -> denominationInput.setText(format.format(Coin.NICKEL)));
        dimeButton.addActionListener(e -> denominationInput.setText(format.format(Coin.DIME)));
        quarterButton.addActionListener(e -> denominationInput.setText(format.format(Coin.QUARTER)));
        halfButton.addActionListener(e -> denominationInput.setText(format.format(Coin.HALF_DOLLAR)));
        dollarButton.addActionListener(e -> denominationInput.setText(format.format(Coin.DOLLAR)));

        errorCheckBox.addActionListener(e -> errorTypeInput.setEnabled(errorCheckBox.isSelected()));

        ComboBoxHelper.setupBoxes(countryInput, yearInput, currencyInput, errorDisplay, api);

        // Restrict input
        ((PlainDocument) denominationInput.getDocument()).setDocumentFilter(MyDocFilter.Companion.getDenominationFilter());
        ((PlainDocument) yearInput.getDocument()).setDocumentFilter(MyDocFilter.Companion.getCurrentYearFilter());
        ((PlainDocument) valueInput.getDocument()).setDocumentFilter(MyDocFilter.Companion.getValueFilter());

        ComboBoxHelper.setContainerList(locationDropDown, api, addContainerButton, parent);

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

        saveNewButton.addActionListener(e -> saveCoin(getBUTTON_SAVE_NEW()));
        saveCopyButton.addActionListener(e -> saveCoin(getBUTTON_SAVE_COPY()));
        OKButton.addActionListener(e -> saveCoin(getBUTTON_OK()));
        cancelButton.addActionListener(e -> goHome());

        parent.getRootPane().setDefaultButton(OKButton);
        // Set initial focus
        SwingUtilities.invokeLater(() -> countryInput.requestFocus());

        setInfo();
    }

    private void setInfo() {
        coinTypeInput.setText(coin.getName());
        String countryName = Country.Companion.getCountry(api.getCountries(), coin.getCountryName()).getName();

        if(!countryName.isBlank())
            ((JTextField)countryInput.getEditor().getEditorComponent()).setText(countryName);

        if(coin.getYear() != 0)
            yearInput.setText("" + coin.getYear());
        else
            yearInput.setText("");

        ((JTextField)mintMarkComboBox.getEditor().getEditorComponent()).setText(coin.getMintMark());

        // Must happen after country and year are set
        try {
            ComboBoxHelper.setCurrency(countryInput, yearInput, currencyInput, api.getCountries());
        }
        catch (NumberFormatException nfe) {
            errorDisplay.setForeground(Main.COLOR_ERROR);
            errorDisplay.setText(Main.getString("error_invalidDate"));
        }

        if(!coin.getCurrency().getNameAbbr().equals("")) {
            ((JTextField) currencyInput.getEditor().getEditorComponent()).setText(coin.getCurrency().getName());
        }

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
        ((JTextField)gradeComboBox.getEditor().getEditorComponent()).setText(coin.getCondition());
        errorCheckBox.setSelected(coin.getError());
        noteInput.setText(coin.getNote());

        locationDropDown.setSelectedItem(api.findContainer(coin.getContainerId()).getName());
        //((JTextField)locationDropDown.getEditor().getEditorComponent()).setText(api.findContainer(coin.getContainerId()).getName());

        // Set images
        if(!coin.getObvImgPath().equals("")) {
            setObvImageLocation(coin.getObvImgPath());
            addImage(getObvImageLocation(), true);
            imageObvLocationInput.setText(getObvImageLocation());
        }
        if(!coin.getRevImgPath().equals("")) {
            setRevImageLocation(coin.getRevImgPath());
            addImage(getRevImageLocation(), false);
            imageRevLocationInput.setText(getRevImageLocation());
        }
    }

    public JPanel getPanel() {
        return addCoinPanel;
    }

    private void saveCoin(final int button) {
        coin.setName(coinTypeInput.getText());

        // True if input is invalid
        boolean invalid = false;
        String invalidText = "";

        // Set country ID
        if(countryInput.getSelectedIndex() != -1) {
            coin.setCountryName(countryInput.ids[countryInput.getSelectedIndex()]);
        }
        else {
            invalid = true;
            if(!invalidText.equals(""))
                invalidText += "\n";
            invalidText += Main.getString("error_emptyCountry");
        }

        // Set currency
        if(currencyInput.getSelectedIndex() != -1) {
            coin.setCurrency(api.findCurrency(currencyInput.ids[currencyInput.getSelectedIndex()]));
        }
        else {
            invalid = true;
            if(!invalidText.equals(""))
                invalidText += "\n";
            invalidText += Main.getString("error_emptyCurrency");
        }

        try {
            coin.setYear(Integer.parseInt(yearInput.getText()));
        }
        catch (NumberFormatException ex) {
            invalid = true;
            if(!invalidText.equals(""))
                invalidText += "\n";
            invalidText += Main.getString("error_emptyYear");
        }

        if(((JTextField)mintMarkComboBox.getEditor().getEditorComponent()).getText().equals(""))
            coin.setMintMark("");
        else
            coin.setMintMark(((JTextField)mintMarkComboBox.getEditor().getEditorComponent()).getText());

        try {
            coin.setDenomination(Double.parseDouble(denominationInput.getText()));
        } catch (NumberFormatException ex) {
            invalid = true;
            if (!invalidText.equals(""))
                invalidText += "\n";
            invalidText += Main.getString("error_emptyDenomination");
        }

        // If value box is not empty
        if(!valueInput.getText().equals("")) {
            try {
                coin.setValue(Double.parseDouble(valueInput.getText()));
            } catch (NumberFormatException ex) {
                invalid = true;
                if (!invalidText.equals(""))
                    invalidText += "\n";

                invalidText += Main.getString("error_emptyValue");
            }
        }

        if(((JTextField)gradeComboBox.getEditor().getEditorComponent()).getText() != null)
            coin.setCondition(((JTextField)gradeComboBox.getEditor().getEditorComponent()).getText());
        coin.setGraded(gradedCheckBox.isSelected());

        coin.setError(errorCheckBox.isSelected());

        if(coin.getError())
            coin.setErrorType(errorTypeInput.getText());
        else
            coin.setErrorType("");

        coin.setNote(Main.escapeForJava(noteInput.getText()));

        if(locationDropDown.getSelectedItem() != null &&
            !locationDropDown.getSelectedItem().equals("")) {
            coin.setContainerId(api.findContainer(locationDropDown.getSelectedItem().toString()).getId());
        }

        if(getValidObvImg())
            coin.setObvImgPath(imageObvLocationInput.getText());
        if(getValidRevImg())
            coin.setRevImgPath(imageRevLocationInput.getText());

        // If some fields contain invalid data
        if(invalid) {
            errorDisplay.setForeground(Main.COLOR_ERROR);
            errorDisplay.setText(invalidText);
        }
        else
        {
            errorDisplay.setForeground(Color.BLACK);
            errorDisplay.setText(Main.getString("addCoin_message_saving"));
            SwingWorker<Void, Void> worker = new SwingWorker<>() {

                int rows = 0;

                @Override
                public Void doInBackground() {
                    rows = coin.saveToDb(api);
                    return null;
                }

                @Override
                public void done() {
                    String successMessage = api.getSuccessMessage(rows);

                    if(successMessage.equals(NumismatistAPI.Companion.getString("db_message_success"))){
                        errorDisplay.setForeground(Main.COLOR_SUCCESS);
                        errorDisplay.setText(MessageFormat.format(Main.getString("addCoin_message_saved"),
                                coin.toString()));

                        // Add to set if necessary
                        if(getParentSet() != null && !getEditingParent())
                            getParentSet().addItem(coin);

                        if(button == getBUTTON_SAVE_COPY()) {
                            Coin newCoin = coin.copy();
                            newCoin.setSet(null);
                            newCoin.setId(DatabaseItem.ID_INVALID);
                            newCoin.setSlotId(DatabaseItem.ID_INVALID);
                            newCoin.setObvImgPath("");
                            newCoin.setRevImgPath("");
                            newCoin.setContainerId(DatabaseItem.ID_INVALID);

                            if(getParentSet() != null) {
                                newCoin.setSet(getParentSet());
                                getParent().setTitle(Main.getString("addCoin_title_addToSet"));
                            }
                            else
                                getParent().setTitle(Main.getString("addCoin_title_add"));

                            setCoin(newCoin);
                        }
                        else if(button == getBUTTON_SAVE_NEW()) {
                            setCoin(new Coin());

                            if(getParentSet() != null)
                                getParent().setTitle(Main.getString("addCoin_title_addToSet"));
                            else
                                getParent().setTitle(Main.getString("addCoin_title_add"));
                        }
                        else if(button == getBUTTON_OK())
                            goHome();
                    }
                    else {
                        errorDisplay.setForeground(Main.COLOR_ERROR);
                        errorDisplay.setText(successMessage);
                    }
                }
            };

            worker.execute();
        }
    }

    public void setCoin(Coin coin) {
        this.coin = coin;
        setInfo();
    }

    private void createUIComponents() {

        countryInput = new AutoComboBox();
        currencyInput = new AutoComboBox();
        gradeComboBox = new AutoComboBox();
        mintMarkComboBox = new AutoComboBox();
        scrollPane = new JScrollPane();
        scrollPane.setBorder(null);

        ComboBoxHelper.setKeyWord(mintMarkComboBox, new ArrayList(Arrays.asList(Coin.Companion.getMINT_MARKS())));
        ComboBoxHelper.setKeyWord(gradeComboBox, new ArrayList(Arrays.asList(Coin.Companion.getCONDITIONS())));

        // Allow resizing of images
        obvPicPanel = new JPanel() {

            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);

                try {
                    Image img = ImageIO.read(new File(getObvImageLocation()));

                    if(img != null) {

                        double heightFactor = (float) getHeight() / img.getHeight(this);
                        double widthFactor = (float) getWidth() / img.getWidth(this);

                        double scaleFactor = Math.min(heightFactor, widthFactor);

                        int newHeight = (int) (img.getHeight(this) * scaleFactor);
                        int newWidth = (int) (img.getWidth(this) * scaleFactor);

                        Image scaled;
                        // Only scale image if it's larger than we want
                        if (scaleFactor < 1) {
                            // Scale to new size
                            scaled = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                        } else
                            scaled = img;

                        g.drawImage(scaled, 0, 0, null);
                    }
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
