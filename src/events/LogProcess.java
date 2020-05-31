package events;

import com.android.ddmlib.logcat.LogCatMessage;
import com.android.ddmlib.logcat.LogCatReceiverTask;
import game.GameException;
import game.GameInstance;
import game.GameStatus;
import org.opencv.core.Point;
import store.Account;
import util.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogProcess {
    private final static Pattern regexWorldScale = Pattern.compile("(\\d*) 1$");
    private final static Pattern regexBtnNamePattern = Pattern.compile("character name is ([^,]*), (\\d*), (\\d*)"); // the pattern to search for
    private final static Pattern regexCurrentOffset = Pattern.compile("m_currentOffset:\\((.*),(.*)\\)"); // the pattern to search for
    private final static Pattern regexBuildTime = Pattern.compile(".*lv.(\\d*) building.*currentTiem: (.*), fireTime: (.*)");
    private final static Pattern regexTalentScroll = Pattern.compile("dummy :(.*)");
    private final static Pattern regexCurrTroops = Pattern.compile("m_currentTroopsNumber = (\\d*)");
    private final static Pattern regexTransportSelected = Pattern.compile("selectedTotal= (\\d*)");
    private final static Pattern regexMaxTransport = Pattern.compile("maxNum (\\d*)");
    private final static Pattern regexTransportIndex = Pattern.compile("SetSlider (\\d*) ");
    private final static Pattern regexMaxRss = Pattern.compile("max resource: (\\d*)");
    private final static Pattern regexGetText = Pattern.compile("showText = (.*) textLength");
    private final static Pattern regexLimitTransport = Pattern.compile("limit (\\d*)");
    private final static DateTimeFormatter buildTimePattern = DateTimeFormatter.ofPattern("MMM d, yyyy h:m:s a");
    public boolean shouldTrain;
    public boolean shouldHeal;
    public int marches;
    public double talentScroll = 0;
    public boolean hasPopupWarning;
    public int transportIndex = 0;
    public int[] transportRss = new int[5];
    public int buildingCompleteLevel = 0;
    public LocalDateTime buidlingCompleteTime = LocalDateTime.now();
    public int[] touchPoint = new int[2];
    public Point city = new Point();
    public double worldScale;
    public int[] worldCurr = new int[4];
    public boolean levelupDialog = false;
    public String btnName = "";
    public int idleTroops;
    public int currTroops;
    public boolean emptyOutPost;
    private GameInstance game;
    public int maxTransportNum = 0;
    public int selectedTransportNum = 0;
    public int limitTransportNum = 0;
    public boolean hasClan = false;
    public boolean isInCity = true;
    public String text;
    LogCatReceiverTask lcrt;

    public LogProcess(GameInstance game) {
        this.game = game;
    }

    public void startLog() {
        lcrt = new LogCatReceiverTask(game.store.device);
        lcrt.addLogCatListener(msgList -> {
            if(game.store.isClose){
                lcrt.stop();
            }
            try {
                for (LogCatMessage msg : msgList) {
                    processLog(msg.getMessage());
                }
            } catch (GameException e) {
                GameException.fire(game, e);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        new Thread(lcrt::run).start();
    }

    /**
     * Log Listener
     */
    private void processLog(String str) throws Exception {


        if (game.debug) {
            handleCityWork(str);
            handleWorldMap(str);
        }


        Matcher m;



        if ((m = regexWorldScale.matcher(str)).find()) {
            if(!m.group(1).trim().equalsIgnoreCase("")){
                worldScale = Double.parseDouble(m.group(1));
                Logger.log("world scale " + worldScale);
            }
        }

        //match click button
        if ((m = regexBtnNamePattern.matcher(str)).find()) {
            btnName = m.group(1);
            touchPoint[0] = Integer.parseInt((m.group(2)));
            touchPoint[1] = Integer.parseInt((m.group(3)));
            System.out.println("Now Click " + btnName + " in " + touchPoint[0] + ", " + touchPoint[1]);

            //Testing


        }
        //match city offset
        else if ((m = regexCurrentOffset.matcher(str)).find()) {
            isInCity = true;
            city.x = Double.parseDouble(m.group(1));
            city.y = Double.parseDouble(m.group(2));
        } else if (str.contains("sfx_event_level_up.ogg")) {
            levelupDialog = true;
        }else if(str.contains("playEffectL filename sound/sfx_event_notice_window.ogg")){
            hasPopupWarning = true;
        }
        else if(str.contains("SaveRawData successs CITYMAP_LOCAL_DATA")){
            isInCity = true;
        }
        else if(str.contains("initTiles =====>")){
            isInCity = false;
        }
        else if(str.contains("setAllianceWarNumber")){
            hasClan = true;
        }


        switch (game.status.get()) {
            case starting:
                handleStart(str);
                break;
            case tutorial:
                handleTutorial(str);
                break;
            case change_server:
                break;
            case when_start:
                if ((m = regexTalentScroll.matcher(str)).find()) {
                    talentScroll = Double.parseDouble(m.group(1));
                }
                else if((m = regexGetText.matcher((str))).find()){
                    text = m.group(1);
                }
                else{
                    handleCityWork(str);
                }
                break;
            case city_work:
                handleCityWork(str);
                break;
            case world_map:
                handleWorldMap(str);
                break;
            default:
        }
    }

    private void handleWorldMap(String str) {
        Matcher m;
        if ((m = regexCurrTroops.matcher(str)).find()) {
            currTroops = Integer.parseInt(m.group(1));
        }
    }


    private void handleStart(String str) throws Exception {

        if (str.contains("_offsetCityMap")) {
            if (game.posTarget != null) {
                handlePosModeStart();
            } else if(game.account != null) {
                if (game.account.getChangedServer()) {
                    game.startEvent(GameStatus.when_start);
                } else {
                    Logger.log("Start change server...");
                    game.startEvent(GameStatus.change_server);

                }
            }
        }
        if (str.contains("ShowDialogBox")) {
            game.dispatch.delay(1);
            game.status.set(GameStatus.tutorial);
            game.dispatch("tutorial_dialog");
        }
    }

    private void handlePosModeStart() {
        if(!game.posTarget.containsKey("temp")) {
            if (!game.posTarget.containsKey("changed_server")) {
                game.startEvent(GameStatus.change_server, "change_server");
            } else if(game.posTarget.containsKey("exist")){
                game.startEvent(GameStatus.world_map, "positioning");
            }else{
                game.startEvent(GameStatus.when_start, "configuring");
            }
        }else{
            game.startEvent(GameStatus.when_start, "configuring");
        }
    }

    private void handleTutorial(String str) throws Exception {
        if (str.contains("ShowDialogBox3")) {
            game.dispatch.delay(1);
            game.dispatch(EventMap.get("tutorial_dialog"));
            game.dispatch.delay(2);
            game.dispatch.zoomout();
            game.dispatch.delay(3);
            game.dispatch.zoomout();
            game.dispatch.staticDelay(10);
            Logger.log("restart");
            game.dispatch.stopGame();
            game.dispatch.delay(3);
            game.dispatch.startGame();
            game.startEvent(GameStatus.starting);
        } else if (str.contains("ShowDialogBox")) {
            game.dispatch("tutorial_dialog");
        }
    }

    private void handleCityWork(String str) {

        Matcher m = regexBuildTime.matcher(str);

        if (m.find()) {
            Logger.log(str);
            buildingCompleteLevel = Integer.parseInt(m.group(1));
            buidlingCompleteTime = LocalDateTime.parse(m.group(3).trim(),   // = 7:07:00 PM
                    buildTimePattern).minus(Duration.between(LocalDateTime.now(),
                    LocalDateTime.parse(m.group(2).trim(),   // = 7:07:00 PM
                            buildTimePattern)));
            Logger.log("Some building complete lvl " + buildingCompleteLevel + " at: " + buidlingCompleteTime);
        }
        else{
            handleTransport(str);
        }
    }

    private void handleTransport(String str){
        Matcher m;

         if((m= regexTransportIndex.matcher(str)).find()){
            transportIndex = Integer.parseInt(m.group(1));
        }
        else if((m=regexMaxRss.matcher(str)).find()){
            transportRss[transportIndex] =  Integer.parseInt(m.group(1));
        }
        else if((m= regexTransportSelected.matcher(str)).find()){
            selectedTransportNum = Integer.parseInt(m.group(1));
        }
        else if((m=regexMaxTransport.matcher(str)).find()){
            maxTransportNum += Integer.parseInt(m.group(1));
        }
        else if((m=regexLimitTransport.matcher(str)).find()){
            limitTransportNum = Integer.parseInt(m.group(1));
        }
    }


    /**
     * Utility
     */

    public int parseServerIndex() {
        return Integer.parseInt(btnName.substring(btnName.indexOf("_") + 1, btnName.indexOf(":")));
    }

    public void resetTransport() {
        maxTransportNum = 0;
        limitTransportNum = 0;
        selectedTransportNum = 0;
    }
}
