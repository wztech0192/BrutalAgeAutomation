package events.register;

import events.common.Event;
import util.Logger;

import java.util.HashMap;

public class ChatEvents {
    public static void register(HashMap<String, Event> _map) {

        Event.builder(_map, "click_chat_input")
                .setLoc(191, 1208)
                .setDelay(1);

        Event.builder(_map, "send_chat")
                .setLoc(618, 1224)
                .setDelay(1);

        Event.builder(_map, "open_chat")
                .setLoc(675, 1012)
                .setDelay(2)
                .setListener(((event, game) -> {
                    game.dispatch.staticDelay(1.5);
                    game.dispatch(Event.builder().setLoc(229, 215).setTargetName("message list"));
                    game.dispatch.staticDelay(1.5);
                    game.dispatch(Event.builder().setLoc(349, 178).setTargetName("select message"));
                    game.dispatch.staticDelay(1.5);
                    game.dispatch(Event.builder().setLoc(349, 178).setTargetName("select person"));
                    game.dispatch.staticDelay(1.5);
                    game.dispatch(Event.builder().setLoc(349, 178).setTargetName("test chat").setDelay(1));
                    if(game.log.btnName.contains("chat_room")){
                        return Event.SUCCESS;
                    }
                    return event;
                }));
    }
}
