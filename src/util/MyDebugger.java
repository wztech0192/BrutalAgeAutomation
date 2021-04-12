package util;

import com.android.ddmlib.*;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import dispatcher.EventDispatcher;
import events.EventMap;
import events.common.Event;
import events.handler.CityWork;
import game.GameException;
import game.GameInstance;
import game.GameStatus;
import net.sf.cglib.core.Local;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyDebugger {

    public MyDebugger(GameInstance game){
        System.out.println("********** Start DEBUGGING *************");
        new Thread(()->{
            System.out.print("**Enter a event: ");
            Scanner input = new Scanner(System.in);
            String cmd;

            Pattern chatDataRegex = Pattern.compile(".*chat\\.pf\\.tap4fun\\.com(.*?)\\{\"");
            Pattern findXRegex = Pattern.compile("\"X\":(\\d*),");
            Pattern findYRegex = Pattern.compile("\"Y\":(\\d*),");
            Pattern chatDateRegex = Pattern.compile("(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\b)");
            LocalDateTime prevRead = LocalDateTime.now();
            DateTimeFormatter timePattern = DateTimeFormatter.ofPattern("yyyy-MM-d H:m:s");

            while(!(cmd = input.nextLine()).equalsIgnoreCase("exit")){
                try {

                    if(cmd.contains("search")){
                        String[] split =  cmd.split(" ");
                        game.dispatch.agathaSearchClick(split[1], Integer.parseInt(split[2]));
                    }
                    if(cmd.contains("status-")){
                        game.startEvent(GameStatus.valueOf(cmd.split("-")[1]));
                    }
                    else if(cmd.contains("osr")){
                        String[] split = cmd.split(" ");
                       System.out.println( game.dispatch.doOSR(Integer.parseInt(split[1]),Integer.parseInt(split[2]),Integer.parseInt(split[3]),Integer.parseInt(split[4])));
                    }
                    else if(cmd.contains("png")){
                       // game.dispatch.getMatch(cmd);
                          Mat rawMat = game.dispatch.rawimg2Mat( game.dispatch.getRaw());
                        game.dispatch.getMatch(100, 100, rawMat.width() - 100, rawMat.height() - 100, 0.25, 0, rawMat, cmd , 3);
                    }
                    else if(cmd.contains("monster:")){
                        String[] split = cmd.split(" ");
                        game.dispatch.getMonsterMatch(3, new ArrayList<> (Arrays.asList(split)));
                    }
                    else {
                        switch (cmd) {
                            case "test":
                                game.dispatch(Event.builder().setLoc(46, 1010));
                                game.dispatch.staticDelay(2);
                                //click search monster
                                game.dispatch(Event.builder().setLoc(332, 557));
                                game.dispatch.staticDelay(2);
                                //click search
                                game.dispatch(Event.builder().setLoc( 356, 937));
                                game.dispatch.staticDelay(2);
                                //click hunt monster
                                game.dispatch(Event.builder().setLoc( 356, 647).setDelay(1));
                                //click go
                                game.dispatch(Event.builder().setLoc(487, 1198).setDelay(1));
                                //click back
                                game.dispatch(Event.builder().setLoc(45, 1212).setDelay(1));
                                game.dispatch(Event.builder().setLoc(45, 1212).setDelay(1));
                                game.dispatch.staticDelay(2);
                                game.dispatch(Event.builder().setLoc(399, 868).setDelay(1));
                                break;
                            case "pos_mode":
                                game.posTarget = new JsonObject();
                                game.posTarget.put("level2Hut", true);
                                game.posTarget.put("horde", "1");
                                CityWork.firePosMode(game);
                                break;
                            case "chat":
                                game.dispatch.exec("adb shell cat /data/data/com.tap4fun.brutalage_test/files/tap4fun/be/Documents/chatdb", s->{
                                    Matcher m = chatDateRegex.matcher(s);
                                    if(m.find()) {
                                        LocalDateTime chatTime = LocalDateTime.parse(m.group(1), timePattern);
                                        long duration = Duration.between(prevRead, chatTime).toMinutes();
                                        if (duration >= 0) {
                                            if ((m = chatDataRegex.matcher(s)).find()) {
                                                System.out.println(m.group(1));
                                                switch (m.group(1)) {
                                                    case "Come and see this!": {
                                                        if ((m = findXRegex.matcher(s)).find()) {
                                                            System.out.println("for x");
                                                            System.out.println(m.group(1));
                                                        }

                                                        if ((m = findYRegex.matcher(s)).find()) {
                                                            System.out.println("for Y");
                                                            System.out.println(m.group(1));
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    return false;
                                });
                                break;
                            case "all_chat":
                                game.dispatch.exec("adb shell cat /data/data/com.tap4fun.brutalage_test/files/tap4fun/be/Documents/chatdb", s->{

                                    System.out.println(s);
                                    return false;
                                });
                                break;
                            case "ls":
                                EventMap.printAll();
                                break;
                            case "zoomout":
                                game.dispatch.zoomout();
                                break;
                            case "new":
                                game.dispatch.stopGame();
                                game.dispatch.changeAccount(game.store.createNewID(), false);
                                game.dispatch.startGame();
                                break;
                            case "capture":
                                game.dispatch.screenshot(FilePath.TEMPLATE_PATH + "/capture.png");
                                break;
                            case "closest_building":
                                game.dispatch.findClosestBuilding(game);
                                break;
                            case "select":
                                game.dispatch.selectMonster(400, 400);
                                break;
                            default:
                                game.dispatch(cmd);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.print("\n**Enter a event: ");
            }
        }).start();
    }

}
