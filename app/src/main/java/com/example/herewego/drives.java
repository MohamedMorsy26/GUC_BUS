package com.example.herewego;

import java.io.Serializable;

public class drives implements Serializable {
    private String phonenumber;
    private String busroute;
    private String busnumber;
    private double buslat;
    private double buslon;

    public drives() {
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

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

    public double getBuslat() {
        return buslat;
    }

    public void setBuslat(double buslat) {
        this.buslat = buslat;
    }

    public double getBuslon() {
        return buslon;
    }

    public void setBuslon(double buslon) {
        this.buslon = buslon;
    }
}
