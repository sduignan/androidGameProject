package com.spoopy.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.TimeUtils;

/**
 * Created by sduignan on 10/12/2016.
 */

public class WinScreen implements Screen {
    SpoopyGame game;
    OrthographicCamera camera;
    Texture winImage;
    long winTime;
    long timeSinceWin;

    public WinScreen(SpoopyGame _game){
        winImage = new Texture(Gdx.files.internal("win.png"));
        game = _game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, game.SCREEN_WIDTH, game.SCREEN_HEIGHT);
    }
    @Override
    public void show() {
        winTime = TimeUtils.millis();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.batch.draw(winImage, 0, 0);
        game.layout.setText(game.font, "YOU WIN!");
        game.font.draw(game.batch, game.layout, (game.SCREEN_WIDTH - game.layout.width) / 2, game.SCREEN_HEIGHT * 3 / 4);
        game.layout.setText(game.font, "PLAY AGAIN?");
        game.font.draw(game.batch, game.layout, (game.SCREEN_WIDTH - game.layout.width) / 2, game.SCREEN_HEIGHT / 3);
        game.batch.end();

        timeSinceWin = TimeUtils.timeSinceMillis(winTime);

        if (timeSinceWin>800 && Gdx.input.isTouched()){
            game.setScreen(new MainMenuScreen(game));
            dispose();
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
        winImage.dispose();
    }
}
