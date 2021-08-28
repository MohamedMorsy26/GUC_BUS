package com.example.herewego;

import java.io.Serializable;

public class MenuBus implements Serializable, Comparable {
    private String busNumber;
    private String route;
    private boolean isOnline;
    private boolean requested;
    private boolean p2pshared;
    private boolean isFavourite;


    public MenuBus(String busNumber, String route, boolean isOnline, boolean requested, boolean p2pshared, boolean isFavourite) {
        this.busNumber = busNumber;
        this.route = route;
        this.isOnline = isOnline;
        this.requested = requested;
        this.p2pshared = p2pshared;
        this.isFavourite = isFavourite;
    }

    public String getBusNumber() {
        return busNumber;
    }

    public String getRoute() {
        return route;
    }

    public boolean isOnline() {return isOnline;}

    public void setBusNumber(String busNumber) {this.busNumber = busNumber;}

    public void setRoute(String route) {this.route = route; }

    public void setOnline(boolean online) {isOnline = online;}

    public boolean isRequested() {return requested;}

    public void setRequested(boolean requested) {this.requested = requested;}

    public boolean isShared() {return p2pshared;}

    public void setShared(boolean shared) {this.p2pshared = shared;}

    public boolean isFavourite() {return isFavourite;}

    public void setFavourite(boolean favourite) {isFavourite = favourite;}

    @Override
    public int compareTo(Object o) {
        if(this.isFavourite() && !((MenuBus)o).isFavourite()){
            return -1;
        }
        else if(!this.isFavourite() && ((MenuBus)o).isFavourite()){
            return 1;
        }
        else if (this.isFavourite() && ((MenuBus)o).isFavourite()){
            return this.route.compareTo(((MenuBus)o).getRoute());
        }
        else if(!this.isFavourite() && !((MenuBus)o).isFavourite()){
            return this.route.compareTo(((MenuBus)o).getRoute());
        }
        return 0;
    }
}
