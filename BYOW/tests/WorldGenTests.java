import core.AutograderBuddy;
import edu.princeton.cs.algs4.StdDraw;
import org.junit.jupiter.api.Test;
import tileengine.TERenderer;
import tileengine.TETile;

public class WorldGenTests {
    @Test
    public void basicTest() {
        // put different seeds here to test different worlds
        TETile[][] tiles = AutograderBuddy.getWorldFromInput("n1234567890123456789s");

        TERenderer ter = new TERenderer();
        ter.initialize(tiles.length, tiles[0].length);
        ter.renderFrame(tiles);
        StdDraw.pause(5000); // pause for 5 seconds so you can see the output
    }

    @Test
    public void basicInteractivityTest() {
        // TODO: write a test that uses an input like "n123swasdwasd"
    }

    @Test
    public void basicSaveTest() {
        TETile[][] tiles1 = AutograderBuddy.getWorldFromInput("n123swasd:Q");
        TETile[][] tiles2 = AutograderBuddy.getWorldFromInput("lwasd");

        TERenderer ter = new TERenderer();
        ter.initialize(tiles1.length, tiles1[0].length);
        ter.renderFrame(tiles1);
        StdDraw.pause(5000);

        TERenderer ter1 = new TERenderer();
        ter1.initialize(tiles2.length, tiles2[0].length);
        ter1.renderFrame(tiles2);
        StdDraw.pause(5000);

        // TODO: write a test that calls getWorldFromInput twice, with "n123swasd:q" and with "lwasd"
    }
    @Test
    public void AutograderTest() {
        AutograderBuddy.getWorldFromInput("N12321312WASD");
    }
}
