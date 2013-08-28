/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package policy;

import core.Action;
import core.GibbsPolicy;
import core.PrabAction;
import core.State;
import core.Task;
import experiment.Trajectory;
import experiment.Tuple;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import weka.classifiers.Classifier;
import weka.classifiers.trees.REPTree;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author daq
 */
public class RankBoostMMPolicy extends GibbsPolicy {

    private List<Double>[] alphas;
    private List<Classifier>[] potentialFunctions;
    private Classifier base;
    private double stepsize;
    private Instances dataHead = null;

    public RankBoostMMPolicy(Random rand) {
        random = rand;
        numIteration = 0;

        REPTree tree = new REPTree();
        tree.setMaxDepth(100);

        base = tree;
        stepsize = 1;
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
    public Action makeDecisionD(State s, Task t, Random outRand) {
        if (numIteration == 0) {
            return null;
        }

        Random thisRand = outRand == null ? random : outRand;
        int K = t.actions.length;

        double[] utilities = getProbability(s, t);
        int bestAction = 0, m = 2;
        for (int k = 1; k < K; k++) {
            if (utilities[k] > utilities[bestAction] + Double.MIN_VALUE) {
                bestAction = k;
                m = 2;
            } else if (Math.abs(utilities[k] - utilities[bestAction]) <= Double.MIN_VALUE) {
                if (thisRand.nextDouble() < 1.0 / m) {
                    bestAction = k;
                }
                m++;
            }
        }

        return new Action(bestAction);
    }

    @Override
    public PrabAction makeDecisionS(State s, Task t, Random outRand) {
        if (numIteration == 0) {
            return null;
        }

        Random thisRand = outRand == null ? random : outRand;
        int K = t.actions.length;

        double[] utilities = getProbability(s, t);
        return makeDecisionS(s, t, utilities, thisRand);
    }

    public PrabAction makeDecisionS(State s, Task t, double[] utilities, Random outRand) {
        if (numIteration == 0 || utilities == null) {
            return null;
        }

        Random thisRand = outRand == null ? random : outRand;
        int K = t.actions.length;

        int bestAction = 0, m = 2;
        for (int k = 1; k < K; k++) {
            if (utilities[k] > utilities[bestAction] + Double.MIN_VALUE) {
                bestAction = k;
                m = 2;
            } else if (Math.abs(utilities[k] - utilities[bestAction]) <= Double.MIN_VALUE) {
                if (thisRand.nextDouble() < 1.0 / m) {
                    bestAction = k;
                }
                m++;
            }
        }

        return new PrabAction(bestAction, utilities[bestAction]);
    }

    @Override
    public double[] getUtility(State s, Task t) {
        int A = t.actions.length;
        double[] utilities = new double[A];

        for (int k = 0; k < A; k++) {
            double[] stateFeature = s.getfeatures();
            Instance ins = contructInstance(stateFeature, 0);
            ins.setDataset(dataHead);
            utilities[k] = 1;
            for (int j = 0; j < numIteration; j++) {
                try {
                    utilities[k] += alphas[k].get(j) * potentialFunctions[k].get(j).classifyInstance(ins);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        return utilities;
    }

    private Instance contructInstance(double[] stateFeature, double label) {
        int D = stateFeature.length;
        double[] values = new double[D + 1];
        System.arraycopy(stateFeature, 0, values, 0, D);
        values[D] = label;
        Instance ins = new Instance(1.0, values);
        return ins;
    }

    public Instances constructDataHead(int D) {
        FastVector attInfo_x = new FastVector();
        for (int i = 0; i < D; i++) {
            attInfo_x.addElement(new Attribute("att_" + i, i));
        }

        attInfo_x.addElement(new Attribute("class", D));
        Instances data = new Instances("dataHead", attInfo_x, 0);
        data.setClassIndex(data.numAttributes() - 1);
        return data;
    }

    class ParallelTrain implements Runnable {

        private Classifier c;
        private Instances data;

        public ParallelTrain(Classifier c, Instances data) {
            this.c = c;
            this.data = data;
        }

        public void run() {
            try {
                c.buildClassifier(data);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public Classifier getC() {
            return c;
        }
    }

    @Override
    public void update(List<Trajectory> rollouts) {
        int A = rollouts.get(0).getTask().actions.length;

        if (potentialFunctions == null) {
            potentialFunctions = new ArrayList[A];
            alphas = new ArrayList[A];
            for (int k = 0; k < A; k++) {
                potentialFunctions[k] = new ArrayList<Classifier>();
                alphas[k] = new ArrayList<Double>();
            }
        }

        double[][] ratios = new double[rollouts.size()][];

        int numZ = rollouts.size();
        double RZ = 0, tildeP = 0;
        for (int i = 0; i < rollouts.size(); i++) {
            Trajectory rollout = rollouts.get(i);
            RZ += rollout.getRZ();
            ratios[i] = compuate_P_z_of_R_z(rollout);
            tildeP += ratios[i][0];
        }

        ArrayList<ArrayList<double[]>> features = new ArrayList<ArrayList<double[]>>();//same features for all model
        List<Double>[] labels = new List[A]; // different labels for different model, so an array needed here
        for (int k = 0; k < A; k++) {
            labels[k] = new ArrayList<Double>();
            features.add(new ArrayList<double[]>());
        }

        double max_abs_label = -1;
        for (int i = 0; i < rollouts.size(); i++) {
            Trajectory rollout = rollouts.get(i);
            Task task = rollout.getTask();
            List<Tuple> samples = rollout.getSamples();

            double R_z = rollout.getRewards();

            double mean_r_z = R_z / rollout.getSamples().size();
            double accumulated_rewards_sofar = 0;
            for (int step = 0; step < samples.size(); step++) {
                Tuple sample = samples.get(step);

                double prab = ((PrabAction) sample.action).probability;
                double tilde_R_z = R_z - accumulated_rewards_sofar, tilde_RZ = RZ;

                // 补偿rewards带来的修改
                if (rollout.isIsSuccess()) {
                    tilde_R_z += step * samples.get(samples.size() - 1).reward;
                } else {
                    tilde_R_z += step * mean_r_z;
                }
                //tilde_RZ += (tilde_R_z - R_z);

                double labelConstant = (ratios[i][step] * (numZ * tilde_R_z - tilde_RZ) / prab + (ratios[i][step] * numZ - tildeP) * sample.reward);


                for (int k = 0; k < A; k++) {
                    if (sample.action.a == k) {
                        features.get(k).add(sample.s.getfeatures());
                        
                        double label = labelConstant * prab * (1 - prab);
                        labels[k].add(label);

                        if (Math.abs(label) > max_abs_label) {
                            max_abs_label = Math.abs(label);
                        }
                    }
                }

                accumulated_rewards_sofar += sample.reward;
            }
        }

        if (null == dataHead) {
            dataHead = constructDataHead(features.get(0).get(0).length);
        }

        // collect examples for regression
        Instance[][] dataTmp = new Instance[A][];
        for (int k = 0; k < A; k++) {
            dataTmp[k] = new Instance[features.get(k).size()];
            for (int i = 0; i < features.get(k).size(); i++) {
                dataTmp[k][i] = contructInstance(features.get(k).get(i), labels[k].get(i) / max_abs_label);
            }
        }

        Instances[] dataTrain = new Instances[A];
        for (int k = 0; k < A; k++) {
            dataTrain[k] = new Instances(dataHead, dataTmp[k].length);
            for (Instance ins : dataTmp[k]) {
                dataTrain[k].add(ins);
            }
        }

        // parallel train
        ExecutorService exec = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors() - 1);
        List<ParallelTrain> rList = new ArrayList<ParallelTrain>();
        for (int k = 0; k < A; k++) {
            //ParallelTrain run = new ParallelTrain(getBaseLearner(), tmpData[k]);
            ParallelTrain run = new ParallelTrain(getBaseLearner(), dataTrain[k]);
            rList.add(run);
            exec.execute(run);
        }

        exec.shutdown();
        try {
            while (!exec.awaitTermination(10, TimeUnit.SECONDS)) {
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        for (int k = 0; k < A; k++) {
            int t = alphas[k].size() + 1;
            alphas[k].add(stepsize / Math.sqrt(t));
            potentialFunctions[k].add(rList.get(k).getC());
        }

        numIteration++;
    }

    public double getStepsize() {
        return stepsize;
    }

    public void setStepsize(double stepsize) {
        this.stepsize = stepsize;
    }

    @Override
    public void setNumIteration(int numIteration) {
        this.numIteration = Math.min(potentialFunctions[0].size(), numIteration);
    }

    private double[] compuate_P_z_of_R_z(Trajectory rollout) {
        boolean flag = numIteration < 0;
        if (flag) {
            System.out.println(rollout.getRewards());
            int x = 1;
        }

        int T = rollout.getSamples().size();
        double[] P_z = new double[T];
        double[] D_z = new double[T];
        double[] R_z = new double[T];

        for (int i = 0; i < T; i++) {
            Tuple tuple = rollout.getSamples().get(i);
            double[] probabilities = getProbability(tuple.s, rollout.getTask());
            P_z[i] = probabilities[tuple.action.a];
            D_z[i] = ((PrabAction) tuple.action).probability;
        }

        // to dealwith the overflow problem of r = (P_z[1]*P_z[2]*...P_z[T-1]) / (D_z[1]*D_z[2]*...D_z[T-1])
        // by calculating r = exp(\sum log(P_z[i]) - \sum log(D_z[i]))

        double sumP = 0, sumD = 0;
        for (int i = T - 1; i >= 0; i--) {
            sumP += Math.log(P_z[i]);
            sumD += Math.log(D_z[i]);

            R_z[i] = Math.exp(sumP - sumD);
            if (flag) {
                System.out.println(P_z[i] + "\t" + D_z[i] + "\t" + R_z[i]);
            }
        }

        if (flag) {
            System.out.println(R_z[0]);
            System.exit(1);
        }
        return R_z;
    }
}
