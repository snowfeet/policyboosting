/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package example.mountaincar2d;

import core.State;
import core.Task;
import domain.acrobot.AcrobotTask;
import domain.mountaincar2d.MountainCarTask;
import experiment.Experiment;
import java.util.Random;
import policy.RankBoostPoolPolicy;
import utills.IO;

/**
 *
 * @author daq
 */
public class TestPara {

    static int maxStep = 2000;
    static boolean isPara = false;

    public static void testStepsize(int trial, double d) {
        Random random = new Random();
        Task task = new MountainCarTask(new Random(random.nextInt()));
        State initialState = task.getInitialState();
        RankBoostPoolPolicy bp = new RankBoostPoolPolicy(new Random(random.nextInt()));
        bp.setStepsize(d);
        Experiment exp = new Experiment();
        double[][] resultsPB = exp.conductExperimentTrain(bp, task, 100, 50, initialState, maxStep, isPara, 0, new Random(random.nextInt()));

        IO.matrixWrite(resultsPB, "results/parameter/stepsize/mc/PB_trial_" + trial + "_setpsize_" + d + ".txt");
    }

    public static void testTreeDepth(int trial, int depth) {
        Random random = new Random();
        Task task = new MountainCarTask(new Random(random.nextInt()));
        State initialState = task.getInitialState();
        RankBoostPoolPolicy bp = new RankBoostPoolPolicy(new Random(random.nextInt()), depth, 10, 10);
        bp.setStepsize(1);
        Experiment exp = new Experiment();
        double[][] resultsPB = exp.conductExperimentTrain(bp, task, 100, 50, initialState, maxStep, isPara, 0, new Random(random.nextInt()));

        IO.matrixWrite(resultsPB, "results/parameter/treedepth/mc/PB_trial_" + trial + "_depth_" + depth + ".txt");
    }

    public static void testPoolSize(int trial, int bestPoolSize, int uniformPoolSize) {
        Random random = new Random();
        Task task = new MountainCarTask(new Random(random.nextInt()));
        State initialState = task.getInitialState();
        RankBoostPoolPolicy bp = new RankBoostPoolPolicy(new Random(random.nextInt()), 100, bestPoolSize, uniformPoolSize);
        bp.setStepsize(1);
        Experiment exp = new Experiment();
        double[][] resultsPB = exp.conductExperimentTrain(bp, task, 100, 50, initialState, maxStep, isPara, 0, new Random(random.nextInt()));

        IO.matrixWrite(resultsPB, "results/parameter/poolsize/mc/PB_trial_" + trial + "_poolsize_" + bestPoolSize + "_" + uniformPoolSize + ".txt");
    }
}
