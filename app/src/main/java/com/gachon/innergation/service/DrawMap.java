package com.gachon.innergation.service;

import com.gachon.innergation.info.MapInfo;
import com.gachon.innergation.info.Node;

public class DrawMap {

    static int[][] maps = {
            { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
            { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
            { 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0 },
            { 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0 },
            { 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0 },
            { 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
            { 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0 }
    };

    public static void draw() {
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
