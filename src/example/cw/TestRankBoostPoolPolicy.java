/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package example.cw;

import core.State;
import core.Task;
import domain.cw.CWTask;
import experiment.Experiment;
import java.util.Random;
import policy.RankBoostPoolPolicy;

/**
 *
 * @author daq
 */
public class TestRankBoostPoolPolicy {

    static int maxStep = 200;
    static boolean isPara = false;

    public static void main(String[] args) throws Exception {
        Random random = new Random();
        Task task = new CWTask(new Random(random.nextInt()));
        State initialState = task.getInitialState();

        Experiment exp = new Experiment();

        RankBoostPoolPolicy bp = new RankBoostPoolPolicy(new Random(random.nextInt()));
        bp.setStepsize(1);
        
        exp.conductExperimentTrain(bp, task, 100, 30, initialState, maxStep, isPara, 0, new Random(random.nextInt()));
    }
}
