package util;

import store.Config;

import javax.swing.*;
import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Global {
    public static final boolean OnlyPosMode = false;
    public static Config config;
    static{
        try {
            config = util.Marshaller.unmarshal(Config.class, FilePath.CONFIG_PATH);
        }
        catch (JAXBException e) {
            e.printStackTrace();
            config =  new Config();
            String ownerName = JOptionPane.showInputDialog(null, "你的谁（・∀・）？拼音only");
            if(ownerName==null || ownerName.equalsIgnoreCase("")){
                JOptionPane.showMessageDialog(null, "不认识你...再见");
                System.exit(0);
            }
            config.setOwnerName(ownerName);
            util.Marshaller.marshell(config, FilePath.CONFIG_PATH);
        }
    }

    public static final Pattern NoxPortRegex = Pattern.compile("<Forwarding name=\"port2\" proto=\"1\" hostip=\"127.0.0.1\" hostport=\"(\\d*)\".* ");



    public static String getNoxPort(String instanceName) throws IOException {
        File vbox = new File( config.getNoxPath() + "/BignoxVMS/" +instanceName + "/"+instanceName+ ".vbox");
        BufferedReader br = new BufferedReader(new FileReader(vbox));

        StringBuilder fileData = new StringBuilder();
        String st;
        while ((st = br.readLine()) != null) {
            fileData.append(st);
        }
        br.close();
        Matcher match =  NoxPortRegex.matcher(fileData.toString());
        if(match.find()) {
            return match.group(1);
        }
        else{
            return "";
        }
    }

}
