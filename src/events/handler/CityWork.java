package events.handler;

import events.common.Event;
import events.register.TestEvent;
import game.GameException;
import game.GameInstance;
import game.GameStatus;
import store.Account;
import store.BuildHammer;
import util.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

public class CityWork {

    public static void fire(GameInstance game) throws Exception {
        game.account.validateBuildings();
        game.account.refreshHammerData();
        game.updateAccount();

        game.dispatch.delay(2);
        if (!game.account.isFinishInit()) {
            Logger.log("**fire init pahse");
            fireInitPhase(game);
            game.account.setLastRound(LocalDateTime.now());
            game.dispatch.pullAccountData(game.account.getId());
            game.updateAccount();
            game.startEvent(GameStatus.initiate);
        } else {
            Logger.log("**fire regular pahse");

            if (game.account.getBuildingLvl("stronghold") < 8 || game.account.doInRound(3)) {
                levelUpAllBuilding(game);
            }

            if (game.account.getFeatureToggler().get("Upgrade Building")) {
                hammerAction(game);
            }

            if (game.account.getBuildingLvl("stronghold") >= 7) {
                game.dispatch("flag");
                game.dispatch("get_troop_info");

                if (game.account.getFeatureToggler().get("Heal Troops") && game.log.shouldHeal) {
                    game.dispatch("healing_spring_access");
                }
                if (game.log.shouldTrain) {
                    if (game.account.getFeatureToggler().get("Train Warrior")) {
                        game.dispatch("warrior");
                    }
                    if (game.account.getFeatureToggler().get("Train Rider (7+)")) {
                        game.dispatch("rider");
                    }
                    if (game.account.getFeatureToggler().get("Train Shaman (7+)")) {
                        game.dispatch("shaman");
                    }
                }

                if(game.account.getFeatureToggler().get("!Speed Train!")){
                    for(int i=0;i<10;i++){
                        if(!game.dispatch("speed_warrior") || !game.dispatch("warrior")){
                            break;
                        }
                    }
                    game.account.getFeatureToggler().set("!Speed Train!", false);
                }


            } else if (game.account.getFeatureToggler().get("Train Warrior")) {
                game.dispatch("warrior");
            }

            if (!game.store.metadata.getFeatureToggler().getGlobalFeatures().get("Transport At Start")) {
                transport(game);
            }

            game.dispatch.pullAccountData(game.account.getId());

            Logger.log("-- Finish current round " + game.account.getRound());
            game.account.setRound(game.account.getRound() + 1);

            game.updateAccount();
            if (game.account.getBuildingLvl("stronghold") >= 6 &&
                    game.log.marches > 0 &&
                    game.log.idleTroops > 0 &&
                    (game.account.getFeatureToggler().get("Gathering (6+)")  || game.store.metadata.getFeatureToggler().getGlobalFeatures().get("Auto Repair"))
            ) {
                game.startEvent(GameStatus.world_map);
            } else {
                game.startEvent(GameStatus.initiate);
            }
        }
    }

    static void transport(GameInstance game) throws Exception {
        int transportRound = game.account.getNumberFeaturer().getNumberSetting().get("Transport Round");
        if (game.account.getBuildingLvl("stronghold") >= 9 && game.log.marches > 0 && transportRound > 0
                && game.account.doInRound(transportRound)) {
            game.dispatch("transport");
            game.dispatch("squirrel");
        }
    }

    private static void hammerAction(GameInstance game) throws Exception {
        if (game.account.getPrimaryHammer().isAvailable()) {
            Logger.log("Use primary hammer");
            boolean isBuild = false;

            if (game.account.getBuildingLvl("portal") < game.account.getBuildingLvl("stronghold")) {
                isBuild = hammerBuild(game, game.account.getPrimaryHammer(), "portal", game.account.getSecondaryHammer().getBuildingName());
            } else if( game.account.getBuildingLvl("stronghold") < 10 ){
                isBuild = hammerBuild(game, game.account.getPrimaryHammer(), "stronghold", game.account.getSecondaryHammer().getBuildingName());
            }

            if (!isBuild) {
                if (!hammerBuild(game,
                        game.account.getPrimaryHammer(),
                        nextBuildingTarget(game.account, game.account.getSecondaryHammer()),
                        game.account.getSecondaryHammer().getBuildingName())) {

                    if( game.account.getBuildingLvl("stronghold") >= 10 ){
                        hammerBuild(game, game.account.getPrimaryHammer(), "stronghold", game.account.getSecondaryHammer().getBuildingName());
                    }
                    // game.account.getPrimaryHammer().setHammer(LocalDateTime.now().plusMinutes(30));
                }
            }
            game.updateAccount();
        }

        if (game.account.isFinishInit()) {
            if (game.account.getFeatureToggler().get("Second Hammer")) {
                //use hammer
                if (game.account.getSecondaryHammer().isExpired() && game.dispatch("use_hammer")) {
                    Logger.log("Expired secondary hammer, reuse");
                    game.account.getSecondaryHammer().resetExpiration();
                    game.updateAccount();
                }
            }

            if (game.account.getSecondaryHammer().isAvailable()) {
                String target = nextBuildingTarget(game.account, game.account.getPrimaryHammer());
                if (!target.equalsIgnoreCase("")) {
                    Logger.log("Use secondary hammer");
                    if (!hammerBuild(game, game.account.getSecondaryHammer(), target, game.account.getPrimaryHammer().getBuildingName())) {
                        //   game.account.getSecondaryHammer().setHammer(LocalDateTime.now().plusMinutes(30));
                    }
                    game.updateAccount();
                }
            }
        }
    }

    private static String nextBuildingTarget(Account account, BuildHammer hammer) {

        String highestWell = "well1";
        String highestWarhub = "warhub1";

        for (int i = 2; i <= 5; i++) {
            if (account.getBuildingLvl(highestWell) < account.getBuildingLvl("well" + i)) {
                highestWell = "well" + i;
            }
            if (account.getBuildingLvl(highestWarhub) < account.getBuildingLvl("warhub" + i)) {
                highestWarhub = "warhub" + i;
            }
        }

        if (buildingCondition(account, highestWarhub, 5, hammer)) {
            return highestWarhub;
        } else if (buildingCondition(account, highestWell, 6, hammer)) {
            return highestWell;
        } else if (buildingCondition(account, "war_camp", 5, hammer)) {
            return "war_camp";
        } else if (buildingCondition(account, "research", 7, hammer)) {
            return "research";
        } else if (buildingCondition(account, "warehouse", 8, hammer)) {
            return "warehouse";
        } else if (buildingCondition(account, "help_wagon", 7, hammer)) {
            return "help_wagon";
        } else if (buildingCondition(account, "golden_tree", 9, hammer)) {
            return "golden_tree";
        } else if (buildingCondition(account, "help_wagon", 25, hammer)) {
            return "help_wagon";
        }

        int lowestLvl = 50;
        String lowest = "";
        if (buildingCondition(account, "war_camp", 13, hammer)) {
            lowest = "war_camp";
            lowestLvl = account.getBuildingLvl("war_camp");
        }
        for (int i = 1; i <= 5; i++) {
            if (buildingCondition(account, "well" + i, 10, hammer)) {
                if (account.getBuildingLvl("well" + i) < lowestLvl) {
                    lowest = "well" + i;
                    lowestLvl = account.getBuildingLvl("well" + i);
                }
            }
            if (buildingCondition(account, "warhub" + i, 7, hammer)) {
                if (account.getBuildingLvl("warhub" + i) < lowestLvl) {
                    lowest = "warhub" + i;
                    lowestLvl = account.getBuildingLvl("warhub" + i);
                }
            }
        }
        if(!lowest.equalsIgnoreCase("")){
            return lowest;
        }

        if (account.getBuildingLvl("stronghold") >= 10) {
            if (buildingCondition(account, "defense_hall", 10, hammer)) {
                return "defense_hall";
            } else if (buildingCondition(account, "tower", 12, hammer)) {
                return "tower";
            }
        }

        return lowest;
    }


    private static boolean buildingCondition(Account account, String target, int maxLvl, BuildHammer hammer) {
        return account.getBuildingLvl(target) != 0
                && !target.equalsIgnoreCase(hammer.getBuildingName())
                && account.getBuildingLvl(target) < account.getBuildingLvl("stronghold")
                && account.getBuildingLvl(target) < maxLvl;
    }


    private static void fireInitPhase(GameInstance game) throws Exception {
        game.dispatch("fire");


        bulkLevelUpBuilding(game, "stronghold", 3);

        boolean dragTut = game.account.getBuildingLvl("portal") < 3;
        bulkLevelUpBuilding(game, "portal", 3);

        if(dragTut){
            game.dispatch("dragon_tutorial");
        }

        game.dispatch("open_my_item");
        game.dispatch("use_all_resource");

        game.dispatch("get_quest_gift");

        levelUpAllBuilding(game);

        hammerAction(game);

        if (game.account.getFeatureToggler().get("Heal Troops")) {
            game.dispatch("healing_spring_access");
        }

        game.dispatch("warrior");

        game.dispatch("bottom_left");
        game.dispatch.delay(3);
        game.dispatch("build_init_outpost");
        game.account.setFinishInit(true);
    }

    private static boolean hammerBuild(GameInstance game, BuildHammer hammer, String building, String ignoreBuilding) throws Exception {
        Logger.log("Trying to upgrade " + building);
        if (!building.equalsIgnoreCase(ignoreBuilding)) {
            if (game.dispatch(building)) {
                if (game.log.btnName.contains("buttons_1:btn_1")) return true;
                game.dispatch.staticDelay(1.25);
                //221 301 256 333
                int level = Math.abs(TestEvent.getNumber(game.dispatch.doOSR(221, 301, 256, 333), true));
                Logger.log("Scan level of " + building + " is " + level + " / " + game.account.getBuildingLvl(building));
                if (level > 3) {
                    if (game.account.getBuildingLvl(building) != level) {
                        if (level < 25) {
                            game.account.setBuildingLevel(building, level);
                            game.updateAccount();
                            game.dispatch("top_left");
                            hammerAction(game);
                            return true;
                        }
                    }
                }

                game.log.buildingCompleteLevel = 1;
                if (game.dispatch("upgrade_building")) {
                    game.dispatch.delay(2);
                    if (building.equalsIgnoreCase("stronghold")) {
                        if (game.account.getBuildingLvl("stronghold") == 5) {
                            game.dispatch("confirm_stronghold_6");
                        } else if (
                                game.account.getBuildingLvl("stronghold") == 6 ||
                                game.account.getBuildingLvl("stronghold") == 7 ||
                                game.account.getBuildingLvl("stronghold") == 8 ||
                                game.account.getBuildingLvl("stronghold") == 11
                        ) {
                            if (game.dispatch("stronghold_speed_up")) {
                                game.account.setBuildingLevel("stronghold", game.account.getBuildingLvl("stronghold") + 1);
                                game.updateAccount();
                                hammerAction(game);
                                return true;
                            }
                        }
                    }

                    if (hammer == game.account.getSecondaryHammer()) {
                        if (!game.dispatch("test_upgrade_secondary")) {
                            if (game.account.getFeatureToggler().get("Second Hammer")) {
                                game.dispatch(Event.builder().setLoc(359, 890).setDelay(1.5));
                                hammer.resetExpiration();
                                game.dispatch("upgrade_building");
                            } else {
                                game.dispatch(Event.builder().setLoc(80, 250).setDelay(1.5));
                                return false;
                            }
                        }
                    }
                    if (game.log.buildingCompleteLevel != 1) {
                        game.dispatch("tap_building");
                        hammer.setHammer(game.log.buidlingCompleteTime);
                        hammer.setBuildingName(building);
                        hammer.setNextBuildingLevel(game.log.buildingCompleteLevel);
                    }
                    return true;
                    //game.account.setBuildingLevel(game, building, game.log.buildingCompleteLevel);
                } else {
                    game.dispatch("test_upgrade_building");
                    if (game.log.btnName.contains("research")) {
                        game.dispatch.delay(3);
                        building = game.dispatch.findClosestBuilding(game);
                        Logger.log("require " + building + " to upgrade!!!!");
                        return hammerBuild(game, hammer, building, ignoreBuilding);
                    } else if (game.log.btnName.contains("add")) {
                        game.dispatch.delay(1.5);
                        //   hammer.setHammer(LocalDateTime.now().plusMinutes(30));
                        game.dispatch("top_left");
                        game.dispatch("top_left");
                        Logger.log("No Resource, try next time");
                        return false;
                    }
                }
            }
        }
        return false;
    }


    private static boolean buildingAction(GameInstance game, String building, String btn, int buyTime) throws Exception {
        game.dispatch(building);

        for(int i=0;i<buyTime;i++){
            game.dispatch(btn);
        }
        return game.dispatch("top_left");
    }

    private static void bulkLevelUpBuilding(GameInstance game, String building, int lvl) throws Exception {

        if(game.account.getBuildingLvl(building) == 0){
            game.dispatch(building);
          if(  game.dispatch("upgrade_building_buy")){
              game.account.levelUpBuilding(game, building);
          }
        }

        while (game.account.getBuildingLvl(building) < lvl) {
            int levelTime = lvl - game.account.getBuildingLvl(building);
            if (buildingAction(game, building, "upgrade_building_buy", levelTime)) {
                for(int i=0;i<levelTime; i++){
                    game.account.levelUpBuilding(game, building);
                }
                if (building.equalsIgnoreCase("golden_tree")) {
                    game.dispatch("golden_tree_access");
                }
            } else {
                break;
            }
        }
    }


    private static void levelUpAllBuilding(GameInstance game) throws Exception {
        if (game.account.getBuildingLvl("stronghold") >= 5)
            bulkLevelUpBuilding(game, "help_wagon", 2);

        bulkLevelUpBuilding(game, "war_camp", 2);
        bulkLevelUpBuilding(game, "healing_spring", 2);
        bulkLevelUpBuilding(game, "research", 2);
        bulkLevelUpBuilding(game, "warehouse", 3);

        bulkLevelUpBuilding(game, "tower", 3);
        bulkLevelUpBuilding(game, "golden_tree", 3);

        if (game.account.getBuildingLvl("warhub1") < 3) {
            game.dispatch("get_quest_gift");
        }

        int wantedLevel = game.account.getBuildingLvl("stronghold") >= 5 ? 4 : 3;
        for (int i = 1; i <= 5; i++) {
            bulkLevelUpBuilding(game, "well" + i, wantedLevel);
        }

        for (int i = 1; i <= 5; i++) {
            bulkLevelUpBuilding(game, "warhub" + i, wantedLevel);
        }

        if (game.account.getBuildingLvl("stronghold") >= 10) {
            bulkLevelUpBuilding(game, "defense_hall", 3);
            if(game.account.getBuildingLvl("defense_hall") == 0){
                game.account.setBuildingLevel("defense_hall", 1);
            }
        }

        if (game.account.getBuildingLvl("defense_hall") >= 3) {
            bulkLevelUpBuilding(game, "war_hall", 3);
        }

        game.dispatch("get_quest_gift");
        game.updateAccount();

    }

    public static void firePosMode(GameInstance game) throws Exception {

        game.dispatch.delay(2);

        game.posTarget.put("stronghold", true);
            buildingAction(game, "stronghold", "upgrade_building_buy", 2);

        game.posTarget.put("portal", true);
            buildingAction(game, "portal", "upgrade_building_buy", 2);

        game.dispatch("dragon_tutorial");

        buildingAction(game, "warehouse", "upgrade_building_buy", 1);
        game.posTarget.put("warehouse", true);
            buildingAction(game, "warehouse", "upgrade_building_buy", 2);

        game.dispatch("get_quest_gift_single");
        game.dispatch.staticDelay(0.5);
        game.dispatch("get_quest_gift_single");
        game.dispatch.staticDelay(0.5);
        game.dispatch("get_quest_gift_single");
        game.dispatch.staticDelay(0.5);

        if (game.posTarget.containsKey("temp")) {

            game.store.metadata.getSavedPosAcc().add((String) game.posTarget.get("temp"));
            game.store.marshellMetadata();
            game.store.updatePosSavedAcc();

            game.startEvent(GameStatus.initiate, "positioning");
        } else {
            game.startEvent(GameStatus.world_map, "positioning");
        }

    }
}
