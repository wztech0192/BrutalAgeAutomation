package events.handler;

import events.common.Event;
import events.register.TestEvent;
import game.GameInstance;
import game.GameStatus;
import util.Logger;

import java.util.Arrays;
import java.util.LinkedList;

public class Tutorial {

    private static SingleStep[] steps = {
            new SingleStep("elect_item_m:btn_fte_1", 360, 673),
            new SingleStep("popup_attach:building_popup_7", 365, 452),
            new SingleStep("loc_3:popup2", 356, 461),
            new SingleStep("loc_16:hitzone", 347, 623),
            new SingleStep("train_panel:btn_train", 509, 1169),
            new SingleStep("popup_attach:building_popup_16", 345, 421),
            new SingleStep("popup_attach:building_popup_2", 358, 433),
            new SingleStep("bottom_panel:btn_left", 74, 1213),
            new SingleStep("my:monster", 131, 612),
            new SingleStep("buttons_1:btn_0", 361, 643),
            new SingleStep("dragon_team:switchButton", 358, 337),
            new SingleStep("listBoxItem_0:choose_bg", 623, 465),
            new SingleStep("dragon_team:btn_save", 353, 1158),
            new SingleStep("BODY:btn_go", 525, 1203),
            new SingleStep("normal_quest:btn_task", 34, 1105),
            new SingleStep("listBoxItem_0:btn_goto", 548, 494),
            new SingleStep("buttons_1:btn_upgrade", 353, 494),
            new SingleStep("upgrade:btn_upgrade", 510, 1181)
    };

    public static void fire(GameInstance game) throws Exception {
        for(int j =0; j< steps.length; j++){
            SingleStep step = steps[j];

            boolean isMatch = false;
            for(int i =0 ;i<50;i++){
                game.log.btnName = "";
                game.dispatch(step.event);
                if(game.log.btnName.contains(step.buttonName)){
                    isMatch = true;
                    break;
                }
            }

            if(!isMatch) {
                for (int k = 0; k <= j; k++) {
                    game.dispatch(steps[k].event);
                }
            }
        }


        for(int i=0;i<3;i++){
            game.dispatch(Event.builder().setLoc(334, 544).setDelay(1.5));
        }

        game.startEvent(GameStatus.change_server);
    }

    private static class SingleStep {
        public String buttonName;
        public Event event;

        public SingleStep(String buttonName, int x, int y) {
            this.buttonName = buttonName;
            this.event = new Event().setLoc(x, y).setDelay(1);
        }
    }
}

