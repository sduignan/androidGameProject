package com.spoopy.game;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class SpoopyGame extends Game {
	SpriteBatch batch;
    ShapeRenderer pathRenderer;
    BitmapFont font;
    GlyphLayout layout;
    MazeGen mazeMaker;

    public void setBatSpeed(int batSpeed) {
        this.batSpeed = Math.max(Math.min(batSpeed, 20), 1);
    }

    public int batSpeed = 2;

    static final int SCREEN_HEIGHT = 960;
    static final int SCREEN_WIDTH = 512;

    public void create () {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

		batch = new SpriteBatch();

        pathRenderer = new ShapeRenderer();

        font = new BitmapFont();
        font.getData().setScale(2, 2);
        layout = new GlyphLayout();

        mazeMaker = new MazeGen();

        this.setScreen(new SplashScreen(this));

	}

	public void render () {
        super.render();
	}

	public void dispose () {
		batch.dispose();
        font.dispose();
        pathRenderer.dispose();
	}
}
