package store;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.TreeMap;

@XmlRootElement(name = "NumberFeaturer")
public class NumberFeaturer {
    private TreeMap<String, Integer> gatherPriorities;
    private HashMap<String, Integer> numberSetting;


    public NumberFeaturer(){
        populateGatherPriority();
        populateNumberSetting();
    }

    public HashMap<String, Integer> getNumberSetting() {
        if(numberSetting == null){
            populateNumberSetting();
        }
        return numberSetting;
    }

    public void setNumberSetting(String key, int value) {
        if(this.numberSetting == null){
            populateNumberSetting();
        }
        this.numberSetting.put(key, value);
    }

    @XmlElement
    public void setNumberSetting(HashMap<String, Integer> numberSetting) {
        this.numberSetting = numberSetting;
    }

    private void populateNumberSetting() {
        numberSetting = new HashMap<>();
        numberSetting.put("Min Monster Level", 2);
        numberSetting.put("Max Monster Level", 4);
        numberSetting.put("Max Troop", 30000);
        numberSetting.put("Transport Round", 0);
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
    }
}
