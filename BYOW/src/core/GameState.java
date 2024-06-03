package core;

import edu.princeton.cs.algs4.StdDraw;
import tileengine.TERenderer;
import tileengine.TETile;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Random;

public class GameState {

    private World world;
    private final TERenderer ter;
    private Avatar avatar;
    private char avatarChar;
    private String savedSeed;
    private boolean lineOfSightEnabled = true;
    private boolean loadedGame = false;
    private static final int LINE_OF_SIGHT_RADIUS = 5;
    private String loadAvatarPos;
    private static final TETile DEFAULT_TILE = new TETile(' ', Color.BLACK, Color.BLACK, "empty", 0);


    public GameState() {
        this.ter = new TERenderer();
        selectAvatarCharacter();
        getSeedInput();
        gameLoop();
    }

    private void selectAvatarCharacter() {
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.text(0.5, 0.5, "Select a character from the keyboard for your avatar");
        StdDraw.show();

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                avatarChar = StdDraw.nextKeyTyped();
                break;
            }
        }
    }

    private void getSeedInput() {
        StringBuilder seed = new StringBuilder();
        GameState.loadMenu();
        boolean newGameStarted = false;
        while (true) { //runs until user pressing S to confirm seed or until user clicks L
            if (StdDraw.hasNextKeyTyped()) {
                char key = Character.toUpperCase(StdDraw.nextKeyTyped());
                if (key == 'Q') {
                    System.exit(0);
                    break;
                }
                if (key == 'L') {
                    loadGame();
                    break;
                }
                if (key == 'N') {
                    newGameStarted = true;
                    StdDraw.clear(StdDraw.BLACK);
                    StdDraw.setPenColor(StdDraw.WHITE);
                    StdDraw.text(250, 250, "Enter Seed, Press 'S' to Confirm");
                    StdDraw.show();
                    seed.setLength(0);
                }
                if (newGameStarted) {
                    if (newGameStarted) {
                        if (key == 'S') {
                            if (!seed.isEmpty()) {
                                String seedString = seed.toString();
                                setupGame(seedString);
                                this.savedSeed = seedString;
                                return;
                            } else {
                                StdDraw.text(250, 150, "Seed cannot be empty!");
                                StdDraw.show();
                            }
                        } else {
                            seed.append(key);
                            StdDraw.clear(StdDraw.BLACK);
                            StdDraw.text(250, 250, "Enter Seed, Press 'S' to Confirm");
                            StdDraw.text(250, 200, seed.toString()); // Display the current seed
                            StdDraw.show();
                        }
                    }
                }
            }
        }
    }



    private void gameLoop() {
        StringBuilder keySequence = new StringBuilder();
        while (true) {
            displayTileUnderMouse();
            if (StdDraw.hasNextKeyTyped()) {
                char key = Character.toUpperCase(StdDraw.nextKeyTyped());
                keySequence.append(key);

                if (keySequence.length() > 2) {
                    keySequence.deleteCharAt(0);
                }

                if (keySequence.toString().equals(":Q")) {
                    saveGame();
                } else if (key == 'T') {
                    lineOfSightEnabled = !lineOfSightEnabled;
                    renderWorld();
                } else {
                    avatar.move(key); // if the key is not L or Q and the user is moving the avatar
                    renderWorld();
                }
            }


        }
    }

    public void loadGame() {
        loadedGame = true;
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.show();
        String loadedString = Parse.getSeed();
        String avatarPositionString = Avatar.getAvatarPos();
        loadAvatarPos = avatarPositionString;
        if (loadedString.equals("")) {
            throw new IllegalArgumentException("Make some progress in a new game first!");
        }
        setupGame(loadedString);
        Iterator<Character> controlKeysIterator = world.getControlKeysFromInput().iterator();
        while (controlKeysIterator.hasNext()) {
            char next = controlKeysIterator.next();
            avatar.move(next);

        }
    }

    private void setupGame(String seed) {
        if (savedSeed == null) {
            this.savedSeed = seed;
        }
        GameState.drawGraph();
        this.world = new World(seed);
        this.ter.initialize(World.WIDTH, World.HEIGHT);

        Random rand = new Random();
        Room room = this.world.getRooms().get(rand.nextInt(this.world.getRooms().size()));
        Point startPosition = room.getRandomFloorPoint(rand);
        TETile avatarTile = new TETile(avatarChar, Color.WHITE, Color.BLACK, "avatar", 8);
        if (loadedGame) { ///
            this.avatar = new Avatar(world, Parse.parsePoint(loadAvatarPos), avatarTile);
        } else {
            this.avatar = new Avatar(world, startPosition, avatarTile);
        }


        renderWorld();
    }


    private void saveGame() {
        try {
            File file = new File("game_save.txt");
            PrintWriter writer = new PrintWriter(file); //automatically clears the file
            writer.println(getSavedSeed());
            writer.println(avatar.getPosition());
            writer.close();
            System.exit(0);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public String getSavedSeed() {
        return savedSeed;
    }


    public void renderWorld() {
        if (lineOfSightEnabled) {
            renderWithLineOfSight();
        } else {
            ter.renderFrame(world.getTiles());
        }

        ///displayTileUnderMouse();
        StdDraw.show();
    }

    private void renderWithLineOfSight() {
        TETile[][] tiles = world.getTiles();
        TETile[][] frame = new TETile[World.WIDTH][World.HEIGHT];

        // Initialize frame with the default tile
        for (int x = 0; x < World.WIDTH; x++) {
            for (int y = 0; y < World.HEIGHT; y++) {
                frame[x][y] = DEFAULT_TILE;
            }
        }
        Point avatarPos = avatar.getPosition();

        for (int x = 0; x < World.WIDTH; x++) {
            for (int y = 0; y < World.HEIGHT; y++) {
                double distance = calculateDistance(avatarPos, new Point(x, y));
                double dimmingFactor = calculateDimmingFactor(distance);
                if (dimmingFactor > 0) {
                    TETile originalTile = tiles[x][y];
                    TETile dimmedTile = new TETile(
                            originalTile.character(),
                            adjustColorTransparency(originalTile.getTextColor(), dimmingFactor),
                            adjustColorTransparency(originalTile.getBackgroundColor(), dimmingFactor),
                            originalTile.description(),
                            originalTile.id()
                    );
                    frame[x][y] = dimmedTile;
                }
            }
        }
        ter.renderFrame(frame);
    }


    private double calculateDistance(Point a, Point b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }
    private double calculateDimmingFactor(double distance) {
        if (distance > LINE_OF_SIGHT_RADIUS) {
            return 0;
        }
        return 1.0 - (distance / LINE_OF_SIGHT_RADIUS);
    }
    private Color adjustColorTransparency(Color color, double factor) {
        int red = (int) (color.getRed() * factor);
        int green = (int) (color.getGreen() * factor);
        int blue = (int) (color.getBlue() * factor);
        return new Color(red, green, blue);
    }

    /// @source this code was generated by ChatGpt. It checks what tile the mouse is on and displays it.
    private void displayTileUnderMouse() {
        int mouseX = (int) StdDraw.mouseX();
        int mouseY = (int) StdDraw.mouseY();
        if (mouseX >= 0 && mouseX < World.WIDTH && mouseY >= 0 && mouseY < World.HEIGHT) {
            TETile tile = world.getTiles()[mouseX][mouseY];


            StdDraw.setPenColor(Color.BLACK);
            StdDraw.filledRectangle(2, World.HEIGHT - 1, 8, 1); // Clear the text area


            StdDraw.setPenColor(Color.WHITE);
            StdDraw.text(4, World.HEIGHT - 1, "Tile: " + tile.getDescription()); // Show the updated tile information
            StdDraw.show(); // Display the updated information
        }
    }



    public static void loadMenu() {
        StdDraw.setCanvasSize(500, 500);
        StdDraw.setXscale(0, 500);
        StdDraw.setYscale(0, 500);
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.text(250, 250, "Press N For New Game");
        StdDraw.text(250, 150, "Press Q To Quit");
        StdDraw.text(250, 200, "Press L To Load Game");
        StdDraw.show();
    }

    public static void drawGraph() {
        StdDraw.setCanvasSize(World.WIDTH * 16, World.HEIGHT * 16);
        StdDraw.setXscale(0, World.WIDTH);
        StdDraw.setYscale(0, World.HEIGHT);
        StdDraw.enableDoubleBuffering();
    }

}


