package store;

import game.GameInstance;
import util.LocalDateTimeAdapter;
import util.Logger;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@XmlRootElement(name = "Account")
public class Account {

    public static String[] Columns = { "ID", "Error", "Stronghold",  "P) Meat", "P) Wood", "P) Rock","P) Ivory", "P) Mana", "Troops",  FeatureToggler.displayShortName()};

    //"Wood", "Ivory", "Mana", "Rock", "Meat"
    public String[] getColumnData(){
        return new String[]{
                getSubId(),
                String.valueOf(getError()),
                String.valueOf(getBuildingLvl("stronghold")),
                getGatherPriorities().get("meat")+") "+df2.format(resources.get("meat") / K) +" K",
                getGatherPriorities().get("wood")+") "+df2.format(resources.get("wood") / K) +" K",
                getGatherPriorities().get("rock")+") "+df2.format( resources.get("rock") / K )+" K",
                getGatherPriorities().get("ivory")+") "+df2.format(resources.get("ivory") / K )+" K",
                getGatherPriorities().get("mana")+") "+df2.format( resources.get("mana") / K) +" K",
                String.valueOf(getTroops()),
                getFeatureToggler().getShortValue()
        };
    }

    public static String[] Hordes = new String[]{
            "black",
            "purple",
            "red",
            "yellow",
            "green",
            "blue"
    };

    private static DecimalFormat df2 = new DecimalFormat("#.##");
    private static double K = 1000.0;

    private int round;
    private FeatureToggler featureToggler;
    private HashMap<String, Integer> buildings;
    private HashMap<String, Integer> resources;
    private TreeMap<String, Integer> gatherPriorities;
    private boolean changedServer = false;
    private boolean finishInit;
    private int troops = 0;

    private String id = "";

    private BuildHammer primaryHammer = new BuildHammer(false);
    private BuildHammer secondaryHammer =  new BuildHammer(true);

    private LocalDateTime lastGiftTime;
    private LocalDateTime lastRound;

    private int serverID = 519;
    private int horde = 0;
    private String clan = "";
    private int level = 1;
    private int error = 0;
    private boolean isJoinClan = false;
    private boolean isRandomized = false;
    private int previousLevel = 0;



    public Account(){}
    public Account(int server, String id){
        featureToggler = new FeatureToggler();

        this.lastRound = LocalDateTime.now();
        this.lastGiftTime = LocalDateTime.now();
        this.serverID = server;
        this.id = id;
        resources = new HashMap<>();

        resources.put("meat", 0);
        resources.put("wood", 0);
        resources.put("rock", 0);
        resources.put("ivory", 0);
        resources.put("mana", 0);

        buildings = new HashMap<>();

        buildings.put("stronghold",1);
        buildings.put("portal",1);
        buildings.put("help_wagon", 0);

        buildings.put("war_camp",0);
        buildings.put("healing_spring",0);
        buildings.put("research",0);
        buildings.put("warehouse",0);
        buildings.put("tower",1);
        buildings.put("golden_tree",0);

        buildings.put("well1",0);
        buildings.put("well2",0);
        buildings.put("well3",0);
        buildings.put("well4",0);
        buildings.put("well5",0);

        buildings.put("warhub1",0);
        buildings.put("warhub2",0);
        buildings.put("warhub3",0);
        buildings.put("warhub4",0);
        buildings.put("warhub5",0);


    populateGatherPriority();

    }


    public int getTroops() {
        return troops;
    }
    @XmlElement
    public void setTroops(int troops) {
        this.troops = troops;
    }

    public String getHordeLabel() {
       return Hordes[horde];
    }

    public int getHordeInt(String hordeStr) {
        for(int i =0; i<Hordes.length; i++){
            if(Hordes[i].equals(hordeStr)){
                return i;
            }
        }
        return 0;
    }


    public int getBuildingLvl(String building) {
        if(!buildings.containsKey(building)){
            return 0;
        }
        return buildings.get(building);
    }

    public void levelUpBuilding(GameInstance game, String building){
        buildings.put(building, getBuildingLvl(building) + 1);
        game.updateAccount();
    }
    public void setBuildingLevel(GameInstance game, String building, int level){
        buildings.put(building, level);
        game.updateAccount();
    }
    public void setBuildingLevel(String building, int level){
        buildings.put(building, level);
    }

    public HashMap<String, Integer> getBuildings() {
        return buildings;
    }

    public boolean isRandomized() {
        return isRandomized;
    }

    @XmlElement
    public void setRandomized(boolean randomized) {
        isRandomized = randomized;
    }


    @XmlElement
    public void setBuildings(HashMap<String, Integer> buildings) {
        this.buildings = buildings;
    }

    public int getServerID() {
        return serverID;
    }

    public int getLevel() {
        return level;
    }

    @XmlElement
    public void setLevel(int level) {
        this.level = level;
    }

    @XmlElement
    public void setServerID(int serverID) {
        this.serverID = serverID;
    }


    public int getHorde() {
        return horde;
    }

    @XmlElement
    public void setHorde(int horde) {
        this.horde = horde;
    }

    public void setHorde(String horde) {
        this.horde = getHordeInt(horde);
    }

    public String getId() {
        return id == null ? "" : id;
    }
    public String getSubId(){
        return id == null || id.length()<6 ? "" : this.getId().substring(this.getId().length()-6);
    }

    public FeatureToggler getFeatureToggler() {
        if(featureToggler == null){
            featureToggler = new FeatureToggler();
        }
        return featureToggler;
    }



    @XmlElement
    public void setFeatureToggler(FeatureToggler featureToggler) {
        this.featureToggler = featureToggler;
    }

    @XmlElement
    public void setId(String id )
    {
        if(id == null) id = "";

        this.id = id;
    }

    public boolean isRssLessThan(String rss1, String rss2){
        return getResource(rss1) < getResource(rss2);
    }

    public BuildHammer getPrimaryHammer() {
        return primaryHammer;
    }
    @XmlElement
    public void setPrimaryHammer(BuildHammer primaryHammer) {
        this.primaryHammer = primaryHammer;
    }

    public BuildHammer getSecondaryHammer() {
        return secondaryHammer;
    }
    @XmlElement
    public void setSecondaryHammer(BuildHammer secondaryHammer) {
        this.secondaryHammer = secondaryHammer;
    }

    public String getClan() {
        return clan;
    }
    @XmlElement
    public void setClan(String clan) {
        this.clan = clan;
    }

    public int getError() {
        return error;
    }
    @XmlElement
    public void setError(int error) {
        this.error = error;
    }
    public void incrementError(){
        error++;
    }

    public void refreshHammerData() {
        getPrimaryHammer().updateData(buildings);
        getSecondaryHammer().updateData(buildings);
    }

    public HashMap<String, Integer> getResources() {
        return resources;
    }
    @XmlElement
    public void setResources(HashMap<String, Integer> resources) {
        this.resources = resources;
    }

    public boolean isFinishInit() {
        return finishInit;
    }
    @XmlElement
    public void setFinishInit(boolean finishInit) {
        this.finishInit = finishInit;
    }

    public int getResource(String rss){
        return resources.get(rss);
    }

    public void setResource(String rss, int num){
        if(num != -1)
            resources.put(rss, num);
    }
    @XmlElement
    public void setChangedServer(boolean changedServer) {
        this.changedServer = changedServer;
    }

    public boolean getChangedServer() {
        return this.changedServer;
    }


    public LocalDateTime getLastGiftTime() {
        return lastGiftTime;
    }

    @XmlJavaTypeAdapter(value = LocalDateTimeAdapter.class)
    public void setLastGiftTime(LocalDateTime lastGiftTime) {
        this.lastGiftTime = lastGiftTime;
    }

    public LocalDateTime getLastRound() {
        return lastRound == null ? LocalDateTime.now() : lastRound;
    }

    @XmlJavaTypeAdapter(value = LocalDateTimeAdapter.class)
    public void setLastRound(LocalDateTime lastRound) {
        this.lastRound = lastRound;
    }

    public boolean isOver(){
        if(lastRound == null) lastRound = LocalDateTime.now();
        long last =  Duration.between(lastRound, LocalDateTime.now()).toMinutes();
        Logger.log(last+" minutes since last round");
        return last >= 30;
    }

    public String nextBuildingTarget(BuildHammer hammer) {

        String highestWell = "well1";
        String highestWarhub = "warhub1";

        for(int i =2; i<=5; i++){
            if(getBuildingLvl(highestWell) < getBuildingLvl("well"+i)){
                highestWell = "well"+i;
            }
            if(getBuildingLvl(highestWarhub) < getBuildingLvl("warhub"+i)){
                highestWarhub = "warhub"+i;
            }
        }

      /*  if(buildingCondition("portal", 10, hammer)){
            return "portal";
        }*/
        if(buildingCondition(highestWarhub, 5, hammer)){
            return highestWarhub;
        }
        else if(buildingCondition(highestWell, 6, hammer)){
            return highestWell;
        }
        else if(buildingCondition("research", 7, hammer)){
            return "research";
        }
        else if(buildingCondition("warehouse", 8, hammer)){
            return "warehouse";
        }
        else if(buildingCondition("golden_tree", 9, hammer)){
            return "golden_tree";
        }
        else if(buildingCondition("help_wagon", 25, hammer)){
            return "help_wagon";
        }

        int lowestLvl = 50;
        String lowest = "";
        if(buildingCondition("war_camp", 25, hammer)){
            lowest = "war_camp";
            lowestLvl = getBuildingLvl("war_camp");
        }

        for(int i =1; i<=5; i++){
            if(buildingCondition("warhub"+i, 25, hammer)){
                if(getBuildingLvl("warhub"+i) < lowestLvl){
                    lowest = "warhub"+i;
                    lowestLvl = getBuildingLvl("warhub"+i);
                }
            }
            if(buildingCondition("well"+i, 25, hammer)){
                if(getBuildingLvl("well"+i) < lowestLvl){
                    lowest = "well"+i;
                    lowestLvl = getBuildingLvl("well"+i);
                }
            }
        }

        return lowest;
    }
    @XmlElement
    public boolean isJoinClan() {
        return isJoinClan;
    }

    public void setJoinClan(boolean joinClan) {
        isJoinClan = joinClan;
    }

    private boolean buildingCondition(String target, int maxLvl, BuildHammer hammer){

        return getBuildingLvl(target) != 0
                && !target.equalsIgnoreCase(hammer.getBuildingName())
                && getBuildingLvl(target) < getBuildingLvl("stronghold")
                && getBuildingLvl(target) < maxLvl;
    }

    public int getRound() {
        return round;
    }
    @XmlElement
    public void setRound(int round) {
        this.round = round;
    }

    public int getPreviousLevel() {
        return previousLevel;
    }
    @XmlElement
    public void setPreviousLevel(int previousLevel) {
        this.previousLevel = previousLevel;
    }

    public TreeMap<String, Integer> getGatherPriorities() {
        if(gatherPriorities == null){
            populateGatherPriority();
        }
        return gatherPriorities;
    }

    public void setGatherPriority(String key, int value) {
        if(this.gatherPriorities == null){
            populateGatherPriority();
        }
        this.gatherPriorities.put(key, value);
    }

    @XmlElement
    public void setGatherPriorities(TreeMap<String, Integer> gatherPriorities) {
        this.gatherPriorities = gatherPriorities;
    }

    public void populateGatherPriority(){
        gatherPriorities = new TreeMap<>();
        gatherPriorities.put("meat", 0);
        gatherPriorities.put("wood", 0);
        gatherPriorities.put("rock", 0);
        gatherPriorities.put("ivory", 0);
        gatherPriorities.put("mana", 0);
        gatherPriorities.put("a_minLevel", 2);
        gatherPriorities.put("a_maxLevel", 4);
    }

    public ArrayList<String> getGatherPrioritiesArray() {
        HashMap<String, Integer> priorityTree = new HashMap<>();
        for(Map.Entry<String, Integer> entry : gatherPriorities.entrySet()){
            if(entry.getValue() != 0 && !entry.getKey().equalsIgnoreCase("a_maxLevel") && !entry.getKey().equalsIgnoreCase("a_minLevel")){
                if(entry.getKey().equalsIgnoreCase("meat") || entry.getKey().equalsIgnoreCase("wood")){
                    priorityTree.put(entry.getKey()+"3", entry.getValue());
                    priorityTree.put(entry.getKey()+"4", entry.getValue());
                }else{
                    priorityTree.put(entry.getKey(), entry.getValue());
                }
            }
        }
        Set<Map.Entry<String, Integer>> set = priorityTree.entrySet();
        ArrayList<Map.Entry<String, Integer>> list = new ArrayList<>(set);
        list.sort((a,b)->b.getValue() - a.getValue());

        return (ArrayList<String>) list.stream().map(Map.Entry::getKey).collect(Collectors.toList());
    }


    public boolean doInRound(int round){
        return this.getBuildingLvl("stronghold") < 10 || this.round % round == 0;
    }
}
