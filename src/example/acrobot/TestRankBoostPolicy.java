/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package example.acrobot;

import core.State;
import core.Task;
import domain.acrobot.AcrobotTask;
import static example.acrobot.TestBoostedPolicy.maxStep;
import experiment.Experiment;
import java.util.Random;
import policy.BoostedPolicy;
import policy.RankBoostPolicy;

/**
 *
 * @author daq
 */
public class TestRankBoostPolicy {

    static int maxStep = 1000;
    static boolean isPara = false;

    public static void main(String[] args) throws Exception {
        Random random = new Random(1);
        Task task = new AcrobotTask(new Random(random.nextInt()));
        State initialState = task.getInitialState();

        Experiment exp = new Experiment();

        RankBoostPolicy bp = new RankBoostPolicy(new Random(random.nextInt()));
        bp.setStepsize(0.001);

        exp.conductExperimentTrain(bp, task, 100, 50, initialState, maxStep, isPara, new Random(random.nextInt()));
    }
}
