/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package example.acrobot;

import experiment.Execution;
import core.State;
import core.Task;
import domain.acrobot.AcrobotTask;
import experiment.Rollout;
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
    static int maxStep = 5000;
    
    public static void main(String[] args) throws Exception {
        Random random = new Random();
        Task task = new AcrobotTask(new Random(random.nextInt()));
        State initialState = task.getInitialState();

        int trials = 2000;
        double mean = 0;
        for (int i = 0; i < trials; i++) {
            Rollout rollout = Execution.runTaskWithFixedStep(task, initialState, new RandomPolicy(new Random(random.nextInt())), maxStep, true, new Random(random.nextInt()));
            double t = 0;
            for (Tuple tuple : rollout.getSamples()) {
                t += tuple.reward;
            }
            mean += t;
            System.out.println(rollout.getSamples().size() + "\t" + t);
        }
        mean /= trials;
        System.out.println(mean);
    }
}
