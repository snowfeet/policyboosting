/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package experiment;

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

        private Rollout rollout;
        private Task task;
        private MetaPolicy mp;
        private State initialState;
        private int maxStep;
        private Random random;
        boolean isStochastic;

        public ParallelExecute(Task task, MetaPolicy mp, State initialState, int maxStep, boolean isStochastic, int seed) {
            this.task = task;
            this.mp = mp;
            this.initialState = initialState;
            this.maxStep = maxStep;
            this.isStochastic = isStochastic;
            this.random = new Random(seed);
        }

        public void run() {
            List<Tuple> samples = Execution.runTask(task, initialState, mp, maxStep, isStochastic, random);
            rollout = new Rollout(task, samples);
        }

        public Rollout getRollout() {
            return rollout;
        }
    }

    public void conductExperimentSingle(MetaPolicy mp, Task task,
            int iteration, int trialsPerTter, State initialState, int maxStep, boolean isPara) {
        for (int iter = 0; iter < iteration; iter++) {
            System.out.println("iter=" + iter);
            System.out.println("collecting samples...");

            List<ParallelExecute> list = new ArrayList<ParallelExecute>();

            ExecutorService exec = Executors.newFixedThreadPool(16);
            for (int i = 0; i < trialsPerTter; i++) {
                ParallelExecute run = new ParallelExecute(task, mp, initialState, maxStep, true, mp.random.nextInt());
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
//                System.out.print(run.getRollout().samples.size() + ",");
            }
            System.out.println();
            System.out.println("collecting samples is done! Updating meta-policy...");
            mp.update(rollouts);
        }
    }

    public void conductTrainAndTest(MetaPolicy mp, TaskSet taskSet, List<Task> testTasks,
            int iteration, int taskPerIter, int trialsPerTask,
            State initialState, int maxStep, boolean isPara, Random random) {
        for (int iter = 0; iter < iteration; iter++) {
            System.out.println("iter=" + iter);
            System.out.println("collecting samples...");

            List<ParallelExecute> list = new ArrayList<ParallelExecute>();

            Task[] tasks = new Task[taskPerIter];
            ExecutorService exec = Executors.newFixedThreadPool(23);
            for (int i = 0; i < taskPerIter; i++) {
                tasks[i] = taskSet.generateTasks();
                for (int j = 0; j < trialsPerTask; j++) {
                    ParallelExecute run = new ParallelExecute(tasks[i], mp, initialState, maxStep, true, random.nextInt());
                    list.add(run);
                    if (isPara) {
                        exec.execute(run);
                    } else {
                        run.run();
                    }
                }
            }
            if (isPara) {
                exec.shutdown();
                try {
                    while (!exec.awaitTermination(10, TimeUnit.SECONDS)) {
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

            int avaStep = 0;
            List<Rollout> rollouts = new ArrayList<Rollout>();
            for (ParallelExecute run : list) {
                rollouts.add(run.getRollout());
                System.out.print(run.rollout.samples.size() + " ");
                avaStep += run.getRollout().samples.size();
            }
            avaStep /= list.size();
            System.out.println("->" + avaStep);

            System.out.println("collecting samples is done! Updating meta-policy...");
            mp.update(rollouts);

            //testing
            List<ParallelExecute> list2 = new ArrayList<ParallelExecute>();
            ExecutorService exec2 = Executors.newFixedThreadPool(23);
            for (int i = 0; i < testTasks.size(); i++) {
                ParallelExecute run = new ParallelExecute(testTasks.get(i), mp, initialState, maxStep, true, random.nextInt());
                list2.add(run);
                if (isPara) {
                    exec2.execute(run);
                } else {
                    run.run();
                }
            }
            if (isPara) {
                exec2.shutdown();
                try {
                    while (!exec2.awaitTermination(10, TimeUnit.SECONDS)) {
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            avaStep = 0;
            for (int i = 0; i < list2.size(); i++) {
                Rollout rollout = list2.get(i).getRollout();
                System.out.print(rollout.samples.size() + " ");
                avaStep += rollout.samples.size();
            }
            avaStep /= list2.size();
            System.out.println("->" + avaStep);
        }
    }

    public void conductExperiment(MetaPolicy mp, TaskSet taskSet,
            int iteration, int taskPerIter, int trialsPerTask,
            State initialState, int maxStep, boolean isPara, Random random) {
        for (int iter = 0; iter < iteration; iter++) {
            System.out.println("iter=" + iter);
            System.out.println("collecting samples...");

            List<ParallelExecute> list = new ArrayList<ParallelExecute>();

            Task[] tasks = new Task[taskPerIter];
            ExecutorService exec = Executors.newFixedThreadPool(23);
            for (int i = 0; i < taskPerIter; i++) {
                tasks[i] = taskSet.generateTasks();
                for (int j = 0; j < trialsPerTask; j++) {
                    ParallelExecute run = new ParallelExecute(tasks[i], mp, initialState, maxStep, true, random.nextInt());
                    list.add(run);
                    if (isPara) {
                        exec.execute(run);
                    } else {
                        run.run();
                    }
                }
            }
            if (isPara) {
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
                System.out.print(run.rollout.samples.size() + "\t");
            }
            System.out.println();

            System.out.println("collecting samples is done! Updating meta-policy...");
            mp.update(rollouts);
        }
    }

    public void conductMultiTaskExperiment(MetaPolicy mp, List<Task> tasks,
            int iteration, int trialsPerTask,
            State initialState, int maxStep, boolean isPara, Random random) {
        int taskPerIter = tasks.size();
        for (int iter = 0; iter < iteration; iter++) {
            System.out.println("iter=" + iter);
            System.out.println("collecting samples...");

            List<ParallelExecute> list = new ArrayList<ParallelExecute>();

            ExecutorService exec = Executors.newFixedThreadPool(23);
            for (int i = 0; i < taskPerIter; i++) {
                Task task = tasks.get(i);
                for (int j = 0; j < trialsPerTask; j++) {
                    ParallelExecute run = new ParallelExecute(task, mp, initialState, maxStep, true, random.nextInt());
                    list.add(run);
                    if (isPara) {
                        exec.execute(run);
                    } else {
                        run.run();
                    }
                }
            }
            if (isPara) {
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

            System.out.println("collecting samples is done! Updating meta-policy...");
            mp.update(rollouts);
        }
    }

    public double[][] conductTesting(MetaPolicy mp, List<Task> tasks,
            State initialState, int maxStep, int bais, boolean isPara, Random random) {
        int numIteration = mp.getNumIteration() - bais;
        List<double[]> resultList = new ArrayList<double[]>();

        double[] avaRewards = new double[tasks.size()];
        for (int iteration = 0; iteration <= numIteration; iteration += 5) {
            mp.setNumIteration(iteration + bais);
            System.out.println("numIteration = " + mp.numIteration);

            List<ParallelExecute> list = new ArrayList<ParallelExecute>();
            ExecutorService exec = Executors.newFixedThreadPool(23);
            for (int i = 0; i < tasks.size(); i++) {
                ParallelExecute run = new ParallelExecute(tasks.get(i), mp, initialState, maxStep, false, random.nextInt());
                list.add(run);
                if (isPara) {
                    exec.execute(run);
                } else {
                    run.run();
                }
            }
            if (isPara) {
                exec.shutdown();
                try {
                    while (!exec.awaitTermination(10, TimeUnit.SECONDS)) {
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

            int avaStep = 0;
            for (int i = 0; i < list.size(); i++) {
                Rollout rollout = list.get(i).getRollout();
                System.out.print(rollout.samples.size() + " ");
                avaStep += rollout.samples.size();
                avaRewards[i] = 0;
                for (Tuple t : rollout.samples) {
                    avaRewards[i] += t.reward;
                }
                avaRewards[i] /= rollout.samples.size();
            }
            avaStep /= list.size();
            System.out.println("->" + avaStep);
            double[] mean_std = Stats.mean_std(avaRewards);
            double[] records = new double[3];
            records[0] = iteration;
            records[1] = mean_std[0];
            records[2] = mean_std[1];
            resultList.add(records);
        }
        mp.setNumIteration(numIteration + bais);

        double[][] results = new double[resultList.size()][];
        for (int i = 0; i < resultList.size(); i++) {
            results[i] = resultList.get(i);
        }
        return results;
    }

    public double[][] conductTesting2(MetaPolicy mp, List<Task> tasks,
            State initialState, int maxStep, int bais, boolean isPara, Random random) {
        List<double[]> resultList = new ArrayList<double[]>();

        double[] avaRewards = new double[tasks.size()];
        for (int iteration = 0; iteration <= 10; iteration += 1) {
            System.out.println("numIteration = " + mp.numIteration);

            List<ParallelExecute> list = new ArrayList<ParallelExecute>();
            ExecutorService exec = Executors.newFixedThreadPool(23);
            for (int i = 0; i < tasks.size(); i++) {
                ParallelExecute run = new ParallelExecute(tasks.get(i), mp, initialState, maxStep, false, random.nextInt());
                list.add(run);
                if (isPara) {
                    exec.execute(run);
                } else {
                    run.run();
                }
            }
            if (isPara) {
                exec.shutdown();
                try {
                    while (!exec.awaitTermination(10, TimeUnit.SECONDS)) {
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

            int avaStep = 0;
            for (int i = 0; i < list.size(); i++) {
                Rollout rollout = list.get(i).getRollout();
                System.out.print(rollout.samples.size() + " ");
                avaStep += rollout.samples.size();
                avaRewards[i] = 0;
                for (Tuple t : rollout.samples) {
                    avaRewards[i] += t.reward;
                }
                avaRewards[i] /= rollout.samples.size();
            }
            avaStep /= list.size();
            System.out.println("->" + avaStep);
            double[] mean_std = Stats.mean_std(avaRewards);
            double[] records = new double[3];
            records[0] = iteration;
            records[1] = mean_std[0];
            records[2] = mean_std[1];
            resultList.add(records);
        }

        double[][] results = new double[resultList.size()][];
        for (int i = 0; i < resultList.size(); i++) {
            results[i] = resultList.get(i);
        }
        return results;
    }
}
