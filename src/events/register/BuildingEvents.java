package events.register;

import events.EventMap;
import events.common.Event;
import game.GameInstance;
import store.Account;
import util.Logger;

import java.util.*;

public class BuildingEvents {

    public static ArrayList<String> AllBuildings = new ArrayList<>();


    public static void register(HashMap<String, Event> _map) {

        Event.builder(_map, "test_upgrade_building")
                .setDelay(1)
                .setListener((event, game)->{
                   for(int i =634 ; i <= 1026; i+=78 ){
                       game.dispatch(Event.builder().setLoc(631, i).setDelay(1));
                       if(game.log.btnName.contains("research") || game.log.btnName.contains("add")){
                           return null;
                       }
                   }
                   return event;
                });

        Event.builder(_map, "upgrade_building")
                .setLoc(527, 1165)
                .setDelay(1)
                .setListener((event, game) -> game.log.btnName.contains("btn_upgrade") ? null : event);
        Event.builder(_map, "test_upgrade_secondary")
                .setLoc(359, 890 , 359, 957, 200)
                .setDelay(1)
                .setListener(((event, game) -> {
                    if(game.log.btnName.toLowerCase().contains("buy") || game.log.btnName.toLowerCase().contains("use")){
                        return event;
                    }
                    return null;
                }));
        Event.builder(_map, "confirm_stronghold_6")
                .setLoc(491, 634)
                .setDelay(1) ;
        Event.builder(_map, "upgrade_building_buy")
                .setLoc(200, 1170)
                .setDelay(1)
                .setListener((event, game) -> game.log.btnName.contains("btn_buy") ||
                        game.log.btnName.contains("btn_buildnow") ? null : event);



        registerBuilding(_map);

    }


    private static void registerBuilding(HashMap<String, Event> _map) {

        Event.builder(_map, "tap_building")
                .setLoc(380, 680)
                .setDelay(1.5);

        Event.builder(_map, "fire")
                .isBuilding()
                .setLoc(-298, 0, 400, 550);

        Event.builder(_map, "monster_access")
                .isBuilding()
                .setLoc(-298, 0, 400, 550)
                .setListener(((event, game) -> {
                    game.dispatch.delay(1.5);
                    game.dispatch(Event.builder().setLoc( 137, 697).setDelay(2));
                    Event hitMonster = Event.builder().setLoc(340, 681).setDelay(1);
                    int redo = 12;
                    do {
                        game.dispatch(hitMonster);
                    }while(game.log.btnName.contains("monster_stage:hitzone") && redo-->0);

                    if(game.log.btnName.contains("btn_confirm")){
                        game.dispatch("top_left");
                    }
                    game.dispatch("top_left");
                    return null;
                }));

        Event.builder(_map, "stronghold")
                .setTargetName("loc_1")
                .isBuilding(true)
                .setLoc(-939, 0, 400, 513);

        Event.builder(_map, "portal")
                .setTargetName("loc_2")
                .isBuilding(true)
                .setLoc(-80, -213, 500, 535);


        Event.builder(_map, "war_camp")
                .setTargetName("loc_3")
                .isBuilding(true)
                .setLoc(-655,-551, 386, 550);

        Event.builder(_map, "healing_spring")
                .setTargetName("loc_4")
                .isBuilding(true)
                .setLoc(-735, -371, 500, 550);

        Event.builder(_map, "healing_spring_access")
                .setTargetName("loc_4")
                .isBuilding()
                .isAccess()
                .setLoc(-735, -371, 250, 550)
                .setChain(
                        Event.builder()
                            .setLoc(588, 1181)
                            .setDelay(1.5),
                        Event.builder()
                            .setLoc(356, 1185)
                            .setDelay(1.5),
                        _map.get("top_left")
                );;

        Event.builder(_map, "research")
                .setTargetName("loc_5")
                .isBuilding(true)
                .setLoc(-512,-234, 500, 550);


        Event.builder(_map, "warehouse")
                .setTargetName("loc_11")
                .isBuilding(true)
                .setLoc(-1402, -768, 500, 525);


        Event.builder(_map, "tower")
                .setTargetName("loc_12")
                .isBuilding(true)
                .setLoc(-1434, 9, 500, 500);

        Event.builder(_map, "golden_tree")
                .setTargetName("loc_13")
                .isBuilding(true)
                .setLoc(-1502, -477, 500, 525);

        Event.builder(_map, "golden_tree_access")
                .setTargetName("loc_13")
                .isBuilding()
                .isAccess()
                .setLoc(-1502, -477, 250, 525)
                .setChain(
                    Event.builder()
                    .setLoc(190, 630)
                    .setDelay(1.5)
                    .setListener((event, game) -> {
                        while(game.log.btnName.contains("BODY:btn_rss")){
                            if(game.account !=null && game.account.getBuildingLvl("stronghold") >= 8 && game.account.isRssLessThan("rock", "wood")){
                                game.dispatch(Event.builder()
                                        .setLoc(520, 812)
                                        .setDelay(1.2));
                            }else{
                                game.dispatch(Event.builder()
                                        .setLoc(190, 630)
                                        .setDelay(1.2));
                            }
                        }
                        game.dispatch(Event.builder()
                                .setLoc(72, 323)
                                .setDelay(1.5)
                        );
                        game.dispatch("top_left");
                        return null;
                    }
                )
        );

        Event.builder(_map, "workshop")
                .setTargetName("loc_8")
                .isBuilding()
                .setLoc(-1321, -594)
                .setChain(_map.get("get_workshop_gift"));


        Event.builder(_map, "daily_reward")
                .setTargetName("loc_9")
                .setLoc(-1018,-375)
                .isBuilding()
                .setListener((event, game) -> {
                    game.dispatch.delay(1.0);
                    game.dispatch(
                            Event.builder().setDelay(2)
                                    .setLoc(396, 998) );
                    game.dispatch(
                            Event.builder().setDelay(1.5)
                                    .setLoc(84, 247));
                    return null;
                });


        Event.builder(_map, "warrior")
                .setTargetName("loc_16")
                .isBuilding()
                .setLoc(-526, -679)
                .setChain(
                        _map.get("tap_building"),
                        Event.builder().setLoc(500, 700, 600,700, 400),
                        Event.builder()
                            .setLoc( 520, 1141)
                            .setDelay(1.5)
                            .setListener(((event, game) -> {
                                if(!game.log.btnName.contains("btn_train")){
                                    game.dispatch("top_left");
                                }
                                return null;
                            }))
                );

        Event.builder(_map, "help_wagon")
                .setTargetName("loc_15")
                .isBuilding(true)
                .setLoc(-232,-964, 505, 552);

        Event.builder(_map, "rider")
                .setTargetName("loc_17")
                .isBuilding()
                .setLoc(-702, -768);

        Event.builder(_map, "shaman")
                .setTargetName("loc_18")
                .isBuilding()
                .setLoc(-876, -691);

        Event.builder(_map, "well1")
                .setTargetName("loc_19")
                .isBuilding(true)
                .setLoc(-614, -1026, 400, 520);

        Event.builder(_map, "well2")
                .setTargetName("loc_20")
                .isBuilding(true)
                .setLoc(-778, -1022, 400, 520);

        Event.builder(_map, "well3")
                .setTargetName("loc_21")
                .isBuilding(true)
                .setLoc(-482, -1063, 380, 785, 400, 630);

        Event.builder(_map, "well4")
                .setTargetName("loc_22")
                .isBuilding(true)
                .setLoc(-649, -1063, 380, 768, 400, 630);

        Event.builder(_map, "well5")
                .setTargetName("loc_23")
                .isBuilding(true)
                .setLoc(-813, -1063,  380, 761, 400, 630);

        Event.builder(_map, "warhub1")
                .setTargetName("loc_24")
                .isBuilding(true)
                .setLoc(-953,-939, 390, 530);

        Event.builder(_map, "warhub2")
                .isBuilding(true)
                .setTargetName("loc_25")
                .setLoc(-1079, -898, 380, 530);

        Event.builder(_map, "warhub3")
                .isBuilding(true)
                .setTargetName("loc_26")
                .setLoc(-1015, -1063, 390, 530);

        Event.builder(_map, "warhub4")
                .isBuilding(true)
                .setTargetName("loc_27")
                .setLoc(-1142, -1013, 390, 530);

        Event.builder(_map, "warhub5")
                .isBuilding(true)
                .setTargetName("loc_28")
                .setLoc(-1244, -937, 390, 530);

        Event.builder(_map, "squirrel")
                .setTargetName("loc_28")
                .isBuilding()
                .setLoc(-1180, -660)
                .setChain(
                        Event.builder().setLoc(380, 680).setDelay(1),
                        Event.builder().setLoc(156, 768).setDelay(1.25),
                        Event.builder().setLoc(156, 768).setDelay(1.5),
                        Event.builder().setLoc(156, 768).setDelay(1.5)
                );
        Event.builder(_map, "flag")
                .setTargetName("building:flag")
                .isBuilding()
                .setLoc(-449,-449)
                .setChain( _map.get("tap_building"));

        for(Map.Entry<String, Event> entry: EventMap.getMap().entrySet()){
            if(entry.getValue().isUpgrade){
                AllBuildings.add(entry.getKey());
            }
        }
    }

}
