package events.handler;

import game.GameInstance;
import util.Logger;

import java.awt.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Chat {
    private final static Pattern findXRegex = Pattern.compile("\"X\":(\\d*),");
    private final static Pattern findYRegex = Pattern.compile("\"Y\":(\\d*),");

    public static ConcurrentLinkedQueue<Point> MonsterClearList = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<Point> FinishedClearList = new ConcurrentLinkedQueue<>();

    public static void fire(GameInstance game, String s, String chatValue)  {
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
                    case "finished":{
                        if(FinishedClearList.isEmpty()){
                            game.dispatch.sendChat("Finished list is empty");
                        }else{
                            String msg = FinishedClearList.stream().map(p -> "("+p.x+", "+p.y+")").collect(Collectors.joining("; "));
                            game.dispatch.sendChat(FinishedClearList.size()+ " finished: "+msg);
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
                                    MonsterClearList.removeIf(lp->lp.y == p.y && lp.x == p.x);
                                    game.dispatch.sendChat("Monster in "+location+" cancelled");
                                }
                            }else{
                                if(MonsterClearList.stream().noneMatch(lp -> lp.y == p.y && lp.x == p.x)){
                                    Logger.log("Clean list for "+p.x+", "+p.y+" added");
                                    game.dispatch.sendChat("Monster in "+location+" successfully queued!");
                                    MonsterClearList.add(p);
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
