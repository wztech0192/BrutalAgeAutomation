package util;

import com.android.ddmlib.*;
import dispatcher.EventDispatcher;
import events.EventMap;
import game.GameException;
import game.GameInstance;
import game.GameStatus;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class MyDebugger {

    public MyDebugger(GameInstance game){
        System.out.println("********** Start DEBUGGING *************");
        new Thread(()->{
            System.out.print("**Enter a event: ");
            Scanner input = new Scanner(System.in);
            String cmd;
            while(!(cmd = input.nextLine()).equalsIgnoreCase("exit")){
                try {

                    if(cmd.contains("status-")){
                        game.startEvent(GameStatus.valueOf(cmd.split("-")[1]));
                    }
                    else if(cmd.contains("osr")){
                        String[] split = cmd.split(" ");
                       System.out.println( game.dispatch.doOSR(Integer.parseInt(split[1]),Integer.parseInt(split[2]),Integer.parseInt(split[3]),Integer.parseInt(split[4])));
                    }
                    else if(cmd.contains("png")){
                       // game.dispatch.getMatch(cmd);
                          Mat rawMat = game.dispatch.rawimg2Mat( game.dispatch.getRaw());
                        game.dispatch.getMatch(100, 100, rawMat.width() - 100, rawMat.height() - 100, 0.25, 0, rawMat, cmd , 3);
                    }
                    else if(cmd.contains("monster:")){
                        String[] split = cmd.split(" ");
                        game.dispatch.getMonsterMatch(3, new ArrayList<> (Arrays.asList(split)));
                    }
                    else {
                        switch (cmd) {
                            case "test":
                                game.dispatch.mapzoom();
                                break;
                            case "ls":
                                EventMap.printAll();
                                break;
                            case "new":
                                game.dispatch.stopGame();
                                game.dispatch.changeAccount(game.store.createNewID(), false);
                                game.dispatch.startGame();
                                break;
                            case "capture":
                                game.dispatch.screenshot(FilePath.TEMPLATE_PATH + "/capture.png");
                                break;
                            case "closest_building":
                                game.dispatch.findClosestBuilding(game);
                                break;
                            case "select":
                                game.dispatch.selectMonster(400, 400);
                                break;
                            default:
                                game.dispatch(cmd);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.print("\n**Enter a event: ");
            }
        }).start();
    }

}
