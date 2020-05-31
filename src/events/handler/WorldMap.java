package events.handler;

import events.register.TestEvent;
import events.register.WorldMapEvents;
import game.GameException;
import game.GameInstance;
import game.GameStatus;
import store.MatchPoint;
import util.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorldMap {
    public static void fire(GameInstance game) throws Exception {
        game.dispatch.delay(1);

        if (game.store.metadata.getFeatureToggler().getGlobalFeatures().get("Feed Temple")) {
            game.dispatch("feedTemple");
            game.startEvent(GameStatus.initiate);
            return;
        }

        for (int redo = 0; redo <= 15; redo++) {
            if (game.log.isInCity) {
                game.dispatch("bottom_left");
            } else if (redo == 15) {
                throw new GameException("stuck at change map");
            } else {
                break;
            }
        }

        game.dispatch.delay(2);


        if (game.store.metadata.getFeatureToggler().getGlobalFeatures().get("Auto Repair")) {
            int[] oldMap = game.log.worldCurr.clone();
            game.dispatch("auto_repair");
            game.dispatch("change_outpost");
            if (!Arrays.equals(oldMap, game.log.worldCurr)) {
                game.dispatch("auto_repair");
            } else {
                game.dispatch("change_outpost");
                if (!Arrays.equals(oldMap, game.log.worldCurr)) {
                    game.dispatch("auto_repair");
                }
            }
        }

        if (game.account.getFeatureToggler().get("Gathering (6+)")) {
            if (game.account.getBuildingLvl("stronghold") < 10) {
                game.dispatch("get_rss_info");
            }


            if (game.log.idleTroops >= 5000 && !game.account.isRandomized() && game.log.marches >= 3) {
                int[] randomCoordinate;
                int redo = 10;
                do {
                    randomCoordinate = WorldMapEvents.getRandomCoordinate(180, 230, -100, 150);
                    game.dispatch.changePosition(randomCoordinate[0], randomCoordinate[1]);
                    if (game.dispatch("safe_teleport")) {
                        game.account.setRandomized(true);
                        game.updateAccount();
                        break;
                    }
                } while (redo-- > 0);
            }


            game.dispatch("world_set");

            if (game.store.metadata.getFeatureToggler().getGlobalFeatures().get("Agatha Search")) {
                agathaSearch(game);
            } else {
                if (game.account.isRandomized()) {
                    for (int i = 0; i < 4; i++) {
                        if (game.log.worldCurr[0] < 270 && game.log.worldCurr[1] < 500) {
                            game.dispatch("change_outpost");
                            game.dispatch("world_set");
                        } else {
                            break;
                        }
                    }
                }
                normalSearch(game);
            }
            game.startEvent(GameStatus.initiate);
        }
    }

    public static SearchOptions createSearchOptions(GameInstance game){

        SearchOptions searchOptions = new SearchOptions();
        searchOptions.targets = game.account.getGatherPrioritiesArray();

        if ( searchOptions.targets.isEmpty()) {
            if (game.account.getResource("meat") < 100000) {
                if (game.account.isRssLessThan("meat", "wood")) {
                    if (game.log.idleTroops > 2500) {
                        searchOptions.targets.add("meat3");
                    } else {
                        searchOptions.targets.add("meat2");
                    }
                    if (game.log.idleTroops > 4500) {
                        searchOptions.targets.add("meat4");
                    }

                }
            } else if (game.account.getBuildingLvl("stronghold") >= 8 && game.account.isRssLessThan("rock", "wood") && game.log.idleTroops > 2500) {
                searchOptions.targets.add("rock");
            } else {
                if (game.log.idleTroops > 2500) {
                    searchOptions.targets.add("wood3");
                } else {
                    searchOptions.targets.add("wood2");
                }
                if (game.log.idleTroops > 4500) {
                    searchOptions.targets.add("wood4");
                }
            }

            if (game.log.idleTroops > 2500) {
                searchOptions.maxLvl = 3;
            } else {
                searchOptions.maxLvl = 2;
            }
            if (game.log.idleTroops > 4500) {
                searchOptions.maxLvl = 4;
                searchOptions.minLvl = 2;
            }
        } else {
            searchOptions.minLvl = game.account.getNumberFeaturer().getNumberSetting().get("Min Monster Level");
            searchOptions.maxLvl = game.account.getNumberFeaturer().getNumberSetting().get("Max Monster Level");
        }

        return searchOptions;
    }


    public static void agathaSearch(GameInstance game) throws Exception{
        int redo = 15;

        ArrayList<String> temp = new ArrayList<>();
        SearchOptions searchOptions = createSearchOptions(game);
        for(String target:searchOptions.targets){
            target = target.replaceAll("[0-9]", "").trim();
            if(!temp.contains(target)){
                temp.add(target+"_rss");
                temp.add(target);
            }
        }
        searchOptions.targets = temp;

        game.dispatch.resetAgathaSearch();
        ArrayList<String> prevSets = new ArrayList<>();
        prevSets.add(game.log.worldCurr[0]+""+game.log.worldCurr[1]);

        for(String target:searchOptions.targets){
            if(game.log.idleTroops <= 0 || game.log.marches <= 0 || redo-- <= 0){
                break;
            }

            String sets;
            for(int lvl=searchOptions.maxLvl; lvl >= searchOptions.minLvl; lvl--){
                for(int j =0; j<10; j++){
                    game.dispatch.agathaSearchClick(target, lvl);
                    game.dispatch("world_set");
                    Logger.log("**Start search");
                    sets = game.log.worldCurr[0]+""+game.log.worldCurr[1];
                    if(prevSets.contains(sets)){
                        Logger.log("** No match because not found");
                        break;
                    }else {
                        game.dispatch("select_monster");
                        if (game.dispatch("attack_monster_test")) {
                            Logger.log("good, attack the monster");
                            game.dispatch("attack_monster");
                            game.log.marches--;
                            game.log.idleTroops -= game.log.currTroops;
                            Logger.log("After Deplay" + game.log.currTroops + " Now Idle Troops: " + game.log.idleTroops);
                            if (game.log.marches <= 0 || game.log.idleTroops <= 0) {
                                lvl = 0;
                                break;
                            }
                        }
                        prevSets.add(sets);
                    }
                    Logger.log("**Find match");
                }
            }
        }
    }

    private static void normalSearch(GameInstance game) throws Exception {

            int loc = 0;
            int[] locArray = new int[]{20, -20, -20, 20, 35, 35, -35, -35};
            int[] setLoc = game.log.worldCurr.clone();

            SearchOptions searchOptions;

            while (game.log.idleTroops > 0 && game.log.marches > 0 && loc < locArray.length) {
                game.dispatch("adjust_map");

                searchOptions = createSearchOptions(game);

                List<MatchPoint> matches = game.dispatch.getMonsterMatch(3, searchOptions.targets);

                game.dispatch.delay(1);
                for (int i = 0; i < matches.size(); i++) {
                    MatchPoint px = matches.get(i);
                    game.dispatch.selectMonster(px.x, px.y);
                    if (game.dispatch("attack_monster_test")) {

                        int monsterLvl = TestEvent.getNumber(game.dispatch.doOSR(223, 504, 241, 522), true);
                        Logger.log("Monster level " + monsterLvl + ": Range " + searchOptions.minLvl + " - " + searchOptions.maxLvl);
                        if (monsterLvl >= searchOptions.minLvl && monsterLvl <= searchOptions.maxLvl) {
                            Logger.log("good, attack the monster");
                            game.dispatch("attack_monster");
                            game.log.marches--;
                            game.log.idleTroops -= game.log.currTroops;
                            Logger.log("After Deplay" + game.log.currTroops + " Now Idle Troops: " + game.log.idleTroops);
                            if (game.log.marches <= 0 || game.log.idleTroops <= 0) {
                                break;
                            }
                        } else {
                            Logger.log("not good, find other monster");
                        }
                    }
                    if (i + 1 != matches.size()) {
                        game.dispatch.changePosition(setLoc[0], setLoc[1]);
                        game.dispatch("adjust_map");
                    }
                }
                setLoc[0] = game.log.worldCurr[0] + locArray[loc++];
                setLoc[1] = game.log.worldCurr[1] + locArray[loc++];
                game.dispatch.changePosition(setLoc[0], setLoc[1]);
            }
        }



    public static void firePosMode(GameInstance game) throws Exception {
        game.dispatch.staticDelay(2);
        for (int redo = 0; redo <= 15; redo++) {
            if (game.log.isInCity) {
                game.dispatch("bottom_left");
            } else if (redo == 15) {
                throw new GameException("stuck at change map");
            } else {
                break;
            }
        }

        game.dispatch.delay(2);
        game.dispatch.changeHorde(Integer.parseInt((String) game.posTarget.get("horde")));
        game.dispatch.delay(1);

        int buiX = Integer.parseInt((String) game.posTarget.get("buiX"));
        int buiY = Integer.parseInt((String) game.posTarget.get("buiY"));
        int telX = Integer.parseInt((String) game.posTarget.get("telX"));
        int telY = Integer.parseInt((String) game.posTarget.get("telY"));

        if (telX != 0 && telY != 0) {
            game.dispatch.changePosition(telX, telY);
            game.dispatch("teleport");
        }

        if (buiX != 0 && buiY != 0) {
            game.dispatch.changePosition(buiX, buiY);
            game.dispatch("tap_build");
        }
        game.dispatch.delay(1.5);
        game.dispatch("change_name");
        game.posTarget.put("status", "complete");
        game.store.sendDataBack("update",  game.posTarget);
        game.startEvent(GameStatus.initiate, "complete");
    }
}


class SearchOptions{
    ArrayList<String> targets;
    int maxLvl = 4;
    int minLvl = 2;
}