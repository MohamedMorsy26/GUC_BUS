package com.example.herewego;

import java.io.Serializable;

public class p2pshare implements Serializable {
    private String busroute;
    private String busnumber;
    private String email;
    private double buslat;
    private double buslon;

    public p2pshare() {}

    public String getBusroute() {
        return busroute;
    }

    public void setBusroute(String busroute) {
        this.busroute = busroute;
    }

    public String getBusnumber() {
        return busnumber;
    }

    public void setBusnumber(String busnumber) {
        this.busnumber = busnumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String user_email) {
        this.email = user_email;
    }

    public double getBuslat() {return buslat; }

    public void setBuslat(double buslat) {this.buslat = buslat; }

    public double getBuslon() {return buslon;}

    public void setBuslon(double buslon) {this.buslon = buslon; }
}
