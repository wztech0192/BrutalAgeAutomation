package game;

import util.FilePath;
import util.Global;
import util.Logger;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;

public class GameException extends Exception {
    public GameException(String errorMessage) {
        super(errorMessage);

    }

    public static String prevErrorID = "";

    public static void fire(GameInstance game, Exception e){
        try {
            e.printStackTrace();
            if(game.account != null) {
                game.dispatch.delay(3);
                if(Global.config.isSaveErrorScreenshot()){
                    if(prevErrorID ==null || !prevErrorID.equalsIgnoreCase(game.account.getId())){
                        prevErrorID = game.account.getId();
                        String errorPath = FilePath.ERROR_PATH + game.account.getId()+"_"+game.account.getError();
                            game.dispatch.screenshot(errorPath + ".png");
                            Logger.addToFile(errorPath + ".txt", game.status.get().name() + ": " + game.account.getId() + " -> " + e.getMessage() + "\n" +
                                    Arrays.toString(e.getStackTrace()));
                        }
                    }
                game.account.incrementError();
                game.updateAccount();
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
