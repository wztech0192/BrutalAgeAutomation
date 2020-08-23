package ui;

import store.Account;
import store.Store;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import static ui.UserInterface.addLabelTextField;

public class NumberPane extends JPanel {

    public NumberPane(Store store, JTable table, DefaultTableModel model, JFrame owner){
        this.setLayout(new GridLayout(store.metadata.getNumberFeaturer().getNumberSetting().size()+1, 2));
        final HashMap<String, JTextField> numberTextFields = new HashMap<>();
        for (Map.Entry<String, Integer> entry : store.metadata.getNumberFeaturer().getNumberSetting().entrySet()) {
            JTextField tf = new JTextField(String.valueOf(entry.getValue()));
          //  tf.setPreferredSize(new Dimension(200, 25));
            numberTextFields.put(entry.getKey(), tf);
            addLabelTextField(entry.getKey(), tf, this);
        }

        final JButton setSelectedNumberBtn = new JButton("Set Selected");
        final JButton setAllNumberBtn = new JButton("Set All");

        this.add(setSelectedNumberBtn);
        this.add(setAllNumberBtn);


        setSelectedNumberBtn.addActionListener(e -> {
            try {
                for (Map.Entry<String, JTextField> entry : numberTextFields.entrySet()) {
                    store.metadata.getNumberFeaturer().setNumberSetting(entry.getKey(), Integer.parseInt(entry.getValue().getText()));
                }
                store.marshellMetadata();
                int start = table.getSelectedRow();
                int end = table.getSelectionModel().getMaxSelectionIndex();
                if (start != -1) {
                    for (; start <= end; start++) {
                        Account acc = store.getAccountGroup().getAccount(start);
                        for (Map.Entry<String, Integer> entry : store.metadata.getNumberFeaturer().getNumberSetting().entrySet()) {
                            acc.getNumberFeaturer().setNumberSetting(entry.getKey(), entry.getValue());
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
        setAllNumberBtn.addActionListener(e -> {
            try {
                for (Map.Entry<String, JTextField> entry : numberTextFields.entrySet()) {
                    store.metadata.getNumberFeaturer().setNumberSetting(entry.getKey(), Integer.parseInt(entry.getValue().getText()));
                }
                store.marshellMetadata();
                int index = 0;
                for (Account acc : store.getAccountGroup().getAccounts()) {
                    for (Map.Entry<String, Integer> entry : store.metadata.getNumberFeaturer().getNumberSetting().entrySet()) {
                        acc.getNumberFeaturer().setNumberSetting(entry.getKey(), entry.getValue());
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
