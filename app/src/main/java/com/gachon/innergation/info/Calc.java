package com.gachon.innergation.info;

public class Calc {

    private String bssid;

    private int count;

    private int avg;

    public Calc(String bssid) {
        this.bssid = bssid;
        this.count = 0;
        this.avg = 0;
    }

    public Calc() {
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getAvg() {
        return avg;
    }

    public void setAvg(int avg) {
        this.avg = avg;
    }
}
