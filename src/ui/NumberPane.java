package ui;

import store.Account;
import store.Store;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import static ui.UserInterface.addLabelTextField;

public class NumberPane extends JPanel {

    public NumberPane(Store store, JTable table, DefaultTableModel model, JFrame owner){
        this.setLayout(new GridLayout(store.metadata.getNumberFeaturer().getNumberSetting().size()+ 2, 2));
        final HashMap<String, JTextField> numberTextFields = new HashMap<>();
        for (Map.Entry<String, Integer> entry : store.metadata.getNumberFeaturer().getNumberSetting().entrySet()) {
            JTextField tf = new JTextField(String.valueOf(entry.getValue()));
          //  tf.setPreferredSize(new Dimension(200, 25));
            numberTextFields.put(entry.getKey(), tf);
            addLabelTextField(entry.getKey(), tf, this);
        }


        long minutes = !store.getAccountGroup().getAccounts().isEmpty()
                ? Duration.between(LocalDateTime.now(), store.getAccountGroup().getAccount(0).getTemplateStartDate()).toMinutes()
                : 0;

        String str = minutes/24/60 + ":" + minutes/60%24 + ':' + minutes%60;
        JTextField feedTempleTimeField = new JTextField(str);

        addLabelTextField("Feed Temple (D:H:M)", feedTempleTimeField, this);

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
                    String feedTempleTime = feedTempleTimeField.getText();
                    String[] split = feedTempleTime.split(":");
                    int min = (Integer.parseInt(split[0]) * 24 * 60) + (Integer.parseInt(split[1]) * 60) + Integer.parseInt(split[2]);
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime templeTime = now.plusMinutes(min);

                    for (; start <= end; start++) {

                        Account acc = store.getAccountGroup().getAccount(start);
                        for (Map.Entry<String, Integer> entry : store.metadata.getNumberFeaturer().getNumberSetting().entrySet()) {
                            acc.getNumberFeaturer().setNumberSetting(entry.getKey(), entry.getValue());
                        }
                        acc.setTemplateStartDate(templeTime);
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

                String feedTempleTime = feedTempleTimeField.getText();
                String[] split = feedTempleTime.split(":");
                int min = (Integer.parseInt(split[0]) * 24 * 60) + (Integer.parseInt(split[1]) * 60) + Integer.parseInt(split[2]);
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime templeTime = now.plusMinutes(min);

                for (Account acc : store.getAccountGroup().getAccounts()) {
                    for (Map.Entry<String, Integer> entry : store.metadata.getNumberFeaturer().getNumberSetting().entrySet()) {
                        acc.getNumberFeaturer().setNumberSetting(entry.getKey(), entry.getValue());
                    }
                    acc.setTemplateStartDate(templeTime);

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
