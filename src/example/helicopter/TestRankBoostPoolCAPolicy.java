/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package example.helicopter;

import core.State;
import core.Task;
import domain.helicopter.HelicopterTask;
import experiment.Experiment;
import java.util.Random;
import policy.RankBoostPoolCANappingPolicy;
import utills.IO;

/**
 * example.helicopter.TestRankBoostPoolCAPolicy
 *
 * @author daq
 */
public class TestRankBoostPoolCAPolicy {

    static int maxStep = 8000;
    static boolean isPara = true;

    public static void main(String[] args) throws Exception {
        int trial = 0;// Integer.parseInt(args[0]);

        Random random = new Random();
        Task task = new HelicopterTask(new Random(random.nextInt()));
        State initialState = task.getInitialState();

        Experiment exp = new Experiment();

//        RankBoostPoolCAPolicy bpca = new RankBoostPoolCAPolicy(new Random(random.nextInt()));
        RankBoostPoolCANappingPolicy bpca = new RankBoostPoolCANappingPolicy(new Random(random.nextInt()), 10);
        bpca.setStepsize(1);

        double[][] resultys = exp.conductExperimentTrainCA(bpca, task, 1000, 50, initialState, maxStep, isPara, new Random(random.nextInt()));
        IO.matrixWrite(resultys, "results/heli/PBWP_trial_" + trial + ".txt");
    }
}
