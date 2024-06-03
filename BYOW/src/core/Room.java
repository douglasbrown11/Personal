package core;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Room {
    private Rectangle bounds;
    private List<Point> coordinates;

    public Room(int x, int y, int width, int height) {
        this.bounds = new Rectangle(x, y, width + 2, height + 2);
        this.coordinates = new ArrayList<>();
        for (int i = x; i < x + width; i++) {
            for (int j = y; j < y + height; j++) {
                coordinates.add(new Point(i, j));
            }
        }
    }

    public Point getRandomFloorPoint(Random random) {
        int x = bounds.x + random.nextInt(bounds.width);
        int y = bounds.y + random.nextInt(bounds.height);
        return new Point(x, y);
    }

    public boolean overlaps(Room other) {
        return this.bounds.intersects(other.bounds);
    }
    public Rectangle getBounds() {
        return bounds;
    }
    public List<Point> getCoordinates() {
        return coordinates;
    }
    public int getWidth() {
        return bounds.width;
    }
    public int getHeight() {
        return bounds.height;
    }
}
