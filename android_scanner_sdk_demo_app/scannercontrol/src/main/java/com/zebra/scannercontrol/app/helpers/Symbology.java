package com.zebra.scannercontrol.app.helpers;

/**
 * Class to encapsulate Symbology Info
 */
public class Symbology {
    private String symbologyName;
    private int rmdAttributeID;
    private boolean isEnabled;
    private boolean isSupported;

    public Symbology(String symbologyName, int rmdAttributeID){
        this.symbologyName = symbologyName;
        this.rmdAttributeID = rmdAttributeID;
    }

    public String getSymbologyName() {
        return symbologyName;
    }

    public void setSymbologyName(String symbologyName) {
        this.symbologyName = symbologyName;
    }

    public int getRmdAttributeID() {
        return rmdAttributeID;
    }

    public void setRmdAttributeID(int rmdAttributeID) {
        this.rmdAttributeID = rmdAttributeID;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public boolean isSupported() {
        return isSupported;
    }

    public void setSupported(boolean isSupported) {
        this.isSupported = isSupported;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final Symbology other = (Symbology) obj;
        return !((this.symbologyName == null) ? (other.symbologyName != null) : !this.symbologyName.equals(other.symbologyName));

    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.symbologyName != null ? this.symbologyName.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return symbologyName + "\n" + rmdAttributeID;
    }
}
