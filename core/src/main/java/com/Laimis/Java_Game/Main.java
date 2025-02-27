package com.Laimis.Java_Game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.Laimis.Java_Game.LevelSelectScreen;  // Correct import statement
import com.badlogic.gdx.Gdx;

public class Main extends Game {
    SpriteBatch batch;
    Texture background;


    @Override
    public void create() {
        batch = new SpriteBatch();
        background = new Texture("assets/Green_Zone/2 Background/Day/Background.png");
        Gdx.graphics.setWindowedMode(1280, 720);
        this.setScreen(new LevelSelectScreen(this));  // Start with LevelSelectScreen
    }


        @Override
        public void resize(int width, int height) {
            super.resize(width, height);
        }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
        background.dispose();
    }
}
