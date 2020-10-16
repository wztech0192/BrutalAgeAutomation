import com.android.ddmlib.*;
import org.opencv.core.Core;
import ui.UserInterface;
import util.FilePath;
import util.Global;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {


    static {
        System.load(FilePath.RootPath + "/" + Core.NATIVE_LIBRARY_NAME + ".dll");
    }

    public static void main(String[] args) {

        if(Global.config.getOwnerName().equalsIgnoreCase("")){
            System.exit(0);
        };
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            Global.PopulateEnvSetting(args);


            AndroidDebugBridge.init(false);
            AndroidDebugBridge bridge = AndroidDebugBridge.createBridge(
                    FilePath.RootPath + "/../adb", false);
            Runtime.getRuntime().addShutdownHook(new Thread(AndroidDebugBridge::disconnectBridge));




            JFrame mFrame = new JFrame();
            JTabbedPane tabbedPane = new JTabbedPane();

            if(Global.DEV_MODE){
                tabbedPane.addTab("dev", new UserInterface(mFrame, bridge, "dev"));
            }else{
                tabbedPane.addTab("0", new UserInterface(mFrame, bridge, "0"));
            }
            tabbedPane.setSelectedIndex(0);

            for(int i=1; i<Global.config.getInstanceNumber();i++){

                String si = String.valueOf(i);
                if(Global.DEV_MODE){
                    si = "dev-"+si;
                }
                tabbedPane.addTab(si, new UserInterface(mFrame, bridge, si));
            }


            final JButton settingButton = new JButton("Setting");
            settingButton.addActionListener(e-> Global.SettingGUI());
            mFrame.add(settingButton, BorderLayout.SOUTH);
            mFrame.add(tabbedPane, BorderLayout.CENTER);
            mFrame.setTitle("Brutal Age Controller");
            mFrame.pack();
            mFrame.setLocationRelativeTo(null);
            mFrame.setResizable(true);
            mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //allow x to exit the application
            mFrame.setVisible(true);
            mFrame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                    AndroidDebugBridge.disconnectBridge();
                    System.exit(0);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
