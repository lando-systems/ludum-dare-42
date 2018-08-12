package lando.systems.ld42.world;

import aurelienribon.tweenengine.Tween;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import lando.systems.ld42.LudumDare42;
import lando.systems.ld42.accessors.Vector2Accessor;
import lando.systems.ld42.screens.GameScreen;
import lando.systems.ld42.teams.Team;
import lando.systems.ld42.utils.TileUtils;

import java.util.Comparator;

public class World {

    public static World THE_WORLD;
    public static final int WORLD_WIDTH = 11;
    public static final int WORLD_HEIGHT = 7;

    public Array<Tile> adjacentTiles;
    public Array<Tile> tiles;
    public Array<Tile> animatingTiles;
    public Rectangle bounds;
    public GameScreen screen;

    public int enemyTileCount;
    public int playerTileCount;
    public int unclaimedTileCount;

    public World(GameScreen screen){
        this.screen = screen;
        THE_WORLD = this;

        generateWorldTiles();
        bounds = new Rectangle(0, 0,(Tile.tileWidth) * WORLD_WIDTH * .75f, Tile.tileHeight * WORLD_HEIGHT);

        animatingTiles = new Array<Tile>();
        adjacentTiles = new Array<Tile>();
//        tiles.get(0).owner = Team.Type.player;
//        tiles.get(tiles.size-1).owner = Team.Type.enemy;

        unclaimedTileCount = tiles.size - 2;
        enemyTileCount = 1;
        playerTileCount = 1;
    }

    public void update(float dt){
        animatingTiles.clear();

        int maxCol = 0;
        int maxRow = 0;
        enemyTileCount = 0;
        playerTileCount = 0;
        unclaimedTileCount = 0;
        for (int i = 0; i < tiles.size; i++){
            Tile t = tiles.get(i);
            if (t != null) {
                if (t.dead) {
                    tiles.set(i, null);
                }
                if (t.animating) {
                    animatingTiles.add(t);
                }
                maxCol = Math.max(maxCol, t.col);
                maxRow = Math.max(maxRow, t.row);
                switch(t.owner){
                    case none: unclaimedTileCount++; break;
                    case player: playerTileCount++; break;
                    case enemy: enemyTileCount++; break;
                }
            }
        }
        bounds.setWidth(Tile.tileWidth * (maxCol+1.5f) * .75f);
        bounds.setHeight(Tile.tileHeight * (maxRow+2));
    }

    public void render(SpriteBatch batch){
        for (int i = tiles.size-1; i >= 0; i--){
            Tile t = tiles.get(i);
            if (t != null && !t.animating){
                t.render(batch);
            }
        }

        for (int i = animatingTiles.size - 1; i >= 0; --i) {
            Tile tile = animatingTiles.get(i);
            if (tile == null) {
                animatingTiles.removeIndex(i);
            } else {
                tile.render(batch);
            }
        }

        batch.setColor(Color.WHITE);

    }

    public void renderPickBuffer(SpriteBatch batch){
        for (int i = tiles.size-1; i >= 0; i--){
            Tile t = tiles.get(i);
            if (t != null) {
                t.renderPickBuffer(batch);
            }
        }
        batch.setColor(Color.WHITE);
    }

    private void generateWorldTiles() {
        tiles = new Array<Tile>(WORLD_WIDTH * WORLD_HEIGHT );
        // Create the tiles.
        for (int row = 0; row < WORLD_HEIGHT; row++){
            for (int col = 0; col < WORLD_WIDTH; col++){
                tiles.add(new Tile(col, row));
            }
        }

        randomAssignTileType();
    }

    public void randomAssignTileType() {
        for (Tile tile : tiles) {
            int randIndex = MathUtils.random(0, 2);
            tile.type = getTileTypeFromInt(randIndex);
        }
    }

    public Tile.Type getTileTypeFromInt(int num) {
        Tile.Type type = Tile.Type.none;
        switch (num) {
            case 0: type = Tile.Type.forest; break;
            case 1: type = Tile.Type.mountain; break;
            default: break;
        }

        return type;
    }

    public int getTileIndex(Tile t){
        if (t == null) return -1;
        return t.col + t.row * WORLD_WIDTH;
    }

    public Tile getTile(int col, int row){
        if (col < 0 || col >= WORLD_WIDTH) return null;
        if (row < 0 || row >= WORLD_HEIGHT) return null;
        int index = col + row * WORLD_WIDTH;
        if (index < 0 || index >= tiles.size) return null;
        return tiles.get(index);

    }

    public void removeTile(Tile t){
        if (t.occupant != null) {
            t.occupant.tileGotSquanched();
        }
        t.killTile();
    }

    private Array<Tile> removalCandidateTiles = new Array<Tile>();
    public void pickRemoveTileStraightforwardly(){
        Tile removeTile = null;
        removalCandidateTiles.clear();
        for (Tile t : tiles){
            if (t != null && t.owner == Team.Type.none){
                removalCandidateTiles.add(t);
            }
        }

        Array<Tile> tilesToRemove = tiles;
        if (removalCandidateTiles.size > 0) {
            tilesToRemove = removalCandidateTiles;
        }

        if (tilesToRemove.size < 2) return;
        while (removeTile == null) {
            int index = MathUtils.random(tilesToRemove.size - 1);
            removeTile = tilesToRemove.get(index);
        }
        removeTile(removeTile);
    }

    public void pickRemoveTileCleverly() {
        removalCandidateTiles.clear();

        int maxColAccum = 0;
        int maxRowAccum = 0;
        for (Tile tile : tiles) {
            if (tile == null) continue;
            if (tile == screen.playerTeam.castle.tile) continue;
            if (tile == screen.enemyTeam.castle.tile) continue;
            removalCandidateTiles.add(tile);
            if (tile.col > maxColAccum) maxColAccum = tile.col;
            if (tile.row > maxRowAccum) maxRowAccum = tile.row;
        }
        final int maxCol = maxColAccum;
        final int maxRow = maxRowAccum;

        if (removalCandidateTiles.size <= 4) return;

        // Sort removalCandidates by 1) unclaimed 2) middleX 3) middleY
        removalCandidateTiles.sort(new Comparator<Tile>() {
            @Override
            public int compare(Tile tile1, Tile tile2) {
                if (tile1 == tile2) return 0;

                int ownerComparison = tile1.owner.compareTo(tile2.owner);
                if (ownerComparison != 0) return ownerComparison;

                int colMiddle = maxCol / 2;
                int colDist1 = Math.abs(colMiddle - tile1.col);
                int colDist2 = Math.abs(colMiddle - tile2.col);
                int colComparison = Integer.compare(colDist1, colDist2);
                if (colComparison != 0) return colComparison;

                int rowMiddle = maxRow / 2;
                int rowDist1 = Math.abs(rowMiddle - tile1.row);
                int rowDist2 = Math.abs(rowMiddle - tile2.row);
                int rowComparison = Integer.compare(rowDist1, rowDist2);
                return rowComparison;
            }
        });

        // Pick randomly from candidates, favoring lower indices (as they are 'better' candidates based on sorting criterio
        int numIntervals = 3;
        int range = removalCandidateTiles.size / numIntervals;
        int indexToRemove = 0;
        float random = MathUtils.random();
        if (random >= 0.6f) {
            indexToRemove = MathUtils.random(0, range - 1);
        } else if (random >= 0.3f) {
            indexToRemove = MathUtils.random(range - 1, 2 * range - 1);
        } else {
            indexToRemove = MathUtils.random(2 * range - 1, removalCandidateTiles.size - 1);
        }

        Tile tileToRemove = removalCandidateTiles.get(indexToRemove);
        if (tileToRemove != null) {
            removeTile(tileToRemove);
        }
    }

    public void moveTile(Tile t, int col, int row){
        float x = TileUtils.getX(col, Tile.tileWidth);
        float y = TileUtils.getY(row, col, Tile.tileHeight);
        int oldIndex = getTileIndex(t);
        Tween.to(t.position, Vector2Accessor.XY, 1f)
                .target(x, y)
                .start(LudumDare42.game.tween);
        t.row = row;
        t.col = col;
        t.pickColor = TileUtils.getColorFromPosition(row, col);
        tiles.set(col + row * WORLD_WIDTH, t);
        tiles.set(oldIndex, null);
        if (t.occupant != null) {
            t.occupant.moveTo(t);
        }
    }

    public void squishHoles(){
        for (int fixthis = 0; fixthis < 5; fixthis++){
            for (int row = 0; row < WORLD_HEIGHT; row ++) {
                for (int col = 0; col < WORLD_WIDTH; col++) {
                    Tile t = getTile(col, row);
                    if (t == null) {
                        boolean emptyCol = row == 0;
                        for (int y = row + 1; y < WORLD_HEIGHT; y++) {
                            Tile next = getTile(col, y);
                            if (next != null) {
                                moveTile(next, col, row);
                                emptyCol = false;
                                break;
                            }
                        }
                        if (emptyCol) {
                            for (int y = 0; y < WORLD_HEIGHT; y++) {
                                Tile rightTile = getTile(col + 1, y);
                                if (rightTile != null) {
                                    moveTile(rightTile, col, y);
                                }
                            }
                        }
                    }
                }
            }
        }


    }
}
