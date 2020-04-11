package events.register;

import events.common.Event;
import org.opencv.core.Point;
import store.MatchPoint;
import util.Logger;

import java.awt.image.BufferedImage;
import java.util.HashMap;

public class TestEvent {

    public static  int getRemain(String numStr, boolean onlyFirst){
        if(numStr.equalsIgnoreCase("")) return -1;
        String[] split = numStr.split("/");
        int curr = getNumber(split[0], true);
        if(onlyFirst) return curr;

        int total = getNumber(split[1], true);
        return total - curr;
    }

    public static int getNumber(String numStr, boolean replaceDot){
        numStr = numStr.trim();
        System.out.println(numStr);
        if(numStr.equalsIgnoreCase("")) return -1;
        numStr = numStr.toUpperCase();
        try {
            int acc = 1;
            int index;
            if ((index = numStr.indexOf("K")) != -1) {
                acc = 1000;
                numStr = numStr.substring(0, index);
            } else if ((index = numStr.indexOf("M")) != -1) {
                acc = 1000000;
                numStr = numStr.substring(0, index);
            }
            numStr = numStr.replaceAll(replaceDot ? "[^0-9]" : "[^0-9.]", "");

            return (int)(Double.parseDouble(numStr) * acc);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return -1;
    }

    public static void register(HashMap<String, Event> _map) {

        Event.builder(_map,"close_update")
                .setDelay(1.5)
                .setLoc(108, 359);

        Event.builder(_map, "later_update")
                .setDelay(3)
                .setLoc(173, 875);


        Event.builder(_map, "template_close")
                .setTemplateName("close_btn.png")
                .setDelay(1.5)
                .setTemplateMatches(1)
                .setListener(((event, game) -> {
                    if (game.dispatch.matchedPoints.isEmpty()) {
                        return event;
                    } else {
                        MatchPoint p = game.dispatch.matchedPoints.get(0);
                        game.dispatch(Event.builder()
                                .setLoc(p)
                                .setDelay(1.5)
                        );
                        return null;
                    }
                }));

        Event.builder(_map, "login_test_2")
                .setDelay(1.5)
                .setLoc(360, 134)
                .setListener(
                        (event, game) -> game.log.btnName.equalsIgnoreCase(":fullHitzone") ? null : event
                );

        Event.builder(_map, "login_test")
                .setTargetName(":fullHitzone")
                .setDelay(1.5)
                .setLoc(510, 110)
                .setMaxRedo(2)
                .setListener((event, game) -> {
                    if (!game.log.btnName.equalsIgnoreCase(event.targetName) &&
                            !game.dispatch("login_test_2") &&
                            !game.dispatch("template_close")) {
                            return event;
                    }
                    return null;
                });


        Event.builder(_map, "get_troop_info").setDelay(2)
                .setListener(((event, game) -> {
                    try{
                        game.dispatch.delay(2);
                        BufferedImage image = game.dispatch.captureAsBI();

                        int troops = getNumber(game.dispatch.doOSR(image,205, 103, 311, 129 ), true);
                        int marches = getRemain(game.dispatch.doOSR(image,208,156,265,181), false);
                        String wounded = game.dispatch.doOSR(538,154 ,663 ,185).replaceAll("[^0-9]", "");
                        int idle = getNumber(game.dispatch.doOSR(362,218 ,479 ,255),true);

                        Logger.log("******** troop status");
                        Logger.log("Troops: "+troops);
                        Logger.log("Idle Troops: "+idle);
                        Logger.log("Marches: "+marches);
                        Logger.log("Wounded: "+wounded);

                        int maxTroop = 0;
                        if(game.account != null) {
                             maxTroop = (game.account.getBuildingLvl("portal") < 6 ? 4000 : 10000) *
                                    (game.account.getBuildingLvl("stronghold") < 6 ? 2 : 3);

                             if(troops > -1){
                                 game.account.setTroops(troops);
                                 game.updateAccount();
                             }
                        }

                        game.log.shouldTrain = troops < maxTroop;
                        game.log.marches = marches;
                        game.log.shouldHeal = !wounded.startsWith("0");;
                        game.log.idleTroops = idle;

                        Logger.log("Should train: "+game.log.shouldTrain);
                        Logger.log("Should heal: "+game.log.shouldHeal);
                        Logger.log("******** troop status end");
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }

                    game.dispatch("top_left");
                    return null;
                }));

        Event.builder(_map, "get_rss_info")
            .setDelay(1.5)
            .setListener(((event, game) -> {
                try{
                    BufferedImage image = game.dispatch.captureAsBI();

                    int level = getNumber(game.dispatch.doOSR(image,65,1, 83, 18 ), true);
                    int wood = getNumber(game.dispatch.doOSR(image,137,61, 217, 93 ), false);
                    int meat = getNumber(game.dispatch.doOSR(image,262,65,338,93), false);
                    int mana = getNumber(game.dispatch.doOSR(image,384,65,462,93), false);
                    int rock = getNumber(game.dispatch.doOSR(image,507,65,584,93), false);
                    int ivory = getNumber(game.dispatch.doOSR(image,628, 60,690, 94), false);
                   // int rock = getNumber(game.dispatch.doOSR(image,378,65,467,93));

                    Logger.log("Level: "+level);
                    Logger.log("Wood: "+wood);
                    Logger.log("Meat: "+meat);
                    Logger.log("Mana: "+mana);
                    Logger.log("Rock: "+rock);
                    Logger.log("Ivory: "+ivory);
                    if(game.account != null) {
                        game.account.setLevel(level);
                        game.account.setResource("meat", meat);
                        game.account.setResource("wood", wood);
                        game.account.setResource("mana", mana);
                        game.account.setResource("rock", rock);
                        game.account.setResource("ivory", ivory);
                        game.updateAccount();
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                }
                return null;
            }));
    }
}
