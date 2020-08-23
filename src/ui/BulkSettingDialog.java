package ui;

import com.sun.javafx.logging.JFRInputEvent;
import store.Account;
import store.Store;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiFunction;

public class BulkSettingDialog extends JButton{

    private Store store;
    private JFrame owner;

    BulkSettingDialog(Store store, JFrame owner, JTable table) {
        this.store = store;
        this.owner = owner;

        this.setText("Bulk Account Setting");
        this.addActionListener(e->{
            JPanel panel = new JPanel(new GridLayout(6, 1));
            addSingleBulkSetting("cb",panel, "Changed Server", table, (acc, value) -> {
                acc.setChangedServer(value.equalsIgnoreCase("y"));
                return true;
            });
            addSingleBulkSetting("cb",panel, "Finished Init", table, (acc, value) -> {
                acc.setFinishInit(value.equalsIgnoreCase("y"));
                return true;
            });
            addSingleBulkSetting("cb",panel, "Joined Clan", table, (acc, value) -> {
                acc.setJoinClan(value.equalsIgnoreCase("y"));
                return true;
            });
            addSingleBulkSetting("cb",panel, "Is Randomized", table, (acc, value) -> {
                acc.setRandomized(value.equalsIgnoreCase("y"));
                return true;
            });
            addSingleBulkSetting("cb",panel, "Is 30 Days", table, (acc, value) -> {
                acc.setThirtyDay(value.equalsIgnoreCase("y"));
                return true;
            });

            addSingleBulkSetting("tf",panel, "Clan", table, (acc, value) -> {
                acc.setClan(value);
                return true;
            });

            JOptionPane.showConfirmDialog(panel, panel,
                    "Bulk Setting", JOptionPane.CLOSED_OPTION);

        });
    }

    private void addSingleBulkSetting( String type, JPanel container, String label, JTable table, BiFunction<Account, String, Boolean> func){
        JPanel singlePanel = new JPanel(new GridLayout(1,4));
        container.add(singlePanel);
        final JTextField textfield = new JTextField();
        final JCheckBox checkbox = new JCheckBox();
        singlePanel.add(new JLabel(label));

        if(type.equalsIgnoreCase("tf")){
            singlePanel.add(textfield);
        }else{
            singlePanel.add( checkbox );
        }

        JButton setSelectedBtn = new JButton("Set Selected");
        JButton setAllBtn = new JButton("Set All");

        singlePanel.add(setSelectedBtn);
        singlePanel.add(setAllBtn);
        setSelectedBtn.addActionListener(e -> {
            String value;
            if(type.equalsIgnoreCase("tf") ){
                value = textfield.getText();
            }else{
                value = checkbox.isSelected() ? "checked" : "unchecked";
            }
            int start = table.getSelectedRow();
            int end = table.getSelectionModel().getMaxSelectionIndex();
            if (start != -1) {
                for (; start <= end; start++) {
                    Account acc = store.getAccountGroup().getAccount(start);
                    if(!func.apply(acc,value )){
                        JOptionPane.showMessageDialog(owner, "Validation Failed");
                        return;
                    }
                    store.updateAccount(acc);
                }
                JOptionPane.showMessageDialog(owner, "Completed");
            }
        });

        setAllBtn.addActionListener(e -> {
            String value;
            if(type.equalsIgnoreCase("tf") ){
                value = textfield.getText();
            }else{
                value = checkbox.isSelected() ? "y" : "n";
            }
            for (Account acc : store.getAccountGroup().getAccounts()) {
                if(!func.apply(acc, value)){
                    JOptionPane.showMessageDialog(owner, "Validation Failed");
                    return;
                }
                store.updateAccount(acc);
            }
            JOptionPane.showMessageDialog(owner, "Completed");

        });

    }
}
