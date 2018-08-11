package lando.systems.ld42.screens;

import aurelienribon.tweenengine.primitives.MutableFloat;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import lando.systems.ld42.Config;
import lando.systems.ld42.LudumDare42;
import lando.systems.ld42.units.Unit;
import lando.systems.ld42.world.*;

public class GameScreen extends BaseScreen {

//    public TutorialManager tutorialManager;
    public TextureRegion debugTex;
    public World world;
//    public TurnCounter turnCounter;
    public Array<Tile> adjacentTiles;
    public Array<Tile> adjacentBuildTiles;
//    public EndTurnButton endTurnButton;
//    public PlayerSelectionHud playerSelection;
    public Player selectedPlayer;
    public Unit testUnit;

    public int turn;
//    public Array<TurnAction> turnActions;
    float time;

    public Vector3 cameraTouchStart;
    public Vector3 touchStart;
    public static float zoomScale = 0.15f;
    public static float maxZoom = 1.6f;
    public static float minZoom = 0.2f;
    public static float DRAG_DELTA = 10f;

    public boolean cancelTouchUp = false;
    public boolean firstRun = false;

    public MutableFloat overlayAlpha;
    public boolean pauseGame;
    public boolean gameOver;
    public boolean gameLost;
//    EndGameOverlay endGameOverlay;
//    public Screenshake shaker;
    public Vector2 cameraCenter;
    public float gullTimer;

    public GameScreen() {
        super();
 //       SoundManager.oceanWaves.play();
        gullTimer = 40;
        cameraCenter = new Vector2();
        gameOver = false;
        overlayAlpha = new MutableFloat(1);
        pauseGame = true;
        time = 0;
        world = new World(this);
        cameraTargetPos.set(world.bounds.width/2f, world.bounds.height/2f, 0);
        worldCamera.position.set(cameraTargetPos);
        targetZoom.setValue(MAX_ZOOM);
        worldCamera.zoom = targetZoom.floatValue();
        worldCamera.update();
        adjacentTiles = new Array<Tile>();
        adjacentBuildTiles = new Array<Tile>();
        turn = 0;

//        endTurnButton = new EndTurnButton(new Rectangle(hudCamera.viewportWidth - 100 - 10, 10, 100, 30), hudCamera);
//        playerSelection = new PlayerSelectionHud(this);
//        testingButton = new Button(Assets.transparentPixel, new Rectangle(50,50,50,50), hudCamera, "Too much Text!", "Tooltip");
        cameraTouchStart = new Vector3();
        touchStart = new Vector3();
//        shaker = new Screenshake(120, 3);
        testUnit = new Unit(LudumDare42.game.assets);
        Tile tile = world.getTile(0, 0);
        testUnit.pos.set(tile.position.x + Tile.tileWidth / 2f - testUnit.size.x / 2f, tile.position.y + Tile.tileHeight - testUnit.size.y);
    }

    @Override
    public void update(float dt) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

//        if (endGameOverlay == null) {
//            stats.totalTime += dt;
//        }
        gullTimer -= dt;
//        if (gullTimer < 0 && endGameOverlay == null){
//            gullTimer = MathUtils.random(30f,90f);
//            SoundManager.playSound(SoundManager.SoundOptions.seagull);
//        }
//        if (tutorialManager != null) {
//            if (!firstRun && tutorialManager.isDisplayed()) {
//                tutorialManager.update(dt);
//                return;
//            }
//            firstRun = false;
//        }

//        if (endGameOverlay != null){
//            endGameOverlay.update(dt);
//        }

//        SoundManager.update(dt);

        time += dt;
        world.update(dt);
        testUnit.update(dt);
//        endTurnButton.update(dt);

//        if (tutorialManager != null) {
//            handleKeyBindings();
//        }

        updateCamera();

//        shaker.update(dt, camera, camera.position.x, camera.position.y);
    }


//    private boolean handleMove(GridPoint2 location) {
//        if (actionButton == null) return false;
//
//        OldTile tile = world.getTile(location);
//        if (actionButton.action == Actions.displayMoves) {
//            if (adjacentTiles.contains(tile, true) && !tile.isInaccessible) {
//                SoundManager.playSound(SoundManager.SoundOptions.player_move);
//                TurnAction turnAction = new TurnAction(selectedPlayer, actionCost);
//                turnAction.action = new ActionTypeMove(turnAction, tile.col, tile.row);
//                addAction(turnAction, selectedPlayer.getHudPostion(camera, hudCamera));
//                clearMovement();
//                return true;
//            }
//        } else if (actionButton.action == Actions.build){
//            if (adjacentTiles.contains(tile, true)) {
//                TurnAction turnAction = new TurnAction(selectedPlayer, actionCost);
//                turnAction.action = new ActionTypeBuild(turnAction, actionButton.region, tile.col, tile.row);
//                addAction(turnAction, selectedPlayer.getHudPostion(camera, hudCamera));
//                clearMovement();
//                return true;
//            }
//        }
//        return false;
//    }

//    private void showMovement(Player player, TextureRegion asset) {
//        // TODO: is there a situation where this could be null?
//        OldTile playerTile = world.getTile(player.row, player.col);
//
//        adjacentTiles.addAll(world.getNeighbors(player.row, player.col));
//        for (OldTile tile : adjacentTiles) {
//            tile.isHighlighted = true;
//
//            // Water inaccessible...
//            if (tile.heightOffset < world.water.waterHeight) {
//                tile.isInaccessible = (asset == null);
//                if (tile.isInaccessible) {
//                    if (tile.item == Assets.raft) {
//                        tile.isInaccessible = false;
//                    } else {
//                        tile.overlayObjectTex = Assets.raft;
//                    }
//                }
//            }
//            // height inaccessible
////            if (tile.height > 1f + playerTile.height) { // NOTE: this line requires ladders for tiles more than 1 'step' above player tile's height
//            if (tile.height > playerTile.height) { // NOTE: this line requires ladders _ALL_ tiles above the player tile's height
//                tile.isInaccessible = (asset == null);
//                if (tile.isInaccessible) {
//                    if (tile.item == Assets.ladder) {
//                        tile.isInaccessible = false;
//                    } else {
//                        tile.overlayObjectTex = Assets.ladder;
//                    }
//                }
//            }
//        }
//    }

//    private void clearMovement() {
//        for (OldTile tile : adjacentTiles) {
//            tile.isHighlighted = false;
//            tile.isInaccessible = false;
//            tile.overlayObjectTex = null;
//        }
//        adjacentTiles.clear();
//    }


//    Vector3 tp = new Vector3();
//    @Override
//    public boolean scrolled (int change) {
//        if (tutorialManager != null && tutorialManager.isDisplayed()) {
//            return false;
//        }
//
//        camera.unproject(tp.set(Gdx.input.getX(), Gdx.input.getY(), 0 ));
//        float px = tp.x;
//        float py = tp.y;
//        camera.zoom += change * camera.zoom * zoomScale;
//        updateCamera();
//
//        camera.unproject(tp.set(Gdx.input.getX(), Gdx.input.getY(), 0));
//        camera.position.add(px - tp.x, py- tp.y, 0);
//        camera.update();
//        return true;
//    }

//    private void updateCamera(){
//        camera.zoom = MathUtils.clamp(camera.zoom, minZoom, maxZoom);
//        float minY = world.bounds.y + camera.viewportHeight/2 * camera.zoom;
//        float maxY = world.bounds.height - camera.viewportHeight/2 * camera.zoom;
//
//        float minX = world.bounds.x + camera.viewportWidth/2 * camera.zoom;
//        float maxX = world.bounds.x + world.bounds.width - camera.viewportWidth/2 * camera.zoom;
//
//        if (camera.viewportHeight * camera.zoom > world.bounds.height){
//            camera.position.y = world.bounds.height/2;
//        } else {
//            camera.position.y = MathUtils.clamp(camera.position.y, minY, maxY);
//        }
//
//
//        if (camera.viewportWidth * camera.zoom > world.bounds.width){
//            camera.position.x = world.bounds.x + world.bounds.width/2;
//        } else {
//            camera.position.x = MathUtils.clamp(camera.position.x, minX, maxX);
//        }
//
//        camera.update();
//    }


    @Override
    public void render(SpriteBatch batch) {
        Gdx.gl.glClearColor(Config.background_color.r, Config.background_color.g, Config.background_color.b, Config.background_color.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw world
        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();
        {
            world.render(batch);
            testUnit.render(batch);
        }
        batch.end();

    }

    // required Konami code
    int[] sequence = new int [] { Input.Keys.UP, Input.Keys.UP, Input.Keys.DOWN, Input.Keys.DOWN, Input.Keys.LEFT, Input.Keys.RIGHT, Input.Keys.LEFT, Input.Keys.RIGHT, Input.Keys.B, Input.Keys.A};
    int index = 0;
    public boolean keyUp(int keyCode) {
        if (index >= sequence.length) index = 0;
        if (sequence[index] == keyCode) {
            if (++index == sequence.length) {
                // insert magic here
                index = 0;
            }
        } else {
            index = 0;
        }
        return false;
    }
}
