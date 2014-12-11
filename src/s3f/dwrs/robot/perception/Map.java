/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.dwrs.robot.perception;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.BitSet;
import static s3f.dwrs.robot.perception.Perception.paintPoints;

/**
 *
 * @author gnome3
 */
public class Map {

    private final float mapSizeInMeters;
    private final float tileSizeInMeters;
    private final float scale;
    private final int mapSize;
    private final BitMatrix map;
    private int lastRobotX = 0;
    private int lastRobotY = 0;

    public Map(float mapSizeInMeters, float tileSizeInMeters, float scale) {
        this.mapSizeInMeters = mapSizeInMeters;
        this.tileSizeInMeters = tileSizeInMeters;
        this.scale = scale;
        mapSize = (int) (mapSizeInMeters / tileSizeInMeters);
        map = new BitMatrix(mapSize, mapSize);
    }

    public static class BitMatrix {

        public BitMatrix(int numRows, int numColumns) {
            rows = new BitSet[numRows];
            for (int i = 0; i < numRows; i++) {
                rows[i] = new BitSet(numColumns);
            }
        }

        protected BitSet[] rows;

        public void clear(int i, int j) {
            rows[i].clear(j);
        }

        public boolean get(int i, int j) {
            return rows[i].get(j);
        }

        public void set(int i, int j) {
            rows[i].set(j);
        }
    }

    public void addLine(int robotX, int robotY, int obstacleX, int obstacleY) {
        float x0 = robotX + 100 / scale;
        float y0 = robotY + 100 / scale;
        float x1 = obstacleX + 100 / scale;
        float y1 = obstacleY + 100 / scale;

        int dx = (int) Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
        int dy = -(int) Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
        int err = dx + dy, e2; /* error value e_xy */

        while (true) {  /* loop */

            if (x0 == x1 && y0 == y1) {
                map.clear((int) x0, (int) y0);
                break;
            } else {
                map.set((int) x0, (int) y0);
            }
            e2 = 2 * err;
            if (e2 >= dy) {
                err += dy;
                x0 += sx;
            } /* e_xy+e_x > 0 */

            if (e2 <= dx) {
                err += dx;
                y0 += sy;
            } /* e_xy+e_y < 0 */

        }
    }

    public void draw(Graphics2D g) {
        g.setColor(new Color(1, 0, 0, .5f));
        for (int x = -50; x < 50; x++) {
            for (int y = -50; y < 50; y++) {
                if (!map.get(x + 100, y + 100)) {
                    g.fillRect(x*4, y*4, 2, 2);
                }
            }
        }
    }

}
