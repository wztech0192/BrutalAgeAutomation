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

        Logger.log("Get Account Index: " + game.store.getAccountGroup().getIndex());
        game.account = game.store.getAccountGroup().getNextAccount();
        String tempID = game.account.getId();
        game.dispatch.delay(1);
        Logger.log("---------------- start " + tempID);
        game.dispatch.changeAccount(tempID, true);

        //  game.statusUpdateListener.onUpdate(game.account);

        game.dispatch.delay(2);
        game.startEvent(GameStatus.starting);

        game.updateAccount();

        game.dispatch.startGame();



    }

    public static void firePosMode(GameInstance game)  throws Exception {
        game.dispatch.stopGame();

        int waitTime = 0;
        while (game.store.positionQueue.isEmpty()) {
            if(game.store.isClose) return;

            System.out.println("waiting for new event....");
            game.dispatch.resetExecuteTime();
            game.dispatch.staticDelay(10);
            if(++waitTime >= 20){
                waitTime = 0;
                game.store.sendPing();
            }
        }


        game.status.setServerChanged(false);
        JsonObject target = game.store.positionQueue.poll();

        if(target != null)
            target.put("status","initiate");
        game.store.sendDataBack("update", target);

        if(target != null)
            Logger.log("Start positiong: " + target.get("telX") + "," + target.get("telY") + " | " + target.get("buiX") + "," + target.get("buiY"));

        game.dispatch.delay(1);

        game.posTarget = target;

        game.dispatch.changeAccount(game.store.createNewID(), false);

        //  game.statusUpdateListener.onUpdate(game.account);

        game.dispatch.delay(2);

        game.startEvent(GameStatus.starting);

        game.dispatch.startGame();

    }
}
