package events.register;

import events.common.Event;
import util.Logger;

import java.util.HashMap;

public class OtherEvents {
    public static void register(HashMap<String, Event> _map) {

        Event.builder(_map, "assign_id")
                .setListener(((event, game) -> {
                    game.dispatch("top_left");
                    game.dispatch(Event.builder().setLoc(360, 640).setDelay(1.5));

                    Logger.log("Account name is " + game.log.text);
                    game.account.setName(game.log.text);
                    game.dispatch(Event.builder().setLoc(72, 300).setDelay(1.5));
                    game.dispatch("top_left");
                    game.updateAccount();
                    return null;
                }));

    }
}
