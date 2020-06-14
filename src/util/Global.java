package util;

import store.Config;

import javax.swing.*;
import javax.xml.bind.JAXBException;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Global {
    public static final boolean OnlyPosMode = false;
    public static Config config;
    static{
        String ownerName= "";
        try {
            config = util.Marshaller.unmarshal(Config.class, FilePath.CONFIG_PATH);
            ownerName= config.getOwnerName();
        }
        catch (Exception e) {
            e.printStackTrace();
            config = new Config();
            util.Marshaller.marshell(config, FilePath.CONFIG_PATH);
        }

        if(ownerName == null || ownerName.equalsIgnoreCase("")) {
            ownerName = JOptionPane.showInputDialog(null, "你的谁（・∀・）？拼音only");
            if (ownerName == null || ownerName.equalsIgnoreCase("")) {
                JOptionPane.showMessageDialog(null, "不认识你...再见");
                System.exit(0);
            }
        }
        try {
            URL  url = new URL("http://142.11.215.231:37212/login");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            String body = "{\"payload\":\""+ownerName+"\"}";
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(body);
            wr.close();
            int status = con.getResponseCode();
            System.out.println(status);
            if(status == 200){
                config.setOwnerName(ownerName);
                util.Marshaller.marshell(config, FilePath.CONFIG_PATH);
            }else{
                config.setOwnerName("");
                util.Marshaller.marshell(config, FilePath.CONFIG_PATH);
                JOptionPane.showMessageDialog(null, "好像不认识你...再见");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            config.setOwnerName("");
            util.Marshaller.marshell(config, FilePath.CONFIG_PATH);
            JOptionPane.showMessageDialog(null, "好像不认识你...再见");
        }
    }

    public static final Pattern NoxPortRegex = Pattern.compile("<Forwarding name=\"port2\" proto=\"1\" hostip=\"127.0.0.1\" hostport=\"(\\d*)\".* ");



    public static ArrayList<String> FindNoxes(){
        File noxBin = new File(Global.config.getNoxPath());
        if (!noxBin.exists()) {
            JOptionPane.showMessageDialog(null, "Nox doesnt exist! Please set the config path");
            return null;
        }

        File noxVMs = new File(Global.config.getNoxPath() + "/BignoxVMS");

        ArrayList<String> noxInstances = new ArrayList<>();
        for(File file: Objects.requireNonNull(noxVMs.listFiles())){
            noxInstances.add(file.getName());
        }

        return noxInstances;
    }

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

    public static void SettingGUI() {

        final JPanel myPanel = new JPanel(new GridLayout(4,2));

        final JTextField noxPathField= new JTextField(config.getNoxPath());
        final JTextField eventField= new JTextField(config.getEventName());
        final JTextField instanceField= new JTextField(String.valueOf(config.getInstanceNumber()));
        final JTextField eventFolderField = new JTextField(config.getEventFolder());

        myPanel.add(new JLabel("Nox Path"));
        myPanel.add(noxPathField);

        myPanel.add(new JLabel("Event Name"));
        myPanel.add(eventField);

        myPanel.add(new JLabel("Event Folder"));
        myPanel.add(eventFolderField);

        myPanel.add(new JLabel("Total Tab"));
        myPanel.add(instanceField);

        int result = JOptionPane.showConfirmDialog(myPanel, myPanel,
                "Setting", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try{
                config.setNoxPath(noxPathField.getText());
                config.setEventName(eventField.getText());
                config.setEventFolder(eventFolderField.getText());
                config.setInstanceNumber(Integer.parseInt(instanceField.getText()));
                util.Marshaller.marshell(config, FilePath.CONFIG_PATH);
            }catch(Exception e){
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,"Invalid input");
            }
        }

    }
}
