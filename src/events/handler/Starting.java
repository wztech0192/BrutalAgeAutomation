package events.handler;

import game.GameException;
import game.GameInstance;
import game.GameStatus;

public class Starting {
    public static void fire(GameInstance game) throws Exception {
        game.dispatch.staticDelay(5);
        while (game.status.is(GameStatus.starting)) {
            game.dispatch("later_update");
        }
    }
}
