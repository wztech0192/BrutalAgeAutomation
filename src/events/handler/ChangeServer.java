package events.handler;

import events.register.TestEvent;
import game.GameException;
import game.GameInstance;
import game.GameStatus;
import util.Logger;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Dictionary;
import java.util.HashMap;

public class ChangeServer {

    public static HashMap<Integer, Integer> serverIndexMap = new HashMap<>();
    static {
        serverIndexMap.put(532, 112);
        serverIndexMap.put(535, 110);
        serverIndexMap.put(557, 100);
        serverIndexMap.put(540, 107);
    }
    public static int startingPoint = 649;

    public static void fire(GameInstance game) throws Exception {

        game.dispatch.delay(2);
        game.dispatch("login_test");
        // get all maill
        game.dispatch("get_all_mail");
        game.dispatch("open_my_item");
        game.dispatch("use_migration");

        game.dispatch.staticDelay(1.2);

        int serverID = TestEvent.getNumber(game.dispatch.doOSR(52, 1128, 98, 1167), true);

        //System.out.println("Server " +serverID);

        int targetServerID = game.posTarget != null ? Integer.parseInt((String)game.posTarget.get("server"))  : game.account.getServerID();

        int targetServerIndex = (serverID - startingPoint) + serverIndexMap.get(targetServerID);
        //int targetServerIndex = 568 - 528 - 1;
        System.out.println("Target server index is " + targetServerIndex);
        int redoCount = 0;


        int diff = targetServerIndex;
        int prevDiff;
        while (true) {
            try {
                System.out.println("Server Different: " + diff);
                if (redoCount > 50) {
                    game.startEvent(GameStatus.initiate, "Server change failure");
                    return;
                }

                game.dispatch.swipeServer(diff);
                //game.dispatch.delay(1);
                prevDiff = diff;
                diff = targetServerIndex - game.log.parseServerIndex();
                if (diff == 0) {
                    if(prevDiff > 0) {
                        game.dispatch.swipeServer(-prevDiff);
                    }
                    game.dispatch("select_server");

                    diff = targetServerIndex - game.log.parseServerIndex();
                    if (diff == 0) {
                        System.out.println("Thats it! " + game.log.btnName);
                        game.dispatch("select_server_confirm");
                        game.status.setServerChanged(true);
                        game.status.set(GameStatus.starting);
                        break;
                    }
                    game.dispatch("select_server_close");
                }
            } catch (Exception e) {
                if (game.log.btnName.contains("player") || game.log.btnName.contains("chat")) {
                    game.dispatch("top_left");
                }
            }
            redoCount++;
        }

        if(game.posTarget != null){
            game.posTarget.put("changed_server", true);
        }

        if(game.account != null) {
            game.account.setChangedServer(true);
            Logger.log("Account completed migration");
            game.updateAccount();
        }

    }
}
