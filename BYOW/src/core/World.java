package core;

import tileengine.TERenderer;
import tileengine.TETile;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static utils.FileUtils.fileExists;

public class World {
    
    public static final int WIDTH = 50;
    public static final int HEIGHT = 30;
    private final TETile[][] tiles;
    private final int ten = 10;

    private final Random random;
    private final List<Room> rooms;
    private final TERenderer ter;

    private ArrayList<Character> controlKeysFromInput;

    private TETile doorwayTile = new TETile('.', Color.GRAY, Color.DARK_GRAY, "doorway", null, 4);
    private TETile hallwayWallTile = new TETile('#', Color.BLUE, Color.DARK_GRAY, "hallway wall", null, 1);


    public World(String seed) {
        this.ter = new TERenderer();
        this.controlKeysFromInput = new ArrayList<>();
        long longSeed = stringToLong(seed);
        if (longSeed == 0) {
            this.random = new Random(stringToLong(getSeed()));
        } else {
            this.random = new Random(longSeed);
        }

        tiles = new TETile[WIDTH][HEIGHT]; // makes graph
        rooms = new ArrayList<>(); // list of rooms
        fillWithEmptyTiles();
        generateWorld();
        connectRooms();

    }
    public String getSeed() {
        try {
            String filePath = "game_save.txt";
            if (fileHasContent(filePath)) {
                String firstLine = Files.readString(Paths.get(filePath)).split("\n")[0]; // Read the first line directly
                return firstLine;
            } else {
                return "0";
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static boolean fileHasContent(String filePath) {
        try {
            if (!fileExists(filePath)) {
                return false; // File does not exist
            }

            String content = Files.readString(Paths.get(filePath));
            return content != null && !content.trim().isEmpty(); // Check if there's content
        } catch (IOException e) {
            throw new RuntimeException("Error reading file.", e);
        }
    }



    private void fillWithEmptyTiles() {
        TETile emptyTile = new TETile('^', Color.RED, Color.BLACK, "empty space", null, 0);
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                tiles[x][y] = emptyTile;
            }
        }
    }
    public void generateWorld() {
        int numberOfRooms = 3 + random.nextInt(8);
        int attempts = 0;
        while (rooms.size() < numberOfRooms && attempts < 1000) {
            Room newRoom = generateRoom();
            if (newRoom != null) {
                rooms.add(newRoom);
                fillRoomWithTiles(newRoom.getBounds());
            }

            /// if statement that checks if prev room is in bounds of next room

            attempts++;
        }
    }
    private boolean doesOverlap(Room newRoom) {
        for (Room room: rooms) {
            if (newRoom.overlaps(room)) {
                return true;
            }
        }
        return false;
    }
    /*
        Random random = new Random(seed);

        TETile wallTile = new TETile('#', Color.WHITE, Color.DARK_GRAY, "wall", null, 1);
        int roomWidth = ten + random.nextInt(5);
        int roomHeight = ten + random.nextInt(5);
        int startX = random.nextInt(World.WIDTH - roomWidth);
        int startY = random.nextInt(World.HEIGHT - roomHeight);

        for (int x = startX; x < startX + roomWidth; x++) {
            for (int y = startY; y < startY + roomHeight; y++) {
                tiles[x][y] = wallTile;
            }
        }*/

    private Room generateRoom() {
        int roomWidth = random.nextInt(15);
        int roomHeight = random.nextInt(16);
        int gap = 1 + random.nextInt(5);
        int startX = random.nextInt(WIDTH - roomWidth - gap * 2) + gap;
        int startY = random.nextInt(HEIGHT - roomHeight - gap * 2) + gap;
        ///list of coordinates

        Room newRoom = new Room(startX, startY, roomWidth, roomHeight);
        if (doesOverlap(newRoom)) {
            return null;
        }
        System.out.println("Room at (" + startX + ", " + startY);

        return new Room(startX, startY, roomWidth, roomHeight);
    }
    public void clearWorld() {
        rooms.clear();
        fillWithEmptyTiles();
    }
    public void addRoomFromSave(int x, int y, int width, int height) {
        Room room = new Room(x, y, width, height);
        rooms.add(room);
        fillRoomWithTiles(room.getBounds());
    }
    public void setTile(int x, int y, TETile tile) {
        if (x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT) {
            tiles[x][y] = tile;
        }
    }

    public void fillRoomWithTiles(Rectangle room) {
        TETile wallTile = new TETile('#', Color.BLUE, Color.DARK_GRAY, "wall", null, 1);
        TETile floorTile = new TETile('.', Color.GRAY, Color.DARK_GRAY, "floor", "Sprites/floor-1.png", 2);
        for (int x = room.x; x < room.x + room.width; x++) {
            for (int y = room.y; y < room.y + room.height; y++) {
                tiles[x][y] = floorTile;
            }
        }
        ///boundary checks to prevent out of bounds errors
        int xMin = Math.max(room.x - 1, 0);
        int xMax = Math.min(room.x + room.width, WIDTH - 1);
        int yMin = Math.max(room.y - 1, 0);
        int yMax = Math.min(room.y + room.height, HEIGHT - 1);
        // Ensure walls are properly marked and placed
        for (int x =  xMin; x <= xMax; x++) {
            if (room.y > 0) {
                tiles[x][yMin] = wallTile;
            }
            if (room.y + room.height < HEIGHT) {
                tiles[x][yMax] = wallTile;
            }
        }
        for (int y =  yMin; y <= yMax; y++) {
            if (room.x > 0) {
                tiles[xMin][y] = wallTile;
            }
            if (room.x + room.width < WIDTH) {
                tiles[xMax][y] = wallTile;
            }
        }
    }

    private void connectRooms() {
        TETile hallwayTile = new TETile('.', Color.GRAY, Color.DARK_GRAY, "hallway", null, 3);
        for (int i = 0; i < rooms.size() - 1; i++) {
            Room r1 = rooms.get(i);
            Room r2 = rooms.get(i + 1);
            Point center1  = new Point(r1.getBounds().x + r1.getWidth() / 2, r1.getBounds().y + r1.getHeight() / 2);
            Point center2  = new Point(r2.getBounds().x + r2.getWidth() / 2, r2.getBounds().y + r2.getHeight() / 2);

            if (Math.abs(center1.x - center2.x) < Math.abs(center1.y - center2.y)) {
                connectHorizontally(center1, new Point(center2.x, center1.y), hallwayTile);
                connectVertically(new Point(center2.x, center1.y), center2, hallwayTile);
            } else {
                connectVertically(center1, new Point(center1.x, center2.y), hallwayTile);
                connectHorizontally(new Point(center1.x, center2.y), center2, hallwayTile);
            }
        }
    }
    private void connectHorizontally(Point start, Point end, TETile tile) {
        int minY = start.y - 1;
        int maxY = start.y + 1;
        for (int x = Math.min(start.x, end.x); x <= Math.max(start.x, end.x); x++) {
            for (int y = minY; y <= maxY; y++) {
                if (y >= 0 && y < HEIGHT) {
                    if (y == minY || y == maxY) {
                        if (tiles[x][y].getDescription().equals("empty space")) {
                            tiles[x][y] = hallwayWallTile;
                        }

                    } else {
                        tiles[x][y] = tile;
                        if (isNextToRoom(x, y)) {
                            tiles[x][y] = doorwayTile;
                        }
                    }
                }
            }
        }
    }
    private void connectVertically(Point start, Point end, TETile tile) {
        int minX = start.x - 1;
        int maxX = start.x + 1;
        for (int y = Math.min(start.y, end.y); y <= Math.max(start.y, end.y); y++) {
            for (int x = minX; x <= maxX; x++) {
                if (x >= 0 && x < WIDTH) {
                    if (x == minX || x == maxX) {
                        if (tiles[x][y].getDescription().equals("empty space")) {
                            tiles[x][y] = hallwayWallTile;
                        }

                    } else {
                        tiles[x][y] = tile;
                        if (isNextToRoom(x, y)) {
                            tiles[x][y] = doorwayTile;
                        }
                    }
                }
            }
        }
    }
    private boolean isNextToRoom(int x, int y) {
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        for (int i = 0; i < dx.length; i++) {
            int nextX = x + dx[i];
            int nextY = y + dy[i];
            if (nextX >= 0 && nextX < WIDTH && nextY >= 0 && nextY < HEIGHT) {
                if (tiles[nextX][nextY].getDescription().equals("floor")
                        || tiles[nextX][nextY].getDescription().equals("wall")) {
                    return true;
                }
            }
        }
        return false;
    }

    public TETile[][] getTiles() {
        return tiles;
    }

    private long stringToLong(String seed) { // Example input "N555WASD" or "lWASD"
    


        StringBuilder sb = new StringBuilder();
        this.controlKeysFromInput = new ArrayList<>(); // Assuming this is a class member

        // Iterate through the seed string.
        for (int i = 0; i < seed.length(); i++) {
            char element = Character.toUpperCase(seed.charAt(i));

            // Check if it's a digit, if so, append to the StringBuilder.
            if (Character.isDigit(element)) {
                sb.append(element);
            } else if (isControlKey(element)) {
                // If it's a control key, add it to the list of controls.
                this.controlKeysFromInput.add(element);

            }
            // Non-digit, non-control characters are ignored.
        }

        // Parse the long from the string of digits.
        long longSeed = 0;
        if (sb.length() > 0) {
            longSeed = Long.parseLong(sb.toString());
        }
        if (controlKeysFromInput.contains(':') && (controlKeysFromInput.contains('q')
                ||
                controlKeysFromInput.contains('Q'))) {
            saveGame(seed);

        }


        return longSeed;
    }
    private void saveGame(String seed) {
        try {
            File file = new File("game_save.txt");
            PrintWriter writer = new PrintWriter(file); //automatically clears the file
            writer.println(seed);
            ///writer.println(avatar.getPosition());
            writer.close();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    private boolean isControlKey(char element) {
        List<Character> listOfControls = Arrays.asList('W', 'A', 'S', 'D', 'L', ':', 'Q'); // List of valid control keys
        return listOfControls.contains(element);
    }

    public List getControlKeysFromInput() {
        return controlKeysFromInput;
    }


    public List<Room> getRooms() {
        return this.rooms;
    }




}


