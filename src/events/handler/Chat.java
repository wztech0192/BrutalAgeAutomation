package events.handler;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import game.GameInstance;
import util.Logger;

import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Chat {
    private final static Pattern findXRegex = Pattern.compile("\"X\":(\\d*),");
    private final static Pattern findYRegex = Pattern.compile("\"Y\":(\\d*),");

    private static ConcurrentLinkedQueue<Point> MonsterClearList = new ConcurrentLinkedQueue<>();
    private static ConcurrentLinkedQueue<Point> FinishedClearList = new ConcurrentLinkedQueue<>();

    private static GameInstance botInstance;

    private static LocalDateTime lastRoundTime = null;

    public static void setLastRoundTime(){
        lastRoundTime = LocalDateTime.now();
    }

    public static Point dequeueClearList(){
        if(MonsterClearList.isEmpty())
            return null;

        Point p = MonsterClearList.poll();
        FinishedClearList.add(p);
        synchronizedWithSocket();
        return p;
    }

    public static void enqueueClearList(Point p){
        MonsterClearList.add(p);
        synchronizedWithSocket();
    }

    public static void removeFromClearList(Point p){
        MonsterClearList.removeIf(lp->lp.y == p.y && lp.x == p.x);
        synchronizedWithSocket();
    }

    private static void synchronizedWithSocket(){
       /*if(botInstance != null && botInstance.store.isPositionMode()){
            JsonObject payload = new JsonObject();
            payload.put("clearList",  new JsonArray(MonsterClearList));
            payload.put("finishedList",  new JsonArray(FinishedClearList));
            botInstance.store.sendDataBack("sync_clear_list", payload);
        }*/
    }


    public static void fire(GameInstance game, String s, String chatValue)  {

        botInstance = game;

        new Thread(()->{
            try {
                Logger.log(chatValue);
                Matcher m;

                String chatValueLower = chatValue.toLowerCase();
                switch (chatValueLower) {
                    case "hi": {
                        game.dispatch.sendChat("Hello!");
                        break;
                    }
                    case "help":{
                        game.dispatch.sendChat("You can say: hi, last round, status, finished; or send location with default message or stop");
                        break;
                    }
                    case "finished":{
                        if(FinishedClearList.isEmpty()){
                            game.dispatch.sendChat("Finished list is empty");
                        }else{
                            String msg = FinishedClearList.stream().map(p -> "("+p.x+", "+p.y+")").collect(Collectors.joining("; "));
                            game.dispatch.sendChat(FinishedClearList.size()+ " finished: "+msg);
                        }
                        break;
                    }

                    case "last round":{
                        if(lastRoundTime != null){

                            long min = Duration.between(lastRoundTime, LocalDateTime.now()).toMinutes();
                            String info = "Last round started in "+min+" ago. ";
                            if(min < 10){
                                info+=" Looks good.";
                            }
                            else if(min < 20){
                                info+=" Might have issue.";
                            }else{
                                info+=" Something is wrong.";
                            }
                            game.dispatch.sendChat(info);
                        }
                        else{
                            game.dispatch.sendChat("No last round recorded");
                        }
                        break;
                    }
                    case "status":{
                        if(MonsterClearList.isEmpty()){
                            game.dispatch.sendChat("Monster list is empty");
                        }else{
                            String msg = MonsterClearList.stream().map(p -> "("+p.x+", "+p.y+")").collect(Collectors.joining("; "));
                            game.dispatch.sendChat(MonsterClearList.size()+ " pending: "+msg);
                        }
                        break;
                    }
                    case "stop":
                    case "快來，看我找到了什麽！":
                    case "come and see this!": {
                        try {
                            Point p = new Point();
                            if ((m = findXRegex.matcher(s)).find()) {
                                p.x = Integer.parseInt(m.group(1));
                            }
                            if ((m = findYRegex.matcher(s)).find()) {
                                p.y = Integer.parseInt(m.group(1));
                            }

                            String location = p.x+", "+p.y;

                            if(chatValueLower.equalsIgnoreCase("stop")){
                                if(MonsterClearList.stream().noneMatch(lp -> lp.y == p.y && lp.x == p.x)){
                                    game.dispatch.sendChat("Monster in "+location+" doest exist!");
                                }else{
                                    removeFromClearList(p);
                                    game.dispatch.sendChat("Monster in "+location+" cancelled");
                                }
                            }else{
                                if(MonsterClearList.stream().noneMatch(lp -> lp.y == p.y && lp.x == p.x)){
                                    Logger.log("Clean list for "+p.x+", "+p.y+" added");
                                    game.dispatch.sendChat("Monster in "+location+" successfully queued!");
                                    enqueueClearList(p);

                                }else{
                                    game.dispatch.sendChat("Monster in "+location+" already in queue");
                                }
                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                            Logger.log("Add clean list Failed!!!");
                        }

                        break;
                    }
                    default:
                }
            }
            catch(Exception e){
                e.printStackTrace();
                Logger.log(e.getMessage());
            }
        }).start();
    }
}
