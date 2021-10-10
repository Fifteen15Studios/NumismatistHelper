import java.util.*;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public class AutoComboBox extends JComboBox<String> {

    String[] keyWord = {};
    String[] ids = {};
    Vector myVector = new Vector();

    @SuppressWarnings({"unchecked"})
    public AutoComboBox() {

        setModel(new DefaultComboBoxModel(myVector));
        setSelectedIndex(-1);
        setEditable(true);
        JTextField text = (JTextField) this.getEditor().getEditorComponent();
        text.setFocusable(true);
        text.setText("");
        text.addKeyListener(new ComboListener(this, myVector));
        setMyVector();
    }

    /**
     * set the item list of the AutoComboBox
     * @param keyWord an array of Strings
     */
    public void setKeyWord(String[] keyWord) {
        this.keyWord = keyWord;
        setMyVectorInitial();

        // redisplay popup if it's visible
        if(isPopupVisible()) {
            hidePopup();
            showPopup();
        }
    }

    @SuppressWarnings("unchecked")
    private void setMyVector() {
        Collections.addAll(myVector, keyWord);
    }

    @SuppressWarnings("unchecked")
    private void setMyVectorInitial() {
        myVector.clear();
        Collections.addAll(myVector, keyWord);
    }

    /**
     * Determine if an array contains the same list of items as the keyWord
     *   variable
     *
     * @param arr2 Array to compare to
     * @return True if the array contains the same items, otherwise false
     */
    public boolean keyWordEquals(String[] arr2) {

        // If they're different sizes, they aren't the same
        if(keyWord.length != arr2.length)
            return false;

        // Store arr1[] elements and their counts in hash map
        Map<String, Integer> map = new HashMap<>();
        int count;
        for (String s : keyWord) {
            if (map.get(s) == null)
                map.put(s, 1);
            else {
                count = map.get(s);
                count++;
                map.put(s, count);
            }
        }

        // Traverse arr2[] elements and check if all elements of arr2[] are
        // present same number of times or not.
        for (String s : arr2) {
            // If there is an element in arr2[], but not in arr1[]
            if (!map.containsKey(s))
                return false;

            // If an element of arr2[] appears more times than it appears in
            // keyword[]
            if (map.get(s) == 0)
                return false;

            count = map.get(s);
            --count;
            map.put(s, count);
        }

        // If it got here they have the same elements, the same number of times
        return true;
    }
}