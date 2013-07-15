/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package experiment;

import core.EpsionGreedyExplorePolicy;
import core.Policy;
import core.State;
import core.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author daq
 */
public class Experiment {

    class ParallelExecute implements Runnable {

        private Rollout rollout;
        private Task task;
        private Policy policy;
        private State initialState;
        private int maxStep;
        private Random random;
        boolean isStochastic;

        public ParallelExecute(Task task, Policy policy, State initialState,
                int maxStep, boolean isStochastic, int seed) {
            this.task = task;
            this.policy = policy;
            this.initialState = initialState;
            this.maxStep = maxStep;
            this.isStochastic = isStochastic;
            this.random = new Random(seed);
        }

        public void run() {
            List<Tuple> samples = Execution.runTaskWithFixedStep(task,
                    initialState, policy, maxStep, isStochastic, random);
            rollout = new Rollout(task, samples);
        }

        public Rollout getRollout() {
            return rollout;
        }
    }

    public void conductExperimentTrain(Policy policy, Task task,
            int iteration, int trialsPerIter, State initialState, int maxStep,
            boolean isPara, Random random) {
        for (int iter = 0; iter < iteration; iter++) {
            System.out.println("iter=" + iter);
            System.out.println("collecting samples...");

            Policy explorePolicy = new EpsionGreedyExplorePolicy(policy, 0.05, new Random(random.nextInt()));
            List<ParallelExecute> list = new ArrayList<ParallelExecute>();

            ExecutorService exec = Executors.newFixedThreadPool(
                    Runtime.getRuntime().availableProcessors() - 1);
            for (int i = 0; i < trialsPerIter; i++) {
                ParallelExecute run = new ParallelExecute(task, explorePolicy,
                        initialState, maxStep, true, random.nextInt());
                list.add(run);
                if (isPara && iter > 0) {
                    exec.execute(run);
                } else {
                    run.run();
                }
            }
            if (isPara && iter > 0) {
                exec.shutdown();
                try {
                    while (!exec.awaitTermination(10, TimeUnit.SECONDS)) {
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

            List<Rollout> rollouts = new ArrayList<Rollout>();
            for (ParallelExecute run : list) {
                rollouts.add(run.getRollout());
            }
            System.out.println();
            System.out.println("collecting samples is done! Updating meta-policy...");
            policy.update(rollouts);
        }
    }
}
