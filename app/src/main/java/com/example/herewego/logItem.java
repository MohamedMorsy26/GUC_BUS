package com.example.herewego;

public class logItem implements Comparable {
    String year;
    String month;
    String day;
    String hour;
    String minute;
    String text;

    public String getYear() {return year;}
    public void setYear(String year) {this.year = year;}
    public String getMonth() {return month;}
    public void setMonth(String month) {this.month = month;}
    public String getDay() {return day;}
    public void setDay(String day) {this.day = day;}
    public String getHour() {return hour;}
    public void setHour(String hour) {this.hour = hour;}
    public String getMinute() {return minute;}
    public void setMinute(String minute) {this.minute = minute;}
    public String getText() {return text;}
    public void setText(String text) {this.text = text;}


    @Override
    public int compareTo(Object o) {
        if(!this.getYear().equals(((logItem)o).getYear())){
            return Integer.parseInt(((logItem) o).getYear()) - Integer.parseInt(this.getYear());
        }
        else if(!this.getMonth().equals(((logItem)o).getMonth())){
            return Integer.parseInt(((logItem) o).getMonth()) - Integer.parseInt(this.getMonth());
        }
        else if(!this.getDay().equals(((logItem)o).getDay())){
            return Integer.parseInt(((logItem) o).getDay()) - Integer.parseInt(this.getDay());
        }
        else if(!this.getHour().equals(((logItem)o).getHour())){
            return Integer.parseInt(((logItem) o).getHour()) - Integer.parseInt(this.getHour());
        }
        else if(!this.getMinute().equals(((logItem)o).getMinute())){
            return Integer.parseInt(((logItem) o).getMinute()) - Integer.parseInt(this.getMinute());
        }
        else{
            return  0;
        }
    }
}
