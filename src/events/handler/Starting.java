package events.handler;

import game.GameException;
import game.GameInstance;
import game.GameStatus;

public class Starting {
    public static void fire(GameInstance game) throws Exception {
        game.dispatch.staticDelay(5);
        int i = 0;
        int count = 120;
        while (game.status.is(GameStatus.starting) && count -- > 0) {
            game.dispatch("starting_check"+i);
            i = (i+1)%3;
        }
    }
}
