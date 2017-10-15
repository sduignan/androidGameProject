package com.spoopy.game;

/**
 * Created by sduignan on 14/11/2016.
 */

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.DefaultConnection;
import com.badlogic.gdx.ai.pfa.indexed.IndexedNode;
import com.badlogic.gdx.utils.Array;

public class MazeNode implements IndexedNode<MazeNode> {
    /** Index that needs to be unique for every node and starts from 0. */
    private int mIndex;

    /** Whether or not this tile is in the path. */
    private boolean mSelected = false;

    /** X pos of node. */
    public final int mI;
    /** Y pos of node. */
    public final int mJ;

    /** The neighbours of this node. i.e to which node can we travel to from here. */
    Array<Connection<MazeNode>> mConnections = new Array<Connection<MazeNode>>();

    /** @param aIndex needs to be unique for every node and starts from 0. */
    public MazeNode(int aI, int aJ, int aIndex) {
        mIndex = aIndex;
        mI = aI;
        mJ = aJ;
    }

    @Override
    public int getIndex() {
        return mIndex;
    }

    @Override
    public Array<Connection<MazeNode>> getConnections() {
        return mConnections;
    }

    public void addNeighbour(MazeNode aNode) {
        if (null != aNode) {
            mConnections.add(new DefaultConnection<MazeNode>(this, aNode));
        }
    }

    public void select() {
        mSelected = true;
    }
}
