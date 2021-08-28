package com.example.herewego;

import java.io.Serializable;
import java.util.ArrayList;

public class user implements Serializable, Comparable {
    private String email;
    private String password;
    private String firstname;
    private String lastname;
    private String macaddress;
    private boolean enabled;
    private double userlat;
    private double userlon;
    private ArrayList<String> bois;

    //I don't remember why the user has its location here it's already in "takes"
    public user() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getMacaddress() {
        return macaddress;
    }

    public void setMacaddress(String macaddress) {
        this.macaddress = macaddress;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public double getUserlat() {
        return userlat;
    }

    public void setUserlat(double userlat) {
        this.userlat = userlat;
    }

    public double getUserlon() {
        return userlon;
    }

    public void setUserlon(double userlon) {
        this.userlon = userlon;
    }

    public ArrayList<String> getBois() {
        return bois;
    }

    public void setBois(ArrayList<String> bois) {
        this.bois = bois;
    }


    @Override
    public int compareTo(Object o) {
        return this.firstname.compareTo(((user)o).firstname);
    }
}
