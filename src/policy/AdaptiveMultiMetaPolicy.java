/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package policy;

import core.Action;
import experiment.Execution;
import core.MetaPolicy;
import experiment.Rollout;
import core.State;
import core.Task;
import core.TaskSet;
import experiment.Tuple;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author daq
 */
public class AdaptiveMultiMetaPolicy extends MetaPolicy {

    int K;
    double epsion;
    double ratio;
    GBMetaPolicy[] policies;
    List<Classifier> useWhichPolicy;
    Instances taskDataHead;
    Instances taskData;
    Classifier base;

    public AdaptiveMultiMetaPolicy(int K, double epsion, double ratio, Random rand) {
        this.random = rand;
        this.K = K;
        this.epsion = epsion;
        this.ratio = ratio;
        policies = new GBMetaPolicy[K];
        for (int k = 0; k < K; k++) {
            policies[k] = new GBMetaPolicy(new Random(rand.nextInt()));
        }
        useWhichPolicy = new ArrayList<Classifier>();
        RandomForest rt = new RandomForest();
        rt.setSeed(0);
        base = rt;
    }

    @Override
    public void setNumIteration(int numIteration) {
        this.numIteration = numIteration;
        for (int k = 0; k < K; k++) {
            policies[k].setNumIteration(numIteration);
        }
    }

    public Classifier getBaseLearner() {
        Classifier c = null;
        try {
            c = Classifier.makeCopy(base);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return c;
    }

    @Override
    public Action makeDecisionD(State s, Task t, Random random) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    class Element implements Comparable<Element> {

        public int i, j;
        public double v;

        public Element(int i, int j, double v) {
            this.i = i;
            this.j = j;
            this.v = v;
        }

        @Override
        public int compareTo(Element e) {
            return -Double.compare(v, e.v);
        }
    }

    public void train(TaskSet taskSet, int iteration, int taskPerIter, int trialsPerTask,
            State initialState, int maxStep, boolean isPara, Random random) {
        for (int iter = 0; iter < iteration; iter++) {
            System.out.println("iter=" + iter);
            System.out.println("collecting samples...");

            Task[] tasks = new Task[taskPerIter];
            for (int i = 0; i < taskPerIter; i++) {
                tasks[i] = taskSet.generateTasks();
            }

            ParallelExecute[][][] runs = new ParallelExecute[taskPerIter][K][trialsPerTask];
            ExecutorService exec = Executors.newFixedThreadPool(23);
            for (int i = 0; i < taskPerIter; i++) {
                for (int k = 0; k < K; k++) {
                    for (int j = 0; j < trialsPerTask; j++) {
                        runs[i][k][j] = new ParallelExecute(tasks[i], policies[k], initialState, maxStep, random.nextInt());
                        if (isPara) {
                            exec.execute(runs[i][k][j]);
                        } else {
                            runs[i][k][j].run();
                        }
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

            System.out.println("collecting samples is done! Updating meta-policy...");

            if (K == 1) {
                List<Rollout> rollouts = new ArrayList<Rollout>();
                for (int i = 0; i < taskPerIter; i++) {
                    for (int j = 0; j < trialsPerTask; j++) {
                        rollouts.add(runs[i][0][j].getRollout());
                        System.out.print(runs[i][0][j].getRollout().samples.size()+" ");
                    }
                    System.out.println();
                }
                policies[0].update(rollouts);
            } else {
                List<Element> elements = new ArrayList<Element>();
                double[][] rewards = new double[taskPerIter][K];
                for (int i = 0; i < taskPerIter; i++) {
                    for (int k = 0; k < K; k++) {
                        rewards[i][k] = 0;
                        for (int j = 0; j < trialsPerTask; j++) {
                            Rollout rollout = runs[i][k][j].getRollout();
                            rewards[i][k] += rollout.getAvaReward();
                        }
                        rewards[i][k] /= trialsPerTask;
                        elements.add(new Element(i, k, rewards[i][k]));
                    }
                }

                int[] selectedPolicy = new int[K];
                int[] policyIndex = new int[K];
                int[] selectedTask = new int[taskPerIter];
                for (int i = 0; i < K; i++) {
                    selectedPolicy[i] = -1;
                    policyIndex[i] = -1;
                }
                for (int i = 0; i < taskPerIter; i++) {
                    selectedTask[i] = -1;
                }

                // select task for policy
                Collections.sort(elements);
                int m = 0, ind = 0;;
                Element elem = elements.get(m);
                while (m < taskPerIter * K * ratio) {
                    while (selectedTask[elem.i] != -1 || selectedPolicy[elem.j] != -1) {
                        m = m + 1;
                        elem = elements.get(m);
                    }

                    boolean flag = true;
                    for (int j = 0; j < ind; j++) {
                        double diff = elem.v - rewards[selectedPolicy[policyIndex[j]]][policyIndex[j]];
                        if (diff < -epsion) {
                            flag = false;
                            break;
                        }
                    }

                    if (flag) {
                        selectedTask[elem.i] = elem.j;
                        selectedPolicy[elem.j] = elem.i;
                        policyIndex[ind++] = elem.j;
                    }

                    if (ind == K) {
                        break;
                    }

                    m++;
                }
                System.out.print(K + "-" + ind + " ");
//                for (int i = 0; i < ind; i++) {
//                    System.out.print(policyIndex[i] + "(" + String.format("%.3f", rewards[selectedPolicy[policyIndex[i]]][policyIndex[i]]) + ")" + " ");
//                }
                for (int i = 0; i < ind; i++) {
                    System.out.print(policyIndex[i] + "(" + String.format("%.3f", 1/(1+rewards[selectedPolicy[policyIndex[i]]][policyIndex[i]])) + ")" + " ");
                }
                System.out.println(m);

                for (int i = 0; i < taskPerIter; i++) {
                    if (selectedTask[i] == -1) {
                        double minDist = Double.MAX_VALUE;
                        int minID = -1;
                        for (int k = 0; k < K; k++) {
                            if (selectedPolicy[k] == -1) {
                                continue;
                            }
                            double[] fea1 = rewards[i];
                            double[] fea2 = rewards[selectedPolicy[k]];
                            double dist = calculateTaskDist(fea1, fea2);
                            if (dist < minDist) {
                                minDist = dist;
                                minID = k;
                            }
                        }
                        selectedTask[i] = minID;
                    }
                }

                exec = Executors.newFixedThreadPool(K);
                for (int k = 0; k < K; k++) {
                    if (selectedPolicy[k] == -1) {
                        continue;
                    }
                    List<Rollout> rollouts = new ArrayList<Rollout>();
                    for (int i = 0; i < taskPerIter; i++) {
                        if (selectedTask[i] == k) {
                            for (int j = 0; j < trialsPerTask; j++) {
                                rollouts.add(runs[i][k][j].getRollout());
                            }
                        }
                    }
                    ParallelTrain train = new ParallelTrain(rollouts, policies[k]);
                    exec.execute(train);
                }
                exec.shutdown();
                try {
                    while (!exec.awaitTermination(10, TimeUnit.SECONDS)) {
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

                if (null == taskDataHead) {
                    taskDataHead = constructDataHeadK(tasks[0].taskDimension, K);
                    taskData = new Instances(taskDataHead, taskPerIter);
                }

                for (int k = 0; k < K; k++) {
                    if (selectedPolicy[k] == -1) {
                        continue;
                    }
                    for (int i = 0; i < taskPerIter; i++) {
                        if (selectedTask[i] == k) {
                            Instance ins = contructInstance(tasks[i].taskParameter, k);
                            taskData.add(ins);
                        }
                    }
                }

                Classifier c = getBaseLearner();
                try {
                    c.buildClassifier(taskData);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                useWhichPolicy.add(c);
            }

            numIteration++;
        }
    }

    private double calculateTaskDist(double[] fea1, double[] fea2) {
        double dist = 0;
        for (int i = 0; i < fea1.length; i++) {
            dist += (fea1[i] - fea2[i]) * (fea1[i] - fea2[i]);
        }
        return Math.sqrt(dist);
    }

    public Instances constructDataHeadK(int D, int K) {
        FastVector attInfo_x = new FastVector();
        for (int i = 0; i < D; i++) {
            attInfo_x.addElement(new Attribute("att_" + i, i));
        }

        FastVector clsvalues = new FastVector(K);
        for (int k = 0; k < K; k++) {
            clsvalues.addElement("" + k);
        }
        Attribute classatt = new Attribute("class", clsvalues, D);
        attInfo_x.addElement(classatt);

        Instances data = new Instances("dataHead", attInfo_x, 0);
        data.setClassIndex(data.numAttributes() - 1);
        return data;
    }

    private Instance contructInstance(double[] feature, double label) {
        int D = feature.length;
        double[] values = new double[D + 1];
        values[D] = label;
        System.arraycopy(feature, 0, values, 0, D);
        Instance ins = new Instance(1.0, values);
        return ins;
    }

    @Override
    public Action makeDecisionS(State s, Task t, Random random) {
        try {
            if (numIteration == 0) {
                return policies[random.nextInt(K)].makeDecisionS(s, t, random);
            } else {
                if (K == 1) {
                    return policies[0].makeDecisionS(s, t, random);
                } else {
                    Instance ins = contructInstance(t.taskParameter, Double.NaN);
                    ins.setDataset(taskDataHead);

                    int k = (int) useWhichPolicy.get(numIteration - 1).classifyInstance(ins);
                    return policies[k].makeDecisionS(s, t, random);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void update(List<Rollout> rollouts) {
    }

    public void setStepsize(double d) {
        for (GBMetaPolicy mp : policies) {
            mp.setStepsize(d);
        }
    }

    public GBMetaPolicy getTaskPolicy(Task t) {
        if (K == 1) {
            return policies[0];
        }
        Instance ins = contructInstance(t.taskParameter, Double.NaN);
        ins.setDataset(taskDataHead);
        int k = -1;
        try {
            k = (int) useWhichPolicy.get(numIteration - 1).classifyInstance(ins);
            System.out.print(k + ":");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return policies[k];
    }

    class ParallelExecute implements Runnable {

        private Rollout rollout;
        private Task task;
        private MetaPolicy mp;
        private State initialState;
        private int maxStep;
        private Random random;

        public ParallelExecute(Task task, MetaPolicy mp, State initialState, int maxStep, int seed) {
            this.task = task;
            this.mp = mp;
            this.initialState = initialState;
            this.maxStep = maxStep;
            this.random = new Random(seed);
        }

        public void run() {
            List<Tuple> samples = Execution.runTask(task, initialState, mp, maxStep,true, random);
            rollout = new Rollout(task, samples);
        }

        public Rollout getRollout() {
            return rollout;
        }
    }

    class ParallelTrain implements Runnable {

        private List<Rollout> rollouts;
        private GBMetaPolicy gbmp;

        public ParallelTrain(List<Rollout> rollouts, GBMetaPolicy gbmp) {
            this.rollouts = rollouts;
            this.gbmp = gbmp;
        }

        public void run() {
            gbmp.update(rollouts);
        }
    }
}
