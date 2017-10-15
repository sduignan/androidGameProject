package com.spoopy.game;

import com.badlogic.gdx.ai.pfa.Heuristic;

/**
 * Created by sduignan on 14/11/2016.
 */

public class ManhattanDistanceHeuristic implements Heuristic<MazeNode> {
    @Override
    public float estimate(MazeNode node, MazeNode endNode){
        return Math.abs(endNode.mI - node.mJ) + Math.abs(endNode.mI - node.mJ);
    }
}
