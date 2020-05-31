package events.handler;

import game.GameInstance;
import game.GameStatus;

import java.time.Duration;
import java.time.LocalDateTime;

public class WhenStart {

    public static void fire(GameInstance game) throws Exception {

        game.dispatch.staticDelay(2);

        if (game.dispatch.requirePullFile) {
            game.dispatch.pullAccountData(game.account.getId());
            game.dispatch.requirePullFile = false;
        }

        if (game.account != null) {
            game.dispatch.staticDelay(1.5);
            if (game.account.getBuildingLvl("stronghold") >= 6) {
                game.dispatch("top_left");
                if (game.log.btnName.contains("profile")) {
                    game.dispatch("top_left");
                }
            }
            if (game.account.getBuildingLvl("stronghold") >= 10) {
                game.dispatch("top_left");
                if (game.log.btnName.contains("profile")) {
                    game.dispatch("top_left");
                }
            }
        }

        for (int redo = 0; redo < 5; redo++) {
            game.dispatch("login_test");

            game.dispatch.staticDelay(1.5);

            game.dispatch("login_test");

            game.dispatch("login_zoom");
            game.dispatch.staticDelay(1);


            if (Math.abs(game.log.city.x - -3222) + Math.abs(game.log.city.y - -1600) < 10 ||
                    Math.abs(game.log.city.x - -3407) + Math.abs(game.log.city.y - -730) < 10 ||
                    Math.abs(game.log.city.x - -1532) + Math.abs(game.log.city.y - -427) < 10 ||
                    Math.abs(game.log.city.x - -340) + Math.abs(game.log.city.y - -268) < 10 ||
                    Math.abs(game.log.city.x - -2484) + Math.abs(game.log.city.y - -1677) < 10
            ) {
                break;
            }
        }


        if (game.store.metadata.getFeatureToggler().getGlobalFeatures().get("Feed Temple")) {
            if (game.account.getTroops() < game.account.getNumberFeaturer().getNumberSetting().get("Min Troop")) {
                game.startEvent(GameStatus.initiate);
            } else {
                game.startEvent(GameStatus.world_map);
            }
            return;
        }

        game.dispatch("get_rss_info");


        if (game.store.metadata.getFeatureToggler().getGlobalFeatures().get("Transport At Start")) {
            int transportRound = game.account.getNumberFeaturer().getNumberSetting().get("Transport Round");
            if (game.account.getBuildingLvl("stronghold") >= 9 && transportRound > 0
                    && game.account.doInRound(transportRound)) {
                game.dispatch("flag");
                game.dispatch("get_troop_info");
                CityWork.transport(game);
            }
        }


        if (!game.store.metadata.getFeatureToggler().getGlobalFeatures().get("No Clan") && game.account.getBuildingLvl("stronghold") >= 6) {
            game.dispatch("help");
        }

        if (!game.account.isFinishInit()) {
            game.account.setPreviousLevel(1);
            game.updateAccount();
            game.dispatch.changeHorde(game.account.getHorde());
        }

        if (game.account.getName() == null || game.account.getName().equalsIgnoreCase("")) {
            game.dispatch("assign_id");
        }

        if (!game.account.isJoinClan()) {

            if(!game.store.metadata.getFeatureToggler().getGlobalFeatures().get("No Clan")) {
                game.dispatch("apply_clan");
                if (game.account.getClan() != null && !game.account.getClan().equalsIgnoreCase("")) {
                    game.dispatch("search_clan");
                    game.dispatch.enterText(game.account.getClan());
                    game.dispatch("confirm_search_clan");
                }
                game.dispatch("join_clan");
                game.dispatch("back_from_clan");
                game.account.setJoinClan(true);
                game.updateAccount();
            }
        }else if(game.store.metadata.getFeatureToggler().getGlobalFeatures().get("No Clan")){
            game.dispatch("open_clan");
            game.dispatch("quit_clan");
            game.account.setJoinClan(false);
            game.updateAccount();
        }



        if (game.account.getPreviousLevel() == 0 ||
                (game.account.getLevel() >= 10 && game.account.getLevel() <= 12 && game.account.getPreviousLevel() != game.account.getLevel())) {
            game.dispatch("open_talent");
            game.dispatch("use_talent");
            game.account.setPreviousLevel(game.account.getLevel());
            game.updateAccount();
        }


        if (game.account.isOver()) {
            if (game.account.getFeatureToggler().get("Hit Monster (8+)")) {
                if (game.account.getBuildingLvl("stronghold") == 8) {
                    game.dispatch("monster_access");
                } else if (game.account.getBuildingLvl("stronghold") > 8) {
                    if (game.account.doInRound(3)) {
                        game.dispatch("monster_access");
                    }
                }
            }

            if (game.account.getFeatureToggler().get("Use Squirrel")) {
                game.dispatch("squirrel");

                if (game.account.getHorde() == 4) {
                    game.dispatch("pig");
                }
            }

            if (game.account.getFeatureToggler().get("Read Mails") && game.account.doInRound(4)) {
                if (game.account.getLevel() > 4) {
                    game.dispatch("get_all_mail");
                }
            }

            if (game.account.getFeatureToggler().get("Use Workshop")) {
                game.dispatch("workshop");
            }


            if (game.account.isFinishInit() && game.account.getFeatureToggler().get("Use Resource") && (game.account.getBuildingLvl("stronghold") < 10 || game.account.doInRound(3))) {
                game.dispatch("open_my_item");
                game.dispatch("use_all_resource");
            }
            game.account.setLastRound(LocalDateTime.now());
        }

        if (game.account.getFeatureToggler().get("Use Golden Tree")) {
            if ((game.account.getPrimaryHammer().isAvailable() && game.account.getPrimaryHammer().getBuildingName().equals("golden_tree")) ||
                    (game.account.getSecondaryHammer().isAvailable() && game.account.getSecondaryHammer().getBuildingName().equals("golden_tree"))
            ) {
                game.dispatch("golden_tree_access");
            }
        }

        if (game.account.getLastGiftTime() == null || Duration.between(game.account.getLastGiftTime(), LocalDateTime.now()).toHours() > 24) {
            game.dispatch("daily_reward");
            if (game.account.getBuildingLvl("golden_tree") > 3) {
                if (game.account.getFeatureToggler().get("Use Golden Tree")) {
                    if (!game.account.getPrimaryHammer().getBuildingName().equals("golden_tree") &&
                            !game.account.getSecondaryHammer().getBuildingName().equals("golden_tree")
                    ) {
                        game.dispatch("golden_tree_access");
                    }
                }
            }
            game.account.setLastGiftTime(LocalDateTime.now());
        }

        game.startEvent(GameStatus.city_work);

    }

    public static void firePosMode(GameInstance game) throws Exception {

        game.dispatch.staticDelay(2);
        game.dispatch("login_test");
        game.dispatch("login_zoom");
        game.startEvent(GameStatus.city_work);

    }
}
