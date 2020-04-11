package events.register;

import events.common.Event;
import util.Logger;

import java.awt.image.BufferedImage;
import java.util.HashMap;

public class TalentEvents {

    public static void register(HashMap<String, Event> _map) {
        Event.builder(_map, "open_talent")
                .setLoc(50, 44)
                .setDelay(1.5)
                .setChain(
                        Event.builder().setLoc(229,1126).setDelay(1.5)
                );
        Event.builder(_map, "use_talent")
                .setDelay(1.5)
                .setListener(((event, game) -> {
                    int[] buttons = new int[]{
                            358, 1000,
                            356, 800,
                            356 , 560,
                            356, 346
                    };

                    int talentPoint = TestEvent.getNumber(game.dispatch.doOSR(437, 261, 492, 292 ), true);
                    game.log.talentScroll = 0 ;

                    if(talentPoint>0){
                        for(int i =0 ; i< 10;i ++){
                            int diff = (int)(game.log.talentScroll) - -120;
                            int absDiff = Math.abs(diff);
                            if(absDiff > 5){
                                //251 680
                                if(Math.abs(diff) > 100){
                                    diff = (diff/absDiff) * 90;
                                }
                                Logger.log("difference "+diff);
                                game.dispatch.exec(String.format("input swipe 251 680 251 %d 300", 680 - diff));
                            }else{
                                break;
                            }
                        }
                    }

                    Event clickLearn = Event.builder().setLoc(373, 723).setDelay(1.25);
                    Event closeEvent = Event.builder().setLoc(61, 287).setDelay(1.25);
                    int btnIndex = 0;
                    for(int redo = 0; redo < 100; redo++) {

                        if (talentPoint <= 0) {
                            break;
                        }
                        game.dispatch(Event.builder().setLoc(buttons[btnIndex], buttons[btnIndex + 1]).setDelay(1.25));
                        game.dispatch.sendEvent(clickLearn);
                        if (game.log.btnName.contains("learn")) {
                            talentPoint--;
                            if (btnIndex == 0) {
                                while(talentPoint>0){
                                    game.dispatch.sendEvent(clickLearn);
                                    talentPoint--;
                                }
                            }else{
                                btnIndex -= 2;
                            }
                        } else {
                            btnIndex = (btnIndex + 2) % 8;
                        }
                        game.dispatch(closeEvent);
                    }
                    game.dispatch("top_left");
                    game.dispatch("top_left");
                    return null;
                }));

    }
}
