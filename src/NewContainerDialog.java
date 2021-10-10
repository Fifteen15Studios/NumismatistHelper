import items.Container;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class NewContainerDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField nameInput;
    private JComboBox parentBox;
    private JTextArea errorDisplay;
    private JComboBox locationsBox;

    private final NumismatistAPI api;

    public NewContainerDialog(JFrame parent) {
        setContentPane(contentPane);
        setModal(true);
        setResizable(false);
        setModalityType(Dialog.DEFAULT_MODALITY_TYPE);
        getRootPane().setDefaultButton(buttonOK);
        setIconImage(Main.getIcon().getImage());

        setTitle(Main.getString("addContainer_title"));

        api = ((Main) parent).api;

        ComboBoxHelper.setContainerList(parentBox, api);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        Container container = new Container();

        if(nameInput.getText() != null && !nameInput.getText().equals("")) {
            container.setName(nameInput.getText());
            if(parentBox.getSelectedItem()  != null && !parentBox.getSelectedItem().toString().equals("")) {
                container.setParentID(api.findContainer(parentBox.toString()).getId());
            }

            int result = container.saveToDb(api);
            if(result != 1)
                errorDisplay.setText(api.getSuccessMessage(result));
            else {
                // Add result to locationBox
                if(locationsBox != null) {
                    locationsBox.addItem(container.getName());
                    locationsBox.setSelectedItem(container.getName());
                    locationsBox.invalidate();
                }
                dispose();
            }
        }
        else {
            errorDisplay.setText(Main.getString("addContainer_error_emptyName"));
        }

    }

    void setLocationsBox(JComboBox locationsBox) {
        this.locationsBox = locationsBox;
    }

    private void onCancel() {
        dispose();
    }
}
