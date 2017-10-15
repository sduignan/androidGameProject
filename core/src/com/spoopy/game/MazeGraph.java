package com.spoopy.game;

import com.badlogic.gdx.ai.pfa.indexed.DefaultIndexedGraph;
import com.badlogic.gdx.utils.Array;

/**
 * Created by sduignan on 14/11/2016.
 */

public class MazeGraph extends DefaultIndexedGraph<MazeNode> {
    public MazeGraph(int aSize) {
        super(aSize);
    }

    public void addNode(MazeNode aNodes) {
        nodes.add(aNodes);
    }

    public MazeNode getNode(int aIndex) {
        return nodes.get(aIndex);
    }

    public void clearGraph(){
        nodes.clear();
    }
}
