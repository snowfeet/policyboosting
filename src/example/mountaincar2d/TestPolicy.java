/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package example.mountaincar2d;

import example.cw.*;
import core.State;
import core.Task;
import domain.cw.CWTask;
import domain.mountaincar2d.MountainCarTask;
import experiment.Experiment;
import java.util.Random;
import policy.NPPGPolicy;
import policy.RankBoostPoolPolicy;
import utills.IO;

/**
 *
 * @author daq
 */
public class TestPolicy {

    static int maxStep = 2000;
    static boolean isPara = true;

    public static void run(int trial) throws Exception {
        Random random = new Random();
        Task task = new MountainCarTask(new Random(random.nextInt()));
        State initialState = task.getInitialState();

        NPPGPolicy gbPolicy = new NPPGPolicy(new Random(random.nextInt()));
        gbPolicy.setStationaryRate(0.7);
        gbPolicy.setStepsize(0.1);
        gbPolicy.setEpsionGreedy(0.2);
        gbPolicy.setMaxStep(maxStep);
        Experiment exp = new Experiment();
        double[][] resultsNPPG = exp.conductExperimentTrain(gbPolicy, task, 100, 50, initialState, maxStep, true, 0.2, new Random(random.nextInt()));

        RankBoostPoolPolicy bp = new RankBoostPoolPolicy(new Random(random.nextInt()));
        bp.setStepsize(1);
        exp = new Experiment();
        double[][] resultsPB = exp.conductExperimentTrain(bp, task, 100, 50, initialState, maxStep, isPara, 0, new Random(random.nextInt()));

        IO.matrixWrite(resultsNPPG, "results/mc/NPPG_trial_" + trial + ".txt");
        IO.matrixWrite(resultsPB, "results/mc/PB_trial_" + trial + ".txt");
    }
}
