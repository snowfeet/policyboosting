/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package example.mountaincar2d;

import experiment.Execution;
import core.State;
import core.Task;
import domain.mountaincar2d.MountainCarTask;
import experiment.Trajectory;
import experiment.Tuple;
import java.util.Random;
import policy.RandomPolicy;

/**
 *
 * @author daq
 */
public class TestRandomPolicy {

    static int maxStep = 4000;

    public static void main(String[] args) throws Exception {
        Random random = new Random();
        Task task = new MountainCarTask(new Random(random.nextInt()));
        State initialState = task.getInitialState();

        int trials = 50;
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
