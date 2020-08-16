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

    public static String[] Columns = { "ID", "Name", "Error", "Stronghold",  "P) Meat", "P) Wood", "Troops", "Wounded"};

    //"Wood", "Ivory", "Mana", "Rock", "Meat"
    public String[] getColumnData(){
        return new String[]{
                getSubId(),
                getName(),
                String.valueOf(getError()),
                String.valueOf(getBuildingLvl("stronghold")),
                getNumberFeaturer().getGatherPriorities().get("meat")+") "+df2.format(resources.get("meat") / K) +" K",
                getNumberFeaturer().getGatherPriorities().get("wood")+") "+df2.format(resources.get("wood") / K) +" K",
                String.valueOf(getTroops()),
                getWounded()
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

    private String name;
    private static DecimalFormat df2 = new DecimalFormat("#.##");
    private static double K = 1000.0;
    private int round;
    private FeatureToggler featureToggler;
    private HashMap<String, Integer> buildings;
    private static HashMap<String, Integer> defaultBuildings;
    private HashMap<String, Integer> resources;
    private boolean changedServer = false;
    private boolean finishInit;
    private int troops = 0;
    private boolean isThirtyDay = false;

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
    private String wounded = "";
    private boolean isJoinClan = false;
    private boolean isRandomized = false;
    private int previousLevel = 0;
    private NumberFeaturer numberFeaturer;

    static{
        defaultBuildings = new HashMap<>();
        defaultBuildings.put("stronghold",1);
        defaultBuildings.put("portal",1);
        defaultBuildings.put("help_wagon", 0);

        defaultBuildings.put("war_camp",0);
        defaultBuildings.put("healing_spring",0);
        defaultBuildings.put("research",0);
        defaultBuildings.put("warehouse",0);
        defaultBuildings.put("tower",1);
        defaultBuildings.put("golden_tree",0);

        defaultBuildings.put("well1",0);
        defaultBuildings.put("well2",0);
        defaultBuildings.put("well3",0);
        defaultBuildings.put("well4",0);
        defaultBuildings.put("well5",0);

        defaultBuildings.put("warhub1",0);
        defaultBuildings.put("warhub2",0);
        defaultBuildings.put("warhub3",0);
        defaultBuildings.put("warhub4",0);
        defaultBuildings.put("warhub5",0);

        defaultBuildings.put("defense_hall",0);
        defaultBuildings.put("war_hall",0);
    }

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

        syncBuildings();
        resetNumberFeaturer();
    }

    private void syncBuildings() {
        if(buildings == null){
            buildings = new HashMap<>();
        }
        for(Map.Entry<String, Integer> entry: defaultBuildings.entrySet()){
            if(!buildings.containsKey(entry.getKey())){
                buildings.put(entry.getKey(),entry.getValue());
            }
        }
    }

    @XmlElement
    public void setThirtyDay(boolean thirtyDay) {
        isThirtyDay = thirtyDay;
    }

    public boolean isThirtyDay() {
        return isThirtyDay;
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
        syncBuildings();
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

    public void validateBuildings(){
        for(Map.Entry<String, Integer> entry: getBuildings().entrySet()){
            if(entry.getValue() > 15){
                entry.setValue(4);
            }
        }

        if(getBuildingLvl("stronghold") < getBuildingLvl("portal")){
            setBuildingLevel("stronghold",  getBuildingLvl("portal"));
        }
    }

    public boolean isJoinClan() {
        return isJoinClan;
    }
    @XmlElement
    public void setJoinClan(boolean joinClan) {
        isJoinClan = joinClan;
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

    public NumberFeaturer getNumberFeaturer() {
        if(numberFeaturer == null){
            resetNumberFeaturer();
        }
        return numberFeaturer;
    }
    @XmlElement
    public void setNumberFeaturer(NumberFeaturer numberFeaturer) {
        this.numberFeaturer = numberFeaturer;
    }

    public void resetNumberFeaturer() {
        this.numberFeaturer = new NumberFeaturer();
    }

    public ArrayList<String> getGatherPrioritiesArray(boolean isPriorityGrowth) {
        HashMap<String, Integer> priorityTree = new HashMap<>();
        for(Map.Entry<String, Integer> entry : getNumberFeaturer().getGatherPriorities().entrySet()){

            int value = entry.getValue();

            if(isPriorityGrowth && getBuildingLvl("stronghold") >= 9){
                if(entry.getKey().equalsIgnoreCase("meat") || entry.getKey().equalsIgnoreCase("wood")){
                    if(getResource(entry.getKey()) < 1500000){
                        value = 999;
                    }
                }
            }

            if(value != 0){
                if(entry.getKey().equalsIgnoreCase("meat") || entry.getKey().equalsIgnoreCase("wood")){

                    priorityTree.put(entry.getKey()+"3",value);
                    priorityTree.put(entry.getKey()+"4", value);
                }else{
                    priorityTree.put(entry.getKey(), value);
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

    public void resetFeatureToggler() {
        this.featureToggler = new FeatureToggler();
    }

    public String getName() {
        return name;
    }
    @XmlElement
    public void setName(String name) {
        this.name  = name;
    }
    @XmlElement
    public void setWounded(String wounded) {
    }

    public String getWounded() {
        return wounded;
    }
}
