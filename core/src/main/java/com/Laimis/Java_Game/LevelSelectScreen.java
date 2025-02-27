package com.Laimis.Java_Game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;

public class LevelSelectScreen implements Screen {
    Main game;
    Texture level1, level2, level3, level4, level5, createLevel, lockedTexture;
    boolean[] levelUnlocked = {true, false, false, false, false};
    BitmapFont font;

    OrthographicCamera camera;
    Viewport viewport;

    public static final int WORLD_WIDTH = 1280;
    public static final int WORLD_HEIGHT = 720;

    ShapeRenderer shapeRenderer;

    public LevelSelectScreen(Main game) {
        this.game = game;

        // Initialize camera and viewport
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply();
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);

        // Load textures
        level1 = new Texture("assets/Green_Zone/3 Objects/Stones/1.png");
        level2 = new Texture("assets/Green_Zone/3 Objects/Stones/2.png");
        level3 = new Texture("assets/Green_Zone/3 Objects/Stones/3.png");
        level4 = new Texture("assets/Green_Zone/3 Objects/Stones/4.png");
        level5 = new Texture("assets/Green_Zone/3 Objects/Stones/5.png");
        createLevel = new Texture("assets/Green_Zone/3 Objects/Stones/6.png");
        lockedTexture = new Texture("assets/Green_Zone/3 Objects/Other/Box.png");

        font = new BitmapFont();
        font.setColor(Color.WHITE);

        shapeRenderer = new ShapeRenderer();  // Initialize ShapeRenderer

    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);  // Clear the screen

        // Update the camera for correct rendering
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // Start drawing the background (make sure it fills the entire screen)
        game.batch.begin();
        game.batch.draw(game.background, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);  // Fullscreen background
        game.batch.end();

        // Layout settings for level buttons (center them properly)
        int startX = (WORLD_WIDTH - 3 * 350) / 2;  // Horizontally center levels (3 columns, each 200px wide)
        int startY = (Gdx.graphics.getHeight() + 250) / 2;     // Vertically center levels (adjusted slightly downward)
        int spacingX = 400;  // Horizontal spacing between levels
        int spacingY = 300;  // Vertical spacing between levels

        int rectSize = 200;  // Size of the white rectangle for levels

        Texture[] levels = {level1, level2, level3, level4, level5, createLevel};

        // --- Detect Mouse Click ---
        if (Gdx.input.justTouched()) {
            Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);  // Convert to world coordinates

            for (int i = 0; i < levels.length; i++) {
                int rectX = startX + (i % 3) * spacingX;
                int rectY = startY - (i / 3) * spacingY;

                // Check if click is inside rectangle for level selection
                if (touchPos.x >= rectX && touchPos.x <= rectX + rectSize &&
                        touchPos.y >= rectY && touchPos.y <= rectY + rectSize) {

                    if (i < 5 && levelUnlocked[i]) {
                        System.out.println("Level " + (i + 1) + " selected!");
                        game.setScreen(new GameScreen(game, i + 1)); // Navigate to the selected level
                    } else if (i == 5) {
                        System.out.println("Create Level selected!");
                        // Navigate to Create Level screen
                    } else {
                        System.out.println("Level " + (i + 1) + " is locked!");
                    }
                }
            }
        }

        // --- Draw Rectangles and Images (Level Buttons) ---
        for (int i = 0; i < levels.length; i++) {
            int rectX = startX + (i % 3) * spacingX;
            int rectY = startY - (i / 3) * spacingY;

            // Draw white-filled rectangle as background for each level
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.rect(rectX, rectY, rectSize, rectSize);
            shapeRenderer.end();

            // Determine which texture to display for the level
            Texture currentTexture = (i < 5 && !levelUnlocked[i]) ? lockedTexture : levels[i];

            // Calculate scaling to fit the image inside the rectangle
            float imgWidth = currentTexture.getWidth();
            float imgHeight = currentTexture.getHeight();

            float scale = Math.min(rectSize / imgWidth, rectSize / imgHeight);
            float scaledWidth = imgWidth * scale;
            float scaledHeight = imgHeight * scale;

            // Center the image inside the rectangle
            float imgX = rectX + (rectSize - scaledWidth) / 2;
            float imgY = rectY + (rectSize - scaledHeight) / 2;

            // Draw the level image
            game.batch.begin();
            game.batch.draw(currentTexture, imgX, imgY, scaledWidth, scaledHeight);
            game.batch.end();
        }

        // --- Draw Black Borders around the level selection boxes ---
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLACK);

        for (int i = 0; i < levels.length; i++) {
            int rectX = startX + (i % 3) * spacingX;
            int rectY = startY - (i / 3) * spacingY;
            shapeRenderer.rect(rectX, rectY, rectSize, rectSize);
        }

        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);  // Adjust viewport on resize
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        level1.dispose();
        level2.dispose();
        level3.dispose();
        level4.dispose();
        level5.dispose();
        createLevel.dispose();
        lockedTexture.dispose();
        font.dispose();
    }
}
