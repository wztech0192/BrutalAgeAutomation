package events.handler;

import events.common.Event;
import events.register.TestEvent;
import game.GameException;
import game.GameInstance;
import game.GameStatus;
import store.BuildHammer;
import util.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

public class CityWork {

    public static void fire(GameInstance game)  throws Exception{

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
                levelUpAllBuilding(game);

                if(game.account.getFeatureToggler().get("Upgrade Building")) {
                    hammerAction(game);
                }

                if(game.account.getBuildingLvl("stronghold") > 5) {
                    game.dispatch("flag");
                    game.dispatch("get_troop_info");

                    if (game.account.getFeatureToggler().get("Heal Troops") && game.log.shouldHeal) {
                        game.dispatch("healing_spring_access");
                    }

                    if (game.account.getFeatureToggler().get("Train Troops") && game.log.shouldTrain) {
                        if (game.account.getResource("wood") > 50000)
                            game.dispatch("warrior");
                    }
                }else{
                    if (game.account.getFeatureToggler().get("Train Troops")) {
                        if (game.account.getResource("wood") > 50000)
                            game.dispatch("warrior");
                    }
                }

                game.dispatch.pullAccountData(game.account.getId());


                if(game.account.getBuildingLvl("stronghold") >= 9 && game.log.marches > 0 && game.account.getFeatureToggler().get("Transport") && game.account.doInRound(2) ){
                    game.dispatch("transport");
                }

                Logger.log("-- Finish current round "+game.account.getRound());
                game.account.setRound(game.account.getRound()+1);
                if (game.account.getBuildingLvl("stronghold") >= 6 &&
                        game.log.marches > 0 &&
                        game.log.idleTroops > 0 &&
                        game.account.getFeatureToggler().get("Gathering")
                ) {
                    game.updateAccount();
                    game.dispatch("squirrel");
                    game.startEvent(GameStatus.world_map);
                } else {
                    game.updateAccount();
                    game.startEvent(GameStatus.initiate);
                }
            }
    }

    private static void hammerAction(GameInstance game) throws Exception {
        if (game.account.getPrimaryHammer().isAvailable()) {
            Logger.log("Use primary hammer");
            boolean isBuild = false;

            if(game.account.getBuildingLvl("portal") < game.account.getBuildingLvl("stronghold")){
                isBuild = hammerBuild(game, game.account.getPrimaryHammer(), "portal", game.account.getSecondaryHammer().getBuildingName());
            }else if (game.account.getBuildingLvl("stronghold") < 10){
                isBuild = hammerBuild(game, game.account.getPrimaryHammer(), "stronghold", game.account.getSecondaryHammer().getBuildingName());
            }
            if(!isBuild){
                if(!hammerBuild(game,
                        game.account.getPrimaryHammer(),
                        game.account.nextBuildingTarget(game.account.getPrimaryHammer()),
                        game.account.getSecondaryHammer().getBuildingName())){
                    game.account.getPrimaryHammer().setHammer(LocalDateTime.now().plusMinutes(30));
                }
            }
            game.updateAccount();
        }

        if (game.account.getFeatureToggler().get("Second Hammer")) {
            //use hammer
            if (game.account.getSecondaryHammer().isExpired() && game.dispatch("use_hammer")) {
                Logger.log("Expired secondary hammer, reuse");
                game.account.getSecondaryHammer().resetExpiration();
                game.updateAccount();
            }
        }

        if(game.account.getSecondaryHammer().isAvailable()) {
            String target = game.account.nextBuildingTarget(game.account.getPrimaryHammer());
            if (!target.equalsIgnoreCase("")) {
                Logger.log("Use secondary hammer");
                if (!hammerBuild(game, game.account.getSecondaryHammer(), target, game.account.getPrimaryHammer().getBuildingName())) {
                    game.account.getSecondaryHammer().setHammer(LocalDateTime.now().plusMinutes(30));
                }
                game.updateAccount();
            }
        }
    }


    private static void fireInitPhase(GameInstance game) throws Exception {
        game.dispatch("fire");


        bulkLevelUpBuilding(game, "stronghold", 3);

        bulkLevelUpBuilding(game, "portal", 3);

        game.dispatch("get_quest_gift");

        game.dispatch("squirrel");

        game.dispatch("get_all_mail");

        game.dispatch("workshop");

        game.dispatch("open_my_item");
        game.dispatch("use_all_resource");

        if(game.account.getBuildingLvl("stronghold") < 8 || game.account.doInRound(6)) {
            levelUpAllBuilding(game);
        }

        hammerAction(game);

        if (game.account.getFeatureToggler().get("Heal Troops")) {
            game.dispatch("healing_spring_access");
        }

        if (game.account.getFeatureToggler().get("Train Troops")) {
            game.dispatch("warrior");
        }

        game.dispatch("bottom_left");
        game.dispatch.delay(3);
        game.dispatch("build_init_outpost");
        game.account.setFinishInit(true);
    }

    private static boolean hammerBuild(GameInstance game, BuildHammer hammer, String building, String ignoreBuilding) throws Exception {
        Logger.log("Trying to upgrade " + building);
        if (!building.equalsIgnoreCase(ignoreBuilding)) {
            if(game.dispatch(building)) {
                if (game.log.btnName.contains("buttons_1:btn_1")) return true;

                //221 301 256 333
                int level = TestEvent.getNumber(game.dispatch.doOSR(221 ,301 ,256 ,333), true);

                if(level > 0 && level < 11){
                    Logger.log("Current level of "+building+" is "+level);
                    game.account.setBuildingLevel(building, level);
                    game.updateAccount();
                }

                game.log.buildingCompleteLevel = 1;
                if (game.dispatch("upgrade_building")) {
                    game.dispatch.delay(2);
                    if (building.equalsIgnoreCase("stronghold") && game.account.getBuildingLvl("stronghold") == 5) {
                        game.dispatch("confirm_stronghold_6");
                    }
                    if (hammer == game.account.getSecondaryHammer()) {
                        if (!game.dispatch("test_upgrade_secondary")) {
                            if (game.account.getFeatureToggler().get("Second Hammer")) {
                                game.dispatch(Event.builder().setLoc(359, 890).setDelay(1.5));
                                hammer.resetExpiration();
                                game.dispatch("upgrade_building");
                            }else{
                                game.dispatch(Event.builder().setLoc(80, 250).setDelay(1.5));
                                return false;
                            }
                        }
                    }
                    if (game.log.buildingCompleteLevel != 1) {
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
                        hammer.setHammer(LocalDateTime.now().plusMinutes(30));
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


    private static boolean buildingAction(GameInstance game, String building, String btn) throws Exception {
        game.dispatch(building);
        return game.dispatch(btn);
    }

    private static void bulkLevelUpBuilding(GameInstance game, String building, int lvl) throws Exception {
        while (game.account.getBuildingLvl(building) < lvl) {
            if (buildingAction(game, building, "upgrade_building_buy")) {
                game.account.levelUpBuilding(game, building);
                if (building.equalsIgnoreCase("golden_tree")) {
                    game.dispatch("golden_tree_access");
                }
            } else {
                break;
            }
        }
    }


    private static void levelUpAllBuilding(GameInstance game) throws Exception {
        if(game.account.getBuildingLvl("stronghold") >= 5)
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


        for (int i = 1; i <= 5; i++) {
            bulkLevelUpBuilding(game, "well" + i, 3);
        }


        for (int i = 1; i <= 5; i++) {
            bulkLevelUpBuilding(game, "warhub" + i, 3);
        }
        game.dispatch("get_quest_gift");

    }

    public static void firePosMode(GameInstance game) throws Exception {

            game.dispatch.delay(2);

            game.posTarget.put("stronghold", true);
            for(int i=0; i<2; i++) {
                buildingAction(game, "stronghold", "upgrade_building_buy");
            }

            game.posTarget.put("portal", true);
            for(int i=0; i<2; i++) {
                buildingAction(game, "portal", "upgrade_building_buy");
            }

            buildingAction(game, "warehouse", "upgrade_building_buy");
            game.posTarget.put("warehouse", true);
            for(int i=0; i<2; i++) {
                buildingAction(game, "warehouse", "upgrade_building_buy");
            }
            game.dispatch("get_quest_gift_single");
            game.dispatch.staticDelay(0.5);
            game.dispatch("get_quest_gift_single");
            game.dispatch.staticDelay(0.5);
            game.dispatch("get_quest_gift_single");
            game.dispatch.staticDelay(0.5);

            if(game.posTarget.containsKey("temp")){

                game.store.metadata.getSavedPosAcc().add((String)game.posTarget.get("temp"));
                game.store.marshellMetadata();
                game.store.updatePosSavedAcc();

                game.startEvent(GameStatus.initiate, "positioning");
            }else{
                game.startEvent(GameStatus.world_map, "positioning");
            }

    }
}
