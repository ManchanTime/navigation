package com.gachon.innergation.service;

import com.gachon.innergation.info.MapInfo;
import com.gachon.innergation.info.Node;

public class DrawMap {

    // 기존에는 여기서 map을 선언하고 지도를 그림.
    // 하지만 이렇게 하면 현재 위치가 갱신되고 draw를 호출할 때마다 지도를 다시 그리게 됨 => 성능 이슈
    // 따라서 maps는 상위 액티비티에서 처음 한번만 그리고, 그 뒤부터는 매개변수로 받아와서 사용허겠음
//    static int[][] maps = {
//            { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
//            { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
//            { 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0 },
//            { 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0 },
//            { 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0 },
//            { 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
//            { 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0 }
//    };

    public static void draw(int[][] maps) {
        MapInfo info=new MapInfo(maps,maps[0].length, maps.length,new Node(1, 1), new Node(5, 4));
        new Astar().start(info);
        printMap(maps);
    }

    public static void printMap(int[][] maps)
    {
        for (int i = 0; i < maps.length; i++)
        {
            for (int j = 0; j < maps[i].length; j++)
            {
                System.out.print(maps[i][j] + " ");
            }
            System.out.println();
        }
    }
}
