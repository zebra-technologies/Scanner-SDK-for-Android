package com.zebra.scannercontrol.app.helpers;

import androidx.annotation.NonNull;

import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.DCSScannerInfo;

/**
 * Class to encapsulate scanner data and connected info
 */
public class AvailableScanner implements Comparable<AvailableScanner> {

    private int scannerId;
    private String scannerName;
    private String scannerAddress;
    private boolean isConnected;

    public boolean isConnectable() {
        return isConnectable;
    }

    public void setIsConnectable(boolean isConnectable) {
        this.isConnectable = isConnectable;
    }

    private  boolean isConnectable;

    private DCSSDKDefs.DCSSDK_CONN_TYPES connectionType;
    public AvailableScanner(DCSScannerInfo activeScanner) {
        this(activeScanner.getScannerID(),activeScanner.getScannerName(),activeScanner.getScannerHWSerialNumber(),activeScanner.isActive(),activeScanner.isAutoCommunicationSessionReestablishment(),activeScanner.getConnectionType());
    }

    public boolean isAutoReconnection() {
        return isAutoReconnection;
    }

    public void setIsAutoReconnection(boolean isAutoReconnection) {
        this.isAutoReconnection = isAutoReconnection;
    }

    public DCSSDKDefs.DCSSDK_CONN_TYPES getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(DCSSDKDefs.DCSSDK_CONN_TYPES connectionType) {
        this.connectionType = connectionType;
    }


    private boolean isAutoReconnection;

    public AvailableScanner(int scannerId, String scannerName, String scannerAddress, boolean isConnected, boolean isAutoReconnection, DCSSDKDefs.DCSSDK_CONN_TYPES connectionType){
        this.scannerId=scannerId;
        this.scannerName = scannerName;
        this.scannerAddress = scannerAddress;
        this.isConnected = isConnected;
        this.isAutoReconnection = isAutoReconnection;
        this.connectionType = connectionType;
        this.isConnectable = false;
    }
    public int getScannerId() {
        return scannerId;
    }

    public void setScannerId(int scannerId) {
        this.scannerId = scannerId;
    }

    public String getScannerName() {
        return scannerName;
    }

    public void setScannerName(String scannerName) {
        this.scannerName = scannerName;
    }

    public String getScannerAddress() {
        return scannerAddress;
    }

    public void setScannerAddress(String scannerAddress) {
        this.scannerAddress = scannerAddress;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final AvailableScanner other = (AvailableScanner) obj;
        return !((this.scannerName == null) ? (other.scannerName != null) : !this.scannerName.equals(other.scannerName));

    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.scannerName != null ? this.scannerName.hashCode() : 0) + (this.scannerName != null ? this.scannerName.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(@NonNull AvailableScanner availableScanner) {
        return this.toString().compareTo(availableScanner.toString());
    }

    @Override
    public String toString() {
        return scannerName + "\n" + scannerAddress;
    }
}
