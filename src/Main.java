import com.android.ddmlib.*;
import com.android.utils.FileUtils;
import dispatcher.EventDispatcher;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import sun.util.resources.cldr.el.TimeZoneNames_el;
import util.FilePath;
import util.Global;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
            boolean debug = args.length > 0;
            AndroidDebugBridge.init(false);

            AndroidDebugBridge bridge = AndroidDebugBridge.createBridge(
                    FilePath.RootPath + "/../adb", false);
            Runtime.getRuntime().addShutdownHook(new Thread(AndroidDebugBridge::disconnectBridge));




            JFrame mFrame = new JFrame();
            JTabbedPane tabbedPane = new JTabbedPane();

            tabbedPane.addTab("0", new UserInterface(mFrame, debug, bridge, "0"));
            tabbedPane.setSelectedIndex(0);

            for(int i=1; i<Global.config.getInstanceNumber();i++){
                String si = String.valueOf(i);
                tabbedPane.addTab(si, new UserInterface(mFrame, debug, bridge, si));
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
