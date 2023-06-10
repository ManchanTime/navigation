package com.gachon.innergation.info;

import java.util.ArrayList;
import java.util.List;

public class InputWifiInfo {
    List<GetWifiInfo> wifiInfos = new ArrayList<>();

    public InputWifiInfo(List<GetWifiInfo> wifiInfos) {
        this.wifiInfos = wifiInfos;
    }

    public List<GetWifiInfo> getWifiInfos() {
        return this.wifiInfos;
    }
}
