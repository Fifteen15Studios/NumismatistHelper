import javax.swing.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;

public class SortedComboBoxModel extends DefaultComboBoxModel {
    public SortedComboBoxModel() {
        super();
    }
    public SortedComboBoxModel(Object[] items) {
        Arrays.sort(items);
        int size = items.length;
        for (int i = 0; i < size; i++) {
            super.addElement(items[i]);
        }
        setSelectedItem(items[0]);
    }
    public SortedComboBoxModel(Vector items) {
        Collections.sort(items);
        int size = items.size();
        for (int i = 0; i < size; i++) {
            super.addElement(items.elementAt(i));
        }
        setSelectedItem(items.elementAt(0));
    }
    public void addElement(Object element) {
        insertElementAt(element, 0);
    }
    public void insertElementAt(Object element, int index) {
        int size = getSize();
        for (index = 0; index < size; index++) {
            Comparable c = (Comparable) getElementAt(index);
            if (c.compareTo(element) > 0) {
                break;
            }
        }
        super.insertElementAt(element, index);
    }
}
