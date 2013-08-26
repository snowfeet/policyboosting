/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package example.acrobot;

import core.State;
import core.Task;
import domain.acrobot.AcrobotTask;
import experiment.Experiment;
import java.util.Random;
import policy.RankBoostPoolInnerIterPolicy;

/**
 *
 * @author daq
 */
public class TestRankBoostPoolInnerIterPolicy {

    static int maxStep = 1000;
    static boolean isPara = false;

    public static void main(String[] args) throws Exception {
        Random random = new Random(100);
        Task task = new AcrobotTask(new Random(random.nextInt()));
        State initialState = task.getInitialState();

        Experiment exp = new Experiment();

        RankBoostPoolInnerIterPolicy bp = new RankBoostPoolInnerIterPolicy(new Random(random.nextInt()));
        bp.setStepsize(0.0005);
//        bp.setStepsize(0.005);

        exp.conductExperimentTrain(bp, task, 100, 50, initialState, maxStep, isPara, new Random(random.nextInt()));
    }
}
