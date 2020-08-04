package com.dexter.beacon;

public class BeaconModel {

    private String bleAddress;
    private String namespaceID;
    private String instanceIDs;
    private double distance;

    public String getBleAddress() {
        return bleAddress;
    }

    public void setBleAddress(String bleAddress) {
        this.bleAddress = bleAddress;
    }

    public String getNamespaceID() {
        return namespaceID;
    }

    public void setNamespaceID(String namespaceID) {
        this.namespaceID = namespaceID;
    }

    public String getInstanceIDs() {
        return instanceIDs;
    }

    public void setInstanceIDs(String instanceIDs) {
        this.instanceIDs = instanceIDs;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
