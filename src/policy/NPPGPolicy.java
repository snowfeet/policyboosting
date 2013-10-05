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
import java.util.List;
import java.util.Random;
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
public class NPPGPolicy extends GibbsPolicy {

    private static final long serialVersionUID = 2259079722984917190l;
    private List<Double> alphas;
    private List<Classifier> potentialFunctions;
    private Classifier base;
    private double stepsize;
    public Instances dataHead;
    double stationaryRate;
    double epsionGreedy;
    double epsionGreedyDecay;
    int maxStep;

    public NPPGPolicy(Random rand) {
        numIteration = 0;
        alphas = new ArrayList<Double>();
        potentialFunctions = new ArrayList<Classifier>();
        random = rand;

        REPTree tree = new REPTree();
        tree.setMaxDepth(100);
        base = tree;

        stepsize = 1;
        stationaryRate = 0.8;
        epsionGreedy = 0.1;
        epsionGreedyDecay = 1;
    }

    @Override
    public void update(List<Trajectory> rollouts) {
        double gamma = 1;
        List<double[]> features = new ArrayList<double[]>();
        List<Double> weights = new ArrayList<Double>();
        List<Double> QHat = new ArrayList<Double>();

        int LAST = (int) (stationaryRate * maxStep);

        for (Trajectory rollout : rollouts) {
            Task task = rollout.getTask();
            List<Tuple> samples = rollout.getSamples();

            double E = 0;
            for (int step = samples.size() - 1; step >= Math.max(0, samples.size() - LAST); step--) {
                Tuple sample = samples.get(step);
                E = gamma * E + (sample.reward);

                features.add(task.getSAFeature(sample.s, sample.action));
                weights.add(((PrabAction) sample.action).probability);
                QHat.add(E);
            }
        }

        if (null == dataHead) {
            int na = rollouts.get(0).getTask().actions.length;
            dataHead = constructDataHead(features.get(0).length, na);
        }
        Instances data = new Instances(dataHead, features.size());
        for (int i = 0; i < features.size(); i++) {
            double pi = weights.get(i);
            double Q = QHat.get(i);
            Instance ins = contructInstance(features.get(i), Q * pi * (1 - pi));
            data.add(ins);
        }
//        IO.saveInstances("gb_data" + numIteration + ".arff", data);
        Classifier c = getBaseLearner();
        try {
            c.buildClassifier(data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        int t = alphas.size() + 1;
        alphas.add(stepsize / Math.sqrt(t));
        potentialFunctions.add(c);

        epsionGreedy = epsionGreedy * epsionGreedyDecay;
        numIteration++;
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

        double[] probabilities = getProbability(s, t);
        int bestAction = 0, m = 2;
        for (int k = 1; k < K; k++) {
            if (probabilities[k] > probabilities[bestAction] + Double.MIN_VALUE) {
                bestAction = k;
                m = 2;
            } else if (Math.abs(probabilities[k] - probabilities[bestAction]) <= Double.MIN_VALUE) {
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

        double[] probabilities = getProbability(s, t);
        return makeDecisionS(s, t, probabilities, thisRand);
    }

    @Override
    public PrabAction makeDecisionS(State s, Task t, double[] probabilities, Random outRand) {
        if (numIteration == 0 || probabilities == null) {
            return null;
        }

        Random thisRand = outRand == null ? random : outRand;
        int K = t.actions.length;

        int bestAction = 0, m = 2;
        for (int k = 1; k < K; k++) {
            if (probabilities[k] > probabilities[bestAction] + Double.MIN_VALUE) {
                bestAction = k;
                m = 2;
            } else if (Math.abs(probabilities[k] - probabilities[bestAction]) <= Double.MIN_VALUE) {
                if (thisRand.nextDouble() < 1.0 / m) {
                    bestAction = k;
                }
                m++;
            }
        }

        return new PrabAction(bestAction, probabilities[bestAction]);
    }

    @Override
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
                    utilities[k] += alphas.get(j) * potentialFunctions.get(j).classifyInstance(ins);
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

    public double potentialFunctionValue(Instance ins, int numIteration) {
        double value = 0;
        for (int j = 0; j < numIteration; j++) {
            try {
                value += alphas.get(j) * potentialFunctions.get(j).classifyInstance(ins);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return value;
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

    public void setEpsionGreedy(double epsionGreedy) {
        this.epsionGreedy = epsionGreedy;
    }

    public void setEpsionGreedyDecay(double epsionGreedyDecay) {
        this.epsionGreedyDecay = epsionGreedyDecay;
    }

    public void setStationaryRate(double stationaryRate) {
        this.stationaryRate = stationaryRate;
    }

    public void setMaxStep(int maxStep) {
        this.maxStep = maxStep;
    }
}