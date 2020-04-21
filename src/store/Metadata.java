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
    private String nox = "";
    private FeatureToggler featureToggler;
    private int delay = 600;
    private NumberFeaturer numberFeaturer;
    private LinkedList<String> SavedPosAcc = new LinkedList<>();
    private int maxPosAcc = 5;
    private String clan = "";
    private int server = 519;
    private int horde = 0;

    public Metadata(){
        featureToggler = new FeatureToggler();
        resetNumberFeaturer();
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

    public int getHorde() {
        return horde;
    }

    public void setClan(String clan) {
        this.clan = clan;
    }

    public int getServer() {
        return server;
    }

    public void setHorde(int horde) {
        this.horde = horde;
    }

    public String getClan() {
        return clan;
    }

    public void setServer(int server) {
        this.server = server;
    }

    public String getNox() {
        return nox;
    }
    @XmlElement
    public void setNox(String nox) {
        this.nox = nox;
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

    public void resetFeatureToggler() {
        this.featureToggler = new FeatureToggler();
    }
}
