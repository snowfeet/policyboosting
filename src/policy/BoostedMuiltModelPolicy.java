/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package policy;

import core.Action;
import core.Policy;
import core.PrabAction;
import core.State;
import core.Task;
import experiment.Rollout;
import experiment.Tuple;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import utills.IO;
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
public class BoostedMuiltModelPolicy extends Policy {

    private List<Double>[] alphas;
    private List<Classifier>[] potentialFunctions;
    private Classifier base;
    private double stepsize;
    private Instances dataHead = null;

    public BoostedMuiltModelPolicy(Random rand) {
        numIteration = 0;
        random = rand;
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

        double[] utilities = getUtility(s, t);
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

        double[] utilities = getUtility(s, t);
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

    public double[] getProbability(double[] utilities) {
        double[] probabilities = new double[utilities.length];
        double maxUtility = Double.NEGATIVE_INFINITY;
        for (int k = 0; k < utilities.length; k++) {
            if (utilities[k] > maxUtility) {
                maxUtility = utilities[k];
            }
        }

        double norm = 0;
        for (int k = 0; k < utilities.length; k++) {
            probabilities[k] = Math.exp(utilities[k] - maxUtility + 10);
            norm += probabilities[k];
        }


        for (int k = 0; k < probabilities.length; k++) {
            probabilities[k] /= norm;
        }

        return utilities;
    }

    public double[] getUtility(State s, Task t) {
        int K = t.actions.length;
        double[] utilities = new double[K];

        for (int k = 0; k < K; k++) {
            double[] stateActionFeature = t.getSAFeature(s, new Action(k));
            Instance ins = contructInstance(stateActionFeature, 0);
            if (null == dataHead) {
                dataHead = constructDataHead(stateActionFeature.length, K);
            }
            ins.setDataset(dataHead);
            utilities[k] = 0;
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

    private Instance contructInstance(double[] stateActionTaskFeature, double label) {
        int D = stateActionTaskFeature.length;
        double[] values = new double[D + 1];
        values[D] = label;
        System.arraycopy(stateActionTaskFeature, 0, values, 0, D);
        Instance ins = new Instance(1.0, values);
        return ins;
    }

    public Instances constructDataHead(int D, int na) {
        FastVector attInfo_x = new FastVector();
        for (int i = 0; i < D - 1; i++) {
            attInfo_x.addElement(new Attribute("att_" + i, i));
        }

        FastVector att = new FastVector(na);
        for (int i = 0; i < na; i++) {
            att.addElement("" + i);
        }
        attInfo_x.addElement(new Attribute("action", att, D - 1));

        attInfo_x.addElement(new Attribute("class", D));
        Instances data = new Instances("dataHead", attInfo_x, 0);
        data.setClassIndex(data.numAttributes() - 1);
        return data;
    }

    @Override
    public void update(List<Rollout> rollouts) {
        int A = rollouts.get(0).getTask().actions.length;

        if (potentialFunctions == null) {
            potentialFunctions = new List[A];
            alphas = new List[A];
            for (int i = 0; i < A; i++) {
                potentialFunctions[i] = new ArrayList<Classifier>();
                alphas[i] = new ArrayList<Double>();
            }
        }

        List<double[]> features = new ArrayList<double[]>();
        List<Double> labels = new ArrayList<Double>();

        for (Rollout rollout : rollouts) {
            Task task = rollout.getTask();
            List<Tuple> samples = rollout.getSamples();

            double[] ratio = compuate_P_z_of_R_z(rollout);
            double R_z = rollout.getRewards();

            for (int step = 0; step < samples.size(); step++) {
                Tuple sample = samples.get(step);

                features.add(task.getSAFeature(sample.s, sample.action));
                double prab = ((PrabAction) sample.action).probability;
                double label = ratio[step] * R_z * (1 + sample.reward / (R_z + 0.5)) * prab * (1 - prab);
                labels.add(label);

                R_z -= sample.reward;
            }
        }

        if (null == dataHead) {
            int na = rollouts.get(0).getTask().actions.length;
            dataHead = constructDataHead(features.get(0).length, na);
        }

        Instances data = new Instances(dataHead, features.size());
        for (int i = 0; i < features.size(); i++) {
            Instance ins = contructInstance(features.get(i), labels.get(i));
            data.add(ins);
        }

        //IO.saveInstances("data/data" + numIteration + ".arff", data);

        Classifier c = getBaseLearner();
        try {
            c.buildClassifier(data);
        } catch (Exception ex) {
            ex.printStackTrace();
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

    private double[] compuate_P_z_of_R_z(Rollout rollout) {
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
            double[] utilities = getUtility(tuple.s, rollout.getTask());
            P_z[i] = utilities[tuple.action.a];
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
