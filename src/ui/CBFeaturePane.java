package ui;

import store.Account;
import store.Metadata;
import store.Store;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;

public class CBFeaturePane extends JPanel {


    public CBFeaturePane(Store store, JTable table, DefaultTableModel model, JFrame owner){
        this.setLayout(new GridLayout( (store.metadata.getFeatureToggler().getFeatures().size() /2) +1, 2));

        this.setBorder(BorderFactory.createTitledBorder(""));
        ItemListener featureListener = e -> {
            store.metadata.getFeatureToggler().set(((JCheckBox) e.getItem()).getText(), e.getStateChange() == ItemEvent.SELECTED);
            store.marshellMetadata();
        };
        for (Map.Entry<String, Boolean> entry : store.metadata.getFeatureToggler().getFeatures().entrySet()) {
            JCheckBox cb = new JCheckBox(entry.getKey(), entry.getValue());
            cb.addItemListener(featureListener);
            this.add(cb);
        }
        final JButton setSelectedFeatureBtn = new JButton("Set Selected");
        final JButton setAllFeatureBtn = new JButton("Set All");
        this.add(setSelectedFeatureBtn);
        this.add(setAllFeatureBtn);

        setSelectedFeatureBtn.addActionListener(e -> {
            int start = table.getSelectedRow();
            int end = table.getSelectionModel().getMaxSelectionIndex();
            if (start != -1) {
                for (; start <= end; start++) {
                    Account acc = store.getAccountGroup().getAccount(start);
                    acc.getFeatureToggler().cloneFeature(store.metadata.getFeatureToggler());
                    String[] newData = acc.getColumnData();
                    for (int i = 0; i < model.getColumnCount(); i++) {
                        model.setValueAt(newData[i], start, i);
                    }
                    store.updateAccount(acc);
                }
                JOptionPane.showMessageDialog(owner, "Completed");
            }
        });

        setAllFeatureBtn.addActionListener(e -> {
            int index = 0;
            for (Account acc : store.getAccountGroup().getAccounts()) {
                acc.getFeatureToggler().cloneFeature(store.metadata.getFeatureToggler());
                String[] newData = acc.getColumnData();
                for (int i = 0; i < model.getColumnCount(); i++) {
                    model.setValueAt(newData[i], index, i);
                }
                store.updateAccount(acc);
                index++;
            }
            JOptionPane.showMessageDialog(owner, "Completed");

        });
    }
}
