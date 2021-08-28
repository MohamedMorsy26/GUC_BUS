package com.example.herewego;

public class bus {
    private String bus_number;
    private String bus_route;
    private String bus_capacity;
    private boolean online;
    private boolean p2pshared;
    private boolean requested;

    public bus() {
    }

    public String getBus_number() {
        return bus_number;
    }

    public void setBus_number(String bus_number) {
        this.bus_number = bus_number;
    }

    public String getBus_route() {
        return bus_route;
    }

    public void setBus_route(String bus_route) {
        this.bus_route = bus_route;
    }

    public String getBus_capacity() {
        return bus_capacity;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public void setBus_capacity(String bus_capacity) {
        this.bus_capacity = bus_capacity;
    }

    public boolean isP2pshared() {return p2pshared;}

    public void setP2pshared(boolean p2pshared) {this.p2pshared = p2pshared;}

    public boolean isRequested() {return requested;}

    public void setRequested(boolean requested) {this.requested = requested;}
}
