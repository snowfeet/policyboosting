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
import policy.RankBoostMMPolicy;

/**
 *
 * @author daq
 */
public class TestRankBoostMMPolicy {

    static int maxStep = 1000;
    static boolean isPara = false;

    public static void main(String[] args) throws Exception {
        Random random = new Random();
        Task task = new AcrobotTask(new Random(random.nextInt()));
        State initialState = task.getInitialState();

        Experiment exp = new Experiment();

        RankBoostMMPolicy bp = new RankBoostMMPolicy(new Random(random.nextInt()));
        bp.setStepsize(0.000001);
//        bp.setStepsize(0.005);
        
        exp.conductExperimentTrain(bp, task, 100, 50, initialState, maxStep, isPara, new Random(random.nextInt()));
    }
}
