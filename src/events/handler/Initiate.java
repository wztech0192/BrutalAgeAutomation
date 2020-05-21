package events.handler;

import com.github.cliftonlabs.json_simple.JsonObject;
import dispatcher.EventDispatcher;
import game.GameInstance;
import game.GameStatus;
import util.Logger;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Initiate {


    public static void fire(GameInstance game) throws Exception {
        game.dispatch.stopGame();

        game.account = null;
        game.posTarget = null;

        if(game.store.isPositionMode()){
            game.store.sendPing();
        }
        int waitTime = 0;
        while (true) {
            if(game.store.isClose) return;
            if(game.store.isPositionMode()){
                if(!game.store.positionQueue.isEmpty() || game.store.metadata.getSavedPosAcc().size() < game.store.metadata.getMaxPosAcc()){
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
                Logger.log("Get Account Index: " + game.store.getAccountGroup().getIndex());
                game.account = game.store.getAccountGroup().getNextAccount();
                String tempID = game.account.getId();
                game.dispatch.delay(1);
                Logger.log("---------------- start " + tempID);
                game.dispatch.changeAccount(tempID, true);
                game.updateAccount();
                break;
            }
            System.out.println("waiting for new event....");
            game.dispatch.resetExecuteTime();
            game.dispatch.staticDelay(10);
        }

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
