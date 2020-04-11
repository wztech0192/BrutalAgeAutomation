package game;

public enum GameStatus {
    none,
    initiate ,  // change account
    starting,  // waiting for game to start
    tutorial, // resolve tutorial
    change_server, // change server
    when_start,  // test login dialog
    city_work,  // upgrade building etc...
    world_map;  //  start gathering


    public static long getTimeout(GameStatus game) {
        switch(game){
            case initiate:
            case starting:
            case tutorial:
            case change_server:
            case when_start:
                return 10;
            case city_work:
            case world_map:
                return 30;
        }
        return 10;
    }
}
