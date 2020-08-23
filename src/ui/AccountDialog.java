package ui;

import store.Account;
import store.BuildHammer;
import store.Store;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static ui.UserInterface.addLabelTextField;
import static ui.UserInterface.NullableTextField;

public class AccountDialog extends JDialog {

    public AccountDialog(Store store, JFrame owner, Account acc, DefaultTableModel model) {
        try {

            ItemListener featureListener = e -> {
                acc.getFeatureToggler().set(((JCheckBox) e.getItem()).getText(), e.getStateChange() == ItemEvent.SELECTED);
            };

            final JPanel topPane = new JPanel(new BorderLayout());
            final JPanel featurePane = new JPanel(new GridLayout(acc.getFeatureToggler().getFeatures().size()/2, 3));
            featurePane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "Features"));
            final HashMap<String, JCheckBox> featureCBs = new HashMap<>();
            for (Map.Entry<String, Boolean> entry : acc.getFeatureToggler().getFeatures().entrySet()) {
                JCheckBox cb = new JCheckBox(entry.getKey(), entry.getValue());
                cb.addItemListener(featureListener);
                featureCBs.put(entry.getKey(), cb);
                featurePane.add(cb);
            }

            final JPanel numberPane = new JPanel(new GridLayout(acc.getNumberFeaturer().getNumberSetting().size(), 2));
            final HashMap<String, JTextField> numberTextfield = new HashMap<>();
            for (Map.Entry<String, Integer> entry : acc.getNumberFeaturer().getNumberSetting().entrySet()) {
                JTextField tf = new JTextField(String.valueOf(entry.getValue()), 7);
                tf.setPreferredSize(new Dimension(200, 25));
                numberTextfield.put(entry.getKey(), tf);
                addLabelTextField(entry.getKey(), tf, numberPane);
            }

            final JPanel priorityPane = new JPanel(new GridLayout( acc.getNumberFeaturer().getGatherPriorities().size(), 2));
            final HashMap<String, JTextField> priorityTextfield = new HashMap<>();
            for (Map.Entry<String, Integer> entry : acc.getNumberFeaturer().getGatherPriorities().entrySet()) {
                JTextField tf = new JTextField(String.valueOf(entry.getValue()), 4);
                tf.setPreferredSize(new Dimension(200, 25));
                priorityTextfield.put(entry.getKey(), tf);
                addLabelTextField(entry.getKey(), tf, priorityPane);
            }

            topPane.setBorder(BorderFactory.createTitledBorder(""));
            topPane.add(numberPane, BorderLayout.WEST);
            topPane.add(priorityPane, BorderLayout.CENTER);
            topPane.add(featurePane, BorderLayout.EAST);


            final JCheckBox changedServerCB = new JCheckBox("Changed Server", acc.getChangedServer());
            final JCheckBox finishInitCB = new JCheckBox("Finished Init", acc.isFinishInit());
            final JCheckBox joinedClanCB = new JCheckBox("Joined Clan", acc.isJoinClan());
            final JCheckBox isRandomizeCB = new JCheckBox("Is Randomized", acc.isRandomized());
            final JCheckBox isThirtyDayCB = new JCheckBox("Is 30 Days", acc.isThirtyDay());
            final JTextField serverIDField = NullableTextField(acc.getServerID());
            final JTextField nameField = NullableTextField(acc.getName());
            final JTextField hordeField = NullableTextField(acc.getHordeLabel());
            final JTextField clanField = NullableTextField(acc.getClan());
            final JTextField levelField = NullableTextField(acc.getLevel());
            final JTextField prevLevelField = NullableTextField(acc.getPreviousLevel());
            final JTextField errorField = NullableTextField(acc.getError());
            final JTextField lastGiftTimeField = NullableTextField(acc.getLastGiftTime());
            final JTextField lastRoundField = NullableTextField(acc.getLastRound());
            final JTextField troopsField = NullableTextField(acc.getTroops());
            final JPanel metaPanel = new JPanel(new GridLayout(13, 2));
            metaPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "Account Data"));
            metaPanel.add(changedServerCB);
            metaPanel.add(finishInitCB);
            metaPanel.add(joinedClanCB);
            metaPanel.add(isRandomizeCB);
            metaPanel.add(isThirtyDayCB);
            metaPanel.add(new JTextField(""));
            addLabelTextField("Server ID: ", serverIDField, metaPanel);
            addLabelTextField("Horde: ", hordeField, metaPanel);
            addLabelTextField("Clan: ", clanField, metaPanel);
            addLabelTextField("Prev Level: ", prevLevelField, metaPanel);
            addLabelTextField("Level: ", levelField, metaPanel);
            addLabelTextField("Name: ", nameField, metaPanel);
            addLabelTextField("Error: ", errorField, metaPanel);
            addLabelTextField("Troops: ", troopsField, metaPanel);
            addLabelTextField("Last Gift Time: ", lastGiftTimeField, metaPanel);
            addLabelTextField("Last Round Time: ", lastRoundField, metaPanel);


            final JPanel rssPanel = new JPanel(new GridLayout(acc.getResources().size(), 2));
            rssPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "Resources"));
            HashMap<String, JTextField> resourcesFields = new HashMap<>();

            for (Map.Entry<String, Integer> entry : acc.getResources().entrySet()) {
                JTextField field = NullableTextField(entry.getValue());
                resourcesFields.put(entry.getKey(), field);
                addLabelTextField(entry.getKey(), field, rssPanel);
            }

            final JPanel buildingPanel = new JPanel(new GridLayout(acc.getBuildings().size(), 2));
            buildingPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "Buildings"));
            HashMap<String, JTextField> buildingFields = new HashMap<>();
            for (Map.Entry<String, Integer> entry : acc.getBuildings().entrySet()) {
                JTextField field = NullableTextField(entry.getValue());
                buildingFields.put(entry.getKey(), field);
                addLabelTextField(entry.getKey(), field, buildingPanel);
            }

            final JPanel[] hammerPanels = new JPanel[2];
            JTextField[][] hammerFields = new JTextField[2][4];
            BuildHammer hammer = acc.getPrimaryHammer();
            for (int i = 0; i < 2; i++) {
                hammerPanels[i] = new JPanel(new GridLayout(4, 2));
                hammerFields[i][0] = NullableTextField(hammer.getBuildingName());
                hammerFields[i][1] = NullableTextField(hammer.getNextBuildingLevel());
                hammerFields[i][2] = NullableTextField(hammer.getHammer());
                hammerFields[i][3] = NullableTextField(hammer.getExpiration());
                addLabelTextField("Building Name: ", hammerFields[i][0], hammerPanels[i]);
                addLabelTextField("Next Level: ", hammerFields[i][1], hammerPanels[i]);
                addLabelTextField("Complete TIme: ", hammerFields[i][2], hammerPanels[i]);
                addLabelTextField("Expiration: ", hammerFields[i][3], hammerPanels[i]);
                hammer = acc.getSecondaryHammer();
            }
            hammerPanels[0].setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "Primary Hammer"));
            hammerPanels[1].setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "Secondary Hammer"));


            final JPanel mainPane = new JPanel(new BorderLayout());

            final JPanel top = new JPanel(new BorderLayout());
            top.add(rssPanel, BorderLayout.WEST);
            top.add(metaPanel, BorderLayout.CENTER);

            final JPanel center = new JPanel(new BorderLayout());
            center.add(top, BorderLayout.CENTER);

            final JPanel centerBottom = new JPanel(new GridLayout(2, 1));
            centerBottom.add(hammerPanels[0]);
            centerBottom.add(hammerPanels[1]);
            center.add(centerBottom, BorderLayout.SOUTH);


            mainPane.add(topPane, BorderLayout.NORTH);
            mainPane.add(center, BorderLayout.CENTER);
            mainPane.add(buildingPanel, BorderLayout.EAST);

            final JButton saveBtn = new JButton("Save");
            saveBtn.addActionListener(e -> {
                try {


                    acc.setChangedServer(changedServerCB.isSelected());
                    acc.setFinishInit(finishInitCB.isSelected());
                    acc.setJoinClan(joinedClanCB.isSelected());
                    acc.setServerID(Integer.parseInt(serverIDField.getText()));
                    acc.setRandomized(isRandomizeCB.isSelected());
                    acc.setHorde(hordeField.getText());
                    acc.setClan(clanField.getText());
                    acc.setName(nameField.getText());
                    acc.setPreviousLevel(Integer.parseInt(prevLevelField.getText()));
                    acc.setLevel(Integer.parseInt(levelField.getText()));
                    acc.setError(Integer.parseInt(errorField.getText()));
                    acc.setLastGiftTime(LocalDateTime.parse(lastGiftTimeField.getText()));
                    acc.setLastRound(LocalDateTime.parse(lastRoundField.getText()));
                    acc.setTroops(Integer.parseInt(troopsField.getText()));

                    for (Map.Entry<String, JTextField> entry : priorityTextfield.entrySet()) {
                        acc.getNumberFeaturer().setGatherPriority(entry.getKey(), Integer.parseInt(entry.getValue().getText()));
                    }

                    for (Map.Entry<String, JTextField> entry : numberTextfield.entrySet()) {
                        acc.getNumberFeaturer().setNumberSetting(entry.getKey(), Integer.parseInt(entry.getValue().getText()));
                    }

                    for (Map.Entry<String, JCheckBox> entry : featureCBs.entrySet()) {
                        acc.getFeatureToggler().set(entry.getKey(), entry.getValue().isSelected());
                    }

                    for (Map.Entry<String, JTextField> entry : resourcesFields.entrySet()) {
                        acc.setResource(entry.getKey(), Integer.parseInt(entry.getValue().getText()));
                    }

                    for (Map.Entry<String, JTextField> entry : buildingFields.entrySet()) {
                        acc.setBuildingLevel(entry.getKey(), Integer.parseInt(entry.getValue().getText()));
                    }

                    BuildHammer hammerSet = acc.getPrimaryHammer();
                    for (int i = 0; i < 2; i++) {
                        if (!hammerFields[i][0].getText().equalsIgnoreCase("")) {
                            hammerSet.setBuildingName(hammerFields[i][0].getText());
                        }
                        if (!hammerFields[i][1].getText().equalsIgnoreCase("")) {
                            hammerSet.setNextBuildingLevel(Integer.parseInt(hammerFields[i][1].getText()));
                        }
                        if (!hammerFields[i][2].getText().equalsIgnoreCase("")) {
                            hammerSet.setHammer(LocalDateTime.parse(hammerFields[i][2].getText()));
                        }
                        if (!hammerFields[i][3].getText().equalsIgnoreCase("")) {
                            hammerSet.setExpiration(LocalDateTime.parse(hammerFields[i][3].getText()));
                        }
                        hammerSet = acc.getSecondaryHammer();
                    }

                    store.updateAccount(acc);

                    int index = store.getAccountGroup().getAccounts().indexOf(acc);
                    String[] newData = acc.getColumnData();
                    for (int i = 0; i < model.getColumnCount(); i++) {
                        model.setValueAt(newData[i], index, i);
                    }

                    this.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(owner, "Save Failed " + ex.getMessage());
                }
            });
            mainPane.add(saveBtn, BorderLayout.SOUTH);

            this.add(mainPane);
            this.setTitle("Account " + acc.getId());
            this.pack();
            this.setLocationRelativeTo(this);
            this.setResizable(true);
            this.setVisible(true);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }


}
