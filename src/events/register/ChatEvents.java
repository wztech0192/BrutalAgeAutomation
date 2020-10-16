package events.register;

import events.common.Event;
import util.Logger;

import java.util.HashMap;

public class ChatEvents {
    public static void register(HashMap<String, Event> _map) {


        Event.builder(_map, "check_chat")
                .setLoc(326, 138)
                .setListener(((event, game) -> {
                    game.dispatch.delay(1.5);
                    if(game.log.btnName.contains("chat_room")){
                        return Event.SUCCESS;
                    }
                    return event;
                }));

        Event.builder(_map, "click_chat_input")
                .setLoc(191, 1208)
                .setListener(((event, game) -> {
                    game.dispatch.delay(1);
                    if(game.log.btnName.contains("inputBox:input_zone")){
                        return Event.SUCCESS;
                    }
                    return event;
                }));

        Event.builder(_map, "send_chat")
                .setLoc(647, 1209)
                .setDelay(1);

        Event.builder(_map, "close_chat_modal")
                .setLoc(75, 275)
                .setDelay(1.5);

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
