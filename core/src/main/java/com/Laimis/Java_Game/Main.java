package com.Laimis.Java_Game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.Laimis.Java_Game.LevelSelectScreen;
import com.badlogic.gdx.Gdx;

public class Main extends Game {
    SpriteBatch batch;
    Texture background;
    boolean[] levelUnlocked;

    @Override
    public void create() {
        batch = new SpriteBatch();
        background = new Texture("assets/Green_Zone/2 Background/Day/Background.png");
        Gdx.graphics.setWindowedMode(1280, 720);
        levelUnlocked = new boolean[]{true, false, false, false, false}; // Initialize the first level as unlocked
        this.setScreen(new LevelSelectScreen(this));  // Start with LevelSelectScreen
    }

    // Method to unlock the next level
    public void unlockLevel(int level) {
        if (level >= 1 && level <= levelUnlocked.length) {
            levelUnlocked[level - 1] = true;
        }
    }

    // Method to get the current level unlocked status
    public boolean[] getLevelUnlockedStatus() {
        return levelUnlocked;
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
