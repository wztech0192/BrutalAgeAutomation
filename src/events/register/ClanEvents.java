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
                            if(i == 311 && game.log.transportRss[0] < game.store.metadata.getNumberFeaturer().getNumberSetting().get("Min Food Wood")){
                                /*
                                * 0 wood  -0
                                    1 rock  -3
                                    2 ivory -4
                                    3 meat -1
                                    4 mana -2
                                * */
                                continue;
                            }
                            else if(i == 411 && game.log.transportRss[3] < game.store.metadata.getNumberFeaturer().getNumberSetting().get("Min Food Wood")){
                                continue;
                            }
                            game.dispatch.exec(String.format("input swipe 539 %d 579 %d", i, i));
                            game.dispatch.staticDelay(0.25);
                            Logger.log("*** Transport: "+game.log.selectedTransportNum+"/"+game.log.limitTransportNum);
                            if (game.log.selectedTransportNum >= game.log.limitTransportNum - 10000) {
                                game.dispatch("start_transport");
                                game.log.marches--;
                                return Event.SUCCESS;
                            }
                        }
                    }else{
                        game.dispatch.staticDelay(1.5);
                        game.dispatch("top_left");
                    }
                    return event;
                }));

        Event.builder(_map, "transport")
                .setDelay(1.5)
                .setListener(((event, game) -> {

                    for(int i =0;;i++) {
                        if(game.store.metadata.getFeatureToggler().getGlobalFeatures().get("No Clan")){
                            game.dispatch("apply_clan");
                            game.dispatch("search_clan");
                            game.dispatch.enterText(game.account.getClan());
                            game.dispatch("confirm_search_clan");
                            game.dispatch("join_clan");
                            game.dispatch.staticDelay(1.5);
                            break;
                        }else{
                            game.dispatch("open_clan");
                            if(game.log.btnName.contains("bottom_panel:btn_right")){
                                break;
                            }else{
                                game.dispatch("top_left");
                            }
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
                    game.dispatch.staticDelay(1.5);
                    game.dispatch("top_left");

                    if(game.store.metadata.getFeatureToggler().getGlobalFeatures().get("No Clan")){
                        game.dispatch("quit_clan");
                    }else{
                        game.dispatch("top_left");
                    }
                    return Event.SUCCESS;
                }));


        Event.builder(_map, "quit_clan")
                .setDelay(1.5)
                .setListener(((event, game) -> {

                    game.dispatch(Event.builder().setLoc(370, 1175,370, 560, 500 ));
                    for(int i=0; i<3; i++){
                        if(!game.log.btnName.contains("listBoxItem")){
                            game.dispatch("top_left");
                            game.dispatch(Event.builder().setLoc(370, 1175,370, 560, 500 ));
                        }else{
                            break;
                        }
                    }
                    game.dispatch.staticDelay(1.5);
                    game.dispatch(Event.builder().setLoc(370, 1100).setDelay(1.25));
                    game.dispatch(Event.builder().setLoc(370, 704).setDelay(1.25));
                    game.dispatch(Event.builder().setLoc(355, 880).setDelay(1.25));

                    return Event.SUCCESS;
                }));

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
                    game.dispatch.staticDelay(1.5);
                    int redo = 0;
                    game.log.isInCity = false;
                    while(!game.log.isInCity && redo++ < 4){
                        game.dispatch("bottom_left");
                    }
                    return Event.SUCCESS;
                }));

        Event.builder(_map, "apply_clan")
                .setDelay(1)
                .setListener(((event, game) -> {
                    game.log.hasClan = false;
                    game.dispatch(Event.builder().setLoc(666, 1211));
                    game.dispatch.staticDelay(1.5);

                    if(!game.log.hasClan){
                        game.dispatch("confirm_clan");
                    }
                    else{
                        if (game.account.isDuringTemplate()  && game.store.metadata.getFeatureToggler().getGlobalFeatures().get("Feed Temple")) {
                            return event;
                        }
                        game.dispatch("quit_clan");
                        game.dispatch("apply_clan");
                    }
                    return Event.SUCCESS;
                }));


    }
}
