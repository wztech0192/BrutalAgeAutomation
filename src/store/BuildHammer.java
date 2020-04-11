package store;

import util.LocalDateTimeAdapter;
import util.Logger;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;

@XmlRootElement(name = "BuildHammer")
public class BuildHammer {

    private LocalDateTime hammer;
    private String buildingName = "";
    private LocalDateTime expiration;
    private int nextBuildingLevel = 1;

    public BuildHammer(){}

    public BuildHammer(boolean isExpirable){
        hammer = LocalDateTime.now();
        if(isExpirable){
            expiration = LocalDateTime.now();
        }else{
            expiration = null;
        }
    }

    public LocalDateTime getHammer() {
        return hammer;
    }

    @XmlJavaTypeAdapter(value = LocalDateTimeAdapter.class)
    public void setHammer(LocalDateTime hammer) {
        this.hammer = hammer;
    }

    public LocalDateTime getExpiration() {
        return expiration;
    }

    @XmlJavaTypeAdapter(value = LocalDateTimeAdapter.class)
    public void setExpiration(LocalDateTime expiration) {
        this.expiration = expiration;
    }

    public String getBuildingName() {
        return buildingName;
    }

    @XmlElement
    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public int getNextBuildingLevel() {
        return nextBuildingLevel;
    }

    @XmlElement
    public void setNextBuildingLevel(int nextBuildingLevel) {
        this.nextBuildingLevel = nextBuildingLevel;
    }

    public boolean isLessThanMin(int min){
        long targetMin = Duration.between( LocalDateTime.now(), hammer).toMinutes();
        Logger.log(buildingName+ " Complete in: "+targetMin+" minutes" );
        return   targetMin <= min;
    }

    public boolean isExpired(){
        if(expiration==null) return false;

        long min = Duration.between(LocalDateTime.now(), expiration).toMinutes();
        Logger.log("Secondary hammer expired in: "+min+" minutes" );
        return min <= 1;
    }

    public boolean isAvailable(){
        return !isExpired() && isLessThanMin(1);
    }



    public void resetExpiration() {
        this.setExpiration(LocalDateTime.now().plusDays(2));
    }

    public void updateData(HashMap<String, Integer> buildings){
        if(!getBuildingName().equalsIgnoreCase("") && isAvailable()){
            buildings.put(getBuildingName(), getNextBuildingLevel());
            setBuildingName("");
            setNextBuildingLevel(1);
        }
    }
}
