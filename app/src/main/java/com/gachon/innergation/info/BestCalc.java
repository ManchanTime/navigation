package com.gachon.innergation.info;

public class BestCalc {

    private String bssid;

    private int count;

    private int avg;

    public BestCalc(String bssid, int count, int avg) {
        this.bssid = bssid;
        this.count = count;
        this.avg = avg;
    }

    public BestCalc() {
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

    public int getAvg() {
        return avg;
    }

    public void setAvg(int avg) {
        this.avg = avg;
    }
}
