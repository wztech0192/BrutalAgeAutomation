package events;

import events.common.Event;
import events.register.*;
import util.Logger;

import java.util.HashMap;
import java.util.Map;

public class EventMap {

    private static HashMap<String, Event> _map;

    static {
        _map = new HashMap<>();

        Event.builder(_map, "top_left")
                .setDelay(1.5)
                .setLoc(58, 65);
        Event.builder(_map, "bottom_left")
                .setDelay(2)
                .setLoc(55, 1198)
                .setChain(
                        Event.builder()
                                .setDelay(1)
                                .setLoc(72, 1107, 87, 1107, 200)
                                .setListener(((event, game) -> {
                                    game.log.isInCity = !game.log.btnName.contains("btn_next_op");
                                    Logger.log("Now in city: "+game.log.isInCity);

                                    if(game.log.btnName.contains("btn_rank")){
                                        game.dispatch("top_left");
                                        game.dispatch("top_left");
                                    }
                                    else if(game.log.btnName.contains("dummy")) {
                                        game.dispatch("top_left");
                                        game.dispatch("top_left");
                                        game.dispatch("top_left");
                                    }
                                    else if(game.log.btnName.contains("flag")){
                                        game.dispatch("top_left");
                                        game.dispatch("top_left");
                                        game.dispatch("top_left");
                                        game.dispatch("top_left");
                                    }
                                    return null;
                                }))
                );


        Event.builder(_map, "open_my_item")
                .setTargetName("side_1:btn_items")
                .setDelay(1.5)
                .setLoc(682, 1100)
                .setChain(
                        Event.builder()
                                .setTargetName("tabMenu:tab_2")
                                .setDelay(1.5)
                                .setLoc(526, 125)
                );

        Event.builder(_map, "levelup_dialog")
                .setTargetName("board:btn_sure")
                .setDelay(1.75)
                .setLoc(368, 930);


        TutorialEvent.register(_map);
        ChangeServerEvents.register(_map);
        TestEvent.register(_map);
        ClanEvents.register(_map);
        GetGiftEvents.register(_map);
        TalentEvents.register(_map);
        BuildingEvents.register(_map);
        WorldMapEvents.register(_map);
        OtherEvents.register(_map);
    }

    public static HashMap<String, Event> getMap() {
        return _map;
    }

    public static Event get(String name) {
        return _map.get(name);
    }


    public static void printAll() {
        for (Map.Entry<String, Event> entry : _map.entrySet()) {
            System.out.println(entry.getKey());
        }
    }
}
