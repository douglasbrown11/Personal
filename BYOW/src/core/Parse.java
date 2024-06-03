package core;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Parse {

    public static String getSeed() {
        try {
            String filePath = "game_save.txt";
            String firstLine = Files.readString(Paths.get(filePath)).split("\n")[0]; // Read the first line directly
            return firstLine;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static Point parsePoint(String pointStr) {
        if (pointStr == null || pointStr.isEmpty()) {
            throw new IllegalArgumentException("Input string is null or empty");
        }

        if (!pointStr.startsWith("java.awt.Point[") || !pointStr.endsWith("]")) {
            throw new IllegalArgumentException("Invalid point string format");
        }
        String content = pointStr.substring(15, pointStr.length() - 1);


        String[] parts = content.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid point format");
        }

        // Extract and parse x and y values
        int x = Integer.parseInt(parts[0].split("=")[1].trim());
        int y = Integer.parseInt(parts[1].split("=")[1].trim());

        return new Point(x, y);
    }
}
