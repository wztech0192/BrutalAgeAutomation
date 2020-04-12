package store;

import util.FilePath;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;

@XmlRootElement(name = "Metadata")
public class Metadata {
    private String accountPath = FilePath.ACCOUNT_PATH;
    private String ip = "127.0.0.1:62001";
    private FeatureToggler featureToggler;
    private int delay = 600;
    private TreeMap<String, Integer> gatherPriorities;
    private LinkedList<String> SavedPosAcc = new LinkedList<>();
    private int maxPosAcc = 5;
    public Metadata(){
        featureToggler = new FeatureToggler();
    }



    public FeatureToggler getFeatureToggler() {
        if(featureToggler == null){
            featureToggler = new FeatureToggler();
        }
        return featureToggler;
    }

    public LinkedList<String> getSavedPosAcc() {
        return SavedPosAcc;
    }

    public int getMaxPosAcc() {
        return maxPosAcc;
    }

    public void setMaxPosAcc(int maxPosAcc) {
        this.maxPosAcc = maxPosAcc;
    }

    @XmlElement
    public void setSavedPosAcc(LinkedList<String> savedPosAcc) {
        SavedPosAcc = savedPosAcc;
    }

    @XmlElement
    public void setFeatureToggler(FeatureToggler featureToggler) {
        this.featureToggler = featureToggler;
    }

    public String getAccountPath() {
        return accountPath;
    }
    @XmlElement
    public void setAccountPath(String accountPath) {
        this.accountPath = accountPath;
    }

    public String getIp() {
        return ip;
    }
    @XmlElement
    public void setIp(String ip) {
        this.ip = ip;
    }
    @XmlElement
    public void setDelay(int delay) {
        this.delay = delay;
    }
    public int getDelay(){
        return this.delay;
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
}
