/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package example.acrobot;

import experiment.Execution;
import core.State;
import core.Task;
import domain.acrobot.AcrobotTask;
import experiment.Trajectory;
import experiment.Tuple;
import java.util.List;
import java.util.Random;
import policy.BoostedPolicy;
import policy.RandomPolicy;

/**
 *
 * @author daq
 */
public class TestRandomPolicy {
    static int maxStep = 2000;
    
    public static void main(String[] args) throws Exception {
        Random random = new Random();
        Task task = new AcrobotTask(new Random(random.nextInt()));
        State initialState = task.getInitialState();

        int trials = 1000;
        double mean = 0;
        for (int i = 0; i < trials; i++) {
            Trajectory rollout = Execution.runTaskWithFixedStep(task, initialState, new RandomPolicy(new Random(random.nextInt())), maxStep, true, new Random(random.nextInt()));
            mean += rollout.getSamples().size();
           // System.out.println(rollout.getSamples().size());
        }
        mean /= trials;
        System.out.println(mean);
    }
}
