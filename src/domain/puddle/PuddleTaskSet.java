/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package domain.puddle;

import core.Task;
import core.TaskSet;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author daq
 */
public class PuddleTaskSet implements TaskSet {

    public double goalSize = 0.1;
    private double[][] dist_x;
    private double[][] dist_y;
    private Random random;

    public PuddleTaskSet(Random rand) {
        dist_x = new double[][]{{0.80, 0.90}, {0, 0.1}, {0, 0.1}, {0.8, 0.9}};
        dist_y = new double[][]{{0.80, 0.90}, {0, 0.1}, {0.8, 0.9}, {0, 0.1}};
        random = rand;
    }

    @Override
    public Task generateTasks() {
        List<Puddle> thePuddles = new ArrayList<Puddle>();

        thePuddles.add(new Puddle(.1, .75, .45, .75, .1));
        thePuddles.add(new Puddle(.45, .4, .45, .8, .1));

        int index = random.nextInt(dist_x.length);
        double x = dist_x[index][0] + random.nextDouble() * (dist_x[index][1] - dist_x[index][0]);
        double y = dist_y[index][0] + random.nextDouble() * (dist_y[index][1] - dist_y[index][0]);

        Rectangle2D goalRect = new Rectangle2D.Double(x, y, goalSize, goalSize);
        return new PuddleTask(thePuddles, goalRect, random);
    }
}
