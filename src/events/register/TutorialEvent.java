package events.register;

import events.common.Event;

import java.util.HashMap;

public class TutorialEvent {
    public static void register(HashMap<String, Event> _map){
        Event.builder(_map, "tutorial_dialog")
                .setDelay(1.5)
                .setLoc( 285, 1115);
    }
}
