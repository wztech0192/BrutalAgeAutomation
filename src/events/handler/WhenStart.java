package events.handler;

import game.GameException;
import game.GameInstance;
import game.GameStatus;

import java.time.Duration;
import java.time.LocalDateTime;

public class WhenStart {

    public static void fire(GameInstance game) throws Exception {

        game.dispatch.delay(3);

        if(game.dispatch.requirePullFile){
            game.dispatch.pullAccountData(game.account.getId());
            game.dispatch.requirePullFile = false;
        }

        if(game.account != null){
            game.dispatch.delay(3);
            if(game.account.getBuildingLvl("stronghold") >= 9){
                game.dispatch("top_left");
                game.dispatch("top_left");
            }
        }
        game.dispatch("login_test");

        game.dispatch("get_rss_info");

        game.dispatch("login_test");

        if(!game.account.isFinishInit()){
            game.dispatch.changeHorde(game.account.getHorde());
        }

        if(!game.account.isJoinClan()){

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


        if(game.account.getPreviousLevel()  == 0 ||
                (game.account.getLevel() >= 7 && game.account.getLevel() <= 12 && game.account.getPreviousLevel() != game.account.getLevel())){
            game.dispatch("open_talent");
            game.dispatch("use_talent");
            game.account.setPreviousLevel(game.account.getLevel());
            game.updateAccount();
        }


        if(game.account.isOver()) {

            if (game.account.getFeatureToggler().get("Hit Monster")) {
                if(game.account.getBuildingLvl("stronghold") >= 8){
                    game.dispatch("monster_access");
                }
            }

            if (game.account.getFeatureToggler().get("Use Squirrel")) {
                game.dispatch("squirrel");
            }

            if(game.account.getFeatureToggler().get("Read Mails") && game.account.doInRound(6)) {
                if (game.account.getLevel() > 4) {
                    game.dispatch("get_all_mail");
                }
            }

            if (game.account.getFeatureToggler().get("Use Workshop") ) {
                game.dispatch("workshop");
            }

            if(game.account.getFeatureToggler().get("Use Golden Tree")) {
                if (game.account.getBuildingLvl("golden_tree") > 3) {
                    if(!game.account.getPrimaryHammer().getBuildingName().equals("golden_tree") &&
                            !game.account.getSecondaryHammer().getBuildingName().equals("golden_tree")
                    ) {
                        game.dispatch("golden_tree_access");
                    }
                }
            }

            if (game.account.getFeatureToggler().get("Use Resource") && game.account.doInRound(6)) {
                game.dispatch("open_my_item");
                game.dispatch("use_all_resource");
            }
            game.account.setLastRound(LocalDateTime.now());

        }

        if (game.account.getLastGiftTime() == null || Duration.between(game.account.getLastGiftTime(), LocalDateTime.now()).toHours() > 24) {
            game.dispatch("daily_reward");
            game.account.setLastGiftTime(LocalDateTime.now());
        }


        game.startEvent(GameStatus.city_work);

    }

    public static void firePosMode(GameInstance game) throws Exception {

        game.dispatch.delay(2);

        game.dispatch("login_test");

        game.startEvent(GameStatus.city_work);

    }
}
