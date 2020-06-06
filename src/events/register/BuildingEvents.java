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
                .setListener((event, game) -> {
                    for (int i = 634; i <= 1026; i += 78) {
                        game.dispatch(Event.builder().setLoc(631, i).setDelay(1));
                        if (game.log.btnName.contains("research") || game.log.btnName.contains("add")) {
                            return Event.SUCCESS;
                        }
                    }
                    return event;
                });

        Event.builder(_map, "upgrade_building")
                .setLoc(527, 1165)
                .setDelay(1)
                .setListener((event, game) -> game.log.btnName.contains("btn_upgrade") ? null : event);
        Event.builder(_map, "test_upgrade_secondary")
                .setLoc(359, 890, 359, 957, 200)
                .setDelay(1)
                .setListener(((event, game) -> {
                    if (game.log.btnName.toLowerCase().contains("buy") || game.log.btnName.toLowerCase().contains("use")) {
                        return event;
                    }
                    return Event.SUCCESS;
                }));
        Event.builder(_map, "confirm_stronghold_6")
                .setLoc(491, 634)
                .setDelay(1);
        Event.builder(_map, "upgrade_building_buy")
                .setLoc(200, 1170)
                .setDelay(1)
                .setListener((event, game) -> game.log.btnName.contains("btn_buy") ||
                        game.log.btnName.contains("btn_buildnow") ? null : event);

        Event.builder(_map, "train")
                .setListener(((event, game) -> {
                    game.log.isRssEnough = true;
                    game.dispatch(Event.builder().setLoc(520, 1141).setDelay(1.5));
                    if (!game.log.btnName.contains("btn_train")) { //70 250
                        game.dispatch("top_left");
                        return event;
                    }

                    if(!game.log.isRssEnough){
                        game.dispatch(Event.builder().setLoc(90,250).setDelay(1.25));
                        game.dispatch("top_left");
                        return event;
                    }

                    return Event.SUCCESS;
                }));

        Event.builder(_map, "close_speedup").setLoc(72, 301).setDelay(1.5);

        Event.builder(_map, "use_train_speedup")
                .setDelay(1.5) .setListener(((event, game) -> {

            game.dispatch(Event.builder().setLoc(563, 361).setDelay(1.5));
            game.dispatch(Event.builder().setLoc( 534, 900).setDelay(1.5));

            if(game.log.btnName.contains("btn_use")){
                game.dispatch.staticDelay(1.5);
                game.dispatch("tap_building");
                if(!game.log.btnName.contains("panel")){
                    return Event.SUCCESS;
                }
            }else{
                game.dispatch(Event.builder().setLoc(77, 298).setDelay(1));
            }
            game.dispatch("top_left");
            return event;
        }));

        Event.builder(_map, "use_speedup")
                .setDelay(1.5)
                .setListener(((event, game) -> {

                    game.dispatch(Event.builder().setLoc(563, 361).setDelay(1.5));
                    game.dispatch(Event.builder().setLoc( 534, 900).setDelay(1.5));

                    if(game.log.btnName.contains("btn_use")){

                        for(int i=0;i<10;i++){
                            game.dispatch( Event.builder().setLoc(580, 200, 580, 250, 200).setDelay(1));
                            if(game.log.btnName.contains("topbar1")){
                                break;
                            }
                        }
                        if(game.log.btnName.contains("btn_free")){
                            game.dispatch( Event.builder().setLoc(580, 200).setDelay(1));
                            return Event.SUCCESS;
                        }
                    }else{
                        game.dispatch(Event.builder().setLoc(77, 298).setDelay(1));
                    }

                    game.dispatch("top_left");
                    return event;
                }));
        registerBuilding(_map);

    }


    private static void registerBuilding(HashMap<String, Event> _map) {

        Event.builder(_map, "tap_building")
                .setLoc(350, 630)
                .setDelay(1.5);


        Event.builder(_map, "fire")
                .isBuilding()
                .setLoc(-574, -349, 432, 434);

        Event.builder(_map, "monster_access")
                .isBuilding()
                .setLoc(-574, -349, 432, 434)
                .setListener(((event, game) -> {
                    game.dispatch.delay(1.5);
                    game.dispatch(Event.builder().setLoc(65, 642).setDelay(2));
                    Event hitMonster = Event.builder().setLoc(340, 681).setDelay(1);
                    int redo = 12;
                    do {
                        game.dispatch(hitMonster);
                    } while (game.log.btnName.contains("monster_stage:hitzone") && redo-- > 0);

                    if (game.log.btnName.contains("btn_confirm")) {
                        game.dispatch("top_left");
                    }
                    game.dispatch("top_left");
                    return Event.SUCCESS;
                }));

        Event.builder(_map, "stronghold")
                .setTargetName("loc_1")
                .isBuilding(true)
                .setLoc(-1591, -306, 400, 513);

        Event.builder(_map, "stronghold_speed_up")
                .isBuilding()
                .setLoc(-1591, -306, 595, 400)
                .setListener(((event, game) -> {
                    for(int i =0; i<10; i++){
                        if(game.log.btnName.contains("speedup")){
                            game.dispatch.staticDelay(1.5);
                            return game.dispatch("use_speedup") ? Event.SUCCESS : event;
                        }else{
                            game.dispatch(Event.builder().isBuilding()
                                    .setLoc(-1591, -306, 595, 400));
                        }
                    }
                    return event;
                }));

        Event.builder(_map, "portal")
                .setTargetName("loc_2")
                .isBuilding(true)
                .setLoc(-315, -649, 500, 505);


        Event.builder(_map, "war_camp")
                .setTargetName("loc_3")
                .isBuilding(true)
                .setLoc(-1227, -1185, 386, 474);

        Event.builder(_map, "healing_spring")
                .setTargetName("loc_4")
                .isBuilding(true)
                .setLoc(-1289, -895, 480, 483);

        Event.builder(_map, "healing_spring_access")
                .setTargetName("loc_4")
                .isBuilding()
                .isAccess()
                .setLoc(-1289, -895, 245, 480)
                .setChain(
                        Event.builder()
                                .setLoc(588, 1181)
                                .setDelay(1.5),
                        Event.builder()
                                .setLoc(356, 1185)
                                .setDelay(1.5),
                        _map.get("top_left")
                );
        ;

        Event.builder(_map, "research")
                .setTargetName("loc_5")
                .isBuilding(true)
                .setLoc(-978, -717, 500, 480);


        Event.builder(_map, "warehouse")
                .setTargetName("loc_11")
                .isBuilding(true)
                .setLoc(-2296, -1489, 500, 480);


        Event.builder(_map, "tower")
                .setTargetName("loc_12")
                .isBuilding(true)
                .setLoc(-2328, -300, 480, 480);

        Event.builder(_map, "golden_tree")
                .setTargetName("loc_13")
                .isBuilding(true)
                .setLoc(-2449, -1071, 480, 480);

        Event.builder(_map, "golden_tree_access")
                .setTargetName("loc_13")
                .isBuilding()
                .isAccess()
                .setLoc(-2449, -1071, 230, 480)
                .setChain(
                        Event.builder()
                                .setLoc(190, 630)
                                .setDelay(1.5)
                                .setListener((event, game) -> {
                                            while (game.log.btnName.contains("BODY:btn_rss")) {

                                                if(game.account.getBuildingLvl("stronghold") >= 10 && game.account.isRssLessThan("meat", "wood")){
                                                    game.dispatch(Event.builder()
                                                            .setLoc(535, 635)
                                                            .setDelay(1.2));
                                                }
                                                else if (game.account.getBuildingLvl("stronghold") >= 8 && game.account.isRssLessThan("rock", "wood")) {
                                                    game.dispatch(Event.builder()
                                                            .setLoc(520, 812)
                                                            .setDelay(1.2));
                                                } else {
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
                                    return Event.SUCCESS;
                                        }
                                )
                );

        Event.builder(_map, "workshop")
                .setTargetName("loc_8")
                .isBuilding()
                .setLoc(-2174, -1242)
                .setChain(_map.get("get_workshop_gift"));


        Event.builder(_map, "daily_reward")
                .setTargetName("loc_9")
                .setLoc(-1739, -895, 300, 510)
                .isAccess()
                .isBuilding()
                .setListener((event, game) -> {
                    game.dispatch.staticDelay(1.0);
                    game.dispatch(Event.builder().setLoc(373, 370).setDelay(1.5));
                    game.dispatch(
                            Event.builder().setDelay(2)
                                    .setLoc(396, 998));
                    game.dispatch(
                            Event.builder().setDelay(1.5)
                                    .setLoc(84, 247));

                    game.dispatch.staticDelay(1.2);

                    if(game.account.getBuildingLvl("stronghold") < 6) {
                        game.dispatch(
                                Event.builder().setDelay(1.5)
                                        .setLoc(359, 564));
                    }
                    else{
                        game.dispatch(
                                Event.builder().setDelay(1.5)
                                        .setLoc(571, 564));
                    }


                    game.dispatch(
                            Event.builder().setDelay(1.2)
                                    .setLoc(358, 1122));
                    game.dispatch(
                            Event.builder().setDelay(1.2)
                                    .setLoc(358, 1122));

                    game.dispatch("top_left");
                    game.dispatch("top_left");

                    return Event.SUCCESS;
                });


        Event.builder(_map, "help_wagon")
                .setTargetName("loc_15")
                .isBuilding(true)
                .setLoc(-545, -1802, 480, 500);

        Event.builder(_map, "speed_warrior")
                .setTargetName("loc_16")
                .isBuilding()
                .setLoc(-991, -1379)
                .setListener(((event, game) -> {
                    game.dispatch("tap_building");
                    game.dispatch(Event.builder().setLoc(643, 753));
                    if(game.log.btnName.contains("btn_speed")){
                        return game.dispatch("use_train_speedup") ? Event.SUCCESS : event;
                    }
                    game.dispatch("top_left");
                    return event;
                }));

        Event.builder(_map, "warrior")
                .setTargetName("loc_16")
                .isBuilding()
                .setLoc(-991, -1379)
                .setListener(((event, game) -> {
                    game.dispatch("tap_building");
                    return game.dispatch("train") ? Event.SUCCESS : event;
                }));
        Event.builder(_map, "rider")
                .setTargetName("loc_17")
                .isBuilding()
                .setLoc(-1258, -1474).setChain(
                _map.get("tap_building"),
                _map.get("train")
        );

        Event.builder(_map, "shaman")
                .setTargetName("loc_18")
                .isBuilding()
                .setLoc(-1524, -1351).setChain(
                _map.get("tap_building"),
                _map.get("train")
        );

        Event.builder(_map, "well1")
                .setTargetName("loc_19")
                .isBuilding(true)
                .setLoc(-1131, -1870, 334, 500);

        Event.builder(_map, "well2")
                .setTargetName("loc_20")
                .isBuilding(true)
                .setLoc(-1373, -1863, 334, 500);

        Event.builder(_map, "well3")
                .setTargetName("loc_21")
                .isBuilding(true)
                .setLoc(-916, -2068, 334, 500);

        Event.builder(_map, "well4")
                .setTargetName("loc_22")
                .isBuilding(true)
                .setLoc(-1166, -2070, 334, 500);

        Event.builder(_map, "well5")
                .setTargetName("loc_23")
                .isBuilding(true)
                .setLoc(-1446, -2041, 334, 500);

        Event.builder(_map, "warhub1")
                .setTargetName("loc_24")
                .isBuilding(true)
                .setLoc(-1616, -1767, 360, 510);

        Event.builder(_map, "warhub2")
                .isBuilding(true)
                .setTargetName("loc_25")
                .setLoc(-1821, -1666, 360, 510);

        Event.builder(_map, "warhub3")
                .isBuilding(true)
                .setTargetName("loc_26")
                .setLoc(-1717, -1944, 360, 510);

        Event.builder(_map, "warhub4")
                .isBuilding(true)
                .setTargetName("loc_27")
                .setLoc(-1910, -1853, 360, 510);

        Event.builder(_map, "warhub5")
                .isBuilding(true)
                .setTargetName("loc_28")
                .setLoc(-2065, -1735, 360, 510);

        Event.builder(_map, "defense_hall")
                .isBuilding(true)
                .setLoc(-583,-1553, 450, 485);

        Event.builder(_map, "war_hall")
                .isBuilding(true)
                .setLoc(-259,-1628,467, 475);

        Event.builder(_map, "squirrel")
                .setTargetName("loc_28")
                .isBuilding()
                .setLoc(-1978, -1322)
                .setListener(((event, game) -> {

                    Event use = Event.builder().setLoc(350, 630).setDelay(1.25);
                    game.dispatch( use);
                    game.dispatch( use);
                    if(!game.account.isFinishInit()){
                        game.dispatch.staticDelay(2.5);
                        game.dispatch(Event.builder().setLoc(156, 768).setDelay(1));
                    }
                    return Event.SUCCESS;
                }));

        Event.builder(_map, "pig")
                .isBuilding()
                .setLoc(-2281,-1951)
                .setChain(
                        Event.builder().setLoc(337, 490).setDelay(1.5),
                        Event.builder().setLoc(337, 517).setDelay(1.5),
                        Event.builder().setLoc(337, 520).setDelay(1.5)
                );

        Event.builder(_map, "flag")
                .setTargetName("building:flag")
                .isBuilding()
                .isAccess()
                .setLoc(-871, -998, 233, 556)
                .setChain(_map.get("tap_building"));

        for (Map.Entry<String, Event> entry : EventMap.getMap().entrySet()) {
            if (entry.getValue().isUpgrade) {
                AllBuildings.add(entry.getKey());
            }
        }
    }

}
