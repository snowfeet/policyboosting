/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package experiment;

import core.EpsionGreedyExplorePolicy;
import core.ExploreCAPolicy;
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
import utills.Stats;

/**
 *
 * @author daq
 */
public class Experiment {

    class ParallelExecute implements Runnable {

        private Trajectory rollout;
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

        public Trajectory getRollout() {
            return rollout;
        }
    }

    public double[][] conductExperimentTrain(Policy policy, Task task,
            int iteration, int trialsPerIter, State initialState, int maxStep,
            boolean isPara, double epsion, Random random) {
        double[][] results = new double[iteration][5];
        List<Trajectory> rolloutsFirst = null;
        for (int iter = 0; iter < iteration; iter++) {
            results[iter][0] = iter;
            System.out.print("iter=" + iter + ", ");

            Policy explorePolicy = new EpsionGreedyExplorePolicy(policy, epsion, new Random(random.nextInt()));
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

            List<Trajectory> rollouts = new ArrayList<Trajectory>();
            double[] rewards = new double[list.size()];
            double[] steps = new double[list.size()];
            int cc = 0;
            for (ParallelExecute run : list) {
                Trajectory rollout = run.getRollout();
                rollout.setProducedIteration(iter);
                rollouts.add(rollout);

                double totalReward = rollout.getRewards();
                rewards[cc] = totalReward;
                steps[cc] = rollout.getSamples().size();
                cc++;
            }
            double[] meanStdReward = Stats.mean_std(rewards);
            double[] meanStdStep = Stats.mean_std(steps);
            results[iter][1] = meanStdReward[0];
            results[iter][2] = meanStdReward[1];
            results[iter][3] = meanStdStep[0];
            results[iter][4] = meanStdStep[1];
            System.out.println("Average Total Rewards = " + meanStdReward[0] + ", Average step = " + meanStdStep[0]);

//            if (iter == 0) {
//                rolloutsFirst = rollouts;
//            } else {
//                rollouts = rolloutsFirst;
//            }

            policy.update(rollouts);

//            ParallelExecute run = new ParallelExecute(task, policy,
//                    initialState, maxStep, true, random.nextInt());
//            run.run();
//            Trajectory rollout = run.getRollout();
//            System.err.println("DDDD " + rollout.getRewards());
        }

        return results;
    }

    public double[][] conductExperimentTrainCA(Policy policy, Task task,
            int iteration, int trialsPerIter, State initialState, int maxStep,
            boolean isPara, Random random) {
        double[][] results = new double[iteration][5];
        List<Trajectory> rolloutsFirst = null;
        for (int iter = 0; iter < iteration; iter++) {
            System.out.print("iter=" + iter + ", ");
            //  System.out.println("collecting samples...");

            Policy explorePolicy = new ExploreCAPolicy(policy, 0.0, new Random(random.nextInt()));
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

            List<Trajectory> rollouts = new ArrayList<Trajectory>();
            double[] rewards = new double[list.size()];
            double[] steps = new double[list.size()];
            int cc = 0, maxStepUsed = -1;
            for (ParallelExecute run : list) {
                Trajectory rollout = run.getRollout();
                rollout.setProducedIteration(iter);
                rollouts.add(rollout);

                double totalReward = rollout.getRewards();
                rewards[cc] = totalReward;
                steps[cc] = rollout.getSamples().size();

                if (steps[cc] > maxStepUsed) {
                    maxStepUsed = rollout.getSamples().size();
                }

                cc++;
            }
            double[] meanStdReward = Stats.mean_std(rewards);
            double[] meanStdStep = Stats.mean_std(steps);
            results[iter][1] = meanStdReward[0];
            results[iter][2] = meanStdReward[1];
            results[iter][3] = meanStdStep[0];
            results[iter][4] = meanStdStep[1];
            System.out.println("Average Total Rewards = " + meanStdReward[0] + ", Average step = " + meanStdStep[0] + "(" + maxStepUsed + ")");
            
//            if(meanStdStep[0] > 6500)
//                break;
            // System.out.println();
            // System.out.println("collecting samples is done! Updating meta-policy...");

//            if (iter == 0) {
//                rolloutsFirst = rollouts;
//            } else {
//                rollouts = rolloutsFirst;
//            }

            policy.update(rollouts);

//            ParallelExecute run = new ParallelExecute(task, policy,
//                    initialState, maxStep, true, random.nextInt());
//            run.run();
//            Trajectory rollout = run.getRollout();
//            System.err.println("DDDD " + rollout.getRewards());         
        }

        return results;
    }

    public static double[] calcRolloutObjective(Trajectory rollout, GibbsPolicy policy) {
        double[] obj = new double[3];

        double log_P_pi_z = 0;

        double R_pi_z = 0;
        for (Tuple sample : rollout.getSamples()) {
            double[] probability = policy.getProbability(sample.s, rollout.getTask());
            R_pi_z += sample.reward * probability[sample.action.a];
            log_P_pi_z += Math.log(probability[sample.action.a]);
        }
        obj[0] = Math.exp(log_P_pi_z);
        obj[1] = R_pi_z;
        obj[2] = obj[0] * R_pi_z;

        return obj;
    }

    public static double[] calcObjective(List<Trajectory> rollouts, GibbsPolicy policy) {
        double[] objective = new double[3];

        for (Trajectory rollout : rollouts) {
            double[] obj = calcRolloutObjective(rollout, policy);
            for (int i = 0; i < obj.length; i++) {
                System.err.print(obj[i] + ",");
                objective[i] += obj[i];
            }
            System.err.println();
        }

        return objective;
    }
}
