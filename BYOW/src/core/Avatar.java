package core;

import tileengine.TETile;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Avatar {
    private Point position;
    private World world;
    private TETile avatarTile;
    private TETile underTile;  // Tile under the avatar to restore when the avatar moves

    public Avatar(World world, Point startPosition, TETile avatarTile) {
        this.world = world;
        this.position = new Point(startPosition);
        this.avatarTile = avatarTile;
        this.underTile = world.getTiles()[startPosition.x][startPosition.y];
        world.getTiles()[position.x][position.y] = avatarTile;
    }

    public static String getAvatarPos() {
        try {
            String filePath = "game_save.txt";
            String firstLine = Files.readString(Paths.get(filePath)).split("\n")[1]; // Read the first line directly
            return firstLine;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void move(char direction) {
        int newX = position.x;
        int newY = position.y;

        switch (direction) {
            case 'W': // Move up
            case 'w':
                newY++;
                break;
            case 'S': // Move down
            case 's':
                newY--;
                break;
            case 'A': // Move left
            case 'a':
                newX--;
                break;
            case 'D': // Move right
            case 'd':
                newX++;
                break;
            default: // If the direction is not recognized, do nothing
                // You can log an error message or handle unexpected directions here if needed.
                break;
        }

        if (canMoveTo(newX, newY)) {
            updatePosition(newX, newY);
        }
    }


    private void updatePosition(int newX, int newY) {
        world.getTiles()[position.x][position.y] = underTile;

        position.setLocation(newX, newY);

        underTile = world.getTiles()[newX][newY];
        world.getTiles()[newX][newY] = avatarTile;
    }
    public Point getPosition() {
        return position;
    }
    public void setPosition(Point newPosition) {
        this.position = newPosition;
    }

    private boolean canMoveTo(int x, int y) {
        if (x < 0 || x >= World.WIDTH || y < 0 || y >= World.HEIGHT) {
            return false; // Bounds check
        }
        TETile tile = world.getTiles()[x][y];
        // Assuming you have set "wall" to represent impassable tiles accurately
        return !tile.getDescription().equals("wall") && !tile.getDescription().equals("hallway wall");
    }
}
