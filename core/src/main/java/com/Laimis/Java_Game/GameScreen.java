package com.Laimis.Java_Game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameScreen implements Screen {
    Main game;
    int levelNumber;
    String customMapPath;
    Texture background;

    OrthographicCamera camera;
    Viewport viewport;
    SpriteBatch batch;
    BitmapFont font;

    // HUD Textures
    Texture hudCoinTexture;
    SpriteBatch hudBatch;

    // Map variables
    int[][] map;
    Texture tileset;
    TextureRegion[][] tileRegions;
    int numRows;
    int numCols;
    int MAP_WIDTH;
    int MAP_HEIGHT;

    private float[] backgroundPositions;

    // Player variables
    Animation<TextureRegion> walkRightAnimation;
    Animation<TextureRegion> walkLeftAnimation;
    Animation<TextureRegion> idleAnimation;
    Animation<TextureRegion> jumpAnimation;
    Animation<TextureRegion> jumpAnimationLeft;
    Animation<TextureRegion> hurtAnimation;
    Animation<TextureRegion> deathAnimation;
    float stateTime;
    Vector2 playerPos;
    Vector2 playerVelocity;
    boolean isJumping;
    boolean isFacingRight;
    boolean isDead;
    private boolean isCustomLevel = false;
    TextureRegion jumpFrame;
    TextureRegion jumpFrameLeft;

    public static final int TILE_SIZE = 32;
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;
    public static final float SCALE_FACTOR = 2f; // Adjust the scale factor to make the map smaller

    Texture moneyTexture;
    Animation<TextureRegion> moneyAnimation;
    List<Vector2> moneyPositions;
    int collectedMoney = 0;
    int requiredMoney = 5;

    Texture spikesTexture;
    List<Vector2> spikesPositions;

    public GameScreen(Main game, int levelNumber) {
        this.game = game;
        this.levelNumber = levelNumber;
        this.customMapPath = null;
        initialize();
        loadMap("assets/map" + levelNumber + ".csv");
    }

    public GameScreen(Main game, int levelNumber, String customMapPath) {
        this.game = game;
        this.levelNumber = levelNumber;
        this.customMapPath = customMapPath;
        this.isCustomLevel = true; // Indicate it's a custom level
        initialize();
        loadMap(customMapPath);
    }

    private void initialize() {
        background = new Texture("assets/Green_Zone/2 Background/Day/Background.png");

        camera = new OrthographicCamera();
        viewport = new FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT, camera);
        camera.position.set(SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f, 0);
        batch = new SpriteBatch();
        hudBatch = new SpriteBatch();
        font = new BitmapFont(); // Initialize BitmapFont

        hudCoinTexture = new Texture("assets/Green_Zone/4 Animated objects/Money.png");

        // Load the tileset and split it into tiles
        tileset = new Texture("assets/Green_Zone/Tileset.png");
        numRows = tileset.getHeight() / TILE_SIZE;
        numCols = tileset.getWidth() / TILE_SIZE;
        tileRegions = TextureRegion.split(tileset, TILE_SIZE, TILE_SIZE);
        Gdx.app.log("Tileset Info", "numRows: " + numRows + ", numCols: " + numCols);

        moneyTexture = new Texture("assets/Green_Zone/4 Animated objects/Money.png");
        TextureRegion[][] moneyFrames = TextureRegion.split(moneyTexture, moneyTexture.getWidth() / 4, moneyTexture.getHeight());
        Array<TextureRegion> moneyArray = new Array<>();
        for (int i = 0; i < 4; i++) {
            moneyArray.add(moneyFrames[0][i]);
        }
        moneyAnimation = new Animation<>(0.1f, moneyArray);

        moneyPositions = new ArrayList<>();
        spikesPositions = new ArrayList<>();

        // Slicing walking animation for walking right
        Texture walkTexture = new Texture("assets/1 Dog/Walk.png");
        TextureRegion[][] walkFrames = TextureRegion.split(walkTexture, walkTexture.getWidth() / 6, walkTexture.getHeight());

        Array<TextureRegion> walkRightFrames = new Array<>();
        for (int i = 0; i < 6; i++) {
            walkRightFrames.add(walkFrames[0][i]);
        }
        walkRightAnimation = new Animation<>(0.1f, walkRightFrames);

        // Create mirrored walking animation for walking left
        Array<TextureRegion> walkLeftFrames = new Array<>();
        for (int i = 0; i < 6; i++) {
            TextureRegion flippedFrame = new TextureRegion(walkFrames[0][i]);
            flippedFrame.flip(true, false);
            walkLeftFrames.add(flippedFrame);
        }
        walkLeftAnimation = new Animation<>(0.1f, walkLeftFrames);

        // Idle animation
        Texture idleTexture = new Texture("assets/1 Dog/Idle.png");
        TextureRegion[][] idleFrames = TextureRegion.split(idleTexture, idleTexture.getWidth() / 4, idleTexture.getHeight());

        Array<TextureRegion> idleFramesArray = new Array<>();
        for (int i = 0; i < 4; i++) {
            idleFramesArray.add(idleFrames[0][i]);
        }
        idleAnimation = new Animation<>(0.1f, idleFramesArray);

        // Jump animation
        jumpFrame = walkFrames[0][4];
        jumpAnimation = new Animation<>(0.1f, jumpFrame);

        jumpFrameLeft = walkLeftFrames.get(4);
        jumpAnimationLeft = new Animation<>(0.1f, jumpFrameLeft);

        // Hurt animation
        Texture hurtTexture = new Texture("assets/1 Dog/Hurt.png");
        TextureRegion[] hurtFrames = new TextureRegion[1];
        hurtFrames[0] = new TextureRegion(hurtTexture);
        hurtAnimation = new Animation<>(0.1f, hurtFrames);

        // Death animation
        Texture deathTexture = new Texture("assets/1 Dog/Death.png");
        TextureRegion[][] deathFrames = TextureRegion.split(deathTexture, deathTexture.getWidth() / 6, deathTexture.getHeight());
        Array<TextureRegion> deathFramesArray = new Array<>();
        for (int i = 0; i < 6; i++) {
            deathFramesArray.add(deathFrames[0][i]);
        }
        deathAnimation = new Animation<>(0.1f, deathFramesArray);

        // Load spikes texture
        spikesTexture = new Texture("assets/spikes.png");

        playerVelocity = new Vector2(0, 0);
        isJumping = false;
        isFacingRight = true;
        isDead = false;
        stateTime = 0f;
    }

    @Override
    public void show() {
        background = new Texture("assets/Green_Zone/2 Background/Day/Background.png");

        if (background != null) {
            int numberOfBackgrounds = 7;
            backgroundPositions = new float[numberOfBackgrounds];

            for (int i = 0; i < backgroundPositions.length; i++) {
                backgroundPositions[i] = i * background.getWidth() - (3 * background.getWidth());
            }
        } else {
            Gdx.app.log("Error", "Background texture is not loaded properly.");
        }
    }

    private void loadMap(String filePath) {
        List<int[]> mapData = new ArrayList<>();
        int maxRowLength = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(Gdx.files.internal(filePath).file()));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                int[] row = new int[values.length];
                for (int col = 0; col < values.length; col++) {
                    row[col] = Integer.parseInt(values[col]);
                }
                mapData.add(row);
                if (values.length > maxRowLength) {
                    maxRowLength = values.length;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        MAP_HEIGHT = mapData.size();
        MAP_WIDTH = maxRowLength;
        map = new int[MAP_HEIGHT][MAP_WIDTH];
        for (int row = 0; row < MAP_HEIGHT; row++) {
            for (int col = 0; col < MAP_WIDTH; col++) {
                if (col < mapData.get(row).length) {
                    map[row][col] = mapData.get(row)[col];
                } else {
                    map[row][col] = 0; // Pad with zeros if the row is shorter
                }
                if (map[row][col] == 100) {
                    moneyPositions.add(new Vector2(col * TILE_SIZE, (MAP_HEIGHT - row - 1) * TILE_SIZE));
                } else if (map[row][col] == 101) {
                    playerPos = new Vector2(col * TILE_SIZE, (MAP_HEIGHT - row - 1) * TILE_SIZE);
                    map[row][col] = 0; // Clear the tile after setting the player position
                } else if (map[row][col] == 102) {
                    spikesPositions.add(new Vector2(col * TILE_SIZE, (MAP_HEIGHT - row - 1) * TILE_SIZE));
                }
            }
        }
    }

    private static final float BACKGROUND_SPEED = 0.5f;
    private static final int NUM_BACKGROUND_TILES = 10;

    private void initBackgrounds() {
        backgroundPositions = new float[NUM_BACKGROUND_TILES];

        for (int i = 0; i < NUM_BACKGROUND_TILES; i++) {
            backgroundPositions[i] = i * background.getWidth();
        }
    }

    private void updateBackgrounds(float delta) {
        float backgroundSpeed = 1.0f;
        float speedAdjustment = backgroundSpeed * delta;

        for (int i = 0; i < backgroundPositions.length; i++) {
            backgroundPositions[i] -= speedAdjustment;
        }

        if (backgroundPositions[0] + background.getWidth() < 0) {
            for (int i = 0; i < backgroundPositions.length - 1; i++) {
                backgroundPositions[i] = backgroundPositions[i + 1];
            }
            backgroundPositions[backgroundPositions.length - 1] = backgroundPositions[backgroundPositions.length - 2] + background.getWidth();
        }
    }

    @Override
    public void render(float delta) {
        if (background == null) {
            Gdx.app.log("Error", "Background texture is missing. Aborting render.");
            return;
        }
        stateTime += delta;

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!isDead) {
            handleInput(delta);
            checkMoneyCollision();
            checkSpikesCollision();
        } else {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                restartGame();
            }
        }
        updateCamera(delta);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        for (int i = 0; i < backgroundPositions.length; i++) {
            batch.draw(background, backgroundPositions[i], 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        }

        // Rendering loop to ensure all tiles are drawn correctly
        for (int row = 0; row < MAP_HEIGHT; row++) {
            for (int col = 0; col < MAP_WIDTH; col++) {
                int tile = map[row][col];
                if (tile != 0 && tile < 100) { // Ensure tile is within valid range
                    int tileIndex = tile - 1;

                    // Ensure the tile index is within valid range
                    if (tileIndex >= 0 && tileIndex < numRows * numCols) {
                        int tileRow = tileIndex / numCols;
                        int tileCol = tileIndex % numCols;
                        TextureRegion tileRegion = tileRegions[tileRow][tileCol];
                        batch.draw(tileRegion, col * TILE_SIZE * SCALE_FACTOR, (MAP_HEIGHT - row - 1) * TILE_SIZE * SCALE_FACTOR, TILE_SIZE * SCALE_FACTOR, TILE_SIZE * SCALE_FACTOR);
                    }
                }
            }
        }

        for (Vector2 moneyPos : moneyPositions) {
            TextureRegion currentMoneyFrame = moneyAnimation.getKeyFrame(stateTime, true);
            batch.draw(currentMoneyFrame, moneyPos.x * SCALE_FACTOR, moneyPos.y * SCALE_FACTOR, TILE_SIZE * SCALE_FACTOR, TILE_SIZE * SCALE_FACTOR);
        }

        for (Vector2 spikesPos : spikesPositions) {
            batch.draw(spikesTexture, spikesPos.x * SCALE_FACTOR, spikesPos.y * SCALE_FACTOR, TILE_SIZE * SCALE_FACTOR, TILE_SIZE * SCALE_FACTOR);
        }

        TextureRegion currentFrame;
        if (isDead) {
            currentFrame = deathAnimation.getKeyFrame(stateTime, false);
        } else if (isJumping) {
            if(isFacingRight) {
                currentFrame = jumpAnimation.getKeyFrame(stateTime, true);
            } else {
                currentFrame = jumpAnimationLeft.getKeyFrame(stateTime, true);
            }
        } else if (playerVelocity.x != 0) {
            currentFrame = isFacingRight ? walkRightAnimation.getKeyFrame(stateTime, true) : walkLeftAnimation.getKeyFrame(stateTime, true);
        } else {
            currentFrame = idleAnimation.getKeyFrame(stateTime, true);
        }
        batch.draw(currentFrame, playerPos.x * SCALE_FACTOR, playerPos.y * SCALE_FACTOR, TILE_SIZE * 0.8f * SCALE_FACTOR, TILE_SIZE * 0.9f * SCALE_FACTOR);
        batch.end();

        drawHUD();

        if (collectedMoney >= requiredMoney) {
            completeLevel();
        }
    }

    private void drawHUD() {
        hudBatch.begin();
        hudBatch.draw(hudCoinTexture, 20, SCREEN_HEIGHT - 50, 32, 32);
        font.draw(hudBatch, "Money: " + collectedMoney, 60, SCREEN_HEIGHT - 20);
        hudBatch.end();
    }

    private void checkMoneyCollision() {
        Iterator<Vector2> iterator = moneyPositions.iterator();
        while (iterator.hasNext()) {
            Vector2 moneyPos = iterator.next();
            if (playerPos.dst(moneyPos) < TILE_SIZE / 2) {
                iterator.remove();
                collectedMoney++;
            }
        }
    }

    private void checkSpikesCollision() {
        for (Vector2 spikesPos : spikesPositions) {
            float spikesWidth = TILE_SIZE * 0.5f;
            float spikesHeight = TILE_SIZE * 0.5f;
            if (playerPos.x < spikesPos.x + spikesWidth && (playerPos.x + TILE_SIZE)-10 > spikesPos.x
                && playerPos.y < spikesPos.y + spikesHeight && playerPos.y > spikesPos.y) {
                isDead = true;
                stateTime = 0f;
                break;
            }
        }
    }

    private void updateCamera(float delta) {
        // Smooth camera follow without bounds on x-axis
        float targetX = playerPos.x * SCALE_FACTOR;
        float targetY = Math.max(SCREEN_HEIGHT / 2f, Math.min(playerPos.y * SCALE_FACTOR, MAP_HEIGHT * TILE_SIZE * SCALE_FACTOR - SCREEN_HEIGHT / 2f));
        camera.position.lerp(new Vector3(targetX, targetY, 0), 0.1f);
        camera.update();
    }

    private void handleInput(float delta) {
        float moveSpeed = 150 * delta; // Increased move speed for farther jumps
        float gravity = -15;
        float playerWidth = TILE_SIZE * 0.8f;
        float playerHeight = TILE_SIZE * 0.9f;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            playerVelocity.x = -moveSpeed;
            isFacingRight = false;
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            playerVelocity.x = moveSpeed;
            isFacingRight = true;
        } else {
            playerVelocity.x = 0;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !isJumping) {
            playerVelocity.y = 5; // Lower jump height
            //playerVelocity.x += (isFacingRight ? 10 : -10); // Add horizontal boost to the jump
            isJumping = true;
        }

        playerVelocity.y += gravity * delta;
        playerPos.x += playerVelocity.x;
        if (checkCollision(playerWidth, playerHeight)) {
            playerPos.x -= playerVelocity.x;
            playerVelocity.x = 0;
        }

        playerPos.y += playerVelocity.y;
        if (checkCollision(playerWidth, playerHeight)) {
            playerPos.y -= playerVelocity.y;
            if (playerVelocity.y < 0) {
                isJumping = false;
            }
            playerVelocity.y = 0;
        }

        if (playerPos.y < 0) {
            playerPos.y = 0;
            playerVelocity.y = 0;
            isJumping = false;
        }
    }

    private boolean checkCollision(float playerWidth, float playerHeight) {
        float playerLeft = playerPos.x;
        float playerRight = playerPos.x + playerWidth;
        float playerBottom = playerPos.y;
        float playerTop = playerPos.y + playerHeight-20;

        int leftTile = (int) Math.floor(playerLeft / TILE_SIZE);
        int rightTile = (int) Math.floor(playerRight / TILE_SIZE);
        int bottomTile = (int) Math.floor(playerBottom / TILE_SIZE);
        int topTile = (int) Math.floor(playerTop / TILE_SIZE);

        boolean collisionDetected = false;

        for (int y = bottomTile; y <= topTile; y++) {
            for (int x = leftTile; x <= rightTile; x++) {
                if (x < 0 || x >= MAP_WIDTH || y < 0 || y >= MAP_HEIGHT) continue;
                int mapRow = MAP_HEIGHT - y - 1;
                if (isSolidTile(x, mapRow)) {
                    collisionDetected = true;
                }

                // Handle diagonal tiles
                if (isDiagonalTile(x, mapRow)) {
                    handleDiagonalTile(x, mapRow, playerWidth, playerHeight);
                }
            }
        }
        return collisionDetected;
    }

    private void handleDiagonalTile(int x, int y, float playerWidth, float playerHeight) {
        int tile = map[y][x];
        float tilePosX = x * TILE_SIZE;
        float tilePosY = (MAP_HEIGHT - y - 1) * TILE_SIZE;
        float tileHeight = TILE_SIZE;
        float tileWidth = TILE_SIZE;
        float slope = tileHeight / tileWidth;

        if (tile == 43) { // Down-left to up-right
            float expectedY = tilePosY + tileHeight + slope * (playerPos.x - tilePosX);
            if (playerPos.y <= expectedY) {
                playerPos.y = expectedY;
                playerVelocity.y = 0;
                isJumping = false;
            } else if (playerPos.x > tilePosX + tileWidth) {
                playerPos.x = tilePosX + tileWidth;
                playerVelocity.x = 0;
            }
        } else if (tile == 42) { // Top-left to bottom-right
            float expectedY = tilePosY + tileHeight - slope * (playerPos.x - tilePosX);
            if (playerPos.y <= expectedY) {
                playerPos.y = expectedY;
                playerVelocity.y = 0;
                isJumping = false;
            } else if (playerPos.x < tilePosX) {
                playerPos.x = tilePosX;
                playerVelocity.x = 0;
            }
        }
    }

    private boolean isDiagonalTile(int x, int y) {
        int tile = map[y][x];
        return tile == 42 || tile == 43;
    }

    private boolean isSolidTile(int x, int y) {
        if (x < 0 || x >= map[0].length || y < 0 || y >= map.length) {
            return false;
        }
        int tile = map[y][x];
        return (tile >= 1 && tile <= 3) || (tile >= 5 && tile <= 8) || tile == 18 || (tile >= 21 && tile <= 23) || tile == 30;
    }

    private void completeLevel() {
        if (isCustomLevel) {
            // Return to the level select screen for custom levels
            game.setScreen(new LevelSelectScreen(game));
        } else {
            // Unlock the next level
            game.unlockLevel(levelNumber + 1);

            // Return to the level select screen
            game.setScreen(new LevelSelectScreen(game));
        }
    }
    private void restartGame() {
        // Reset player position and state
        playerPos = new Vector2(300, 100); // or set to initial position from map
        playerVelocity.set(0, 0);
        isJumping = false;
        isFacingRight = true;
        isDead = false;
        stateTime = 0f;
        collectedMoney = 0;

        // Reset money positions and spikes positions
        moneyPositions.clear();
        spikesPositions.clear();

        // Reload the map
        loadMap("assets/map" + levelNumber + ".csv");
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
        hudBatch.dispose();
        tileset.dispose();
        hudCoinTexture.dispose();
        if (background != null) {
            background.dispose();
        }
        moneyTexture.dispose();
        spikesTexture.dispose();
        font.dispose();
    }
}
