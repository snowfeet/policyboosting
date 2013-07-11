/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package run;

import experiment.Experiment;
import core.Task;
import domain.mountaincar3d.MCar3DTask;
import domain.mountaincar3d.MCar3DTaskSet;
import java.util.List;
import java.util.Random;
import policy.AdaptiveMultiMetaPolicyV2;
import utills.IO;

/**
 *
 * @author daq
 */
public class DebugAdaptive {

    static int maxIter = 500;
    static boolean isPara = true;

    public static void main(String[] args) throws Exception {
        int K = 4;
        double epsion = 0.03;
        double ratio = 0.8;
        double delay = 0;

        Random random = new Random();
        MCar3DTaskSet taskSet = new MCar3DTaskSet(new Random(random.nextInt()));

        List<Task> tasks = (List<Task>) IO.loadObject("task/MCar3D_2000.task");

        Experiment exp = new Experiment();
        AdaptiveMultiMetaPolicyV2 ammp = new AdaptiveMultiMetaPolicyV2(K, epsion, ratio, delay, new Random(random.nextInt()));
        ammp.setStepsize(0.3);
        ammp.train(taskSet, 100, 50, 2, MCar3DTask.getInitialState(), maxIter, true, random);

        double[][] results = exp.conductTesting(ammp, tasks, MCar3DTask.getInitialState(), maxIter, 0, isPara, random);
        for (int i = 0; i < results.length; i++) {
            for (int j = 0; j < results[i].length; j++) {
                System.out.print(results[i][j] + "\t");
            }
            System.out.println();
        }
    }
}
