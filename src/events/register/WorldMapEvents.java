package events.register;

import events.common.Event;
import game.GameException;
import util.Logger;

import java.util.HashMap;

public class WorldMapEvents {

    public static void register(HashMap<String, Event> _map) {
        Event.builder(_map, "tel_confirm")
                .setDelay(1.5)
                .setLoc(335, 664);

        Event.builder(_map, "change_outpost")
                .setDelay(1.5)
                .setLoc(47, 1100);

        Event.builder(_map, "safe_teleport")
                .setDelay(1.5)
                .setLoc(514, 655, 514, 694, 200)
                .setListener(((event, game) -> {
                    if(game.log.btnName.contains("buttons_2:btn_2")){
                        game.dispatch(Event.builder().setDelay(1.5).setLoc(514, 655));
                        game.dispatch(Event.builder().setDelay(1.5).setLoc(611, 1158));
                        if (game.log.btnName.contains("btn_buyUse")) {
                            game.dispatch(Event.builder().setDelay(1).setLoc(67, 322));
                        }else if (game.log.btnName.contains("bottom_panel:btn_right")){
                            game.dispatch("top_left");
                        }
                        return null;
                    }
                    return event;
                }));

        Event.builder(_map, "teleport")
                .setDelay(1.5)
                .setLoc(459, 655)
                .setListener(((event, game) -> {
                    game.dispatch(_map.get("tel_confirm"));
                    game.dispatch(Event.builder().setDelay(1.5).setLoc(611, 1158));
                    if (game.log.btnName.contains("btn_buyUse")) {
                        game.dispatch(Event.builder().setDelay(1).setLoc(67, 322));
                    }else if (game.log.btnName.contains("bottom_panel:btn_right")){
                        game.dispatch("top_left");
                    }
                    return null;
                }));

        Event.builder(_map, "tap_build")
                .setDelay(1.5)
                .setLoc(160, 635)
                .setListener((event, game) -> {
                    if (game.log.btnName.contains("btn_1")) {
                        game.dispatch("tel_confirm");
                        Event tapEvent = Event.builder().setDelay(1.5).setLoc(474, 1169);
                        game.dispatch(tapEvent);
                        game.dispatch(tapEvent);
                        return null;
                    }
                    game.dispatch(Event.builder().setLoc(349, 1008).setDelay(1.5));
                    return event;
                });

        Event.builder(_map, "build_init_outpost")
                .setDelay(1.5)
                .setListener((event, game) -> {
                    int[] tapLoc = new int[]{
                            427, 549,
                            226, 453,
                            416, 467,
                            248, 577
                    };
                    Event tapEvent = Event.builder().setDelay(2);
                    for (int i = 0; i < tapLoc.length / 2; i += 2) {
                        game.dispatch(tapEvent.setLoc(tapLoc[i], tapLoc[i + 1]));
                        if (game.dispatch("tap_build")) {
                            return null;
                        }
                    }
                    return event;
                });


        Event.builder(_map, "world_set")
                .setListener(((event, game) -> {
                    game.log.world_set[0] = ((game.log.world[0] + game.log.world[2]) / 2);
                    game.log.world_set[1] = ((game.log.world[1] + game.log.world[3]) / 2);
                    System.out.println("Set: " + game.log.world_set[0] + "," + game.log.world_set[1]);
                    return null;
                }));

        Event.builder(_map, "adjust_map")
                .setDelay(1.5)
                .setListener((event, game) -> {

                    System.out.println("******Start adjust map zoom");
                   game.dispatch.mapzoom();

                    Event testClick = Event.builder().setLoc(73, 104).setDelay(1.5);
                    int redo = 10;
                    do{
                        game.dispatch(testClick);
                        if(!game.log.btnName.contains("scene_tiles") ){
                            game.dispatch.mapzoomin();
                        }else{
                            break;
                        }
                    }while( --redo > 0 );

                    if (redo <= 0) {
                        throw new GameException("Stuck at adjust map");
                    }

                    System.out.println("***** Adjust zoom ended");
                    return null;
                });


        Event.builder(_map, "attack_monster")
                .setDelay(2)
                .setListener(((event, game) -> {
                    int redo = 0;
                    game.dispatch(Event.builder().setLoc(332, 661).setDelay(2));
                    Event quickSelect = Event.builder().setLoc(214, 1175).setDelay(1);
                    Logger.log("Current Idle: "+game.log.idleTroops);
                    Logger.log("Current Troops: "+game.log.currTroops);
                    if(game.log.idleTroops == game.log.currTroops)
                        game.dispatch(quickSelect);
                    while (game.log.idleTroops >0 && game.log.currTroops <= 0 && redo++ < 10) {
                        game.dispatch(quickSelect);
                    }

                    game.dispatch(Event.builder().setLoc(520, 1198).setDelay(1));
                    if (!game.log.btnName.contains("btn_go")) {
                        game.dispatch("top_left");
                    }
                    return null;
            }));

        Event.builder(_map, "attack_monster_test")
                .setLoc(332, 661, 332, 693, 200)
                .setListener(((event, game) -> {
                    if (game.log.btnName.equalsIgnoreCase("buttons_1:btn_0")
                            || game.log.btnName.equalsIgnoreCase("buttons_1:btn_1")) {
                        return null;
                    }
                    return event;
                }));

    }

   /* public static int[] rotate(int[] center, int x, int y ){

        double xRot = center[0] + Math.cos(45) * (x - center[0] ) - Math.sin(45) * (y - center[1] );
        double yRot = center[1]  + Math.sin(45) * (x - center[0]) + Math.cos(45) * (y - center[1] );
        return new int[] {(int)xRot, (int)yRot};
    }
    Event.builder(_map, "adjust_map_offset")
            .setDelay(2)
                .setListener((event, game) -> {
        Event tapBuildingEvent = EventMap.get("tap_building");
        System.out.println("******Start adjust map offset");
        game.log.world_set[0] = 538;
        game.log.world_set[1] = 215;
        int redo = 50;
        while(redo > 0) {
            int diffX =   game.log.world_set[0]- game.log.world[0];
            int diffY = game.log.world_set[1]- game.log.world[1];
            diffX *= 20;
            diffY *= 20;



            if (diffX > 500)
                diffX = 500;
            else if (diffX < -500) {
                diffX = -500;
            }
            if (diffY > 500)
                diffY = 500;
            else if (diffY < -500) {
                diffY = -500;
            }
            game.dispatch.delay(1);
            if (Math.abs(diffX) + Math.abs(diffY) > 0) {

                int swipeX  =  tapBuildingEvent.loc[0] - diffX;
                int swipeY = tapBuildingEvent.loc[1] - diffY;
                int[] rotateLoc = rotate(tapBuildingEvent.loc, swipeX, swipeY);
                game.dispatch.exec(
                        String.format("input swipe %d %d %d %d 500", tapBuildingEvent.loc[0], tapBuildingEvent.loc[1],
                                rotateLoc[0],
                                rotateLoc[1]
                        ));
                redo--;
            } else break;
        }
        System.out.println("***** Adjust offset ended");

        if(redo == 0 ){
            throw new GameException("Map cannot located", game);
        }

        return null;
    });*/
}
