package events.handler;

import game.GameException;
import game.GameInstance;
import game.GameStatus;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class ChangeServer {

    public static void fire(GameInstance game) throws Exception {

        long openDate = Duration.between(Instant.ofEpochSecond(game.log.openTime)
                .atZone(ZoneId.systemDefault()).toLocalDateTime(), LocalDateTime.now()).toDays();

        System.out.println("Server " + game.log.serverID + " opened for " + openDate + " days");


        int targetServerID = game.store.isPositionMode() ? Integer.parseInt((String)game.posTarget.get("server"))  : game.account.getServerID();

        int targetServerIndex = game.log.serverID - targetServerID - (openDate >= 3 ? 0 : 1);
        //int targetServerIndex = 568 - 528 - 1;
        System.out.println("Target server index is " + targetServerIndex);
        int redoCount = 0;


        game.dispatch.delay(2);
        game.dispatch("login_test");
        // get all maill
        game.dispatch("get_all_mail");
        game.dispatch("open_my_item");
        game.dispatch("use_migration");

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
                game.dispatch.delay(1);
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

    }
}
