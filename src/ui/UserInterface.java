package ui;

import com.android.ddmlib.*;
import dispatcher.EventDispatcher;
import game.GameInstance;
import store.Account;
import store.BuildHammer;
import store.Store;
import util.Global;
import util.Logger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class UserInterface extends JPanel {

    private final static Pattern currIDPattern = Pattern.compile(", value=(.*)$");

    private JPanel featureCBPane;
    private JPanel globalFeaturePane;
    private JPanel numberPaneWrapper;
    private JPanel priorityPaneWrapper;
    private JComboBox<String> emulatorSelect;
    private Store store;
    private boolean debug;
    private GameInstance gameInstance;
    private IDevice device;
    private AndroidDebugBridge bridge;
    private JFrame owner;
    private JButton activeOrCloseBTN;
    private String tag;
    private boolean isPosMode = Global.OnlyPosMode;

    public UserInterface(JFrame owner, boolean debug, AndroidDebugBridge bridge, String tag) {
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



    private JComboBox<String> getEmulatorSelectBox (){
        ArrayList<String> noxes = null;
        if(store.metadata.isUseNox()){
             noxes = Global.FindNoxes();
        }

        if(noxes != null){
            return new JComboBox<>(noxes.toArray(new String[0]));
        }else{
            ArrayList<String> devices = new ArrayList<>();
            for(IDevice device: bridge.getDevices()){
                devices.add(device.getName());
            }
            return new JComboBox<>(devices.toArray(new String[0]));
        }
    };

    private boolean connectToDevice() {
        try {
            store = new Store(tag, bridge);


            emulatorSelect = getEmulatorSelectBox();

            emulatorSelect.setSelectedItem(this.store.metadata.getSelectedEmulator());

            JPanel myPanel = new JPanel(new BorderLayout());
            myPanel.setLayout(new BorderLayout());

            JPanel emulatorPanel = new JPanel(new GridLayout(2, 1));

            JButton useEmulatorBtn = new JButton("Use Other");

            useEmulatorBtn.addActionListener(e->{
                if(store.metadata.isUseNox()){
                    useEmulatorBtn.setText("Use Nox");
                }else{
                    useEmulatorBtn.setText("Use Other");
                }
                store.metadata.setUseNox(!store.metadata.isUseNox());
                emulatorPanel.remove(emulatorSelect);
                emulatorSelect = getEmulatorSelectBox();
                emulatorPanel.add(emulatorSelect);
            });

            emulatorPanel.add(useEmulatorBtn);
            emulatorPanel.add(emulatorSelect);
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

            String selectedEmulator = (String) emulatorSelect.getSelectedItem();
            String ip = "";

            if(store.metadata.isUseNox()){
                EventDispatcher.exec(Global.config.getNoxPath() + "/Nox.exe -clone:" + selectedEmulator, null);

                int redo = 0;
                String port = Global.getNoxPort(selectedEmulator);

                if (port.equalsIgnoreCase("")) {
                    JOptionPane.showMessageDialog(owner, "Device not found! Retry");
                    closeInstance();
                    return false;
                }

                ip = "127.0.0.1:" + port;
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
            }else{
                ip = selectedEmulator;
                for (IDevice tempDevice : bridge.getDevices()) {
                    if (tempDevice.getName().contains(ip)) {
                        Logger.log("Connected!");
                        device = tempDevice;
                        break;
                    }
                }

                if(device == null){
                    JOptionPane.showMessageDialog(owner, "Device not found! Retry");
                    closeInstance();
                    return false;
                }
            }

            EventDispatcher.execADBIP(ip, "logcat -c", s -> false);

            if (!Global.OnlyPosMode) {
                Logger.log("Account path: " + chooser.getCurrentDirectory().getAbsolutePath() + "\\");
                this.store.metadata.setAccountPath(chooser.getCurrentDirectory().getAbsolutePath() + "\\");
            }
            this.store.metadata.setIp(ip);
            this.store.metadata.setSelectedEmulator(selectedEmulator);
            this.store.marshellMetadata();
            System.out.println("push event");
            EventDispatcher.execADBIP(store.metadata.getIp(), "shell rm -r /sdcard/"+Global.config.getEventFolder(), s -> {
                Logger.log(s);
                return false;
            });

            EventDispatcher.execADBIP(store.metadata.getIp(), "shell ime set com.android.adbkeyboard/.AdbIME", s -> {
                Logger.log(s);
                return false;
            });

            EventDispatcher.execADBIP(store.metadata.getIp(), "push \"" + Global.config.getEventFolderPath() + "\" /sdcard/", s -> {
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


    private void createPanes(JTable table, DefaultTableModel model){
        globalFeaturePane = new GlobalFeaturePane(store);
        featureCBPane = new CBFeaturePane(store, table, model, owner);
        numberPaneWrapper = new NumberPane(store, table, model, owner);
        priorityPaneWrapper = new PriorityPane(store, table, model, owner);
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

        StringBuilder currentIDBuilder = new StringBuilder();
        EventDispatcher.execADBIP(
                store.metadata.getIp(),
                " shell content query --uri content://settings/secure --where \"name=\\'android_id\\'\"",
                s -> {
                    Matcher m = currIDPattern.matcher(s);
                    if(m.find()){
                        currentIDBuilder.append(m.group(1));
                        return true;
                    }
                    return false;
                });

        String currID = currentIDBuilder.toString();


        // Column Names
        DefaultTableModel model = new DefaultTableModel(store.getAccountGroup().getTableData(), Account.Columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                //all cells false
                return false;
            }
        };
        final JTable table = new JTable(model);;

        int currAccountIndex = 0;
        for(int i=0; i< store.getAccountGroup().getAccounts().size(); i++){
            if(store.getAccountGroup().getAccount(i).getId().equalsIgnoreCase(currID)){
                currAccountIndex = i;
                break;
            }
        }

        if(!store.getAccountGroup().getAccounts().isEmpty())
            table.setRowSelectionInterval(currAccountIndex, currAccountIndex);

        //fix col width
        /* {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component component = super.prepareRenderer(renderer, row, column);
                int rendererWidth = component.getPreferredSize().width;
                TableColumn tableColumn = getColumnModel().getColumn(column);
                tableColumn.setPreferredWidth(Math.max(rendererWidth + getIntercellSpacing().width, tableColumn.getPreferredWidth()));
                return component;
            }
        };
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);*/

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() == 2) {     // to detect doble click events
                    JTable target = (JTable) me.getSource();
                    int row = target.getSelectedRow(); // select a row
                    Account acc = store.getAccountGroup().getAccount(row);
                    new AccountDialog(store, owner, acc, model);
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
        topPane.setBorder(BorderFactory.createTitledBorder("Current: "+currID));
        topPane.add(actionBtn, BorderLayout.WEST);
        topPane.add(posModeCB, BorderLayout.EAST);
        topPane.add(actionPane, BorderLayout.CENTER);

        JScrollPane sp = new JScrollPane(table);
        sp.setViewportView(table);
        panel.add(topPane, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);


        final JPanel botPane = new JPanel(new BorderLayout());

        createPanes(table, model);

        final JButton resetAll = new JButton("Reset All");
        final JPanel northPanel = new JPanel(new GridLayout(2,1));
        northPanel.add(resetAll, BorderLayout.NORTH);
        northPanel.add(new BulkSettingDialog(store, owner, table));

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

            createPanes(table, model);
            botPane.add(northPanel, BorderLayout.NORTH);
            botPane.add(featureCBPane, BorderLayout.WEST);
            botPane.add(numberPaneWrapper, BorderLayout.CENTER);
            botPane.add(priorityPaneWrapper, BorderLayout.EAST);
            botPane.add(globalFeaturePane, BorderLayout.SOUTH);
            botPane.repaint();
            owner.pack();
        });

        botPane.add(northPanel, BorderLayout.NORTH);
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

    private void AddSingleBulkSetting(String type, JPanel container, String label, JTable table, BiFunction<Account, String, Boolean> func){
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



    public static JTextField NullableTextField(Object obj) {
        if (obj == null) {
            return new JTextField();
        }
        return new JTextField(obj.toString());
    }

    public static void addLabelTextField(String label, JTextField textfield, JPanel panel) {
        panel.add(new JLabel(label));
        panel.add(textfield);
    }



}
