package game;

import game.GameStatus;

public class StatusIndicator {

    public boolean insideCity;
    private GameStatus gameStatus;
    private boolean created;
    private boolean serverChanged;
    private boolean isOnlyGenerate;
    private boolean isOnlyPosition;

    public StatusIndicator(){
        this.gameStatus = GameStatus.none;
        created = false;
        serverChanged = false;
        insideCity = true;
    }



    public boolean getIsOnlyPosition(){
        return isOnlyPosition;
    }

    public boolean getIsOnlyGenerate(){
        return isOnlyGenerate;
    }

    public boolean is(GameStatus gameStatus){
        return this.gameStatus == gameStatus;
    }

    public GameStatus get(){
        return this.gameStatus;
    }
    public void set(GameStatus gameStatus){
        this.gameStatus = gameStatus;
    }
    public void set(GameStatus gameStatus, boolean created, boolean serverChanged){
        this.gameStatus = gameStatus;
        this.created = created;
        this.serverChanged = serverChanged;
    }
    public void setCreated(boolean created){
        this.created = created;
    }
    public void setServerChanged(boolean serverChanged){
        this.serverChanged = serverChanged;
    }
    public boolean isCreated(){
        return this.created;
    }
    public boolean isServerChanged(){
        return this.serverChanged;
    }

}
