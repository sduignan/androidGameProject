package com.spoopy.game;

import static java.lang.Math.sqrt;

/**
 * Created by sduignan on 01/12/2016.
 */

public class IPoint2D {
    public int i;
    public int j;

    public IPoint2D(int in_i, int in_j){
        i = in_i;
        j = in_j;
    }

    public void setPoint(int in_i, int in_j){
        i = in_i;
        j = in_j;
    }

    public void offset(int iOffset, int jOffset){
        i += iOffset;
        j += jOffset;
    }

    public IPoint2D(){
    }

    public int x(){
        return j;
    }

    public int y(){
        return i;
    }
}
