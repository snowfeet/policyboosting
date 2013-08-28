/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package example.acrobot;

import core.State;
import core.Task;
import domain.acrobot.AcrobotTask;
import domain.mountaincar2d.MountainCarTask;
import experiment.Experiment;
import java.util.Random;
import policy.RankBoostPoolPolicy;

/**
 *
 * @author daq
 */
public class TestRankBoostPoolPolicy {

    static int maxStep = 1000;
    static boolean isPara = false;

    public static void main(String[] args) throws Exception {
        Random random = new Random();
        Task task = new AcrobotTask(new Random(random.nextInt()));
//        Task task = new MountainCarTask(new Random(random.nextInt()));
        State initialState = task.getInitialState();

        Experiment exp = new Experiment();

        RankBoostPoolPolicy bp = new RankBoostPoolPolicy(new Random(random.nextInt()));
        bp.setStepsize(0.005);
//        bp.setStepsize(0.005);
        
        exp.conductExperimentTrain(bp, task, 100, 50, initialState, maxStep, isPara, new Random(random.nextInt()));
    }
}
