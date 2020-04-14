package store;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Config")
public class Config {


    private String NoxPath = System.getenv("ProgramFiles(X86)")+"/Nox/bin";

    private String OwnerName = "";

    public String getOwnerName() {
        return OwnerName;
    }

    public void setOwnerName(String ownerName) {
        OwnerName = ownerName;
    }

    public String getNoxPath() {
        return NoxPath;
    }
    @XmlElement
    public void setNoxPath(String noxPath) {
        NoxPath = noxPath;
    }
}
