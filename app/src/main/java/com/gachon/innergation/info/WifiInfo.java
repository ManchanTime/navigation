package com.gachon.innergation.info;

public class WifiInfo {
    String id;
    String x;
    String y;
    String ssid;
    String bssid;
    int rssi;

    public WifiInfo(String id, String x, String y, String ssid, String bssid, int rssi){
        this.id = id;
        this.x = x;
        this.y = y;
        this.ssid = ssid;
        this.bssid = bssid;
        this.rssi = rssi;
    }

    public String getId(){
        return id;
    }
    public void setId(String id){
        this.id = id;
    }

    public String getX(){
        return x;
    }
    public void setX(String x){
        this.x = x;
    }

    public String getY(){
        return y;
    }
    public void setY(String y){
        this.y = y;
    }

    public String getSsid(){
        return ssid;
    }
    public void setSsid(String ssid){
        this.ssid = ssid;
    }

    public String getBssid(){
        return bssid;
    }
    public void setBssid(String bssid){
        this.bssid = bssid;
    }

    public int getRssi(){
        return rssi;
    }
    public void setRssi(int rssi){
        this.rssi = rssi;
    }
}
