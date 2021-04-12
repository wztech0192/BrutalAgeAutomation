package events.register;

import events.common.Event;
import util.Logger;

import java.util.HashMap;

public class OtherEvents {
    public static void register(HashMap<String, Event> _map) {

        Event.builder(_map, "assign_id")
                .setListener((event, game) -> {
                    game.dispatch("top_left");
                    game.dispatch(Event.builder().setLoc(360, 736).setDelay(1.5));

                    Logger.log("Account name is " + game.log.text);
                    game.account.setName(game.log.text);
                    game.dispatch(Event.builder().setLoc(72, 300).setDelay(1.5));
                    game.dispatch("top_left");
                    game.updateAccount();
                    return Event.SUCCESS;
                });

        Event.builder(_map, "levelOutpost")
                .setLoc(554, 926)
                .setListener((event, game)->{
                    game.dispatch.delay(1);
                    Event research = Event.builder().setLoc(484, 903).setDelay(1.5);
                    Event speedup = Event.builder().setLoc(576, 770).setDelay(1.5);

                    //click durability
                    game.dispatch(Event.builder().setLoc( 371, 295).setDelay(1.5));

                    game.dispatch(research);
                    game.dispatch(speedup);
                    game.dispatch("use_speedup");

                    //close
                    game.dispatch(Event.builder().setLoc(82, 238).setDelay(1.5));

                    //second research
                    game.dispatch(Event.builder().setLoc(362, 543).setDelay(1.5));

                    game.dispatch(research);
                    game.dispatch(speedup);
                    game.dispatch("use_speedup");

                    //close
                    game.dispatch(Event.builder().setLoc( 71, 300).setDelay(1.5));
                    game.dispatch(Event.builder().setLoc( 71, 300).setDelay(1.5));
                    game.dispatch("top_left");
                    game.dispatch("top_left");

                    return Event.SUCCESS;
                });

        Event.builder(_map, "use_speedup")
                .setLoc(572, 356)
                .setListener(((event, game) -> {
                    game.dispatch.delay(1);
                    game.dispatch(Event.builder().setLoc(519, 900).setDelay(2));
                    return Event.SUCCESS;
                }));

        Event.builder(_map, "upgrade_outpost")
                .setLoc(341, 500)
                .setListener((event, game)->{
                    game.dispatch.delay(1.5);
                    game.dispatch(Event.builder().setLoc( 236, 639).setDelay(1));
                    game.dispatch(Event.builder().setLoc( 263, 1172).setDelay(1));
                    game.dispatch(Event.builder().setLoc( 515, 1172).setDelay(1));
                    game.dispatch(Event.builder().setLoc( 618, 1082).setDelay(1.5));
                    game.dispatch("use_speedup");
                    game.dispatch("top_left");
                    return Event.SUCCESS;
                });
    }
}
