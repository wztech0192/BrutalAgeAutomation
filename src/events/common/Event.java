package events.common;

import java.util.HashMap;
import org.opencv.core.Point;
import store.MatchPoint;

public class Event{
    public String targetName;
    public int maxRedo;
    public Event[] chain;
    public String templateName;
    public int templateMatches;
    public int[] loc;
    public int loop;
    public double delay;
    public boolean isAccess;
    public String eventType;
    public String name;
    public boolean isUpgrade;
    public boolean isBuilding;
    public ConditionListener listener;

    public Event(){};

    public static Event builder(HashMap<String,Event> _map, String name){
        Event event = builder(name);
        _map.put(name, event);
        return event;
    }

    public static Event builder(){
        return new Event();
    }


    public static Event builder(String name){
        return builder().setName(name);
    }

    public Event copy(){
        return builder(this.name)
                .setDelay(this.delay)
                .setLoc(this.loc.clone())
                .setTargetName(this.targetName)
                .setListener(this.getListener())
                .setMaxRedo(this.maxRedo);
    }

    public ConditionListener getListener(){
        return listener;
    }

    public Event setListener(ConditionListener listener){
        this.listener = listener;
        return this;
    }

    public Event setTemplateName(String templateName){
        this.templateName = templateName;
        return this;
    }

    public String getName() {
        return name;
    }

    public Event setName(String name) {
        this.name = name;
        return this;
    }

    public String getTargetName() {
        return targetName;
    }

    public Event setTargetName(String targetName) {
        this.targetName = targetName;
        return this;
    }

    public int getMaxRedo() {
        return maxRedo;
    }

    public Event setTemplateMatches(int templateMatches){
        this.templateMatches=templateMatches;
        return this;
    }

    public Event setMaxRedo(int maxRedo) {
        this.maxRedo = maxRedo;
        return this;
    }


    public Event[] getChain() {
        return chain;
    }

    public Event setChain(Event ...chain) {
        this.chain = chain;
        return this;
    }

    public int[] getLoc() {
        return loc;
    }


    public Event setLoc(MatchPoint point) {
        this.loc = new int[]{
               point.x,
               point.y
        };
        return this;
    }

    public Event setLoc(int ...loc) {
        this.loc = loc;
        return this;
    }

    public int getLoop() {
        return loop;
    }

    public Event setLoop(int loop) {
        this.loop = loop;
        return this;
    }

    public double getDelay() {
        return delay;
    }

    public Event setDelay(double delay) {
        this.delay = delay;
        return this;
    }

    public String getEventType() {
        return eventType;
    }

    public Event setEventType(String eventType) {
        this.eventType = eventType;
        return this;
    }

    public Event isBuilding() {
        this.isBuilding = true;
        return this;
    }

    public Event isBuilding(boolean isUpgrade) {
        this.isUpgrade = isUpgrade;
        return isBuilding();
    }

    public Event isUpgrade() {
        this.isUpgrade = true;
        return this;
    }

    public Event isAccess() {
        this.isAccess = true;
        return this;
    }



/*
    public Event(String name, String targetName, int[] loc, int delay){
        this.name = name;
        this.targetName = targetName;
        this.loc = loc;
        if(loc.length == 2){
            eventType = "tap";
        }
        else{
            eventType = "swipe";
        }
        this.delay = delay;

    };

    public Event(String name, String targetName, int[] loc) {
        this(name, targetName, loc, 0);
    }*/
}

