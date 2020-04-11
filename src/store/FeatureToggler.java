package store;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement(name = "Feature")
public class FeatureToggler {

    private HashMap<String, Boolean> features;

    public FeatureToggler(){
        features = new HashMap<>();
        features.put("Second Hammer", false);
        features.put("Use Squirrel", true);
        features.put("Heal Troops", true);
        features.put("Train Troops", true);
        features.put("Use Workshop", true);
        features.put("Use Resource", true);
        features.put("Hit Monster", true);
        features.put("Use Golden Tree", true);
        features.put("Read Mails", true);
        features.put("Gathering", true);
        features.put("Transport", false);
        features.put("Upgrade Building", true);
    }

    public void cloneFeature(FeatureToggler featureToggler){
        for(Map.Entry<String, Boolean> entry: featureToggler.getFeatures().entrySet()){
            features.put(entry.getKey(), entry.getValue());
        }
    }

    public static String displayShortName() {
        return "SH US HT TT UW UR HM GT RM GA TR UB";
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

    public String getShortValue() {
        String str="";
        str += get("Second Hammer") ? "T " : "F ";
        str += get("Use Squirrel")? "T " : "F ";
        str += get("Heal Troops")? "T " : "F ";
        str +=get("Train Troops")? "T " : "F ";
        str +=get("Use Workshop")? "T " : "F ";
        str +=get("Use Resource")? "T " : "F ";
        str +=get("Hit Monster")? "T " : "F ";
        str +=get("Use Golden Tree")? "T " : "F ";
        str +=get("Read Mails")? "T " : "F ";
        str +=get("Gathering")? "T " : "F ";
        str +=get("Transport")? "T" : "F";
        str +=get("Upgrade Building")? "T" : "F";
        return str;
    }
}
