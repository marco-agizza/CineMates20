package com.SoftEngUniNA.CineMates20Desktop.models;

public class User {
    private int accesses;
    private boolean hasfavourites, haslists, hassearches;

    public int getAccesses() {
        return accesses;
    }

    public boolean isHasfavourites() {
        return hasfavourites;
    }

    public boolean isHaslists() {
        return haslists;
    }

    public boolean isHassearches() {
        return hassearches;
    }

    public void setAccesses(int accessees) {
        this.accesses = accessees;
    }

    public void setHasfavourites(boolean hasfavourites) {
        this.hasfavourites = hasfavourites;
    }

    public void setHaslists(boolean haslists) {
        this.haslists = haslists;
    }

    public void setHassearches(boolean hassearches) {
        this.hassearches = hassearches;
    }

    @Override
    public String toString() {
        return "User{" + "accesses=" + accesses + ", hasfavourites=" + hasfavourites + ", haslists=" + haslists + ", hassearches=" + hassearches + '}';
    }
    
}
