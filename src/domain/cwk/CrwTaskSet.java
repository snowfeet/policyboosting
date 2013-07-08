/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package domain.cwk;

import core.Task;
import core.TaskSet;
import java.util.Random;

/**
 *
 * @author daq
 */
public class CrwTaskSet implements TaskSet {

    private double[] dist_p;
    private double[] dist_l;
    private double[] dist_n;
    private double[] dist_g;
    private Random random;

    public CrwTaskSet(Random rand) {
        dist_p = new double[]{0.9, 1};
        dist_l = new double[]{0.5, 0.8};
        dist_n = new double[]{0.05, 0.1};
        dist_g = new double[]{8, 10, 2, 3};
        random = rand;
    }

    @Override
    public Task generateTasks() {
        double p, l, n, g1, g2;

        p = dist_p[0] + random.nextDouble() * (dist_p[1] - dist_p[0]);
        l = dist_l[0] + random.nextDouble() * (dist_l[1] - dist_l[0]);
        n = dist_n[0] + random.nextDouble() * (dist_n[1] - dist_n[0]);
        g1 = dist_g[0] + random.nextDouble() * (dist_g[1] - dist_g[0]);
        g2 = dist_g[2] + random.nextDouble() * (dist_g[3] - dist_g[2]);

        if (random.nextDouble() < 0.5) {
            g1 = -g1 - g2;
        }

        return new CrwTask(p, l, n, g1, g2, random);
    }
}
