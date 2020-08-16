package store;

import util.FilePath;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Config")
public class Config {
    private String EventName = "event4";
    private String NoxPath = System.getenv("ProgramFiles(X86)")+"/Nox/bin";
    private String EventFolder = "baevents";
    private String OwnerName = "";
    private boolean SaveErrorScreenshot = false;
    private int instanceNumber = 4;

    public String getOwnerName() {
        return OwnerName;
    }
    @XmlElement
    public void setSaveErrorScreenshot(boolean saveErrorScreenshot) {
        SaveErrorScreenshot = saveErrorScreenshot;
    }

    public boolean isSaveErrorScreenshot() {
        return SaveErrorScreenshot;
    }

    public void setOwnerName(String ownerName) {
        OwnerName = ownerName;
    }

    public String getEventFolder() {
        return EventFolder;
    }
    @XmlElement
    public void setEventFolder(String eventFolder) {
        EventFolder = eventFolder;
    }

    public int getInstanceNumber() {
        return instanceNumber;
    }
    @XmlElement
    public void setInstanceNumber(int instanceNumber) {
        this.instanceNumber = instanceNumber;
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

    public String getEventFolderPath() {
        return FilePath.RootPath+"/"+getEventFolder();
    }
}
