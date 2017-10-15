package com.spoopy.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;

/**
 * Created by sduignan on 10/12/2016.
 */

public class MainMenuScreen implements Screen {
    OrthographicCamera camera;
    final SpoopyGame game;
    Texture menuImage;
    Rectangle ghostsUp;
    Rectangle ghostsDown;
    Rectangle batsUp;
    Rectangle batsDown;
    Rectangle speedUp;
    Rectangle speedDown;
    Rectangle play;
    long touchTime;
    long timeSinceTouch;
    Vector3 touchPos;

    public MainMenuScreen(final SpoopyGame _game){
        menuImage = new Texture(Gdx.files.internal("menu.png"));
        this.game = _game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, game.SCREEN_WIDTH, game.SCREEN_HEIGHT);

        ghostsDown = new Rectangle(336, 688, 32, 32);
        ghostsUp = new Rectangle(400, 688, 32, 32);
        batsDown = new Rectangle(336, 592, 32, 32);
        batsUp = new Rectangle(400, 592, 32, 32);
        speedDown = new Rectangle(336, 528, 32, 32);
        speedUp = new Rectangle(400, 528, 32, 32);
        play = new Rectangle(176, 176, 160, 64);

        touchTime = TimeUtils.millis();

        touchPos = new Vector3();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.batch.draw(menuImage, 0, 0);
        game.layout.setText(game.font, String.valueOf(game.mazeMaker.numEnemies));
        game.font.draw(game.batch, game.layout, (368+(32-game.layout.width)/2), 688+(32+game.layout.height)/2);
        game.layout.setText(game.font, String.valueOf((game.mazeMaker.numFastEnemies)));
        game.font.draw(game.batch, game.layout, (368+(32-game.layout.width)/2), 592+(32+game.layout.height)/2);
        game.layout.setText(game.font, String.valueOf((game.batSpeed)));
        game.font.draw(game.batch, game.layout, (368+(32-game.layout.width)/2), 528+(32+game.layout.height)/2);
        game.batch.end();

        timeSinceTouch = TimeUtils.timeSinceMillis(touchTime);

        if (Gdx.input.isTouched() && timeSinceTouch > 200) {
            touchTime = TimeUtils.millis();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);

            if (ghostsDown.contains(touchPos.x, touchPos.y)){
                game.mazeMaker.setNumEnemies(game.mazeMaker.numEnemies-1);
            } else if (ghostsUp.contains(touchPos.x, touchPos.y)){
                game.mazeMaker.setNumEnemies(game.mazeMaker.numEnemies+1);
            } else if (batsDown.contains(touchPos.x, touchPos.y)){
                game.mazeMaker.setNumFastEnemies(game.mazeMaker.numFastEnemies-1);
            } else if (batsUp.contains(touchPos.x, touchPos.y)){
                game.mazeMaker.setNumFastEnemies(game.mazeMaker.numFastEnemies+1);
            } else if (speedDown.contains(touchPos.x, touchPos.y)){
                game.setBatSpeed(game.batSpeed-1);
            } else if (speedUp.contains(touchPos.x, touchPos.y)){
                game.setBatSpeed(game.batSpeed+1);
            } else if (play.contains(touchPos.x, touchPos.y)){
                game.setScreen(new GameScreen(game));
                dispose();
            }
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
        menuImage.dispose();
    }
}
