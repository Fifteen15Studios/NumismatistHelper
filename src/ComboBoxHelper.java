import items.Container;
import items.Country;
import items.Currency;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.*;

public class ComboBoxHelper {

    public static void setupBoxes(AutoComboBox countryInput, JTextField yearInput, AutoComboBox currencyInput, JTextArea errorDisplay, NumismatistAPI api) {

        setKeyWord(countryInput, api.getCountries());

        // set currency when country changes
        countryInput.addActionListener(e -> ComboBoxHelper.setCurrency(countryInput, yearInput, currencyInput, api.getCountries()));

        yearInput.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {

            }

            @Override
            public void focusLost(FocusEvent e) {
                try {
                    ComboBoxHelper.setCurrency(countryInput, yearInput, currencyInput, api.getCountries());
                    errorDisplay.setText("");
                }
                catch (NumberFormatException nfe) {
                    errorDisplay.setForeground(Main.COLOR_ERROR);
                    errorDisplay.setText(Main.getString("error_invalidDate"));
                }
            }
        });

        yearInput.addActionListener(e -> {
            try {
                ComboBoxHelper.setCurrency(countryInput, yearInput, currencyInput, api.getCountries());
                errorDisplay.setText("");
            }
            catch (NumberFormatException nfe) {
                errorDisplay.setForeground(Main.COLOR_ERROR);
                errorDisplay.setText(Main.getString("error_invalidDate"));
            }
        });

        countryInput.setSelectedIndex(0);
    }

    public static void setCurrency(AutoComboBox countryInput, JTextField yearInput, AutoComboBox currencyInput, ArrayList<Country> countries) throws NumberFormatException {
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);

        JTextField countryBox = (JTextField) countryInput.getEditor().getEditorComponent();

        String startingSelection = "";
        if(currencyInput.getSelectedItem() != null) {
            startingSelection = currencyInput.getSelectedItem().toString();
        }

        NumberFormatException numberFormatException = null;

        ArrayList<Country.Range> ranges = new ArrayList<>();
        ArrayList<items.Currency> validCurrencies = new ArrayList<>();
        ArrayList<String> validIds = new ArrayList<>();

        for (Country country : countries) {
            if(country.getName().equals(countryBox.getText())) {
                ranges = country.getRanges();
                break;
            }
        }

        if(ranges.isEmpty()) {
            ((JTextField)currencyInput.getEditor().getEditorComponent()).setText("");
            setKeyWord(currencyInput, new ArrayList<String>());
        }
        else {
            for(Country.Range range : ranges) {
                Currency currency = range.getCurrency();
                int start = range.getYrStart();
                int end = range.getYrEnd();

                int input = thisYear;

                // Get year from input box
                if (yearInput.getText() != null && !yearInput.getText().isBlank())
                {
                    try {
                        input = Integer.parseInt(yearInput.getText());
                    } catch (NumberFormatException nfe) {
                        numberFormatException = nfe;
                    }
                }

                // If invalid input or if year input is empty, show all valid currencies for this country
                if(numberFormatException != null || yearInput.getText() == null || yearInput.getText().isBlank()) {
                    validCurrencies.add(currency);
                    validIds.add(currency.getNameAbbr());
                }
                // if year is valid, Check if this currency is valid for this time period
                else if( (start <= input || start == Country.Range.YEAR_START_INVALID) && (input <= end || end == Country.Range.YEAR_END_INVALID) ) {
                    validCurrencies.add(currency);
                    validIds.add(currency.getNameAbbr());
                }
            }

            // If no valid currencies for this country / year combination,
            // display all valid currencies for this country
            if(validCurrencies.isEmpty()) {

                // Add valid IDs too
                // Clear it first, just in case
                validIds.clear();
                for(Country.Range range : ranges) {
                    validCurrencies.add(range.getCurrency());
                    validIds.add(range.getCurrency().getNameAbbr());
                }
            }

            setKeyWord(currencyInput, validCurrencies);
            //currencyInput.setIds(validIds.toArray(new String[0]));
        }

        // If current selection no longer valid
        if(!Arrays.asList(currencyInput.keyWord).contains(startingSelection)) {
            // Select first in the list
            if(currencyInput.getItemCount() > 0)
                currencyInput.setSelectedIndex(0);
            // Clear the box if list is empty
            else
                ((JTextField) currencyInput.getEditor().getEditorComponent()).setText("");
        }
        // Reselect old selection if index has changed
        else if(currencyInput.getItemCount() > 0)
            currencyInput.setSelectedIndex(Arrays.asList(currencyInput.keyWord).indexOf(startingSelection));

        if(numberFormatException != null)
            throw numberFormatException;
    }

    public static void setContainerList(JComboBox comboBox, NumismatistAPI api) {
        setContainerList(comboBox, api, null, null);
    }

    public static void setContainerList(JComboBox comboBox, NumismatistAPI api, JButton addButton, JFrame parent) {
        Vector<String> items = new Vector<>();
        items.add("");
        for(Container container : api.getContainers() ) {
            items.add(container.getName());
        }

        SortedComboBoxModel model = new SortedComboBoxModel(items);
        comboBox.setModel(model);

        if(addButton != null && parent != null) {
            addButton.addActionListener(e -> {
                NewContainerDialog newContainerDialog = new NewContainerDialog(parent);
                newContainerDialog.pack();
                newContainerDialog.setLocationRelativeTo(parent);
                newContainerDialog.setLocationsBox(comboBox);
                newContainerDialog.setVisible(true);
            });
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void setKeyWord(AutoComboBox box, ArrayList list) {
        ArrayList<String> newList = new ArrayList<>();
        ArrayList<String> ids = new ArrayList<>();

        if(!list.isEmpty() && list.get(0) instanceof Country) {
            ArrayList<Country> countryList = ((ArrayList<Country>)list);
            for (Country country : countryList) {
                newList.add(country.getName());
                ids.add(country.getName());
            }
            box.setKeyWord(newList.toArray(String[]::new));
            box.ids = ids.toArray(new String[0]);
        }
        else if(!list.isEmpty() && list.get(0) instanceof items.Currency) {
            ArrayList<items.Currency> currencyList = ((ArrayList<items.Currency>)list);
            for (Currency currency : currencyList) {
                newList.add(currency.getName());
                ids.add(currency.getNameAbbr());
            }
            box.setKeyWord(newList.toArray(String[]::new));
            box.ids = ids.toArray(new String[0]);
        }
        else if(!list.isEmpty() && list.get(0) instanceof String) {
            newList.addAll(((ArrayList<String>) list));
            box.setKeyWord(newList.toArray(String[]::new));
        }

        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>( new Vector<>(newList) );
        box.setModel(model);
    }
}
