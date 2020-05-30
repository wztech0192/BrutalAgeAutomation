import com.android.ddmlib.*;
import dispatcher.EventDispatcher;
import game.GameInstance;
import store.Account;
import store.BuildHammer;
import store.Store;
import util.FilePath;
import util.Global;
import util.Logger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class UserInterface extends JPanel {
    private JPanel featureCBPane;
    private JPanel globalFeaturePane;
    private JPanel numberPaneWrapper;
    private JPanel priorityPaneWrapper;

    private Store store;
    private boolean debug;
    private GameInstance gameInstance;
    private IDevice device;
    private AndroidDebugBridge bridge;
    private JFrame owner;
    private JButton activeOrCloseBTN;
    private String tag;
    private boolean isPosMode = Global.OnlyPosMode;

    private ArrayList<String> noxInstances;

    public UserInterface(JFrame owner, boolean debug, AndroidDebugBridge bridge, String tag, ArrayList<String> noxInstances) {
        this.noxInstances = noxInstances;
        this.tag = tag;
        this.owner = owner;
        this.debug = debug;
        this.bridge = bridge;
        this.setLayout(new BorderLayout());

        activeOrCloseBTN = new JButton("Active");
        activeOrCloseBTN.setPreferredSize(new Dimension(200, 50));

        this.add(activeOrCloseBTN, BorderLayout.CENTER);

        activeOrCloseBTN.addActionListener(e -> {
            switch (activeOrCloseBTN.getText()) {
                case "Active":
                    activeOrCloseBTN.setText("Loading.........");
                    owner.repaint();
                    active();
                    break;
                case "Close":
                    closeInstance();
                    owner.pack();
                    owner.repaint();
                default:
            }
        });
    }

    private void active() {
        new Thread(() -> {
            if (connectToDevice()) {
                this.removeAll();
                gameInstance = new GameInstance(store, debug);
                System.out.println("active interface");
                this.add(createFarmPanel(), BorderLayout.CENTER);
                this.add(activeOrCloseBTN, BorderLayout.NORTH);
                activeOrCloseBTN.setText("Close");
                activeOrCloseBTN.setPreferredSize(null);
                if (debug)
                    gameInstance.start();

                owner.pack();
                owner.repaint();
            }
        }).start();
    }

    ;

    private boolean connectToDevice() {
        try {
            store = new Store(tag, bridge);

            final JComboBox<String> noxSelect = new JComboBox<>(noxInstances.toArray(new String[0]));
            noxSelect.setSelectedItem(this.store.metadata.getNox());


            JPanel myPanel = new JPanel(new BorderLayout());
            myPanel.setLayout(new BorderLayout());

            JPanel emulatorPanel = new JPanel(new GridLayout(1, 1));
            emulatorPanel.add(noxSelect);
            emulatorPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "Emulator"));
            JFileChooser chooser = null;
            if (!Global.OnlyPosMode) {
                JPanel chooserPanel = new JPanel();
                chooser = new JFileChooser();
                chooser.setControlButtonsAreShown(false);
                chooser.setCurrentDirectory(new File(store.metadata.getAccountPath()));
                chooser.setDialogTitle("Choose Account Directory");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooserPanel.add(chooser);
                chooserPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "Account Directory Path"));
                myPanel.add(chooserPanel, BorderLayout.CENTER);
            }

            myPanel.add(emulatorPanel, BorderLayout.NORTH);

            int result = JOptionPane.showConfirmDialog(myPanel, myPanel,
                    "Setup", JOptionPane.OK_CANCEL_OPTION);

            if (result != JOptionPane.OK_OPTION) {
                closeInstance();
                return false;
            }

            String selectedNox = (String) noxSelect.getSelectedItem();
            EventDispatcher.exec(Global.config.getNoxPath() + "/Nox.exe -clone:" + selectedNox, null);


            int redo = 0;
            String port = Global.getNoxPort(selectedNox);

            if (port.equalsIgnoreCase("")) {
                JOptionPane.showMessageDialog(owner, "Device not found! Retry");
                closeInstance();
                return false;
            }

            String ip = "127.0.0.1:" + port;
            while (true) {
                Logger.log("Trying to connected to " + ip + "...attempt " + redo);
                EventDispatcher.exec("adb connect " + ip, null);
                Thread.sleep(1500);
                if (bridge.hasInitialDeviceList()) {
                    for (IDevice tempDevice : bridge.getDevices()) {
                        if (tempDevice.getName().contains(ip)) {
                            Logger.log("Connected!");
                            device = tempDevice;
                            if (redo > 1) {
                                Logger.log("Wait for 12 sec....................");
                                Thread.sleep(12000);
                            }
                            break;
                        }
                    }
                }
                if (device != null) {
                    break;
                }

                redo++;
                if (redo > 8) {
                    JOptionPane.showMessageDialog(owner, "Device not found! Retry");
                    closeInstance();
                    return false;
                }
                Thread.sleep(3000);
            }

            EventDispatcher.execADBIP(ip, "logcat -c", s -> false);

            if (!Global.OnlyPosMode) {
                Logger.log("Account path: " + chooser.getCurrentDirectory().getAbsolutePath() + "\\");
                this.store.metadata.setAccountPath(chooser.getCurrentDirectory().getAbsolutePath() + "\\");
            }
            this.store.metadata.setIp(ip);
            this.store.metadata.setNox(selectedNox);
            this.store.marshellMetadata();
            System.out.println("push event");
            EventDispatcher.execADBIP(store.metadata.getIp(), "push \"" + FilePath.EVENTS_PATH + "\" /sdcard/", s -> {
                Logger.log(s);
                return false;
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        store.init(device);
        return true;

    }

    private void closeInstance() {
        store.close();
        this.removeAll();
        this.add(activeOrCloseBTN, BorderLayout.CENTER);
        activeOrCloseBTN.setText("Active");
        activeOrCloseBTN.setPreferredSize(new Dimension(200, 50));
    }

    private JPanel createGlobalFeaturePane() {
        final JPanel globalPane = new JPanel();
        globalPane.setBorder(BorderFactory.createTitledBorder(""));
        ItemListener featureListener = e -> {
            store.metadata.getFeatureToggler().getGlobalFeatures().put(((JCheckBox) e.getItem()).getText(), e.getStateChange() == ItemEvent.SELECTED);
            store.marshellMetadata();
        };
        for (Map.Entry<String, Boolean> entry : store.metadata.getFeatureToggler().getGlobalFeatures().entrySet()) {
            JCheckBox cb = new JCheckBox(entry.getKey(), entry.getValue());
            cb.addItemListener(featureListener);
            globalPane.add(cb);
        }
        return globalPane;
    }


    private JPanel createCBFeaturePane(JTable table, DefaultTableModel model) {
        final JPanel featureCBPane = new JPanel(new GridLayout(8, 2));
        featureCBPane.setBorder(BorderFactory.createTitledBorder(""));
        ItemListener featureListener = e -> {
            store.metadata.getFeatureToggler().set(((JCheckBox) e.getItem()).getText(), e.getStateChange() == ItemEvent.SELECTED);
            store.marshellMetadata();
        };
        for (Map.Entry<String, Boolean> entry : store.metadata.getFeatureToggler().getFeatures().entrySet()) {
            JCheckBox cb = new JCheckBox(entry.getKey(), entry.getValue());
            cb.addItemListener(featureListener);
            featureCBPane.add(cb);
        }
        final JButton setSelectedFeatureBtn = new JButton("Set Selected");
        final JButton setAllFeatureBtn = new JButton("Set All");
        featureCBPane.add(setSelectedFeatureBtn);
        featureCBPane.add(setAllFeatureBtn);

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
        return featureCBPane;
    }


    private JPanel createNumberPane(JTable table, DefaultTableModel model) {
        final JPanel numberPaneWrapper = new JPanel(new GridLayout(6, 2));
        final HashMap<String, JTextField> numberTextFields = new HashMap<>();
        for (Map.Entry<String, Integer> entry : store.metadata.getNumberFeaturer().getNumberSetting().entrySet()) {
            JTextField tf = new JTextField(String.valueOf(entry.getValue()), 7);
            tf.setPreferredSize(new Dimension(200, 25));
            numberTextFields.put(entry.getKey(), tf);
            addLabelTextField(entry.getKey(), tf, numberPaneWrapper);
        }

        final JButton setSelectedNumberBtn = new JButton("Set Selected");
        final JButton setAllNumberBtn = new JButton("Set All");

        numberPaneWrapper.add(setSelectedNumberBtn);
        numberPaneWrapper.add(setAllNumberBtn);


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
        return numberPaneWrapper;
    }

    private JPanel createPriorityPane(JTable table, DefaultTableModel model) {
        final JPanel priorityPaneWrapper = new JPanel(new GridLayout(6, 2));

        priorityPaneWrapper.setBorder(BorderFactory.createTitledBorder("Priority: (Never) 0 <<<<<< 5 (Always)"));
        final HashMap<String, JTextField> priorityTextfields = new HashMap<>();
        for (Map.Entry<String, Integer> entry : store.metadata.getNumberFeaturer().getGatherPriorities().entrySet()) {
            JTextField tf = new JTextField(String.valueOf(entry.getValue()), 7);
            tf.setPreferredSize(new Dimension(200, 25));
            priorityTextfields.put(entry.getKey(), tf);
            addLabelTextField(entry.getKey(), tf, priorityPaneWrapper);
        }


        final JButton setSelectedPriorityBtn = new JButton("Set Selected");
        final JButton setAllPriorityBtn = new JButton("Set All");
        priorityPaneWrapper.add(setSelectedPriorityBtn);
        priorityPaneWrapper.add(setAllPriorityBtn);

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
        return priorityPaneWrapper;
    }


    private JPanel createFarmPanel() {
        final JPanel panel = new JPanel(new BorderLayout());

        final JPanel actionPane = new JPanel(new GridLayout(2, 4));
        final JPanel topPane = new JPanel(new BorderLayout());
        final JButton actionBtn = new JButton("Start");

        final JButton createBtn = new JButton("Add Account");
        final JButton gotoBtn = new JButton("Go Into");
        final JButton currBtn = new JButton("Current");
        final JButton deleteBtn = new JButton("Delete account");
        final JButton delay = new JButton("Delay");


        final JCheckBox posModeCB = new JCheckBox("Position Mode", store.isPositionMode());
        posModeCB.addActionListener(e->{
            if(!store.isPositionMode()) {
                store.createRemoteWS();
            }else{
                store.closeRemoteWS();
            }
        });



        final JButton link = new JButton("Go to Web Interface: www.wztechs.com/brutalage_controller");
        link.addActionListener(be -> {
            Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(new URI("http://www.wztechs.com/brutalage_controller"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        currBtn.addActionListener(e -> {
            StringBuilder builder = new StringBuilder();
            EventDispatcher.execADBIP(
                    store.metadata.getIp(),
                    " shell content query --uri content://settings/secure --where \"name=\\'android_id\\'\"",
                    s -> {
                        builder.append(s);
                        return false;
                    });
            JOptionPane.showMessageDialog(owner, builder.toString());
        });

        // Column Names
        DefaultTableModel model = new DefaultTableModel(store.getAccountGroup().getTableData(), Account.Columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                //all cells false
                return false;
            }
        };
        final JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component component = super.prepareRenderer(renderer, row, column);
                int rendererWidth = component.getPreferredSize().width;
                TableColumn tableColumn = getColumnModel().getColumn(column);
                tableColumn.setPreferredWidth(Math.max(rendererWidth + getIntercellSpacing().width, tableColumn.getPreferredWidth()));
                return component;
            }
        };

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() == 2) {     // to detect doble click events
                    JTable target = (JTable) me.getSource();
                    int row = target.getSelectedRow(); // select a row
                    Account acc = store.getAccountGroup().getAccount(row);
                    displayAccountDialog(acc, model);
                }
            }
        });


        gotoBtn.addActionListener(e -> {
            if (gotoBtn.getText().equals("Go Into")) {
                int i = table.getSelectedRow();
                if (i != -1) {
                    Account acc = store.getAccountGroup().getAccount(i);
                    topPane.setBorder(BorderFactory.createTitledBorder("Current: " + acc.getSubId()));
                    try {
                        gameInstance.startAccountID(acc.getId());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                store.getAccountGroup().setIndex(table.getSelectedRow());
                store.setForceStop(true);
            }
        });

        createBtn.addActionListener(e -> {

            try {
                JTextField count = new JTextField("1");
                JComboBox<String> hordeList = new JComboBox<>(Account.Hordes);
                hordeList.setSelectedIndex(store.metadata.getHorde());
                JTextField serverID = new JTextField(String.valueOf(store.metadata.getServer()));
                JTextField clan = new JTextField(store.metadata.getClan());

                JPanel createPanel = new JPanel(new GridLayout(4, 2));
                createPanel.add(new JLabel("Create How Many?"));
                createPanel.add(count);
                createPanel.add(new JLabel("server id: "));
                createPanel.add(serverID);
                createPanel.add(new JLabel("horde: "));
                createPanel.add(hordeList);
                createPanel.add(new JLabel("clan name (optional): "));
                createPanel.add(clan);

                int result = JOptionPane.showConfirmDialog(owner, createPanel,
                        "Create", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    int countNum = Integer.parseInt(count.getText());
                    int server = Integer.parseInt(serverID.getText());
                    for(int i=1; i<=countNum; i++){
                        Account acc = new Account(Integer.parseInt(serverID.getText()), store.createNewID()+"_"+i);
                        acc.setServerID(server);
                        acc.setHorde(hordeList.getSelectedIndex());
                        acc.setClan(clan.getText());
                        store.addAccount(acc);
                        model.addRow(acc.getColumnData());
                    }
                    store.metadata.setClan(clan.getText());
                    store.metadata.setServer(server);
                    store.metadata.setHorde(hordeList.getSelectedIndex());
                }
            } catch (NumberFormatException ignore) {
                JOptionPane.showMessageDialog(owner, "Invalid server ID");
            }
        });

        gameInstance.setAccountUpdateListener(acc -> {
            if(acc != null){
                int index = store.getAccountGroup().getAccounts().indexOf(acc);
                topPane.setBorder(BorderFactory.createTitledBorder("Current: " + acc.getSubId()));
                String[] newData = acc.getColumnData();
                for (int i = 0; i < model.getColumnCount(); i++) {
                    model.setValueAt(newData[i], index, i);
                }
            }else{
                topPane.setBorder(BorderFactory.createTitledBorder("Pending "+store.positionQueue.size()+
                        ", stored account "+store.metadata.getSavedPosAcc().size()+"/"+store.metadata.getMaxPosAcc()));
            }
        });

        actionBtn.addActionListener(e -> {
            switch (actionBtn.getText()) {
                case "Start":
                    gotoBtn.setText("Jump Into");
                    this.revalidate();
                    int startIndex = table.getSelectedRow();
                    if (startIndex == -1) {
                        startIndex = 0;
                    }
                    topPane.setBorder(BorderFactory.createTitledBorder("Starting"));
                    actionBtn.setText("PAUSE");
                    store.getAccountGroup().setIndex(startIndex);
                    gameInstance.start();
                    break;
                case "PAUSE":
                    actionBtn.setText("RESUME");
                    store.setPause(true);
                    break;
                case "RESUME":
                    actionBtn.setText("PAUSE");
                    store.setPause(false);
                    break;
            }
        });


        deleteBtn.addActionListener(e -> {
            if (table.getSelectedRow() >= 0) {
                int confirm = JOptionPane.showConfirmDialog(owner, "Are you sure to delete this account?", "Warning",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (confirm == 0) {
                    Account acc = store.getAccountGroup().getAccount(table.getSelectedRow());
                    Logger.log("delete account: " + acc.getId());
                    store.deleteAccount(table.getSelectedRow());
                    model.removeRow(table.getSelectedRow());
                }
            }
        });

        delay.addActionListener(e -> {
            try {
                String str = JOptionPane.showInputDialog(null, "Delay", store.getDelay() + "");
                if (!str.equalsIgnoreCase(""))
                    store.setDelay(Integer.parseInt(str));
            } catch (NumberFormatException ignored) {
                JOptionPane.showMessageDialog(owner, "Number only");
            }
        });

        final JButton resetError = new JButton("Reset Error");
        resetError.addActionListener(e -> {
            int index = 0;
            for (Account acc : store.getAccountGroup().getAccounts()) {
                acc.setError(0);
                store.updateAccount(acc);
                String[] newData = acc.getColumnData();
                for (int i = 0; i < model.getColumnCount(); i++) {
                    model.setValueAt(newData[i], index, i);
                }
                index++;
            }
            JOptionPane.showMessageDialog(owner, "Reset all error");
        });


        actionPane.add(createBtn);
        actionPane.add(deleteBtn);
        actionPane.add(gotoBtn);
        actionPane.add(currBtn);
        actionPane.add(delay);
        actionPane.add(resetError);


        actionBtn.setBackground(Color.DARK_GRAY);
        actionBtn.setForeground(Color.WHITE);
        actionBtn.setFont(new Font("Arial", Font.ITALIC, 16));
        actionBtn.setPreferredSize(new Dimension(100, 60));
        topPane.setBorder(BorderFactory.createTitledBorder("Current: None"));
        topPane.add(actionBtn, BorderLayout.WEST);
        topPane.add(posModeCB, BorderLayout.EAST);
        topPane.add(actionPane, BorderLayout.CENTER);

        JScrollPane sp = new JScrollPane(table);
        sp.setViewportView(table);
        panel.add(topPane, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);


        final JPanel botPane = new JPanel(new BorderLayout());

        globalFeaturePane = createGlobalFeaturePane();
        featureCBPane = createCBFeaturePane(table, model);
        numberPaneWrapper = createNumberPane(table, model);
        priorityPaneWrapper = createPriorityPane(table, model);

        final JButton resetAll = new JButton("Reset All");
        resetAll.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure to reset all?",null, JOptionPane.OK_CANCEL_OPTION);
            if (confirm == JOptionPane.OK_OPTION) {
                store.metadata.resetNumberFeaturer();
                store.metadata.resetFeatureToggler();
                store.marshellMetadata();
                for (Account acc : store.getAccountGroup().getAccounts()) {
                    acc.resetNumberFeaturer();
                    acc.resetFeatureToggler();
                    store.updateAccount(acc);
                }
            }
            botPane.removeAll();
            globalFeaturePane = createGlobalFeaturePane();
            featureCBPane = createCBFeaturePane(table, model);
            numberPaneWrapper = createNumberPane(table, model);
            priorityPaneWrapper = createPriorityPane(table, model);
            botPane.add(resetAll, BorderLayout.NORTH);
            botPane.add(featureCBPane, BorderLayout.WEST);
            botPane.add(numberPaneWrapper, BorderLayout.CENTER);
            botPane.add(priorityPaneWrapper, BorderLayout.EAST);
            botPane.add(globalFeaturePane, BorderLayout.SOUTH);
            botPane.repaint();
            owner.pack();
        });

        botPane.add(resetAll, BorderLayout.NORTH);
        botPane.add(featureCBPane, BorderLayout.WEST);
        botPane.add(numberPaneWrapper, BorderLayout.CENTER);
        botPane.add(priorityPaneWrapper, BorderLayout.EAST);
        botPane.add(globalFeaturePane, BorderLayout.SOUTH);
        panel.add(botPane, BorderLayout.SOUTH);


        if (Global.OnlyPosMode) {
            isPosMode = true;
            panel.removeAll();
            panel.add(link, BorderLayout.NORTH);
            panel.add(actionBtn, BorderLayout.CENTER);
            panel.add(delay, BorderLayout.EAST);
            panel.revalidate();
        }
        System.out.println("interface added");
        return panel;
    }


    private JTextField NullableTextField(Object obj) {
        if (obj == null) {
            return new JTextField();
        }
        return new JTextField(obj.toString());
    }

    private void addLabelTextField(String label, JTextField textfield, JPanel panel) {
        panel.add(new JLabel(label));
        panel.add(textfield);
    }

    private void displayAccountDialog(Account acc, DefaultTableModel model) {
        try {
            final JDialog dialog = new JDialog(owner, true);

            ItemListener featureListener = e -> {
                acc.getFeatureToggler().set(((JCheckBox) e.getItem()).getText(), e.getStateChange() == ItemEvent.SELECTED);
            };

            final JPanel topPane = new JPanel(new BorderLayout());
            final JPanel featurePane = new JPanel(new GridLayout(5, 3));
            featurePane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "Features"));
            final HashMap<String, JCheckBox> featureCBs = new HashMap<>();
            for (Map.Entry<String, Boolean> entry : acc.getFeatureToggler().getFeatures().entrySet()) {
                JCheckBox cb = new JCheckBox(entry.getKey(), entry.getValue());
                cb.addItemListener(featureListener);
                featureCBs.put(entry.getKey(), cb);
                featurePane.add(cb);
            }

            final JPanel numberPane = new JPanel(new GridLayout(5,2));
            final HashMap<String, JTextField> numberTextfield = new HashMap<>();
            for (Map.Entry<String, Integer> entry : acc.getNumberFeaturer().getNumberSetting().entrySet()) {
                JTextField tf = new JTextField(String.valueOf(entry.getValue()), 7);
                tf.setPreferredSize(new Dimension(200, 25));
                numberTextfield.put(entry.getKey(), tf);
                addLabelTextField(entry.getKey(), tf, numberPane);
            }

            final JPanel priorityPane = new JPanel(new GridLayout(5,2));
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
            final JTextField serverIDField = NullableTextField(acc.getServerID());
            final JTextField nameField = NullableTextField( acc.getName());
            final JTextField hordeField = NullableTextField(acc.getHordeLabel());
            final JTextField clanField = NullableTextField(acc.getClan());
            final JTextField levelField = NullableTextField(acc.getLevel());
            final JTextField prevLevelField = NullableTextField(acc.getPreviousLevel());
            final JTextField errorField = NullableTextField(acc.getError());
            final JTextField lastGiftTimeField = NullableTextField(acc.getLastGiftTime());
            final JTextField lastRoundField = NullableTextField(acc.getLastRound());

            final JPanel metaPanel = new JPanel(new GridLayout(11, 2));
            metaPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "Account Data"));
            metaPanel.add(changedServerCB);
            metaPanel.add(finishInitCB);
            metaPanel.add(joinedClanCB);
            metaPanel.add(isRandomizeCB);
            addLabelTextField("Server ID: ", serverIDField, metaPanel);
            addLabelTextField("Horde: ", hordeField, metaPanel);
            addLabelTextField("Clan: ", clanField, metaPanel);
            addLabelTextField("Prev Level: ", prevLevelField, metaPanel);
            addLabelTextField("Level: ", levelField, metaPanel);
            addLabelTextField("Name: ", nameField, metaPanel);
            addLabelTextField("Error: ", errorField, metaPanel);
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

                    dialog.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(owner, "Save Failed " + ex.getMessage());
                }
            });
            mainPane.add(saveBtn, BorderLayout.SOUTH);

            dialog.add(mainPane);
            dialog.setTitle("Account " + acc.getId());
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setResizable(true);
            dialog.setVisible(true);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }


}
