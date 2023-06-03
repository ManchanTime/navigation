package com.gachon.innergation.info;

import java.util.ArrayList;
import java.util.List;

public class GetWifiInfoList {
    List<GetWifiInfo> RSSI = new ArrayList<>();


    public GetWifiInfoList() {
    }

    public GetWifiInfoList(List<GetWifiInfo> getWifiInfoList) {
        this.RSSI = getWifiInfoList;
    }

    public List<GetWifiInfo> getRSSI() {
        return RSSI;
    }

    public void setRSSI(List<GetWifiInfo> RSSI) {
        this.RSSI = RSSI;
    }
}
