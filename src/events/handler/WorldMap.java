package events.handler;

import events.register.TestEvent;
import game.GameException;
import game.GameInstance;
import game.GameStatus;
import org.opencv.core.Point;
import store.MatchPoint;
import util.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WorldMap {
    public static void fire(GameInstance game) throws Exception {
        game.dispatch.delay(1);

        for(int redo = 0; redo <= 15; redo++){
            if(game.log.isInCity){
                game.dispatch("bottom_left");
            }
            else if(redo == 15){
                throw new GameException("stuck at change map");
            }
            else{
                break;
            }
        }

        game.dispatch.delay(2);
        if(game.account.getBuildingLvl("stronghold") < 10) {
            game.dispatch("get_rss_info");
        }


        if( game.log.idleTroops >= 5000 && !game.account.isRandomized() && game.log.marches >= 3){
            int[] randomCoordinate;
            int redo = 10;
            do{
                randomCoordinate = getRandomCoordinate();
                game.dispatch.changePosition(randomCoordinate[0], randomCoordinate[1]);
                if(game.dispatch("safe_teleport")){
                    game.account.setRandomized(true);
                    game.updateAccount();
                    break;
                }
            }while(redo-- > 0 );
        }


        game.dispatch("world_set");

        if(game.account.isRandomized()){
            for(int i =0 ; i<4; i++) {
                if (game.log.world_set[0] < 270 && game.log.world_set[1] < 600) {
                    game.dispatch("change_outpost");
                    game.dispatch("world_set");
                }else{
                    break;
                }
            }
        }



        int loc = 0;
        int[] locArray = new int[]{20, -20, -20, 20, 35, 35, -35, -35};
        int[] setLoc = game.log.world_set.clone();

        int maxLvl = 4;
        int minLvl = 2;

        ArrayList<String> targets;

        while (game.log.idleTroops > 0 && game.log.marches > 0 && loc < locArray.length ) {
            game.dispatch.changePosition(setLoc[0], setLoc[1]);
            game.dispatch("adjust_map");

            targets = game.account.getGatherPrioritiesArray();

            if(targets.isEmpty()) {
                if (game.account.getResource("meat") < 100000) {
                    if (game.account.isRssLessThan("meat", "wood")) {
                        if (game.log.idleTroops > 2500) {
                            targets.add("meat3");
                        } else {
                            targets.add("meat2");
                        }
                        if (game.log.idleTroops > 4500) {
                            targets.add("meat4");
                        }

                    }
                } else if (game.account.getBuildingLvl("stronghold") >= 8 && game.account.isRssLessThan("rock", "wood") && game.log.idleTroops > 2500) {
                    targets.add("rock");
                } else {
                    if (game.log.idleTroops > 2500) {
                        targets.add("wood3");
                    } else {
                        targets.add("wood2");
                    }
                    if (game.log.idleTroops > 4500) {
                        targets.add("wood4");
                    }
                }

                if(game.log.idleTroops > 2500){
                    maxLvl = 3;
                }
                else{
                    maxLvl = 2;
                }
                if(game.log.idleTroops > 4500){
                    maxLvl = 4;
                    minLvl = 2;
                }
            }else{
                minLvl = game.account.getGatherPriorities().get("a_minLevel");
                maxLvl = game.account.getGatherPriorities().get("a_maxLevel");
            }


            List<MatchPoint> matches = game.dispatch.getMonsterMatch(3, targets);

            game.dispatch.delay(1);
            for (int i = 0; i < matches.size(); i++) {
                MatchPoint px = matches.get(i);
                game.dispatch.selectMonster( px.x,  px.y);
                if (game.dispatch("attack_monster_test")) {

                    int monsterLvl = TestEvent.getNumber(game.dispatch.doOSR(223, 504,241, 522), true);
                    Logger.log("Monster level "+monsterLvl+": Range "+minLvl+" - "+maxLvl);
                    if(monsterLvl >= minLvl && monsterLvl <= maxLvl) {
                        Logger.log("good, attack the monster");
                        game.dispatch("attack_monster");
                        game.log.marches--;
                        game.log.idleTroops -= game.log.currTroops;
                        Logger.log("After Deplay" + game.log.currTroops + " Now Idle Troops: " + game.log.idleTroops);
                        if (game.log.marches <= 0 || game.log.idleTroops <= 0) {
                            break;
                        }
                    }else{
                        Logger.log("not good, find other monster");
                    }
                }
                if (i + 1 != matches.size()) {
                    game.dispatch.changePosition(setLoc[0], setLoc[1]);
                    game.dispatch("adjust_map");
                }
            }
            setLoc[0] = game.log.world_set[0] + locArray[loc++];
            setLoc[1] = game.log.world_set[1] + locArray[loc++];
        }


        game.startEvent(GameStatus.initiate);

    }




    public static int[] getRandomCoordinate(){
        int cx = 512;
        int cy = 512;

        int minR = 190;
        int maxR = 230;

        int minD = -90;
        int maxD = 120;

        int randomR = (int) (Math.random() * ((maxR - minR) + 1)) + minR;
        int randomD = (int) (Math.random() * ((maxD - minD) + 1)) + minD;
        int pointX = (int) (cx + randomR * Math.cos(Math.toRadians(randomD)));
        int pointY = (int) (cy + randomR * Math.sin(Math.toRadians(randomD)));
        return new int[]{pointX, pointY};
    }


    public static void firePosMode(GameInstance game) throws Exception {

        game.dispatch.staticDelay(2);
        for(int redo = 0; redo <= 15; redo++){
            if(game.log.isInCity){
                game.dispatch("bottom_left");
            }
            else if(redo == 15){
                throw new GameException("stuck at change map");
            }
            else{
                break;
            }
        }

        game.dispatch.delay(2);
        game.dispatch.changeHorde(Integer.parseInt((String)game.posTarget.get("horde")));
        game.dispatch.delay(1);

        int buiX = Integer.parseInt((String)game.posTarget.get("buiX"));
        int buiY = Integer.parseInt((String)game.posTarget.get("buiY"));
        int telX = Integer.parseInt((String)game.posTarget.get("telX"));
        int telY = Integer.parseInt((String)game.posTarget.get("telY"));

        if(telX != 0 && telY != 0){
            game.dispatch.changePosition(telX, telY);
            game.dispatch("teleport");
        }

        if(buiX != 0 && buiY != 0){
            game.dispatch.changePosition(buiX, buiY);
            game.dispatch("tap_build");
        }
        game.dispatch.delay(1.5);
        game.dispatch("change_name");

        game.startEvent(GameStatus.initiate, "complete");
    }
}
