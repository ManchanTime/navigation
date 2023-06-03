package com.gachon.innergation.info;

import java.io.Serializable;

public class GetWifiInfo implements Comparable<GetWifiInfo>, Serializable {
    String ssid;
    String bssid;
    int rssi;


    public GetWifiInfo() {
    }


    public GetWifiInfo(String ssid, String bssid, int rssi){
        this.ssid = ssid;
        this.bssid = bssid;
        this.rssi = rssi;
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

    @Override
    public int compareTo(GetWifiInfo getWifiInfo) {
        if(getWifiInfo.getRssi() > getRssi())
            return 1;
        else if(getWifiInfo.getRssi() == getRssi()){
            return getWifiInfo.getBssid().compareTo(getBssid());
        }else
            return -1;
    }
}
