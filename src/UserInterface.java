import com.android.ddmlib.*;
import dispatcher.EventDispatcher;
import game.GameInstance;
import jdk.nashorn.internal.scripts.JD;
import store.Account;
import store.BuildHammer;
import store.Store;
import util.FilePath;
import util.Logger;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class UserInterface extends JPanel  {


    private Store store;
    private boolean debug;
    private GameInstance gameInstance;
    private IDevice device;
    private AndroidDebugBridge bridge;
    private JFrame owner;
    private JButton activeOrCloseBTN;
    private String tag;

    public UserInterface(JFrame owner, boolean debug, AndroidDebugBridge bridge, String tag) {
        this.tag = tag;
        this.owner = owner;
        this.debug = debug;
        this.bridge = bridge;
        this.setLayout(new BorderLayout());

        activeOrCloseBTN = new JButton("Active");
        activeOrCloseBTN.setPreferredSize(new Dimension(600, 600));

        this.add(activeOrCloseBTN, BorderLayout.CENTER);

        activeOrCloseBTN.addActionListener(e->{
            if(activeOrCloseBTN.getText().equalsIgnoreCase("Active")){
                this.removeAll();
                if(active()) {
                    this.add(activeOrCloseBTN, BorderLayout.NORTH);
                    activeOrCloseBTN.setText("Close");
                    activeOrCloseBTN.setPreferredSize(null);
                }
            }else{
               closeInstance();
            }
            owner.pack();
            owner.repaint();
        });
    }

    private boolean active(){
        if(connectToDevice()) {
            gameInstance = new GameInstance(store, debug);
                System.out.println("active interface");
                this.add(createFarmPanel(), BorderLayout.CENTER);

            if(debug)
                gameInstance.start();
            return true;
        }
        return false;
    }

    private boolean connectToDevice() {
        try {
            store = new Store(tag);
            final JTextField ip = new JTextField(store.metadata.getIp());
            JFileChooser chooser = new JFileChooser();
            chooser.setControlButtonsAreShown(false);
            chooser.setCurrentDirectory(new File( store.metadata.getAccountPath()));
        //    chooser.setFileHidingEnabled(false);
            chooser.setDialogTitle("Choose Account Directory");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            JPanel myPanel = new JPanel(new BorderLayout());
            JPanel ipPanel = new JPanel(new GridLayout(1, 1));
            JPanel chooserPanel = new JPanel();

            ipPanel.add(ip);
            ipPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "Emulator IP"));

            chooserPanel.add(chooser);
            chooserPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "Account Directory Path"));

            myPanel.setLayout(new BorderLayout());
            myPanel.add(ipPanel, BorderLayout.NORTH);
            myPanel.add(chooserPanel, BorderLayout.CENTER);

            do {
                int result = JOptionPane.showConfirmDialog(myPanel, myPanel,
                        "Setup", JOptionPane.OK_CANCEL_OPTION);

                if (result != JOptionPane.OK_OPTION) {
                    closeInstance();
                    return false;
                }

                EventDispatcher.exec("adb connect " + ip.getText(), s -> false);
                EventDispatcher.execADBIP( ip.getText(), "logcat -c", s -> false);
                Logger.log("Account path: "+chooser.getCurrentDirectory().getAbsolutePath()+"\\");
                Thread.sleep(1000);

            } while (!bridge.hasInitialDeviceList() || bridge.getDevices().length <= 0);

            for(IDevice tempDevice:bridge.getDevices()){
                if(tempDevice.getName().contains(ip.getText())){
                    device = tempDevice;
                }
            }

            if(device == null){
                JOptionPane.showMessageDialog(null, "Device not found! Retry");
                closeInstance();
                return false;
            }

            this.store.metadata.setAccountPath(chooser.getCurrentDirectory().getAbsolutePath()+"\\");
            this.store.metadata.setIp(ip.getText());
            this.store.marshellMetadata();
                System.out.println("push event");
                EventDispatcher.execADBIP(store.metadata.getIp(), "push \""+FilePath.EVENTS_PATH+"\" /sdcard/" , s->{
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
        activeOrCloseBTN.setPreferredSize(new Dimension(600, 600));
    }

    private JPanel createFarmPanel() {
        final JPanel panel = new JPanel(new BorderLayout());

        final JPanel actionPane = new JPanel(new GridLayout(2, 4));
        final JPanel topPane = new JPanel(new BorderLayout());
        final JButton actionBtn = new JButton("Start");

        final JButton posMode = new JButton("Position Mode");
        final JButton createBtn = new JButton("Add Account");
        final JButton gotoBtn = new JButton("Go Into");
        final JButton currBtn = new JButton("Current");
        final JButton deleteBtn = new JButton("Delete account");
        final JButton delay = new JButton("Delay");
        final JPanel featurePane = new JPanel(new BorderLayout());
        final JPanel featureCBPane = new JPanel(new GridLayout(2,6));

        currBtn.addActionListener(e->{
            StringBuilder builder = new StringBuilder();
            EventDispatcher.execADBIP(
                    store.metadata.getIp(),
                    " shell content query --uri content://settings/secure --where \"name=\\'android_id\\'\"",
                    s->{
                        builder.append(s);
                        return false;
                    });
            JOptionPane.showMessageDialog(null, builder.toString());
        });

        // Column Names
        DefaultTableModel model = new DefaultTableModel(store.getAccountGroup().getTableData(), Account.Columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                //all cells false
                return false;
            }
        };
        final JTable table = new JTable(model){
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component component = super.prepareRenderer(renderer, row, column);
                int rendererWidth = component.getPreferredSize().width;
                TableColumn tableColumn = getColumnModel().getColumn(column);
                tableColumn.setPreferredWidth(Math.max(rendererWidth + getIntercellSpacing().width, tableColumn.getPreferredWidth()));
                return component;
            }
        };

        table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() == 2) {     // to detect doble click events
                    JTable target = (JTable)me.getSource();
                    int row = target.getSelectedRow(); // select a row
                    Account acc= store.getAccountGroup().getAccount(row);
                    displayAccountDialog(acc, model);
                }
            }
        });



        gotoBtn.addActionListener(e->{
            if(gotoBtn.getText().equals("Go Into")) {
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
            }else{
                store.getAccountGroup().setIndex(table.getSelectedRow());
                store.setForceStop(true);
            }
        });

        createBtn.addActionListener(e -> {

            try {
                JTextField accountID = new JTextField("");
                JComboBox<String> hordeList = new JComboBox<>(Account.Hordes);
                JTextField serverID=  new JTextField("519");
                JTextField clan = new JTextField("");

                JPanel createPanel = new JPanel(new GridLayout(4,2));
                createPanel.add(new JLabel("Account id (optional): "));
                createPanel.add(accountID);
                createPanel.add(new JLabel("server id: "));
                createPanel.add(serverID);
                createPanel.add(new JLabel("horde: "));
                createPanel.add(hordeList);
                createPanel.add(new JLabel("clan name (optional): "));
                createPanel.add(clan);

                int result = JOptionPane.showConfirmDialog(this, createPanel,
                        "Create", JOptionPane.OK_CANCEL_OPTION);
                if(result == JOptionPane.OK_OPTION) {
                    Account acc = new Account( Integer.parseInt(serverID.getText()), !accountID.getText().equalsIgnoreCase("") ? accountID.getText() : store.createNewID());
                    acc.setServerID( Integer.parseInt(serverID.getText()));
                    acc.setHorde(hordeList.getSelectedIndex());
                    acc.setClan(clan.getText());
                    store.addAccount(acc);

                    model.addRow(acc.getColumnData());
                }
            }
            catch(NumberFormatException ignore){
                JOptionPane.showMessageDialog(null, "Invalid server ID");
            }
        });

        gameInstance.setAccountUpdateListener(acc -> {
            int index = store.getAccountGroup().getAccounts().indexOf(acc);
            topPane.setBorder(BorderFactory.createTitledBorder("Current: "+acc.getSubId()));
            String[] newData = acc.getColumnData();
            for (int i = 0; i < model.getColumnCount(); i++) {
                model.setValueAt(newData[i], index, i);
            }
        });

        actionBtn.addActionListener(e->{
            switch (actionBtn.getText()) {
                case "Start":
                    if (!store.getAccountGroup().isEmpty()) {
                        topPane.remove(posMode);
                        gotoBtn.setText("Jump Into");
                        panel.revalidate();
                        panel.repaint();
                        actionPane.revalidate();
                        topPane.revalidate();
                        this.revalidate();
                        int startIndex = table.getSelectedRow();
                        if(startIndex == -1){
                            startIndex = 0;
                        }
                        topPane.setBorder(BorderFactory.createTitledBorder("Current: #"+(startIndex+1)));

                        actionBtn.setText("PAUSE");
                        store.getAccountGroup().setIndex(startIndex);
                        gameInstance.start();
                    } else {
                        JOptionPane.showMessageDialog(null, "You dont have any account");
                    }
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
                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure to delete this account?", "Warning",
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

        delay.addActionListener(e->{
            try {
                String str = JOptionPane.showInputDialog(null, "Delay",store.getDelay()+"" );
                if(!str.equalsIgnoreCase(""))
                    store.setDelay(Integer.parseInt(str));
            }
            catch(NumberFormatException ignored){
                JOptionPane.showMessageDialog(null,"Number only");
            }
        });

        final JButton resetError = new JButton("Reset Error");
        resetError.addActionListener(e->{
            int index = 0;
            for(Account acc: store.getAccountGroup().getAccounts()){
                acc.setError(0);
                store.updateAccount(acc);
                String[] newData = acc.getColumnData();
                for (int i = 0; i < model.getColumnCount(); i++) {
                    model.setValueAt(newData[i], index, i);
                }
                index++;
            }
            JOptionPane.showMessageDialog(null,"Reset all error");
        });


        actionPane.add(createBtn);
        actionPane.add(deleteBtn);
        actionPane.add(gotoBtn);
        actionPane.add(currBtn);
        actionPane.add(delay);
        actionPane.add(resetError);


        ItemListener featureListener = e -> {
            store.metadata.getFeatureToggler().set(((JCheckBox)e.getItem()).getText(), e.getStateChange() == ItemEvent.SELECTED);
            store.marshellMetadata();
        };
        for(Map.Entry<String, Boolean> entry: store.metadata.getFeatureToggler().getFeatures().entrySet()){
            JCheckBox cb = new JCheckBox(entry.getKey(), entry.getValue());
            cb.addItemListener(featureListener);
            featureCBPane.add(cb);
        }
        featurePane.add(featureCBPane, BorderLayout.CENTER);

        final JPanel featureActionPane = new JPanel();
        final JButton setSelectedFeatureBtn = new JButton("Set Selected");
        final JButton setAllFeatureBtn = new JButton("Set All");
        featureActionPane.add(setSelectedFeatureBtn);
        featureActionPane.add(setAllFeatureBtn);
        featurePane.add(featureActionPane, BorderLayout.SOUTH);

        setSelectedFeatureBtn.addActionListener(e->{
            int start = table.getSelectedRow();
            int end = table.getSelectionModel().getMaxSelectionIndex();
            if(start != -1){
                for(;start<=end;start++){
                    Account acc = store.getAccountGroup().getAccount(start);
                    acc.getFeatureToggler().cloneFeature(store.metadata.getFeatureToggler());
                    String[] newData = acc.getColumnData();
                    for (int i = 0; i < model.getColumnCount(); i++) {
                        model.setValueAt(newData[i], start, i);
                    }
                    store.updateAccount(acc);
                }
            }
        });

        setAllFeatureBtn.addActionListener(e->{
            int index = 0;
            for(Account acc : store.getAccountGroup().getAccounts()){
                acc.getFeatureToggler().cloneFeature(store.metadata.getFeatureToggler());
                String[] newData = acc.getColumnData();
                for (int i = 0; i < model.getColumnCount(); i++) {
                    model.setValueAt(newData[i], index, i);
                }
                store.updateAccount(acc);
                index++;
            }
        });

        actionBtn.setBackground(Color.DARK_GRAY);
        actionBtn.setForeground(Color.WHITE);
        actionBtn.setFont(new Font("Arial", Font.ITALIC, 16));
        actionBtn.setPreferredSize(new Dimension(100, 60));
        topPane.setBorder(BorderFactory.createTitledBorder("Current: None"));
        topPane.add(actionBtn, BorderLayout.WEST);
        topPane.add(posMode, BorderLayout.EAST);
        topPane.add(actionPane, BorderLayout.CENTER);

        JScrollPane sp = new JScrollPane(table);
        sp.setViewportView(table);
        panel.add(topPane, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);


        JPanel botPane = new JPanel(new BorderLayout());

        featurePane.setBorder(BorderFactory.createTitledBorder(""));

        JPanel priorityPane = new JPanel();
        for(Map.Entry<String, Integer> entry: store.metadata.getGatherPriorities().entrySet()){
            JTextField tf = new JTextField(String.valueOf(entry.getValue()),2);
            tf.setPreferredSize(new Dimension(70,25));



            tf.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent ke) {
                    if(ke.getKeyCode() == KeyEvent.VK_BACK_SPACE){
                        tf.setEditable(true);
                        if(tf.getText().length() == 1 || tf.getText().length() == 0){
                            tf.setText("0");
                            store.metadata.setGatherPriority(entry.getKey(), 0);;
                        }else{
                            store.metadata.setGatherPriority(entry.getKey(),Integer.parseInt(tf.getText().substring(0, tf.getText().length()-1)));
                        }
                        store.marshellMetadata();
                    }
                    else if (ke.getKeyChar() >= '0' && ke.getKeyChar() <= '9') {
                        store.metadata.setGatherPriority(entry.getKey(), Integer.parseInt(tf.getText()+ ke.getKeyChar()));
                        store.marshellMetadata();
                        tf.setEditable(true);
                    }else{
                        tf.setEditable(false);
                    }
                }
            });
            addLabelTextField(entry.getKey(), tf, priorityPane);
        }


        final JPanel priorityPaneWrapper = new JPanel(new BorderLayout());
        final JPanel priorityAction = new JPanel();
        final JButton setSelectedPriorityBtn = new JButton("Set Selected");
        final JButton setAllPriorityBtn = new JButton("Set All");
        final JButton resetAllPriorityBtn = new JButton("Reset All");

        priorityAction.add(setSelectedPriorityBtn);
        priorityAction.add(setAllPriorityBtn);
        priorityAction.add(resetAllPriorityBtn);
        priorityPaneWrapper.add(priorityPane, BorderLayout.CENTER);
        priorityPaneWrapper.add(priorityAction, BorderLayout.SOUTH);

        resetAllPriorityBtn.addActionListener(e->{
            store.metadata.populateGatherPriority();
            store.marshellMetadata();
            for(Account acc: store.getAccountGroup().getAccounts()){
                acc.populateGatherPriority();
                store.updateAccount(acc);
            }
            setAllPriorityBtn.doClick();
        });


        setSelectedPriorityBtn.addActionListener(e->{
            int start = table.getSelectedRow();
            int end = table.getSelectionModel().getMaxSelectionIndex();
            if(start != -1){
                for(;start<=end;start++){
                    Account acc = store.getAccountGroup().getAccount(start);
                    for(Map.Entry<String,Integer> entry:store.metadata.getGatherPriorities().entrySet()){
                        acc.setGatherPriority(entry.getKey(), entry.getValue());
                    }
                    String[] newData = acc.getColumnData();
                    for (int i = 0; i < model.getColumnCount(); i++) {
                        model.setValueAt(newData[i], start, i);
                    }
                    store.updateAccount(acc);
                }
            }
        });

        setAllPriorityBtn.addActionListener(e->{
            int index = 0;
            for(Account acc : store.getAccountGroup().getAccounts()){
                for(Map.Entry<String,Integer> entry:store.metadata.getGatherPriorities().entrySet()){
                    acc.setGatherPriority(entry.getKey(), entry.getValue());
                }
                String[] newData = acc.getColumnData();
                for (int i = 0; i < model.getColumnCount(); i++) {
                    model.setValueAt(newData[i], index, i);
                }
                store.updateAccount(acc);
                index++;
            }
        });


        botPane.add(featurePane, BorderLayout.CENTER);
        botPane.add(priorityPaneWrapper,BorderLayout.SOUTH);
        panel.add(botPane, BorderLayout.SOUTH);

        posMode.addActionListener(e->{
            panel.remove(sp);
            panel.remove(botPane);
            panel.remove(topPane);
            actionBtn.setText("PAUSE")  ;
            panel.add(new JLabel("Web Interface............... wztechs.com/brutalage_controller", SwingConstants.CENTER), BorderLayout.NORTH);
            panel.add(actionBtn, BorderLayout.CENTER);
            panel.add(delay, BorderLayout.EAST);
            panel.revalidate();
            store.createRemoteWS();
            gameInstance.start();
        });

        System.out.println("interface added");
        return panel;
    }

    private JTextField NullableTextField(Object obj){
        if(obj == null){
            return new JTextField();
        }
        return new JTextField(obj.toString());
    }

    private void addLabelTextField(String label, JTextField textfield, JPanel panel){
        panel.add(new JLabel(label));
        panel.add(textfield);
    }

    private void displayAccountDialog(Account acc, DefaultTableModel model) {
        try {
            final JDialog dialog = new JDialog(owner, true);

            ItemListener featureListener = e -> {
                acc.getFeatureToggler().set(((JCheckBox)e.getItem()).getText(), e.getStateChange() == ItemEvent.SELECTED);
            };

            final JPanel topPane = new JPanel(new BorderLayout());
            final JPanel featurePane = new JPanel(new GridLayout(2,5));
            featurePane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "Features"));
            final HashMap<String, JCheckBox> featureCBs = new HashMap<>();
            for(Map.Entry<String, Boolean> entry: acc.getFeatureToggler().getFeatures().entrySet()){
                JCheckBox cb = new JCheckBox(entry.getKey(), entry.getValue());
                cb.addItemListener(featureListener);
                featureCBs.put(entry.getKey(), cb);
                featurePane.add(cb);
            }

            final JPanel priorityPane = new JPanel();
            for(Map.Entry<String, Integer> entry: acc.getGatherPriorities().entrySet()){
                JTextField tf = new JTextField(String.valueOf(entry.getValue()),2);
                tf.setPreferredSize(new Dimension(70,25));
                tf.addKeyListener(new KeyAdapter() {
                    public void keyPressed(KeyEvent ke) {
                        if(ke.getKeyCode() == KeyEvent.VK_BACK_SPACE){
                            tf.setEditable(true);
                            if(tf.getText().length() == 1 || tf.getText().length() == 0){
                                tf.setText("0");
                                acc.setGatherPriority(entry.getKey(), 0);;
                            }else{
                                acc.setGatherPriority(entry.getKey(),Integer.parseInt(tf.getText().substring(0, tf.getText().length()-1)));
                            }
                            store.updateAccount(acc);
                        }
                        else if (ke.getKeyChar() >= '0' && ke.getKeyChar() <= '9') {
                            acc.setGatherPriority(entry.getKey(), Integer.parseInt(tf.getText()+ ke.getKeyChar()));
                            store.updateAccount(acc);
                            tf.setEditable(true);
                        }else{
                            tf.setEditable(false);
                        }
                    }
                });
                addLabelTextField(entry.getKey(), tf, priorityPane);
            }

            topPane.setBorder(BorderFactory.createTitledBorder(""));
            topPane.add(priorityPane, BorderLayout.NORTH);
            topPane.add(featurePane, BorderLayout.CENTER);


            final JCheckBox changedServerCB = new JCheckBox("Changed Server", acc.getChangedServer());
            final JCheckBox finishInitCB = new JCheckBox("Finished Init", acc.isFinishInit());
            final JCheckBox joinedClanCB = new JCheckBox("Joined Clan", acc.isJoinClan());
            final JCheckBox isRandomizeCB = new JCheckBox("Is Randomized", acc.isRandomized());
            final JTextField serverIDField = NullableTextField(acc.getServerID());
            final JTextField hordeField = NullableTextField(acc.getHordeLabel());
            final JTextField clanField = NullableTextField(acc.getClan());
            final JTextField levelField = NullableTextField(acc.getLevel());
            final JTextField errorField = NullableTextField(acc.getError());
            final JTextField lastGiftTimeField = NullableTextField(acc.getLastGiftTime());
            final JTextField lastRoundField = NullableTextField(acc.getLastRound());

            final JPanel metaPanel = new JPanel(new GridLayout(9, 2));
            metaPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "Account Data"));
            metaPanel.add(changedServerCB);
            metaPanel.add(finishInitCB);
            metaPanel.add(joinedClanCB);
            metaPanel.add(isRandomizeCB);
            addLabelTextField("Server ID: ", serverIDField, metaPanel);
            addLabelTextField("Horde: ", hordeField, metaPanel);
            addLabelTextField("Clan: ", clanField, metaPanel);
            addLabelTextField("Level: ", levelField, metaPanel);
            addLabelTextField("Error: ", errorField, metaPanel);
            addLabelTextField("Last Gift Time: ", lastGiftTimeField, metaPanel);
            addLabelTextField("Last Round Time: ", lastRoundField, metaPanel);


            final JPanel rssPanel = new JPanel(new GridLayout(acc.getResources().size(), 2));
            rssPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "Resources"));
            HashMap<String, JTextField> resourcesFields = new HashMap<>();

            for(Map.Entry<String, Integer> entry: acc.getResources().entrySet()){
                JTextField field = NullableTextField(entry.getValue());
                resourcesFields.put(entry.getKey(), field);
                addLabelTextField(entry.getKey(), field, rssPanel);
            }

            final JPanel buildingPanel = new JPanel(new GridLayout(acc.getBuildings().size(), 2));
            buildingPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "Buildings"));
            HashMap<String, JTextField> buildingFields = new HashMap<>();
            for(Map.Entry<String, Integer> entry: acc.getBuildings().entrySet()){
                JTextField field = NullableTextField(entry.getValue());
                buildingFields.put(entry.getKey(), field);
                addLabelTextField(entry.getKey(), field, buildingPanel);
            }

            final JPanel[] hammerPanels = new JPanel[2];
            JTextField[][] hammerFields = new JTextField[2][4];
            BuildHammer hammer = acc.getPrimaryHammer();
            for(int i =0 ; i<2; i++) {
                hammerPanels[i] = new JPanel(new GridLayout(4, 2));
                hammerFields[i][0] = NullableTextField(hammer.getBuildingName());
                hammerFields[i][1] = NullableTextField(hammer.getNextBuildingLevel());
                hammerFields[i][2] = NullableTextField(hammer.getHammer());
                hammerFields[i][3] = NullableTextField(hammer.getExpiration());
                addLabelTextField("Building Name: ",  hammerFields[i][0], hammerPanels[i]);
                addLabelTextField("Next Level: ",  hammerFields[i][1], hammerPanels[i]);
                addLabelTextField("Complete TIme: ",  hammerFields[i][2], hammerPanels[i]);
                addLabelTextField("Expiration: ",  hammerFields[i][3], hammerPanels[i]);
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
            saveBtn.addActionListener(e->{
                try{

                    acc.setChangedServer(changedServerCB.isSelected());
                    acc.setFinishInit(finishInitCB.isSelected());
                    acc.setJoinClan(joinedClanCB.isSelected());
                    acc.setServerID(Integer.parseInt(serverIDField.getText()));
                    acc.setRandomized(isRandomizeCB.isSelected());
                    acc.setHorde(hordeField.getText());
                    acc.setClan(clanField.getText());
                    acc.setLevel(Integer.parseInt(levelField.getText()));
                    acc.setError(Integer.parseInt(errorField.getText()));
                    acc.setLastGiftTime(LocalDateTime.parse(lastGiftTimeField.getText()));
                    acc.setLastRound(LocalDateTime.parse(lastRoundField.getText()));

                    for(Map.Entry<String, JCheckBox> entry:featureCBs.entrySet()){
                        acc.getFeatureToggler().set(entry.getKey(), entry.getValue().isSelected());
                    }

                    for(Map.Entry<String, JTextField> entry:resourcesFields.entrySet()){
                        acc.setResource(entry.getKey(), Integer.parseInt(entry.getValue().getText()));
                    }

                    for(Map.Entry<String, JTextField> entry:buildingFields.entrySet()){
                        acc.setBuildingLevel(entry.getKey(), Integer.parseInt(entry.getValue().getText()));
                    }

                    BuildHammer hammerSet = acc.getPrimaryHammer();
                    for(int i =0 ; i<2; i++) {
                        if(!hammerFields[i][0].getText().equalsIgnoreCase("")){
                            hammerSet.setBuildingName(hammerFields[i][0].getText());
                        }
                        if(!hammerFields[i][1].getText().equalsIgnoreCase("")){
                            hammerSet.setNextBuildingLevel(Integer.parseInt(hammerFields[i][1].getText()));
                        }
                        if(!hammerFields[i][2].getText().equalsIgnoreCase("")){
                            hammerSet.setHammer(LocalDateTime.parse(hammerFields[i][2].getText()));
                        }
                        if(!hammerFields[i][3].getText().equalsIgnoreCase("")){
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
                }
                catch (Exception ex){
                    JOptionPane.showMessageDialog(null,"Save Failed "+ex.getMessage());
                }
            });
            mainPane.add(saveBtn, BorderLayout.SOUTH);

            dialog.add(mainPane);
            dialog.setTitle("Account " + acc.getId());
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setResizable(true);
            dialog.setVisible(true);
        }
        catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }


}
