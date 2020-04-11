package game;

import util.FilePath;
import util.Logger;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;

public class GameException extends Exception {
    public GameException(String errorMessage) {
        super(errorMessage);

    }

    public static void fire(GameInstance game, Exception e){
        try {
            e.printStackTrace();
            game.dispatch.delay(3);
            if(game.account != null) {
                String errorPath = FilePath.ERROR_PATH + game.account.getId()+"_"+game.account.getError();
                game.dispatch.screenshot(errorPath + ".png");
                Logger.addToFile(errorPath + ".txt", game.status.get().name() + ": " + game.account.getId() + " -> " + e.getMessage() + "\n" +
                        Arrays.toString(e.getStackTrace()));
                game.account.incrementError();
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
