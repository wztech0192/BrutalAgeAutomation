package store;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement(name = "Feature")
public class FeatureToggler {

    private HashMap<String, Boolean> features;
    private HashMap<String, Boolean> globalFeatures;

    public FeatureToggler(){
        features = new HashMap<>();
        features.put("Second Hammer", true);
        features.put("Use Squirrel", true);
        features.put("Heal Troops", true);
        features.put("Use Workshop", true);
        features.put("Use Resource", true);
        features.put("Hit Monster (8+)", true);
        features.put("Use Golden Tree", true);
        features.put("Read Mails", true);
        features.put("Train Warrior", true);
        features.put("Train Rider (7+)", true);
        features.put("Gathering (6+)", true);
        features.put("Upgrade Building", true);
        features.put("Train Shaman (7+)", true);
        features.put("Auto Repair", false);
    }

    public void cloneFeature(FeatureToggler featureToggler){
        for(Map.Entry<String, Boolean> entry: featureToggler.getFeatures().entrySet()){
            features.put(entry.getKey(), entry.getValue());
        }
    }


    public HashMap<String, Boolean> getGlobalFeatures() {
        if(globalFeatures == null){
            globalFeatures = new HashMap<>();
            globalFeatures.put("Transport At Start", false);
            globalFeatures.put("Auto Use Speed up", true);
            globalFeatures.put("Feed Temple", false);
         }
        return globalFeatures;
    }

    public void setGlobalFeatures(HashMap<String, Boolean> globalFeatures) {
        this.globalFeatures = globalFeatures;
    }

    public HashMap<String, Boolean> getFeatures() {
        return features;
    }

    @XmlElement
    public void setFeatures(HashMap<String, Boolean> features) {
        this.features = features;
    }

    public void set(String feature, boolean toggle){
        features.put(feature, toggle);
    }

    public boolean get(String feature) {
        if (features.containsKey(feature)) {
            return features.get(feature);
        }
        features.put(feature, false);
        return false;
    }


}
