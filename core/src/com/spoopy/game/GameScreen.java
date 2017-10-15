package com.spoopy.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

/**
 * Created by sduignan on 10/12/2016.
 */

public class GameScreen implements Screen {
    public enum GameState {
        MOVING,
        TOUCHED,
        WAITING
    }

    SpoopyGame game;
    OrthographicCamera camera;
    private Texture ghostImage;
    private Texture pcImage;
    private Texture wallImage;
    private Texture pumpkinImage;
    private Texture batImage;
    private IPoint2D currentLocation;
    private IPoint2D[] fastEnemyLocations;
    private IPoint2D[] enemyLocations;
    private IPoint2D endOfPathLocation;
    private IPoint2D[] enemyEOPLocations;
    private Rectangle currentAbove;
    private Rectangle currentBelow;
    private Rectangle currentLeft;
    private Rectangle currentRight;
    private Vector3 touchPos;
    private float xOffset;
    private float yOffset;
    private int[][] activeMaze;
    private DefaultGraphPath<MazeNode> playerPath;
    private DefaultGraphPath<MazeNode>[] enemyPaths;
    long touchTime;
    long timeSinceTouch;
    long moveTime;
    long timeSinceMove;
    long fastEnemyMoveTime;
    long timeSinceFastEnemyMove;
    MazeNode moveNode;
    private GameState gameState;

    private MazeGen mazeGenerator;
    private MazeSolver mazeSolver;

    static final int SPRITE_HEIGHT = 32;
    static final int SPRITE_WIDTH = 32;

    private void setReset() {
        touchTime = TimeUtils.millis();
        moveTime = TimeUtils.millis();
        fastEnemyMoveTime = TimeUtils.millis();

        mazeGenerator.setMaze();

        enemyLocations = new IPoint2D[mazeGenerator.numEnemies];
        for (int i=0; i<mazeGenerator.numEnemies; i++){
            enemyLocations[i] = new IPoint2D();
        }

        endOfPathLocation = new IPoint2D();
        enemyEOPLocations = new IPoint2D[mazeGenerator.numEnemies];
        for (int i=0; i<mazeGenerator.numEnemies; i++){
            enemyEOPLocations[i] = new IPoint2D();
        }

        fastEnemyLocations = new IPoint2D[mazeGenerator.numFastEnemies];
        for (int i=0; i<mazeGenerator.numFastEnemies; i++){
            fastEnemyLocations[i] = new IPoint2D();
        }

        enemyPaths = new DefaultGraphPath[mazeGenerator.numEnemies];
        for (int i=0; i<mazeGenerator.numEnemies; i++){
            enemyPaths[i] = new DefaultGraphPath<MazeNode>();
        }

        playerPath.clear();


        activeMaze = mazeGenerator.mazeArray;

        int currEnemy = 0;
        int currFastEnemy = 0;
        for (int i=0; i<mazeGenerator.MAZE_HEIGHT; i++) {
            for (int j=0; j<mazeGenerator.MAZE_WIDTH; j++){
                switch(activeMaze[i][j]){
                    case 2:
                        currentLocation.setPoint(i, j);
                        endOfPathLocation.setPoint(i, j);
                        currentAbove.set((xOffset+j*SPRITE_WIDTH), (yOffset+((mazeGenerator.MAZE_HEIGHT - i)*SPRITE_HEIGHT)), 32, 32);
                        currentBelow.set((xOffset+j*SPRITE_WIDTH), (yOffset+((mazeGenerator.MAZE_HEIGHT -2 - i)*SPRITE_HEIGHT)), 32, 32);
                        currentLeft.set((xOffset+(j-1)*SPRITE_WIDTH), (yOffset+((mazeGenerator.MAZE_HEIGHT -1 - i)*SPRITE_HEIGHT)), 32, 32);
                        currentRight.set((xOffset+(j+1)*SPRITE_WIDTH), (yOffset+((mazeGenerator.MAZE_HEIGHT -1 - i)*SPRITE_HEIGHT)), 32, 32);
                        break;
                    case 4:
                        enemyLocations[currEnemy].setPoint(i, j);
                        enemyEOPLocations[currEnemy].setPoint(i, j);
                        currEnemy++;
                        break;
                    case 5:
                        fastEnemyLocations[currFastEnemy].setPoint(i, j);
                        currFastEnemy++;
                        break;
                }
            }
        }
        gameState = GameState.WAITING;
    }



    public GameScreen(final SpoopyGame _game){
        this.game = _game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, game.SCREEN_WIDTH, game.SCREEN_HEIGHT);

        gameState = GameState.WAITING;

        //load images
        pcImage = new Texture(Gdx.files.internal("self.png"));
        wallImage = new Texture(Gdx.files.internal("wall.png"));
        pumpkinImage = new Texture(Gdx.files.internal("pumpkin.png"));
        ghostImage = new Texture(Gdx.files.internal("spook.png"));
        batImage = new Texture(Gdx.files.internal("bat.png"));

        mazeGenerator = game.mazeMaker;

        xOffset = (game.SCREEN_WIDTH - (SPRITE_WIDTH*mazeGenerator.MAZE_WIDTH))/2;
        yOffset = (game.SCREEN_HEIGHT - (SPRITE_HEIGHT*mazeGenerator.MAZE_HEIGHT))/2;

        currentAbove = new Rectangle();
        currentBelow = new Rectangle();
        currentLeft = new Rectangle();
        currentRight = new Rectangle();

        currentLocation = new IPoint2D();

        playerPath = new DefaultGraphPath<MazeNode>();

        setReset();

        touchPos = new Vector3();

        mazeSolver = mazeGenerator.getSolver();
    }

    private void moveEnemy(int enemyIndex, int iOffset, int jOffset){
        if ((activeMaze[enemyLocations[enemyIndex].i + iOffset][enemyLocations[enemyIndex].j + jOffset] == 2 ||activeMaze[enemyLocations[enemyIndex].i][enemyLocations[enemyIndex].j] == 2)){
            game.setScreen(new LossScreen(game));
            dispose();
        }
        activeMaze[enemyLocations[enemyIndex].i][enemyLocations[enemyIndex].j] = 1;
        activeMaze[enemyLocations[enemyIndex].i + iOffset][enemyLocations[enemyIndex].j + jOffset] = 4;
        enemyLocations[enemyIndex].offset(iOffset, jOffset);
    }

    private void enemiesStep(){
        for (int i=0; i<mazeGenerator.numEnemies; i++){
            enemyStep(i);
        }
    }

    private void enemiesStepBack(){
        for (int i=0; i<mazeGenerator.numEnemies; i++){
            enemyStepBack(i);
        }
    }

    private void enemyStep(int enemy){
        MazeNode newNode = mazeSolver.calculateFirstStep(enemyEOPLocations[enemy], endOfPathLocation);
        enemyPaths[enemy].add(newNode);
        enemyEOPLocations[enemy].setPoint(newNode.mI, newNode.mJ);
    }

    private void enemyStepBack(int enemy){
        if (enemyPaths[enemy].getCount() > 0) {
            deTailPath(enemyPaths[enemy]);
            MazeNode last = enemyPaths[enemy].get(enemyPaths[enemy].getCount() - 1);
            enemyEOPLocations[enemy].setPoint(last.mI, last.mJ);
        }
    }

    private void moveEnemies() {
        for (int i=0; i<mazeGenerator.numEnemies; i++){
            if (enemyPaths[i].getCount() > 0){
                moveNode = beheadPath(enemyPaths[i]);
                moveEnemy(i, moveNode.mI-enemyLocations[i].i, moveNode.mJ-enemyLocations[i].j);
            }
        }
    }

    private void moveFastEnemies(){
        for (int i=0; i<mazeGenerator.numFastEnemies; i++){
            moveFastEnemy(i);
        }
    }

    private void moveFastEnemy(int fastEnemy){
        MazeNode tempNode = mazeSolver.calculateFirstStep(fastEnemyLocations[fastEnemy], currentLocation);
        if (tempNode != null){
            if (activeMaze[tempNode.mI][tempNode.mJ] == 2){
                game.setScreen(new LossScreen(game));
                dispose();
            }
            else {
                activeMaze[fastEnemyLocations[fastEnemy].i][fastEnemyLocations[fastEnemy].j] = 1;
                fastEnemyLocations[fastEnemy].setPoint(tempNode.mI, tempNode.mJ);
                activeMaze[fastEnemyLocations[fastEnemy].i][fastEnemyLocations[fastEnemy].j] = 5;
            }

        }
    }

    private void movePlayer(){
        if (playerPath.getCount() == 0){
            return;
        }
        moveNode = beheadPath(playerPath);
        int iOffset = moveNode.mI - currentLocation.i;
        int jOffset = moveNode.mJ - currentLocation.j;
        if (activeMaze[currentLocation.i + iOffset][currentLocation.j + jOffset] == 3){
            game.setScreen(new WinScreen(game));
            dispose();
        }
        activeMaze[currentLocation.i][currentLocation.j] = 1;
        activeMaze[currentLocation.i + iOffset][currentLocation.j + jOffset] = 2;
        currentLocation.offset(iOffset, jOffset);
    }

    private boolean addToPlayerPath(int iOffset, int jOffset) {
        endOfPathLocation.offset(iOffset, jOffset);
        boolean increment = true;
        if (playerPath.getCount() > 1 && playerPath.nodes.get(playerPath.getCount()-2) == mazeSolver.mazeNodes[endOfPathLocation.i][endOfPathLocation.j]){
            deTailPath(playerPath);
            increment = false;
        } else {
            /*if (playerPath.getCount() > 0) {
                Gdx.app.debug("NODE", "New node: " + String.valueOf(mazeSolver.mazeNodes[endOfPathLocation.i][endOfPathLocation.j].mI) + ", " + String.valueOf(mazeSolver.mazeNodes[endOfPathLocation.i][endOfPathLocation.j].mJ) + " Index: " + String.valueOf(mazeSolver.mazeNodes[endOfPathLocation.i][endOfPathLocation.j].getIndex()));
                Gdx.app.debug("NODE", "Old node: " + String.valueOf(playerPath.nodes.get(playerPath.getCount()-1).mI) + ", " + String.valueOf(playerPath.nodes.get(playerPath.getCount() - 1).mJ) + " Index: " + String.valueOf(playerPath.nodes.get(playerPath.getCount() - 1).getIndex()));
            }*/
            playerPath.add(mazeSolver.mazeNodes[endOfPathLocation.i][endOfPathLocation.j]);
        }
        currentAbove.y -= iOffset*32;
        currentBelow.y -= iOffset*32;
        currentLeft.y -= iOffset*32;
        currentRight.y -= iOffset*32;
        currentAbove.x += jOffset*32;
        currentBelow.x += jOffset*32;
        currentLeft.x += jOffset*32;
        currentRight.x += jOffset*32;
        return increment;
    }

    private MazeNode beheadPath(DefaultGraphPath<MazeNode> path){
        MazeNode returnNode = null;
        if (path.getCount() > 0) {
            Array<MazeNode> temp = new Array<MazeNode>(path.getCount()-1);
            int i = 0;
            for (MazeNode node : path.nodes){
                if(i>0){
                    temp.add(node);
                } else {
                    returnNode = node;
                }
                i++;
            }
            path.nodes.clear();
            path.nodes.addAll(temp);
        }
        return returnNode;
    }

    private void deTailPath(DefaultGraphPath<MazeNode> path) {
        path.nodes.reverse();
        beheadPath(path);
        path.nodes.reverse();
    }

    private void drawPath(DefaultGraphPath<MazeNode> path, int offset,  Color colour){
        game.pathRenderer.setColor(colour);
        int prevI = -1;
        int prevJ = -1;
        for (MazeNode node : path.nodes){
            if((prevI > 0) && (prevJ > 0)){
                float startX = xOffset + node.mJ*32 + 15 + offset;
                float endX = xOffset + prevJ*32 + 15 + offset;
                float startY = yOffset + (mazeGenerator.MAZE_HEIGHT - node.mI - 1)*32 + 15 + offset;
                float endY = yOffset + (mazeGenerator.MAZE_HEIGHT - prevI - 1)*32 + 15 + offset;
                game.pathRenderer.line(startX, startY, endX, endY);
            }
            prevI = node.mI;
            prevJ = node.mJ;
        }

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);    //black background
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        game.pathRenderer.setProjectionMatrix(camera.combined);
        game.pathRenderer.begin(ShapeRenderer.ShapeType.Line);
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        timeSinceTouch = TimeUtils.timeSinceMillis(touchTime);
        timeSinceMove = TimeUtils.timeSinceMillis(moveTime);
        timeSinceFastEnemyMove = TimeUtils.timeSinceMillis(fastEnemyMoveTime);

        for (int i = 0; i < activeMaze.length; i++) {
            for (int j = 0; j < activeMaze[i].length; j++) {
                switch (activeMaze[i][j]) {
                    case 0:
                        //wall
                        game.batch.draw(wallImage, (xOffset + j * SPRITE_WIDTH), (yOffset + ((activeMaze.length - 1 - i) * SPRITE_HEIGHT)));
                        break;
                    case 2:
                        //player icon
                        game.batch.draw(pcImage, (xOffset + j * SPRITE_WIDTH), (yOffset + ((activeMaze.length - 1 - i) * SPRITE_HEIGHT)));
                        break;
                    case 3:
                        //pumpkin
                        game.batch.draw(pumpkinImage, (xOffset + j * SPRITE_WIDTH), (yOffset + ((activeMaze.length - 1 - i) * SPRITE_HEIGHT)));
                        break;
                    case 4:
                        //enemy
                        game.batch.draw(ghostImage, (xOffset + j * SPRITE_WIDTH), (yOffset + ((activeMaze.length - 1 - i) * SPRITE_HEIGHT)));
                        break;
                    case 5:
                        //other enemy
                        game.batch.draw(batImage, (xOffset + j * SPRITE_WIDTH), (yOffset + ((activeMaze.length - 1 - i) * SPRITE_HEIGHT)));
                        break;
                }
            }
        }

        drawPath(playerPath, 1, Color.GREEN);
        for (int i=0; i<mazeGenerator.numEnemies; i++){
            drawPath(enemyPaths[i], -1, Color.RED);
        }

        game.batch.end();
        game.pathRenderer.end();

        if (timeSinceFastEnemyMove > (2000/game.batSpeed)){
            moveFastEnemies();
            fastEnemyMoveTime = TimeUtils.millis();
        }

        switch (gameState) {
            case TOUCHED:
            case WAITING:
                if (Gdx.input.isTouched()) {
                    if (timeSinceTouch > 20){
                        touchTime = TimeUtils.millis();
                        gameState = GameState.TOUCHED;
                        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                        camera.unproject(touchPos);

                        if (currentAbove.contains(touchPos.x, touchPos.y) && (endOfPathLocation.i - 1 >= 0)) {
                            if (activeMaze[endOfPathLocation.i - 1][endOfPathLocation.j] != 0) {
                                if (addToPlayerPath(-1, 0)) {
                                    enemiesStep();
                                } else {
                                    enemiesStepBack();
                                }
                            }
                        } else if (currentBelow.contains(touchPos.x, touchPos.y) && (endOfPathLocation.i + 1 < activeMaze.length)) {
                            if (activeMaze[endOfPathLocation.i + 1][endOfPathLocation.j] != 0) {
                                if (addToPlayerPath(1, 0)) {
                                    enemiesStep();
                                } else {
                                    enemiesStepBack();
                                }
                            }
                        } else if (currentLeft.contains(touchPos.x, touchPos.y) && (endOfPathLocation.j - 1 < activeMaze[0].length)) {
                            if (activeMaze[endOfPathLocation.i][endOfPathLocation.j - 1] != 0) {
                                if (addToPlayerPath(0, -1)) {
                                    enemiesStep();
                                } else {
                                    enemiesStepBack();
                                }
                            }
                        } else if (currentRight.contains(touchPos.x, touchPos.y) && (endOfPathLocation.j + 1 < activeMaze[0].length)) {
                            if (activeMaze[endOfPathLocation.i][endOfPathLocation.j + 1] != 0) {
                                if (addToPlayerPath(0, 1)) {
                                    enemiesStep();
                                } else {
                                    enemiesStepBack();
                                }
                            }
                        }
                    }
                } else {
                    if (gameState == GameState.TOUCHED) {
                        gameState = GameState.MOVING;
                    }
                }
                break;
            case MOVING:
                if (playerPath.getCount() == 0){
                    playerPath.nodes.clear();
                    for(int i=0; i<mazeGenerator.numEnemies; i++){
                        enemyPaths[i].nodes.clear();
                    }
                    gameState = GameState.WAITING;
                }
                if (timeSinceMove > 300) {
                    moveTime = TimeUtils.millis();
                    movePlayer();
                    moveEnemies();
                }
                break;
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        pcImage.dispose();
        wallImage.dispose();
        pumpkinImage.dispose();
        ghostImage.dispose();
        batImage.dispose();
    }
}
