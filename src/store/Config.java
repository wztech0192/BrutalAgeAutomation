package store;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Config")
public class Config {
    private String EventName = "event4";
    private String NoxPath = System.getenv("ProgramFiles(X86)")+"/Nox/bin";

    private String OwnerName = "";

    public String getOwnerName() {
        return OwnerName;
    }

    public void setOwnerName(String ownerName) {
        OwnerName = ownerName;
    }

    public String getEventName() {
        if(EventName == null || EventName.equalsIgnoreCase("")){
            return "event4";
        }
        return EventName;
    }
    @XmlElement
    public void setEventName(String eventName) {
        EventName = eventName;
    }

    public String getNoxPath() {
        return NoxPath;
    }
    @XmlElement
    public void setNoxPath(String noxPath) {
        NoxPath = noxPath;
    }
}
