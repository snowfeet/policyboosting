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
import policy.RankBoostPoolCAPolicy;

/**
 * example.helicopter.TestRankBoostPoolCAPolicy
 * @author daq
 */
public class TestRankBoostPoolCAPolicy {

    static int maxStep = 6000;
    static boolean isPara = true;

    public static void main(String[] args) throws Exception {
        Random random = new Random();
        Task task = new HelicopterTask(new Random(random.nextInt()));
        State initialState = task.getInitialState();

        Experiment exp = new Experiment();

        RankBoostPoolCAPolicy bpca = new RankBoostPoolCAPolicy(new Random(random.nextInt()));
        bpca.setStepsize(1);

        exp.conductExperimentTrainCA(bpca, task, 1000, 500, initialState, maxStep, isPara, new Random(random.nextInt()));
    }
}
