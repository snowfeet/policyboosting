/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package example.helicopter;

import core.State;
import core.Task;
import domain.helicopter.HelicopterTask;
import experiment.Execution;
import experiment.Trajectory;
import java.util.Random;
import policy.RandomCAPolicy;

/**
 *
 * @author daq
 */
public class TestRandomPolicy {

    static int maxStep = 6000 ;

    public static void main(String[] args) throws Exception {
        Random random = new Random();
        Task task = new HelicopterTask(new Random(random.nextInt()));
        State initialState = task.getInitialState();

        int trials = 100;
        double mean = 0;
        for (int i = 0; i < trials; i++) {
            Trajectory rollout = Execution.runTaskWithFixedStep(task, initialState, new RandomCAPolicy(new Random(random.nextInt())), maxStep, true, new Random(random.nextInt()));
            mean += rollout.getRewards();
            System.out.println(rollout.getSamples().size());
        }
        mean /= trials;
        System.out.println(mean);
    }
}
