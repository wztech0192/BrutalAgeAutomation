package events.register;

import events.common.Event;

import java.util.HashMap;

public class ChangeServerEvents {


    public static void register(HashMap<String, Event> _map) {

        Event.builder(_map, "select_server")
                .setLoc(350, 840)
                .setDelay(1.5);

        Event.builder(_map, "select_server_close")
                .setTargetName("board:btn_close")
                .setLoc(69, 239)
                .setDelay(1.5);

        Event.builder(_map, "select_server_confirm")
                .setTargetName("board:btn_confirm")
                .setLoc(368, 890)
                .setDelay(1.5);

        Event.builder(_map, "test_migration")
                .setTargetName("listBoxItem")
                .setLoc(130, 310, 700, 310, 300)
                .setDelay(1.5)
                .setListener(
                        (event, game) -> game.log.btnName.contains(event.targetName) ? null : event);


        Event.builder(_map, "use_migration")
                .setTargetName("btn_use")
                .setDelay(3)
                .setLoc(573, 739)
                .setMaxRedo(10)
                .setListener((event, game) -> {
                    if (game.log.btnName.contains(event.targetName)) {
                        System.out.println("Test migration");
                        if (game.dispatch("test_migration")) {
                            return Event.SUCCESS;
                        }
                        game.dispatch("top_left");
                    }else{
                        Event newEvent = event.copy();
                        newEvent.loc[1] += 147;
                        if (newEvent.loc[1] > 1200) {
                            newEvent.loc[1] = 300;
                        }
                        return newEvent;
                    }
                    return event;
                });

    }
}
