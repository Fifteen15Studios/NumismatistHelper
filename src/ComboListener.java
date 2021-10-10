import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Locale;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public class ComboListener extends KeyAdapter
{
    @SuppressWarnings("rawtypes")
    JComboBox cbListener;
    @SuppressWarnings("rawtypes")
    Vector vector;

    @SuppressWarnings("rawtypes")
    public ComboListener(JComboBox cbListenerParam, Vector vectorParam)
    {
        cbListener = cbListenerParam;
        vector = vectorParam;
    }

    /**
     * Called when any key is typed while cursor is in the ComboBox
     *
     * @param key - The key that was pressed
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void keyTyped(KeyEvent key)
    {
        boolean popupHidden = false;

        // Find any selected text
        String selected = ((JTextField)key.getSource()).getSelectedText();

        // Text currently in the ComboBox
        String text = ((JTextField) key.getSource()).getText();

        // If no selected text
        if(selected == null) {
            // Current cursor position
            int pos = ((JTextField) key.getSource()).getCaretPosition();

            // Let system handle backspace and delete... Just re-display text
            if(key.getKeyChar() == KeyEvent.VK_BACK_SPACE || key.getKeyChar() == KeyEvent.VK_DELETE) {
                cbListener.setModel(new DefaultComboBoxModel(getFilteredList(text)));
                cbListener.setSelectedIndex(-1);
                ((JTextField)cbListener.getEditor().getEditorComponent()).setText(text);
                ((JTextField) key.getSource()).setCaretPosition(pos);
            }
            // For escape and enter, redisplay text and hide popup
            else if(key.getKeyChar() == KeyEvent.VK_ESCAPE || key.getKeyChar() == KeyEvent.VK_ENTER) {
                cbListener.setModel(new DefaultComboBoxModel(getFilteredList(text)));
                cbListener.setSelectedIndex(-1);
                ((JTextField)cbListener.getEditor().getEditorComponent()).setText(text);
                ((JTextField) key.getSource()).setCaretPosition(pos);
                cbListener.hidePopup();
                popupHidden = true;
                key.consume();
            }
            // for visible characters
            else {
                // If cursor is not at the end: add character at cursor position
                if(pos != text.length()) {
                    StringBuilder sb = new StringBuilder(text);
                    sb.insert(pos, key.getKeyChar());
                    cbListener.setModel(new DefaultComboBoxModel(getFilteredList(sb.toString())));
                    ((JTextField) key.getSource()).setText(sb.toString());
                }
                // If cursor is at the end of the text: add character to the end
                else {
                    cbListener.setModel(new DefaultComboBoxModel(getFilteredList(text + key.getKeyChar())));
                    cbListener.setSelectedIndex(-1);
                    ((JTextField) key.getSource()).setText(text + key.getKeyChar());
                }

                // Advance the cursor by 1
                ((JTextField) key.getSource()).setCaretPosition(pos+1);

                // Don't let system continue processing for visible characters
                key.consume();
            }
        }
        // Handle selected text
        else {

            // If escape or enter: hide popup
            if(key.getKeyChar() == KeyEvent.VK_ESCAPE || key.getKeyChar() == KeyEvent.VK_ENTER) {
                cbListener.hidePopup();
                popupHidden = true;
            }
            // If anything else, handle key press
            else {
                // Replace selected text with typed character
                ((JTextField) key.getSource()).replaceSelection("" + key.getKeyChar());
                // Get new text
                text = ((JTextField) key.getSource()).getText();
            }

            // Display new value
            cbListener.setModel(new DefaultComboBoxModel(getFilteredList(text)));
            cbListener.setSelectedIndex(-1);
            ((JTextField)cbListener.getEditor().getEditorComponent()).setText(text);

            // Don't let system continue processing
            key.consume();
        }

        // Remove any selection
        cbListener.setSelectedIndex(-1);
        if(!popupHidden)
            cbListener.showPopup();
    }

    /**
     * Filters the list of items in the ComboBox based on what is currently
     * typed in the box
     *
     * @param text Text currently typed into the ComboBox
     * @return List of filtered items to appear in the ComboBox list
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Vector getFilteredList(String text)
    {
        Vector v = new Vector();
        for (Object o : vector) {
            if (o.toString().toLowerCase(Locale.ROOT).startsWith(text.toLowerCase(Locale.ROOT))) {
                v.add(o.toString());
            }
        }
        return v;
    }
}