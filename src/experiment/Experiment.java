/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package experiment;

import core.EpsionGreedyExplorePolicy;
import core.GibbsPolicy;
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
            rollout = Execution.runTaskWithFixedStep(task,
                    initialState, policy, maxStep, isStochastic, random);
        }

        public Rollout getRollout() {
            return rollout;
        }
    }

    public void conductExperimentTrain(Policy policy, Task task,
            int iteration, int trialsPerIter, State initialState, int maxStep,
            boolean isPara, Random random) {
        for (int iter = 0; iter < iteration; iter++) {
            System.out.print("iter=" + iter + ", ");
            //  System.out.println("collecting samples...");

            Policy explorePolicy = new EpsionGreedyExplorePolicy(policy, 0.1, new Random(random.nextInt()));
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
            double averageReward = 0;
            for (ParallelExecute run : list) {
                Rollout rollout = run.getRollout();
                rollouts.add(rollout);

                double totalReward = rollout.getRewards();
                averageReward += totalReward;
                //    System.out.print(totalReward+" ");                
            }
            averageReward /= list.size();
            System.out.println("Average Total Rewards = " + averageReward);
            // System.out.println();
            // System.out.println("collecting samples is done! Updating meta-policy...");
            policy.update(rollouts);
        }
    }
    
    public static double calcRolloutObjective(Rollout rollout, GibbsPolicy policy){       
        double R_pi_z = 0;
        for(Tuple sample : rollout.getSamples()){
            double[] probability = policy.getProbability(sample.s, rollout.getTask());
            R_pi_z += sample.reward * probability[sample.action.a];
        }
        
        return R_pi_z;
    }
    
    public static  double calcObjective(List<Rollout> rollouts, GibbsPolicy policy){
        double objective = 0;
        
        for(Rollout rollout : rollouts)
            objective += calcRolloutObjective(rollout, policy);
        return objective;
    }
}
