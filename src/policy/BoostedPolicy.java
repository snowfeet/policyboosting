/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package policy;

import core.Action;
import core.Policy;
import core.PrabAction;
import experiment.Rollout;
import core.State;
import core.Task;
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
public class BoostedPolicy extends Policy {

    private RandomPolicy rp;
    private List<Double> alphas;
    private List<Classifier> potentialFunctions;
    private Classifier base;
    private double stepsize;
    private Instances dataHead = null;

    public BoostedPolicy(Random rand) {
        rp = new RandomPolicy(new Random());
        numIteration = 0;
        alphas = new ArrayList<Double>();
        potentialFunctions = new ArrayList<Classifier>();
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

        int K = t.actions.length;

        double[] utilities = getUtility(s, t);

        int bestAction = 0;
        for (int k = 1; k < K; k++) {
            if (utilities[k] > utilities[bestAction]) {
                bestAction = k;
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

    public double[] getUtility(State s, Task t) {
        int K = t.actions.length;
        double[] utilities = new double[K];
        double maxUtility = Double.NEGATIVE_INFINITY;
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
                    utilities[k] += alphas.get(j) * potentialFunctions.get(j).classifyInstance(ins);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (utilities[k] > maxUtility) {
                maxUtility = utilities[k];
            }
        }

        double norm = 0;
        double T = numIteration == 0 ? 1 : numIteration;
        for (int k = 0; k < K; k++) {
            utilities[k] = Math.exp((utilities[k] - maxUtility) / 10);
            norm += utilities[k];
        }
        for (int k = 0; k < K; k++) {
            utilities[k] /= norm;
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
        List<double[]> features = new ArrayList<double[]>();
        List<Double> labels = new ArrayList<Double>();

        for (Rollout rollout : rollouts) {
            Task task = rollout.task;
            List<Tuple> samples = rollout.samples;

            double P_z = 1;//compuate_P_z(rollout);
            double R_z = compuate_R_z(rollout);

            for (int step = samples.size() - 1; step >= 0; step--) {
                Tuple sample = samples.get(step);

                features.add(task.getSAFeature(sample.s, sample.a));
                double prab = ((PrabAction) sample.a).probability;
                double label = P_z * R_z * (1 + sample.reward / (R_z + 0.5)) * prab * (1 - prab);
                labels.add(label);
            }
        }

        if (null == dataHead) {
            int na = rollouts.get(0).task.actions.length;
            dataHead = constructDataHead(features.get(0).length, na);
        }

        Instances data = new Instances(dataHead, features.size());
        for (int i = 0; i < features.size(); i++) {
            Instance ins = contructInstance(features.get(i), labels.get(i));
            data.add(ins);
        }

        //IO.saveInstances("data.arff", data);

        Classifier c = getBaseLearner();
        try {
            c.buildClassifier(data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        int t = alphas.size() + 1;
        alphas.add(stepsize / Math.sqrt(t));
        potentialFunctions.add(c);
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
        this.numIteration = Math.min(potentialFunctions.size(), numIteration);
    }

    private double compuate_P_z(Rollout rollout) {
        double P_z = 1;
        for (Tuple tulpe : rollout.samples) {
            P_z *= ((PrabAction) tulpe.a).probability;
        }
        return P_z;
    }

    private double compuate_R_z(Rollout rollout) {
        double R_z = 0;
        for (Tuple tulpe : rollout.samples) {
            R_z += tulpe.reward;
        }
        return R_z;
    }
}
