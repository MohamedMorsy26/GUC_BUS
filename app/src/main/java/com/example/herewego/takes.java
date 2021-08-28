package com.example.herewego;

import java.io.Serializable;

public class takes implements Serializable {
    private String phonenumber;
    private String busroute;
    private String busnumber;
    private String user_email;
    private double user_lat;
    private double user_lon;

    public takes() {
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

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    public double getUser_lat() {
        return user_lat;
    }

    public void setUser_lat(double user_lat) {
        this.user_lat = user_lat;
    }

    public double getUser_lon() {
        return user_lon;
    }

    public void setUser_lon(double user_lon) {
        this.user_lon = user_lon;
    }

}
