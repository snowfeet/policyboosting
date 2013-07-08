/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package domain.mountaincar3d;

import core.Task;
import core.TaskSet;
import java.util.Random;

/**
 *
 * @author daq
 */
public class MCar3DTaskSet implements TaskSet {

    private double[][] dist_x;
    private double[][] dist_y;
    private Random random;

    public MCar3DTaskSet(Random rand) {
        random = rand;
        dist_x = new double[][]{{0.50, 0.55}, {-1.15, -1.1}, {-1.15, -1.1}, {0.5, 0.55}};
        dist_y = new double[][]{{0.50, 0.55}, {0.5, 0.55}, {-1.15, -1.1}, {-1.15, -1.1}};
    }

    @Override
    public Task generateTasks() {
        int index = random.nextInt(4);
        double x = dist_x[index][0] + random.nextDouble() * (dist_x[index][1] - dist_x[index][0]);
        double y = dist_y[index][0] + random.nextDouble() * (dist_y[index][1] - dist_y[index][0]);
//        return new MCar3DTask(x ,y, random);
        return new MCar3DTask(0.5, 0.5, new Random(0));
    }
}
