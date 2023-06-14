package com.gachon.innergation.service;

import com.gachon.innergation.info.Coord;
import com.gachon.innergation.info.MapInfo;
import com.gachon.innergation.info.Node;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class Astar {

    public final static int BAR = 1;
    public final static int PATH = 2; // 경로 (길, 도로)
    public final static int DIRECT_VALUE = 10;
    // 대각선이 아닌, 직선 거리 이동 비용. 여기서는 10으로 정의하며, 이 값은 자유롭게 설정하되 대각선 이동비용보다는 작아야 한다.
    public final static int OBLIQUE_VALUE = 14;
    // 대각선 이동 비용.

    Queue<Node> openList = new PriorityQueue<Node>();
    List<Node> closeList = new ArrayList<Node>();

    public void start(MapInfo mapInfo, List<Node> paths)
    {
        if(mapInfo==null) return;
        openList.clear();
        closeList.clear();
        openList.add(mapInfo.start);
        moveNodes(mapInfo, paths);
    }

    private void moveNodes(MapInfo mapInfo, List<Node> paths)
    {
        while (!openList.isEmpty())
        {
            Node current = openList.poll();
            closeList.add(current);
            addNeighborNodeInOpen(mapInfo,current);
            if (isCoordInClose(mapInfo.end.coord))
            {
                drawPath(mapInfo.maps, mapInfo.end, paths);
                break;
            }
        }
    }

    private void drawPath(int[][] maps, Node end, List<Node> paths)
    {
        if(end==null||maps==null) return;
        System.out.println("total cost ：" + end.G);
        while (end != null)
        {
            Coord c = end.coord;
//            maps[c.x][c.y] = PATH;
            paths.add(new Node(c.x, c.y));
            end = end.parent;
        }
    }

    private void addNeighborNodeInOpen(MapInfo mapInfo, Node current)
    {
        int x = current.coord.x;
        int y = current.coord.y;
        addNeighborNodeInOpen(mapInfo,current, x - 1, y, DIRECT_VALUE);
        addNeighborNodeInOpen(mapInfo,current, x, y - 1, DIRECT_VALUE);
        addNeighborNodeInOpen(mapInfo,current, x + 1, y, DIRECT_VALUE);
        addNeighborNodeInOpen(mapInfo,current, x, y + 1, DIRECT_VALUE);
        addNeighborNodeInOpen(mapInfo,current, x - 1, y - 1, OBLIQUE_VALUE);
        addNeighborNodeInOpen(mapInfo,current, x + 1, y - 1, OBLIQUE_VALUE);
        addNeighborNodeInOpen(mapInfo,current, x + 1, y + 1, OBLIQUE_VALUE);
        addNeighborNodeInOpen(mapInfo,current, x - 1, y + 1, OBLIQUE_VALUE);
    }

    private void addNeighborNodeInOpen(MapInfo mapInfo,Node current, int x, int y, int value)
    {
        if (canAddNodeToOpen(mapInfo,x, y))
        {
            Node end=mapInfo.end;
            Coord coord = new Coord(x, y);
            int G = current.G + value;
            Node child = findNodeInOpen(coord);
            if (child == null)
            {
                int H=calcH(end.coord,coord);
                if(isEndNode(end.coord,coord))
                {
                    child=end;
                    child.parent=current;
                    child.G=G;
                    child.H=H;
                }
                else
                {
                    child = new Node(coord, current, G, H);
                }
                openList.add(child);
            }
            else if (child.G > G)
            {
                child.G = G;
                child.parent = current;
                openList.add(child);
            }
        }
    }

    private Node findNodeInOpen(Coord coord)
    {
        if (coord == null || openList.isEmpty()) return null;
        for (Node node : openList)
        {
            if (node.coord.equals(coord))
            {
                return node;
            }
        }
        return null;
    }



    private int calcH(Coord end,Coord coord)
    {
        return (Math.abs(end.x - coord.x) + Math.abs(end.y - coord.y)) * DIRECT_VALUE;
    }

    private boolean isEndNode(Coord end,Coord coord)
    {
        return coord != null && end.equals(coord);
    }

    private boolean canAddNodeToOpen(MapInfo mapInfo,int x, int y)
    {
        if (x < 0 || x >= mapInfo.hight || y < 0 || y >= mapInfo.width) return false;
        if (mapInfo.maps[x][y] == BAR) return false;
        if (isCoordInClose(x, y)) return false;
        return true;
    }

    private boolean isCoordInClose(Coord coord)
    {
        return coord!=null&&isCoordInClose(coord.x, coord.y);
    }

    private boolean isCoordInClose(int x, int y)
    {
        if (closeList.isEmpty()) return false;
        for (Node node : closeList)
        {
            if (node.coord.x == x && node.coord.y == y)
            {
                return true;
            }
        }
        return false;
    }
}
