package events.register;

import events.common.Event;
import util.Logger;

import java.util.HashMap;

public class ClanEvents {
    public static void register(HashMap<String, Event> _map){


        Event.builder(_map, "open_clan")
                .setLoc(666, 1240)
                .setDelay(1);

        Event.builder(_map, "open_member")
                .setLoc( 103, 612)
                .setDelay(1.5);
        Event.builder(_map, "open_leader")
                .setLoc(90, 282)
                .setDelay(1.5);
        Event.builder(_map, "open_transport")
                .setLoc(133, 462)
                .setDelay(2);

        Event.builder(_map, "start_transport")
                .setLoc(360, 1170)
                .setDelay(1.25);

        Event.builder(_map, "round_transport")
                .setListener(((event, game) -> {
                    for(int i =0;;i++){
                        game.dispatch("open_leader");
                        if(game.log.btnName.contains("head")){
                            break;
                        }else{
                            game.dispatch("open_member");
                        }
                        if(i>5){
                            return event;
                        }
                    }

                    game.log.resetTransport();

                    for(int i =0;;i++){
                        game.dispatch("open_transport");
                        if(game.log.btnName.contains("btn_transport")){
                            break;
                        }
                        if(i>5){
                            return event;
                        }
                    }
                    game.dispatch.staticDelay(1);

                   Logger.log("*** Can Transport: "+game.log.maxTransportNum);
                    Logger.log("*** Max Transport: "+game.log.limitTransportNum);
                    if(game.log.maxTransportNum >= game.log.limitTransportNum) {
                        for (int i = 311; i <= 711; i += 100) {
                            game.dispatch.exec(String.format("input swipe 539 %d 579 %d", i, i));
                            game.dispatch.staticDelay(0.25);
                            Logger.log("*** Transport: "+game.log.selectedTransportNum+"/"+game.log.limitTransportNum);
                            if (game.log.selectedTransportNum >= game.log.limitTransportNum - 10000) {
                                game.dispatch("start_transport");
                                game.log.marches--;
                                return null;
                            }
                        }
                    }
                    return event;
                }));

        Event.builder(_map, "transport")
                .setDelay(1.5)
                .setListener(((event, game) -> {

                    for(int i =0;;i++) {
                        game.dispatch("open_clan");
                        if(game.log.btnName.contains("bottom_panel:btn_right")){
                            break;
                        }else{
                            game.dispatch("top_left");
                        }
                        if(i>=5){
                            return event;
                        }
                    }
                    game.dispatch("open_member");

                    for(int i =0; i<4; i++){
                        if(!game.dispatch("round_transport") || game.log.marches<=0){
                            break;
                        }
                    }
                    game.dispatch.staticDelay(1);
                    game.dispatch("top_left");
                    game.dispatch("top_left");
                    game.dispatch("top_left");
                    return null;
                }));


        Event.builder(_map, "quit_clan")
                .setDelay(1.5)
                .setLoc(666, 1211)
                .setChain(
                        Event.builder()
                            .setDelay(1.5)
                            .setLoc(355, 1186),
                        Event.builder()
                            .setDelay(1.5)
                            .setLoc(340, 704),
                        Event.builder()
                                .setDelay(1.5)
                                .setLoc(355, 880)
                );

        Event.builder(_map, "confirm_clan")
                .setDelay(1.5)
                .setLoc(359, 638);

        Event.builder(_map, "join_clan")
                .setTargetName("listBoxItem_0:btn_join")
                .setDelay(1.5)
                .setLoc(603, 381);

        Event.builder(_map, "search_clan")
                .setLoc(83, 195)
                .setDelay(1.5);
        Event.builder(_map, "confirm_search_clan")
                .setLoc(654, 200)
                .setDelay(1.5);

        Event.builder(_map, "back_from_clan")
                .setDelay(2)
                .setListener(((event, game) -> {
                    game.dispatch("top_left");
                    game.dispatch.delay(2);
                    int redo = 0;
                    while(!game.log.isInCity && redo++ < 4){
                        game.dispatch("bottom_left");
                    }
                    return null;
                }));

        Event.builder(_map, "apply_clan")
                .setLoc(666, 1211)
                .setDelay(2)
                .setChain(
                        _map.get("confirm_clan")
                );

    }
}
