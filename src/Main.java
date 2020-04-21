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


            if (!debug) {
                Runtime.getRuntime().addShutdownHook(new Thread(EventDispatcher::killServer));
            }


            File noxBin = new File(Global.config.getNoxPath());
            if (!noxBin.exists()) {
                JOptionPane.showMessageDialog(null, "Nox doesnt exist! Please set the config path");
                System.exit(0);
            }

            File noxVMs = new File(Global.config.getNoxPath() + "/BignoxVMS");

            ArrayList<String> noxInstances = new ArrayList<>();
            for(File file: Objects.requireNonNull(noxVMs.listFiles())){
                noxInstances.add(file.getName());
            }


            JFrame mFrame = new JFrame();
            JTabbedPane tabbedPane = new JTabbedPane();

            tabbedPane.addTab("One", new UserInterface(mFrame, debug, bridge, "One", noxInstances));
            tabbedPane.setSelectedIndex(0);

            tabbedPane.addTab("Two", new UserInterface(mFrame, debug, bridge, "Two", noxInstances));

            tabbedPane.addTab("Three", new UserInterface(mFrame, debug, bridge, "Three", noxInstances));

            tabbedPane.addTab("Four", new UserInterface(mFrame, debug, bridge, "Four",noxInstances ));


            mFrame.add(tabbedPane);
            mFrame.setTitle("Brutal Age Controller");
            mFrame.pack();
            mFrame.setLocationRelativeTo(null);
            mFrame.setResizable(true);
            mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //allow x to exit the application
            mFrame.setVisible(true);


        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
