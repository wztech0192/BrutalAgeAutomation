package events.handler;

import com.github.cliftonlabs.json_simple.JsonObject;
import dispatcher.EventDispatcher;
import game.GameInstance;
import game.GameStatus;
import net.sf.cglib.core.Local;
import store.Account;
import util.Global;
import util.Logger;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Initiate {


    public static void fire(GameInstance game) throws Exception {
        game.dispatch.stopGame();
        game.log.reset();
        game.account = null;
        game.posTarget = null;


        if(game.store.isPositionMode()){
            game.store.sendPing();
        }
        int waitTime = 0;
        while (true) {
            if(game.store.isBotMode()){
                game.startEvent(GameStatus.starting);
                game.dispatch.startGame();
                return;
            }
            if(game.store.isClose) return;
            if(game.store.isPositionMode()){
                if(!game.store.positionQueue.isEmpty() || game.store.metadata.getSavedPosAcc().size() < Global.config.getMaxStorePos()){
                    createPosItem(game);
                    game.updateListener.onUpdate(null);
                    break;
                }
                if(++waitTime >= 20){
                    waitTime = 0;
                    game.store.sendPing();
                }
            }
            if(game.store.getAccountGroup().getAccounts().size() > 0){

                Account account = game.store.getAccountGroup().getNextAccount();
                Account tempAcount = account;
                boolean roundCheck = false;

                if (account.isDuringTemplate() && game.store.metadata.getFeatureToggler().getGlobalFeatures().get("Feed Temple")) {

                    boolean feedOver = false;
                    while(true){
                        if ( account.getTroops() >= account.getNumberFeaturer().getNumberSetting().get("Min Troop") + 1000) {
                            break;
                        }else if(tempAcount == account && roundCheck){
                            feedOver = true;
                            break;
                        }
                        else{
                            roundCheck = true;
                            account = game.store.getAccountGroup().getNextAccount();
                        }
                    }

                    if(feedOver){
                        Logger.log("Finished Feeding, so stop!!!!");
                        game.store.metadata.getFeatureToggler().getGlobalFeatures().put("Feed Temple", false);
                        game.store.marshellMetadata();
                    }
                }
                else if(game.store.metadata.getFeatureToggler().getGlobalFeatures().get("Hide Mode")){
                    if(game.account == null){
                        game.account = game.store.getAccountGroup().getLastAccount();
                    }
                    while(true){

                        long hideTimeLeft = account.getHideTime() == null ? -1 : 12-Duration.between(account.getHideTime(), LocalDateTime.now()).toHours();

                        //check if hide time left is 0, or every 2 hours
                        if (hideTimeLeft <= 0 || (hideTimeLeft != 12 && hideTimeLeft % 2 == 0)) {
                            //check every two hours
                            break;
                        }else if(tempAcount == account && roundCheck){
                            account = null;
                            break;
                        }
                        else{
                            roundCheck = true;
                            account = game.store.getAccountGroup().getNextAccount();
                        }
                    }
                }


                if(account != null){
                    game.account = account;

                    Logger.log("Get Account Index: " + game.store.getAccountGroup().getIndex());
                    String tempID = game.account.getId();
                    game.dispatch.delay(1);
                    Logger.log("---------------- start " + tempID);
                    game.dispatch.changeAccount(tempID, true);

                    Chat.setLastRoundTime();
                    game.updateAccount();
                    break;
                }

            }
            System.out.println("waiting for new event....");
            game.dispatch.resetExecuteTime();
            game.dispatch.staticDelay(10);
        }

        game.updateTable();
        game.store.currentPosItem = game.posTarget;
        game.dispatch.delay(2);
        game.startEvent(GameStatus.starting);
        game.dispatch.startGame();
    }

    public static void createPosItem(GameInstance game)  throws Exception {
        game.status.setServerChanged(false);
        game.dispatch.delay(1);
        JsonObject target = game.store.positionQueue.poll();

        if(target != null) {
            target.put("status", "initiate");
            game.store.sendDataBack("update", target);
            Logger.log("Start positiong: " + target.get("telX") + "," + target.get("telY") + " | " + target.get("buiX") + "," + target.get("buiY"));
            game.posTarget = target;

            if(game.store.metadata.getSavedPosAcc().isEmpty()){
                Logger.log("Create new account");
                game.dispatch.changeAccount( game.store.createNewID() ,false);
            }else{
                Logger.log("Get existing account");
                game.dispatch.changeAccount( game.store.metadata.getSavedPosAcc().poll() ,false);
                game.posTarget.put("exist", true);
            }

            game.store.marshellMetadata();
            game.store.updatePosSavedAcc();
        }else{
            Logger.log("Create temp account for pos mode!!!!");
            game.posTarget = new JsonObject();
            String newID = game.store.createNewID();
            game.posTarget.put("temp", newID);

            game.dispatch.changeAccount(newID,false);
        }

    }
}
