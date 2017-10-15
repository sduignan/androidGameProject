package com.spoopy.game;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;

import java.util.Random;

import static java.lang.Math.min;

/**
 * Created by sduignan on 08/11/2016.
 */

public final class MazeGen {
    public int[][] mazeArray;
    private int[][][] doors;
    private int [][] roomList;
    private static final int[][] adjacents = {{-2, 0}, {0, -2}, {0, 2}, {2, 0}};    //up, left, right, down
    private Random rand;
    private int viableChanges = 1;
    private int openDoors = 0;
    private int adjacentOpenDoors = 0;
    private int numOptions = 0;
    private int[] options;
    private IPoint2D playerStart;
    private IPoint2D pumpkinEnd;
    private IPoint2D[] enemyStarts;

    public MazeSolver mazeChecker;

    public static final int MAZE_HEIGHT = 29;
    public static final int MAZE_WIDTH = 15;

    public void setNumEnemies(int enemies) {
        numEnemies = Math.max(Math.min(enemies, 12), 0);
    }

    public void setNumFastEnemies(int fastEnemies) {
        numFastEnemies =  Math.max(Math.min(fastEnemies, ((MAZE_WIDTH-1)/2)), 0);
    }

    public int numEnemies = 2;
    public int numFastEnemies = 1;

    public MazeGen(){
        rand = new Random();
        mazeArray = new int[MAZE_HEIGHT][MAZE_WIDTH];
        doors = new int[MAZE_HEIGHT][MAZE_WIDTH][4];
        roomList = new int[((MAZE_HEIGHT-1)/2)*((MAZE_WIDTH-1)/2)][2];
        for (int i=0; i<((MAZE_HEIGHT-1)/2); i++){
            for (int j=0; j<((MAZE_WIDTH-1)/2); j++){
                roomList[(i*((MAZE_WIDTH-1)/2))+j][0] = (i*2)+1;
                roomList[(i*((MAZE_WIDTH-1)/2))+j][1] = (j*2)+1;
            }
        }
        options = new int[4];
        playerStart = new IPoint2D();
        pumpkinEnd = new IPoint2D();
        enemyStarts = new IPoint2D[numEnemies];
        for (int i=0; i<numEnemies; i++){
            enemyStarts[i] = new IPoint2D();
        }

        mazeChecker = new MazeSolver(MAZE_HEIGHT, MAZE_WIDTH);
    }

    public void setMaze(){
        enemyStarts = new IPoint2D[numEnemies];
        for (int i=0; i<numEnemies; i++){
            enemyStarts[i] = new IPoint2D();
        }
        generateMaze();
        mazeChecker.createGraph(mazeArray);
        while(!checkMaze()){
            generateMaze();
            mazeChecker.createGraph(mazeArray);
        }
    }

    public MazeSolver getSolver(){
        return mazeChecker;
    }

    // Implementing Fisher–Yates shuffle
    // Thank you Stack Overflow
    private void shuffleArray(int[][] ar){
        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = rand.nextInt(i + 1);
            // Simple swap
            int[] a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }


    private int sumOpenDoors(int i, int j){
        int sum = 0;
        for (int x=0; x<4; x++) {
            sum += doors[i][j][x];
        }
        return sum;
    }

    private boolean checkMaze(){
        boolean goodMaze = true;
        goodMaze = goodMaze && mazeChecker.calculatePath(playerStart, pumpkinEnd);
        for (int i=0; i<numEnemies; i++){
            goodMaze = goodMaze && mazeChecker.calculatePath(playerStart, enemyStarts[i]);
        }
        return goodMaze;
    }

    private void generateMaze(){
        shuffleArray(roomList);
        for (int i=0; i<MAZE_HEIGHT; i++) {
            for (int j=0; j<MAZE_WIDTH; j++){
                if (i==0 || i==(MAZE_HEIGHT-1) || j==0 || j==(MAZE_WIDTH-1)){
                    mazeArray[i][j] = 0;
                } else if (i%2 == 1){
                    if (j%2 == 1){
                        mazeArray[i][j] = 2;
                    } else {
                        mazeArray[i][j] = 1;
                    }
                } else {
                    if (j%2 == 1){
                        mazeArray[i][j] = 1;
                    } else {
                        mazeArray[i][j] = 0;
                    }
                }
            }
        }

        for (int i=0; i<MAZE_HEIGHT; i++) {
            for (int j=0; j<MAZE_WIDTH; j++){
                if (mazeArray[i][j] == 2){
                    if (mazeArray[i-1][j] == 1){ //up
                        doors[i][j][0] = 1;
                    } else {
                        doors[i][j][0] = 0;
                    }
                    if (mazeArray[i][j-1] == 1){ //left
                        doors[i][j][1] = 1;
                    } else {
                        doors[i][j][1] = 0;
                    }
                    if (mazeArray[i][j+1] == 1){ //right
                        doors[i][j][2] = 1;
                    } else {
                        doors[i][j][2] = 0;
                    }
                    if (mazeArray[i+1][j] == 1){ //down
                        doors[i][j][3] = 1;
                    } else {
                        doors[i][j][3] = 0;
                    }
                }
            }
        }

        viableChanges = 10;
        while (viableChanges > 5){
            viableChanges = 0;
            for (int[] room : roomList){
                openDoors = sumOpenDoors(room[0], room[1]);
                if (openDoors > 2) {
                    //closing a door won't make this room a dead end.
                    //now check if it will make an adjacent room a dead end or closed loop
                    numOptions = 0;
                    for (int i=0; i<4; i++){
                        //only check room if door to it is open
                        if(doors[room[0]][room[1]][i]==1){
                            //make sure room exists
                            int iCoord = room[0]+adjacents[i][0];
                            int jCoord = room[1]+adjacents[i][1];
                            if (iCoord < 0 || iCoord >= MAZE_HEIGHT || jCoord < 0 || jCoord >= MAZE_WIDTH){
                                adjacentOpenDoors = 0;
                            } else {
                                adjacentOpenDoors = sumOpenDoors(iCoord, jCoord);
                            }
                            if (adjacentOpenDoors <= 2) {
                                if (openDoors == 3) {
                                    viableChanges -= numOptions;
                                    break;
                                }
                            } else {
                                    options[numOptions] = i;
                                    numOptions++;
                                    viableChanges++;
                            }
                        }
                    }
                    if (numOptions > 0){
                        //chance of closing a door = #options/(#options + 1)
                        //chance of closing a given door = 1/(#options + 1)
                        int doorChance = rand.nextInt(numOptions + 1);  //returns number between 0 & #options
                        for (int i=0; i<numOptions; i++){
                            if(i == doorChance) {
                                //close that door from both sides
                                //3-option gives opposing door, i.e. 3-up = down, 3-left = right etc
                                doors[room[0]][room[1]][options[i]] = 0;
                                doors[room[0]+adjacents[options[i]][0]][room[1]+adjacents[options[i]][1]][3-options[i]] = 0;
                                mazeArray[room[0]+(adjacents[options[i]][0]/2)][room[1]+(adjacents[options[i]][1]/2)] = 0;
                                viableChanges--;
                                break;
                            }
                        }
                    }
                }
            }
        }

        //convert rooms to normal paths
        for (int i=0; i<MAZE_HEIGHT; i++){
            for(int j=0; j<MAZE_WIDTH; j++){
                mazeArray[i][j] = min(mazeArray[i][j], 1);
            }
        }

        //place player character
        playerStart.setPoint(MAZE_HEIGHT-1, rand.nextInt((MAZE_WIDTH-1)/2)*2 + 1);   //this should always be just below a former room, therefor a gap
        mazeArray[playerStart.i][playerStart.j] = 2;

        //place victory pumpkin
        pumpkinEnd.setPoint(0, rand.nextInt((MAZE_WIDTH-1)/2)*2 + 1);   //this should always be just above a former room, therefor a gap
        mazeArray[pumpkinEnd.i][pumpkinEnd.j] = 3;

        if (numFastEnemies > 0) {
            mazeArray[1][pumpkinEnd.j] = 5;
        }
        if (numFastEnemies > 1) {
            for (int i = 0; i<(numFastEnemies-1); i++) {
                int loc = rand.nextInt((MAZE_WIDTH - 1) / 2) * 2 + 1;
                while (mazeArray[1][loc] != 1) {
                    loc = rand.nextInt((MAZE_WIDTH - 1) / 2) * 2 + 1;
                }
                mazeArray[1][loc] = 5;
            }
        }

        //place enemies
        //place enemies not at top or bottom of maze - not directly on player or goal
        //evenly vertically spaced
        int gap = MAZE_HEIGHT/(numEnemies+1);
        for(int i=0; i<numEnemies; i++){
            int enemyHeight = gap*(i+1);
            if(enemyHeight%2 == 0){
                enemyHeight++;  //make it odd, always in a gap then
            }
            enemyStarts[i].setPoint(enemyHeight, rand.nextInt((MAZE_WIDTH-1)/2)*2 + 1);
            mazeArray[enemyStarts[i].i][enemyStarts[i].j] = 4;
        }
    }
}
