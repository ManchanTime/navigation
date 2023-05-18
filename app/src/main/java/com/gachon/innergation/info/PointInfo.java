package com.gachon.innergation.info;

import java.io.Serializable;

public class PointInfo implements Serializable {
    String id;
    String point_x;
    String point_y;

    public PointInfo(String id, String point_x, String point_y){
        this.id = id;
        this.point_x = point_x;
        this.point_y = point_y;
    }

    public String getId(){
        return id;
    }
    public void setId(String id){
        this.id = id;
    }

    public String getPoint_x(){
        return point_x;
    }
    public void setPoint_x(String point_x){
        this.point_x = point_x;
    }

    public String getPoint_y(){
        return point_y;
    }
    public void setPoint_y(String point_y){
        this.point_y = point_y;
    }
}
