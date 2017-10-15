package com.spoopy.game;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;

/**
 * Created by sduignan on 16/11/2016.
 */

public class MazeSolver {
    private IndexedAStarPathFinder<MazeNode> mazePathFinder;
    private MazeGraph mazeGraph;
    //Where the solution goes:
    public DefaultGraphPath<MazeNode> mazePath;
    //Heuristic to estimate how close node is to the end
    private ManhattanDistanceHeuristic mazeHeuristic;
    MazeNode[][] mazeNodes;
    int mazeHeight;
    int mazeWidth;

    public MazeSolver(int newMazeHeight, int newMazeWidth) {
        mazePath = new DefaultGraphPath<MazeNode>();
        mazeHeuristic = new ManhattanDistanceHeuristic();
        mazeNodes = new MazeNode[newMazeHeight][newMazeWidth];
        mazeGraph = new MazeGraph((newMazeHeight - 2)*(newMazeWidth-2));
        mazeHeight = newMazeHeight;
        mazeWidth = newMazeWidth;
    }

    public void createGraph(int[][] mazeArray){
        int index = 0;
        mazeGraph.clearGraph();
        for (int i=0; i<mazeHeight; i++){
            for (int j=0; j<mazeWidth; j++){
                if (mazeArray[i][j] != 0) {
                    mazeNodes[i][j] = new MazeNode(i, j, index++);
                    mazeGraph.addNode(mazeNodes[i][j]);
                } else {
                    mazeNodes[i][j] = null;
                }
            }
        }

        for (int i=0; i<mazeHeight; i++) {
            for (int j = 0; j<mazeWidth; j++) {
                if (null != mazeNodes[i][j]){
                    addNodeNeighbour(mazeNodes[i][j], i - 1, j); // Node to left
                    addNodeNeighbour(mazeNodes[i][j], i + 1, j); // Node to right
                    addNodeNeighbour(mazeNodes[i][j], i, j - 1); // Node above
                    addNodeNeighbour(mazeNodes[i][j], i, j + 1); // Node below
                }
            }
        }
        mazePathFinder = new IndexedAStarPathFinder<MazeNode>(mazeGraph, false);
    }


    //Add connections to the node at aX aY. If there is no node present at those coordinates no connection will be created.
    private void addNodeNeighbour(MazeNode aNode, int aI, int aJ){
        // Make sure that we are within our array bounds.
        if (aI >= 0 && aI <mazeHeight && aJ >= 0 && aJ <mazeWidth) {
            aNode.addNeighbour(mazeNodes[aI][aJ]);
        }
    }

    public boolean calculatePath(IPoint2D start, IPoint2D end){
        return calculatePath(start, end, mazePath);
    }

    public boolean calculatePath(IPoint2D start, IPoint2D end, DefaultGraphPath<MazeNode> path){
        mazePath.clear();

        mazePathFinder.searchNodePath(mazeNodes[start.i][start.j], mazeNodes[end.i][end.j], mazeHeuristic, path);

        return (mazePath.nodes.size > 0);
    }

    public MazeNode calculateFirstStep(IPoint2D start, IPoint2D end){
        calculatePath(start, end, mazePath);

        if (mazePath.nodes.size > 0) {
            return mazePath.nodes.get(Math.min(1, (mazePath.nodes.size-1)));
        }
        return null;
    }
}
