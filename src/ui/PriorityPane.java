package ui;

import store.Account;
import store.Store;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import static ui.UserInterface.addLabelTextField;

public class PriorityPane extends JPanel {

    public PriorityPane(Store store, JTable table, DefaultTableModel model, JFrame owner){
        this.setLayout(new GridLayout(store.metadata.getNumberFeaturer().getGatherPriorities().size()+1, 2));
        this.setBorder(BorderFactory.createTitledBorder("Priority: (Never) 0 <<<<<< 5 (Always)"));
        final HashMap<String, JTextField> priorityTextfields = new HashMap<>();
        for (Map.Entry<String, Integer> entry : store.metadata.getNumberFeaturer().getGatherPriorities().entrySet()) {
            JTextField tf = new JTextField(String.valueOf(entry.getValue()));
            priorityTextfields.put(entry.getKey(), tf);
            addLabelTextField(entry.getKey(), tf, this);
        }


        final JButton setSelectedPriorityBtn = new JButton("Set Selected");
        final JButton setAllPriorityBtn = new JButton("Set All");
        this.add(setSelectedPriorityBtn);
        this.add(setAllPriorityBtn);

        setSelectedPriorityBtn.addActionListener(e -> {
            try {
                for (Map.Entry<String, JTextField> entry : priorityTextfields.entrySet()) {
                    store.metadata.getNumberFeaturer().setGatherPriority(entry.getKey(), Integer.parseInt(entry.getValue().getText()));
                }
                store.marshellMetadata();
                int start = table.getSelectedRow();
                int end = table.getSelectionModel().getMaxSelectionIndex();
                if (start != -1) {
                    for (; start <= end; start++) {
                        Account acc = store.getAccountGroup().getAccount(start);
                        for (Map.Entry<String, Integer> entry : store.metadata.getNumberFeaturer().getGatherPriorities().entrySet()) {
                            acc.getNumberFeaturer().setGatherPriority(entry.getKey(), entry.getValue());
                        }
                        String[] newData = acc.getColumnData();
                        for (int i = 0; i < model.getColumnCount(); i++) {
                            model.setValueAt(newData[i], start, i);
                        }
                        store.updateAccount(acc);
                    }
                    JOptionPane.showMessageDialog(owner, "Completed");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(owner, "Number only!");
            }
        });

        setAllPriorityBtn.addActionListener(e -> {
            try {
                for (Map.Entry<String, JTextField> entry : priorityTextfields.entrySet()) {
                    store.metadata.getNumberFeaturer().setGatherPriority(entry.getKey(), Integer.parseInt(entry.getValue().getText()));
                }
                store.marshellMetadata();
                int index = 0;
                for (Account acc : store.getAccountGroup().getAccounts()) {
                    for (Map.Entry<String, Integer> entry : store.metadata.getNumberFeaturer().getGatherPriorities().entrySet()) {
                        acc.getNumberFeaturer().setGatherPriority(entry.getKey(), entry.getValue());
                    }
                    String[] newData = acc.getColumnData();
                    for (int i = 0; i < model.getColumnCount(); i++) {
                        model.setValueAt(newData[i], index, i);
                    }
                    store.updateAccount(acc);
                    index++;
                }
                JOptionPane.showMessageDialog(owner, "Completed");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(owner, "Number only!");
            }
        });
    }
}
