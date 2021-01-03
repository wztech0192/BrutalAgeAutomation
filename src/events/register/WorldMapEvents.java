package events.register;

import com.android.ddmlib.RawImage;
import com.github.cliftonlabs.json_simple.JsonObject;
import dispatcher.EventDispatcher;
import events.common.Event;
import game.GameException;
import org.opencv.core.Mat;
import store.MatchPoint;
import util.Logger;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

public class WorldMapEvents {

    private static String encryptName(JsonObject json) {
        if (json != null) {

            if(json.containsKey("file_id"))
                return (String)json.get("file_id");
          /*  StringBuffer str = new StringBuffer();
            str.append(json.get("buiX"));
            str.append(json.get("buiY"));
            str.append(0);
            str.append(json.get("telX"));
            str.append(json.get("telY"));
             return str.reverse().toString();*/
        }
        return Math.random() + "";
    }


    public static int[] getRandomCoordinate(int minR, int maxR, int minD, int maxD) {
        int cx = 512;
        int cy = 512;
        int randomR = (int) (Math.random() * ((maxR - minR) + 1)) + minR;
        int randomD = (int) (Math.random() * ((maxD - minD) + 1)) + minD;
        int pointX = (int) (cx + randomR * Math.cos(Math.toRadians(randomD)));
        int pointY = (int) (cy + randomR * Math.sin(Math.toRadians(randomD)));
        return new int[]{pointX, pointY};
    }

    public static void register(HashMap<String, Event> _map) {

        Event.builder(_map, "change_name")
                .setDelay(1)
                .setLoc(40, 56)
                .setListener(((event, game) -> {
                    game.dispatch(Event.builder().setLoc(360, 742).setDelay(1.5));
                    game.dispatch(Event.builder().setLoc(384, 428).setDelay(1.5));
                    game.dispatch.enterText(encryptName(game.posTarget));
                    game.dispatch.delay(1);
                    game.dispatch(Event.builder().setLoc(365, 717).setDelay(1.5));
                    game.dispatch.delay(1.5);
                    return Event.SUCCESS;
                }));

        Event.builder(_map, "tel_confirm")
                .setDelay(1.5)
                .setLoc(335, 664);

        Event.builder(_map, "change_outpost")
                .setDelay(1.5)
                .setLoc(47, 1100);

        Event.builder(_map, "safe_teleport")
                .setDelay(1.5)
                .setLoc(514, 655, 514, 694, 200)
                .setListener(((event, game) -> {
                    if (game.log.btnName.contains("buttons_2:btn_2")) {
                        game.dispatch(Event.builder().setLoc(514, 655));
                        game.dispatch.staticDelay(1.25);
                        game.dispatch(Event.builder().setLoc(611, 1158));
                        game.dispatch.staticDelay(1.25);
                        if (game.log.btnName.contains("btn_buyUse")) {
                            game.dispatch(Event.builder().setDelay(1).setLoc(67, 322));
                        } else if (game.log.btnName.contains("bottom_panel:btn_right")) {
                            game.dispatch("top_left");
                        }
                        return Event.SUCCESS;
                    }
                    return event;
                }));

        Event.builder(_map, "teleport")
                .setDelay(1.5)
                .setLoc(459, 655)
                .setListener(((event, game) -> {
                    game.dispatch(_map.get("tel_confirm"));
                    game.dispatch(Event.builder().setDelay(1.5).setLoc(611, 1158));
                    if (game.log.btnName.contains("btn_buyUse")) {
                        game.dispatch(Event.builder().setDelay(1).setLoc(67, 322));
                    } else if (game.log.btnName.contains("bottom_panel:btn_right")) {
                        game.dispatch("top_left");
                    }
                    return Event.SUCCESS;
                }));

        Event.builder(_map, "tap_build")
                .setDelay(1.5)
                .setLoc(160, 635)
                .setListener((event, game) -> {
                    if (game.log.btnName.contains("btn_1")) {
                        game.dispatch("tel_confirm");
                        Event tapEvent = Event.builder().setDelay(1.5).setLoc(474, 1169);
                        game.dispatch(tapEvent);
                        game.dispatch(tapEvent);
                        return Event.SUCCESS;
                    }
                    game.dispatch(Event.builder().setLoc(349, 1008).setDelay(1.5));
                    return event;
                });

        Event.builder(_map, "build_empty_outpost")
                .setDelay(1.5)
                .setListener((event, game) -> {
                    int minR, maxR, minD, maxD, outpostNum;
                    if(game.log.emptyOutPost){
                        if(game.account.getBuildingLvl("stronghold") >= 10){
                            minR= 180;
                            maxR= 230;
                            minD= -100;
                            maxD= 150;
                            outpostNum= 2;
                        }
                        else{
                            minR= 240;
                            maxR= 400;
                            minD= -180;
                            maxD= 180;
                            outpostNum=1;
                        }
                        int[] randomCoordinate;

                        int redo = 10;
                        for(int i=0; i<outpostNum;i++) {
                            do {
                                randomCoordinate = WorldMapEvents.getRandomCoordinate(minR, maxR, minD, maxD);
                                game.dispatch.changePosition(randomCoordinate[0], randomCoordinate[1]);
                                if (game.dispatch("tap_build")) {
                                    return Event.SUCCESS;
                                }
                            } while (redo-- > 0);
                        }
                    }
                    return Event.SUCCESS;
                });


        Event.builder(_map, "build_init_outpost")
                .setDelay(1.5)
                .setListener((event, game) -> {
                    game.dispatch("build_empty_outpost");
                    int[] tapLoc = new int[]{
                            427, 549,
                            226, 453,
                            416, 467,
                            248, 577
                    };
                    Event tapEvent = Event.builder().setDelay(2);
                    for (int i = 0; i < tapLoc.length; i += 2) {
                        game.dispatch(tapEvent.setLoc(tapLoc[i], tapLoc[i + 1]));
                        if (game.dispatch("tap_build")) {
                            return Event.SUCCESS;
                        }
                    }
                    return event;
                });

        Event.builder(_map, "select_monster")
                .setLoc(367, 615)
                .setDelay(1.5);

        Event.builder(_map, "world_set")
                .setListener(((event, game) -> {

                    BufferedImage image = game.dispatch.captureAsBI();

                    int x = TestEvent.getNumber(game.dispatch.doOSR(image,293, 1156, 358, 1187 ), true);
                    int y = TestEvent.getNumber(game.dispatch.doOSR(image,410, 1156, 477, 1187 ), true);

                    game.log.worldCurr[0] = x -1;
                    game.log.worldCurr[1] = y -1;

                    System.out.println("Current Coordinate: " + game.log.worldCurr[0] + "," + game.log.worldCurr[1]);
                    return Event.SUCCESS;
                }));

        Event.builder(_map, "adjust_map")
                .setDelay(1.5)
                .setListener((event, game) -> {

                    System.out.println("******Start adjust map zoom");
                    game.dispatch.mapzoom();

                    Event testClick = Event.builder().setLoc(73, 104).setDelay(1.5);
                    int redo = 10;
                    do {
                        game.dispatch(testClick);
                        if (!game.log.btnName.contains("scene_tiles")) {
                            game.dispatch.mapzoomin();
                        } else {
                            break;
                        }
                    } while (--redo > 0);

                    if (redo <= 0) {
                        throw new GameException("Stuck at adjust map");
                    }

                    System.out.println("***** Adjust zoom ended");
                    return Event.SUCCESS;
                });


        Event.builder(_map, "quickSelect")
                .setLoc(214, 1175)
                .setDelay(1);

        Event.builder(_map, "gather_check")
                .setListener(((event, game) -> {
                    RawImage rawImage = game.store.device.getScreenshot();
                    Mat source = EventDispatcher.rawimg2Mat(rawImage);
                    List<MatchPoint> points = game.dispatch.getMatch(592, 1100, 651, 1147 ,0, 0, source, "gather_check.png", 1);
                    if (points.isEmpty()) {
                        Logger.log("gather not checked");
                       game.dispatch(Event.builder().setLoc(623, 1123).setDelay(1));
                    }else{
                        Logger.log("gather checked");
                    }

                    return Event.SUCCESS;
                }));

        Event.builder(_map, "attack_monster")
                .setDelay(2)
                .setListener(((event, game) -> {
                    int redo = 0;
                    game.dispatch(Event.builder().setLoc(332, 661).setDelay(2));
                    Logger.log("Current Idle: " + game.log.idleTroops);

                    int currTroops = game.log.getCurrentTroops();
                    Logger.log("Current Troops: " + currTroops);

                    if (game.log.idleTroops == currTroops || currTroops> 25000) {
                        game.dispatch("quickSelect");
                        currTroops =  game.log.getCurrentTroops();
                    }
                    while ( currTroops <= 0 && redo++ < 10) {
                        game.dispatch("quickSelect");
                        currTroops =  game.log.getCurrentTroops();
                    }

                    game.dispatch("gather_check");
                    game.dispatch(Event.builder().setLoc(520, 1198).setDelay(1));
                    if (!game.log.btnName.contains("btn_go")) {
                        game.dispatch("top_left");
                    }

                    game.log.marches--;
                    game.log.idleTroops -= currTroops;
                    Logger.log("After Deplay" + currTroops + " Now Idle Troops: " + game.log.idleTroops);

                    return Event.SUCCESS;
                }));

        Event.builder(_map, "attack_monster_test")
                .setLoc(332, 661, 332, 693, 200)
                .setListener(((event, game) -> {
                    if (game.log.btnName.contains("btn_confirm")) {
                        game.dispatch(Event.builder().setLoc(75, 335).setDelay(1));
                    } else if (game.log.btnName.contains("list")) {
                        game.dispatch("top_left");
                    } else if (game.log.btnName.equalsIgnoreCase("buttons_1:btn_0")
                            || game.log.btnName.equalsIgnoreCase("buttons_1:btn_1")) {
                        return Event.SUCCESS;
                    }
                    return event;
                }));

        Event.builder(_map, "attackTemple")
                .setListener(((event, game) -> {
                    game.log.oops = false;

                    game.dispatch.staticDelay(1);

                    for (int redo =0 ; redo < 5; redo ++) {
                        if(game.log.getCurrentTroops() > 0){
                            game.dispatch("quickSelect");
                        }else{
                            break;
                        }
                    }

                    RawImage image = game.dispatch.getRaw();

                    //, new int[]{  98, 747, -146 }, new int[]{  98, 870, -146 }) ,  new int[]{  98, 994, -146 }
                    if( game.dispatch.isPixelMatch(image, new int[]{  98, 994, -146 })){
                        game.dispatch(Event.builder().setTargetName("click shaman").setLoc(576, 925).setDelay(1.5));
                        game.dispatch.staticDelay(1.5);
                        game.dispatch.enterText(String.valueOf(99999));
                        game.dispatch.staticDelay(1.5);
                    }
                    if( game.dispatch.isPixelMatch(image, new int[]{  98, 870, -146 })){
                        game.dispatch(Event.builder().setTargetName("click beast").setLoc(576, 797).setDelay(1.5));
                        game.dispatch.staticDelay(1.5);
                        game.dispatch.enterText(String.valueOf(99999));
                        game.dispatch.staticDelay(1.5);
                    }
                    if( game.dispatch.isPixelMatch(image,  new int[]{  98, 747, -146 })){
                        //for the first
                        int currTroops = game.log.getCurrentTroops();
                        int sendWarriorsCount = game.account.getTroops() - currTroops - game.account.getNumberFeaturer().getNumberSetting().get("Min Troop");

                        Logger.log(game.account.getTroops()+" / " + currTroops+", Send "+sendWarriorsCount+" warriors");
                        if (sendWarriorsCount > 0) {
                            game.dispatch(Event.builder().setLoc(566, 670));
                            game.dispatch.staticDelay(1.5);
                            game.dispatch.enterText(String.valueOf(sendWarriorsCount));
                        }

                        game.dispatch(Event.builder().setLoc(682, 669).setDelay(1));

                        int currentTroop =  game.log.getCurrentTroops();

                        //click out;
                        game.dispatch(Event.builder().setLoc(521, 1200).setDelay(1));

                        //click send
                        game.dispatch(Event.builder().setLoc(521, 1200));
                        game.dispatch.staticDelay(1.5);
                        game.dispatch(Event.builder().setLoc(381, 650));
                        game.dispatch.staticDelay(1.5);
                        game.dispatch(Event.builder().setLoc(450, 720));
                        Logger.log("oops "+game.log.oops);
                        if(!game.log.oops ){
                            int leftTroops = game.account.getTroops() - currentTroop;
                            Logger.log("After minus "+currentTroop+" Troops, Left Troops: "+leftTroops);
                            game.account.setTroops(leftTroops);
                            game.account.setLastRound(LocalDateTime.now());
                            game.updateAccount();
                        }
                    }else{
                        game.account.setTroops(game.account.getNumberFeaturer().getNumberSetting().get("Min Troop") - 1);
                        game.updateAccount();
                    }

                    return Event.SUCCESS;
                }));

        Event.builder(_map, "feedTemple")
                .setDelay(1)
                .setLoc(659, 321)
                .setListener(((event, game) -> {
                    game.log.currTroops = 0;
                    game.dispatch.delay(1.25);
                    game.dispatch(Event.builder().setLoc(156, 1180).setDelay(1.25));
                    game.dispatch(Event.builder().setLoc(600, 200));

                    for(int i=0;i<3;i++){
                        game.dispatch.staticDelay(2.5);
                        game.dispatch(Event.builder().setLoc(471, 832).setDelay(1.25));
                        if(game.log.btnName.contains("buttons_5:btn_5")){
                            game.dispatch(Event.builder().setLoc(471, 832).setDelay(1.25));
                            break;
                        }
                    }

                    game.dispatch("attackTemple");


                    return Event.SUCCESS;
                }));

        Event.builder(_map, "auto_repair")
                .setDelay(1)
                .setLoc(342, 500)
                .setDelay(1.25)
                .setListener((event, game) -> {
                    game.dispatch.delay(1);
                    game.dispatch(Event.builder().setLoc(237,645).setDelay(1.25));
                    game.dispatch(Event.builder().setLoc(363,456).setDelay(1.25));
                    if(game.log.btnName.contains("extinguish")){
                        game.dispatch(Event.builder().setLoc(363,456).setDelay(1.25));
                    }

                    if(game.log.btnName.contains("repair")){
                        game.dispatch(Event.builder().setLoc(377, 722).setDelay(1.25));
                    }

                    game.dispatch(Event.builder().setLoc(68, 291).setDelay(1.25));

                    game.dispatch("top_left");
                    return Event.SUCCESS;
                });

    }
}
