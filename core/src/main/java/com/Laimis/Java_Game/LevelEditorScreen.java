package com.Laimis.Java_Game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.files.FileHandle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JOptionPane;

public class LevelEditorScreen implements Screen {
    private final Main game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private Texture tileset;
    private Texture moneyTexture;
    private Texture playerTexture;
    private Texture spikesTexture;
    private TextureRegion[][] tileRegions;
    private TextureRegion selectedBlock;
    private boolean isSelecting;
    private Vector2 selectedBlockPos;
    private int[][] map;
    private static final int MAP_WIDTH = 40;
    private static final int MAP_HEIGHT = 22;
    Texture background;
    public static final int TILE_SIZE = 32;
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;

    public LevelEditorScreen(Main game) {
        this.game = game;
        initialize();
    }

    private void initialize() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT, camera);
        camera.position.set(SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f, 0);
        batch = new SpriteBatch();
        background = new Texture("assets/Green_Zone/2 Background/Day/Background.png");
        tileset = new Texture("assets/Green_Zone/Tileset.png");
        moneyTexture = new Texture("assets/Green_Zone/4 Animated objects/Money.png");
        playerTexture = new Texture("assets/1 Dog/Idle.png");
        spikesTexture = new Texture("assets/spikes.png");
        tileRegions = TextureRegion.split(tileset, TILE_SIZE, TILE_SIZE);

        selectedBlock = tileRegions[0][0]; // Default selected block
        isSelecting = false;
        selectedBlockPos = new Vector2(10, SCREEN_HEIGHT - TILE_SIZE - 10); // Top-left corner

        map = new int[MAP_HEIGHT][MAP_WIDTH];
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        batch.draw(background, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        // Draw selected block in the top-left corner
        batch.draw(selectedBlock, selectedBlockPos.x, selectedBlockPos.y, TILE_SIZE, TILE_SIZE);

        // If selecting, draw the full tileset for selection and additional objects
        if (isSelecting) {
            for (int row = 0; row < tileRegions.length; row++) {
                for (int col = 0; col < tileRegions[row].length; col++) {
                    batch.draw(tileRegions[row][col], col * TILE_SIZE, SCREEN_HEIGHT - (row + 1) * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
            // Draw additional objects for selection
            batch.draw(moneyTexture, 0, SCREEN_HEIGHT - (tileRegions.length + 1) * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            batch.draw(playerTexture, TILE_SIZE, SCREEN_HEIGHT - (tileRegions.length + 1) * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            batch.draw(spikesTexture, 2 * TILE_SIZE, SCREEN_HEIGHT - (tileRegions.length + 1) * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        } else {
            // Draw the map
            for (int row = 0; row < MAP_HEIGHT; row++) {
                for (int col = 0; col < MAP_WIDTH; col++) {
                    if (map[row][col] != 0) {
                        if (map[row][col] == 100) {
                            batch.draw(moneyTexture, col * TILE_SIZE, SCREEN_HEIGHT - (row + 1) * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                        } else if (map[row][col] == 101) {
                            batch.draw(playerTexture, col * TILE_SIZE, SCREEN_HEIGHT - (row + 1) * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                        } else if (map[row][col] == 102) {
                            batch.draw(spikesTexture, col * TILE_SIZE, SCREEN_HEIGHT - (row + 1) * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                        } else {
                            int tileIndex = map[row][col] - 1;
                            int tileRow = tileIndex / tileRegions[0].length;
                            int tileCol = tileIndex % tileRegions[0].length;
                            if (tileRow < tileRegions.length && tileCol < tileRegions[tileRow].length) {
                                TextureRegion tile = tileRegions[tileRow][tileCol];
                                batch.draw(tile, col * TILE_SIZE, SCREEN_HEIGHT - (row + 1) * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                            }
                        }
                    }
                }
            }
        }

        batch.end();

        handleInput();
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);

            if (isSelecting) {
                // Handle block selection from the tileset and additional objects
                int col = (int) (touchPos.x / TILE_SIZE);
                int row = (int) ((SCREEN_HEIGHT - touchPos.y) / TILE_SIZE);

                if (row >= 0 && row < tileRegions.length && col >= 0 && col < tileRegions[row].length) {
                    selectedBlock = tileRegions[row][col];
                    isSelecting = false;
                } else if (row == tileRegions.length) {
                    if (col == 0) {
                        selectedBlock = new TextureRegion(moneyTexture);
                    } else if (col == 1) {
                        selectedBlock = new TextureRegion(playerTexture);
                    } else if (col == 2) {
                        selectedBlock = new TextureRegion(spikesTexture);
                    }
                    isSelecting = false;
                }
            } else {
                // Handle block placement or selection
                if (touchPos.x >= selectedBlockPos.x && touchPos.x <= selectedBlockPos.x + TILE_SIZE &&
                    touchPos.y >= selectedBlockPos.y && touchPos.y <= selectedBlockPos.y + TILE_SIZE) {
                    isSelecting = true;
                } else {
                    // Place or erase the block at the nearest grid position
                    int gridX = (int) (touchPos.x / TILE_SIZE);
                    int gridY = (int) ((SCREEN_HEIGHT - touchPos.y) / TILE_SIZE);
                    if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
                        // Erase the block
                        map[gridY][gridX] = 0;
                    } else {
                        // Place the selected block
                        if (selectedBlock.getTexture() == moneyTexture) {
                            map[gridY][gridX] = 100; // Money block
                        } else if (selectedBlock.getTexture() == playerTexture) {
                            map[gridY][gridX] = 101; // Player starting position block
                        } else if (selectedBlock.getTexture() == spikesTexture) {
                            map[gridY][gridX] = 102; // Spikes block
                        } else {
                            map[gridY][gridX] = (selectedBlock.getRegionY() / TILE_SIZE) * tileRegions[0].length + (selectedBlock.getRegionX() / TILE_SIZE) + 1;
                        }
                    }
                }
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            saveMap();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            loadMap();
        }
    }

    private void saveMap() {
        String mapName = JOptionPane.showInputDialog(null, "Enter map name", "Save Map", JOptionPane.PLAIN_MESSAGE);
        if (mapName != null && !mapName.isEmpty()) {
            try {
                FileHandle file = Gdx.files.local("assets/" + mapName + ".csv");
                file.file().getParentFile().mkdirs(); // Ensure the directory exists
                BufferedWriter writer = new BufferedWriter(new FileWriter(file.file()));
                for (int row = 0; row < MAP_HEIGHT; row++) {
                    for (int col = 0; col < MAP_WIDTH; col++) {
                        writer.write(map[row][col] + (col == MAP_WIDTH - 1 ? "" : ","));
                    }
                    writer.newLine();
                }
                writer.close();
                Gdx.app.log("Map Save", "Map saved as " + mapName + ".csv");

                // Ask if the player wants to play the map
                int response = JOptionPane.showConfirmDialog(null, "Do you want to play the map?", "Play Map", JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    game.setScreen(new GameScreen(game, 0, "assets/" + mapName + ".csv"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadMap() {
        String mapName = JOptionPane.showInputDialog(null, "Enter map name to load", "Load Map", JOptionPane.PLAIN_MESSAGE);
        if (mapName != null && !mapName.isEmpty()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(Gdx.files.local("assets/" + mapName + ".csv").file()));
                String line;
                int row = 0;
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(",");
                    for (int col = 0; col < values.length; col++) {
                        map[row][col] = Integer.parseInt(values[col]);
                    }
                    row++;
                }
                reader.close();
                Gdx.app.log("Map Load", "Map loaded from " + mapName + ".csv");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        tileset.dispose();
        moneyTexture.dispose();
        playerTexture.dispose();
        spikesTexture.dispose();
    }
}
