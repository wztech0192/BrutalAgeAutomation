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

        //converted
        Event.builder(_map, "top_left")
                .setDelay(1.5)
                .setLoc(58, 65);

        //converted
        Event.builder(_map, "bottom_left")
                .setDelay(2)
                .setLoc(55, 1198)
                .setListener(((event, game) -> {
                    game.dispatch.staticDelay(1.25);
                    return Event.SUCCESS;
                }));


        Event.builder(_map, "open_my_item")
                .setTargetName("side_1:btn_items")
                .setDelay(1.5)
                .setLoc(682, 1100)
                .setListener(((event, game) -> {
                    for(int i=0; i<5; i++){
                        if(game.log.btnName.contains("btn_items")){
                            game.dispatch.delay(1.5);
                            game.dispatch(Event.builder()
                                    .setTargetName("tabMenu:tab_2")
                                    .setDelay(1.5)
                                    .setLoc(526, 125));
                            return Event.SUCCESS;
                        }else{
                            game.dispatch("top_left");
                            game.dispatch(Event.builder().setLoc(682, 1100).setDelay(1.5));
                        }
                    }
                    return event;
                }));

        //converted
        Event.builder(_map, "levelup_dialog")
                .setTargetName("board:btn_sure")
                .setDelay(1.75)
                .setLoc(368, 930);

        Event.builder(_map, "close_warning")
                .setDelay(1.75)
                .setLoc(116, 740)
                .setListener(((event, game) -> {
                    if(game.log.btnName.contains("neverPopup")){
                        game.dispatch(Event.builder().setLoc(369, 623)
                                .setDelay(1.75));
                        return Event.SUCCESS;
                    }
                    return event;

                }));


        TutorialEvent.register(_map);
        ChangeServerEvents.register(_map);
        TestEvent.register(_map);
        ClanEvents.register(_map);
        GetGiftEvents.register(_map);
        TalentEvents.register(_map);
        BuildingEvents.register(_map);
        WorldMapEvents.register(_map);
        ChatEvents.register(_map);
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
