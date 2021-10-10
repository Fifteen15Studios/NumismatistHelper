import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings({"rawtypes", "unused"})
public class DoubleListDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JList sourceList;
    private JButton addButton;
    private JButton removeButton;
    private JList destList;
    private JLabel sourceLabel;
    private JLabel destLabel;
    private JButton upButton;
    private JButton downButton;

    private DefaultListModel source = new DefaultListModel();
    private DefaultListModel dest = new DefaultListModel();

    private final ArrayList<Object> sourceArgs = new ArrayList<>();
    private final ArrayList<Object> destArgs = new ArrayList<>();

    boolean cancelled = false;

    public DoubleListDialog(ArrayList source, ArrayList dest) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setSize(new Dimension(500,400));
        setMinimumSize(new Dimension(500,400));

        // Remove anything from the source that is already moved to the destination side
        source.removeAll(dest);

        this.source.addAll(source);
        this.dest.addAll(dest);

        setIconImage(Main.getIcon().getImage());

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

        sourceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sourceList.setLayoutOrientation(JList.VERTICAL);
        destList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        destList.setLayoutOrientation(JList.VERTICAL);

        getRootPane().setDefaultButton(buttonOK);

        addButton.addActionListener(e -> {
            if(sourceList.getSelectedIndex()!=-1) {
                Object selected = sourceList.getSelectedValue();
                this.source.remove(sourceList.getSelectedIndex());

                // Add new item below selection, if possible
                if(destList.getSelectedIndex()==-1)
                    this.dest.add(this.dest.size(), selected);
                else
                    this.dest.add(destList.getSelectedIndex()+1, selected);
                refreshLists();
            }
        });

        removeButton.addActionListener(e -> {
            if(destList.getSelectedIndex()!=-1) {
                Object selected = destList.getSelectedValue();
                this.dest.remove(destList.getSelectedIndex());
                this.source.add(0, selected);
                refreshLists();
            }
        });

        upButton.addActionListener(e -> {
            if(destList.getSelectedIndex()>0) {
                int selected = destList.getSelectedIndex();
                Object selectedItem = destList.getSelectedValue();
                int above = selected-1;
                Object aboveItem = this.dest.get(above);
                this.dest.set(above,selectedItem);
                this.dest.set(selected, aboveItem);
                refreshLists();
                this.destList.setSelectedIndex(above);
            }
        });

        downButton.addActionListener(e -> {
            if(destList.getSelectedIndex()!=-1 && destList.getSelectedIndex() < this.dest.size()-1) {
                int selected = destList.getSelectedIndex();
                Object selectedItem = destList.getSelectedValue();
                int below = selected+1;
                Object belowItem = this.dest.get(below);
                this.dest.set(below,selectedItem);
                this.dest.set(selected, belowItem);
                refreshLists();
                this.destList.setSelectedIndex(below);
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void refreshLists() {

        // Remove already added items
        for(Object item : dest.toArray()) {
            for(int i = 0; i < source.size(); i++)
                if(source.get(i).equals(item))
                    source.remove(i);
        }

        // Always keep source sorted
        Object[] sortedSource = Arrays.stream(source.toArray()).sorted().toArray();
        source.clear();
        source.addAll(Arrays.asList(sortedSource));

        sourceList.setListData(source.toArray());
        destList.setListData(dest.toArray());
    }

    /**
     * Set the method for the source object type to get something sortable.
     * Ex: getName if there is a getName() function
     * Ex: (getName, en-us) if there is a getName(Locale) function
     *
     * @param sourceMethod name of the method to use to turn this object into something sortable
     * @param args arguments to use in the function
     */
    public void setSourceMethod(String sourceMethod, Object ... args ) {
        sourceArgs.addAll(Arrays.asList(args));

        if(!sourceMethod.equals("")) {
            ArrayList<Object> newSource = new ArrayList<>();

            for(Object item : source.toArray()) {
                try {
                    newSource.add(item.getClass().getMethod(sourceMethod).invoke(item, sourceArgs.toArray()));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            source.clear();
            source.addAll(newSource);
            refreshLists();
        }
    }

    /**
     * Set the method for the destination object type to get something sortable.
     * Ex: getName if there is a getName() function
     * Ex: (getName, en-us) if there is a getName(Locale) function
     *
     * @param destMethod name of the method to use to turn this object into something sortable
     * @param args arguments to use in the function
     */
    public void setDestMethod(String destMethod, Object ... args) {
        destArgs.addAll(Arrays.asList(args));

        if(!destMethod.equals("")) {
            ArrayList<Object> newDest = new ArrayList<>();

            for(Object item : dest.toArray()) {
                try {
                    newDest.add(item.getClass().getMethod(destMethod).invoke(item, destArgs.toArray()));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            dest.clear();
            dest.addAll(newDest);
            refreshLists();
        }
    }

    private void onOK() {
        dispose();
    }

    private void onCancel() {
        cancelled = true;
        dispose();
    }

    /**
     * Sets the label text for the source list
     *
     * @param name label text
     */
    void setSourceName(String name) {
        sourceLabel.setText(name);
    }

    /**
     * Sets the label text for the destination list
     *
     * @param name label text
     */
    void setDestName(String name) {
        destLabel.setText(name);
    }

    private void createUIComponents() {
        dest = new DefaultListModel();
        source = new DefaultListModel();

        destList = new JList(dest);
        sourceList = new JList(source);
    }

    /**
     * Gets an array of destination items
     *
     * @return An array of items in the destination column
     */
    public Object[] getDest() {
        return dest.toArray();
    }

    /**
     * Gets an array of source items
     *
     * @return An array of items in the source column
     */
    public Object[] getSource() {
        return source.toArray();
    }
}
