package store;

import com.android.ddmlib.IDevice;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import com.neovisionaries.ws.client.*;
import org.apache.commons.io.FileUtils;
import util.FilePath;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;


public class Store extends WebSocketAdapter {
    public Metadata metadata;
    private static final String SERVER = "ws://142.11.215.231:41998";
    private WebSocket ws;
    private boolean pause;
    private AccountGroup accountGroup;
    public IDevice device;
    public LinkedList<JsonObject> positionQueue;
    public String accountPath;
    public boolean isForceStop;
    public boolean isClose;
    public String tag;

    public Store(String tag){
        this.tag = tag;
        unmarshellMetadata();
    }


    public Store(IDevice device) {
        unmarshellMetadata();
        init(device);
    }

    public void init(IDevice device){
        this.device = device;
        this.accountPath = metadata.getAccountPath();
        accountGroup = new AccountGroup(unmarshellAcounts());
    }

    public boolean isPositionMode(){
        return positionQueue != null;
    }

    public void unmarshellMetadata(){
        try {
            metadata = util.Marshaller.unmarshal(Metadata.class, FilePath.METADATA_PATH+"_"+tag);
        }
        catch (JAXBException e) {
            metadata =  new Metadata();
            util.Marshaller.marshell(metadata, FilePath.METADATA_PATH+"_"+tag);
        }
        metadata.getFeatureToggler().getShortValue();
    }

    public ArrayList<Account> unmarshellAcounts(){
        ArrayList<Account> accounts = new ArrayList<>();
        try{
            File[] files = new File(accountPath).listFiles();
            if(files != null) {
                for (File file : files) {
                    if(file.getName().contains("wzz")) {
                        System.out.println("load account: " + file.getName());
                        accounts.add(util.Marshaller.unmarshal(Account.class, file.getAbsolutePath()+"/setting"));
                    }
                }
            }
        }
        catch (JAXBException e) {
            e.printStackTrace();
        }
        return accounts;
    }



    public void setPause(boolean pause) {
        this.pause = pause;
    }

    public boolean isPause() {
        return this.pause;
    }



    public void createRemoteWS() {
        try {
            positionQueue = new LinkedList<>();
            ws = new WebSocketFactory().setConnectionTimeout(10000).createSocket(SERVER).addListener(this)
                    .addExtension(WebSocketExtension.PERMESSAGE_DEFLATE).connect();
            JsonObject json = new JsonObject();
            json.put("max",0);
            json.put("current", 0);
            sendDataBack("receiver", json);
            System.out.println("Connect to server...");
        } catch (WebSocketException | IOException e) {
            e.printStackTrace();
        }
    }

    public void sendPing(){

        ws.sendPing();

    }

    public void sendDataBack(String type, JsonObject payload) {
        JsonObject json = new JsonObject();
        json.put("type", type);
        json.put("payload", payload);
        String str = json.toJson();
        System.out.println(str);
        ws.sendText(str);
    }

    @Override
    public void onTextMessage(WebSocket websocket, String message) {

        try {
            JsonObject data = (JsonObject) Jsoner.deserialize(message);
            System.out.println("Received: " + message);

            switch ((String) data.get("type")) {
                case "queue":
                    JsonObject payload = (JsonObject) data.get("payload");
                    sendDataBack("queueSuccess", payload);
                    positionQueue.add(payload);
                    break;
                case "cancel":
                    Iterator<JsonObject> it = positionQueue.iterator();
                    JsonObject target = null;
                    while(it.hasNext()){
                        target = it.next();
                        if(((String)target.get("id")).equalsIgnoreCase((String) data.get("payload"))){
                            it.remove();
                            break;
                        }
                    }
                    if(target != null){
                        target.put("status", "cancel");
                        sendDataBack("update",target);
                    }
                    System.out.println(positionQueue.size());
                    break;
            }

        } catch (JsonException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {

    }

    @Override
    public void onConnectError(WebSocket websocket, WebSocketException exception) {
        System.out.println("Connection error.....");
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame,
                               boolean closedByServer) {
        System.out.println("Disconnected error.....");
    }


    public void updateAllAccounts() {
        for(Account acc: accountGroup.getAccounts()){
                updateAccount(acc);
        }
    }

    public void updateAccount(Account acc) {
        if(!acc.getId().equalsIgnoreCase("")) {
            System.out.println("update "+acc.getId());
            File file = new File(accountPath + acc.getId());
            if(!file.exists()){
                file.mkdir();
            }
            util.Marshaller.marshell(acc, file.getAbsolutePath()+"/setting");
        }
    }

    public void updateAccount(int i) {
        if(i >= accountGroup.getAccounts().size()) {
            Account acc = accountGroup.getAccount(i);
            updateAccount(acc);
        }
    }


    public AccountGroup getAccountGroup(){
        return accountGroup;
    }

    public String createNewID() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String currentDatedTime = sdf.format(new Date());
        return "wzz"+currentDatedTime;
    }

    public void setDelay(int delay) {
        this.metadata.setDelay(delay);
        marshellMetadata();
    }

    public int getDelay() {
        return this.metadata.getDelay();
    }

    public void marshellMetadata() {
        util.Marshaller.marshell(metadata, FilePath.METADATA_PATH+"_"+tag);
    }

    public void deleteAccount(int i) {
        try {
            Account acc = getAccountGroup().getAccount(i);
            File accFile = new File(accountPath + acc.getId());
            FileUtils.deleteDirectory(accFile);
            getAccountGroup().getAccounts().remove(i);
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }

    public void addAccount(Account acc) {
        updateAccount(acc);
        getAccountGroup().getAccounts().add(acc);
    }


    public void setForceStop(boolean isForceStop) {
        this.isForceStop = isForceStop;
    }

    public void close() {
        isClose = true;
        if(ws != null)
            ws.sendClose();
    }
}




