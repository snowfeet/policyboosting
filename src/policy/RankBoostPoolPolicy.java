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
import java.util.Arrays;
import java.util.Collections;
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
public class RankBoostPoolPolicy extends GibbsPolicy {

    private List<Double> alphas;
    private List<Classifier> potentialFunctions;
    private Classifier base;
    private double stepsize;
    private Instances dataHead = null;
    private Trajectory[] bestPool = null;
    private Trajectory[] uniformPool = null;
    private int bestPoolSize;
    private int uniformPoolSize;
    private int bestPoolCurSize;
    private int uniformPoolIndex;
    private int uniformPoolCount;

    public RankBoostPoolPolicy(Random rand) {
        numIteration = 0;
        alphas = new ArrayList<Double>();
        potentialFunctions = new ArrayList<Classifier>();
        random = rand;
        REPTree tree = new REPTree();
        tree.setMaxDepth(100);
        base = tree;
        stepsize = 1;

        bestPoolSize = 50;
        uniformPoolSize = 50;

        bestPool = new Trajectory[bestPoolSize];
        uniformPool = new Trajectory[uniformPoolSize];

        bestPoolCurSize = 0;
        uniformPoolIndex = 0;
        uniformPoolCount = 0;
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

    @Override
    public void update(List<Trajectory> trajectories) {
        double[][] ratios = new double[trajectories.size()][];

        int numZ = trajectories.size();
        for (int i = 0; i < trajectories.size(); i++) {
            Trajectory trajectory = trajectories.get(i);
            ratios[i] = compuate_P_z_of_R_z(trajectory);
        }

        double[][] Jzz = new double[numZ][numZ];
        for (int i = 0; i < numZ; i++) {
            for (int j = i + 1; j < numZ; j++) {
                Trajectory trajectory_i = trajectories.get(i);
                Trajectory trajectory_j = trajectories.get(j);
                double jzz = Math.exp(-(ratios[i][0] - ratios[j][0]) * (trajectory_i.getRewards() - trajectory_j.getRewards()));

                Jzz[i][j] = Jzz[j][i] = jzz;
            }
        }

        double[] Jz = new double[numZ];
        double[] JzR = new double[numZ];
        double[] JzGamma = new double[numZ];
        for (int i = 0; i < numZ; i++) {
            Jz[i] = 0;
            JzR[i] = 0;
            JzGamma[i] = 0;
            for (int j = 0; j < numZ; j++) {
                Jz[i] += Jzz[i][j];
                JzR[i] += Jzz[i][j] * trajectories.get(j).getRewards();
                JzGamma[i] += Jzz[i][j] * ratios[j][0];
            }
        }

        double max_abs_label = -1;
        for (int i = 0; i < trajectories.size(); i++) {
            List<double[]> features = new ArrayList<double[]>();
            List<Double> labels = new ArrayList<Double>();

            Trajectory trajectory = trajectories.get(i);
            Task task = trajectory.getTask();
            List<Tuple> samples = trajectory.getSamples();

            double R_z = trajectory.getRewards();

            for (int step = 0; step < samples.size(); step++) {
                Tuple sample = samples.get(step);

                features.add(task.getSAFeature(sample.s, sample.action));
                double prab = ((PrabAction) sample.action).probability;

                double label = ((Jz[i] * R_z - JzR[i]) * ratios[i][0] / prab + (Jz[i] * ratios[i][0] - JzGamma[i]) * sample.reward) * prab * (1 - prab);
                labels.add(label);

                if (Math.abs(label) > max_abs_label) {
                    max_abs_label = Math.abs(label);
                }
            }

            trajectory.setFeatures(features);
            trajectory.setLabels(labels);
        }

        if (null == dataHead) {
            int na = trajectories.get(0).getTask().actions.length;
            dataHead = constructDataHead(trajectories.get(0).getFeatures().get(0).length, na);
        }

        Instances data = new Instances(dataHead);

        // gather data to learn
        {
            List<Trajectory> trainTrajectories = new ArrayList<Trajectory>();
            // current data
            for (int i = 0; i < trajectories.size(); i++) {
                trainTrajectories.add(trajectories.get(i));
            }
            // best data
            for (int i = 0; i < bestPoolCurSize; i++) {
                trainTrajectories.add(bestPool[i]);
            }
            for (int i = 0; i < uniformPoolIndex; i++) {
                trainTrajectories.add(uniformPool[i]);
            }

            for (Trajectory trajectory : trainTrajectories) {
                List<double[]> features = trajectory.getFeatures();
                List<Double> labels = trajectory.getLabels();
                for (int j = 0; j < features.size(); j++) {
//            Instance ins = contructInstance(features.get(j), labels.get(j) / Math.max(1, max_abs_label));
                    Instance ins = contructInstance(features.get(j), labels.get(j));
                    data.add(ins);
                }
            }
        }

        // sample for best and uniform
        {
            // best
            Trajectory[] allTrajectory = new Trajectory[trajectories.size() + bestPoolCurSize];
            trajectories.toArray(allTrajectory);
            System.arraycopy(bestPool, 0, allTrajectory, trajectories.size(), bestPoolCurSize);
            Arrays.sort(allTrajectory);
            bestPoolCurSize = Math.min(bestPoolSize, allTrajectory.length);
            System.arraycopy(allTrajectory, 0, bestPool, 0, bestPoolCurSize);

            for (Trajectory trajectory : trajectories) {
                // uniform
                if (uniformPoolIndex < uniformPoolSize) {
                    uniformPool[uniformPoolIndex] = trajectory;
                    uniformPoolIndex++;
                } else {
                    int repInd = random.nextInt(uniformPoolCount);
                    if (repInd < uniformPoolSize) {
                        uniformPool[repInd] = trajectory;
                    }
                }
                uniformPoolCount++;
            }
        }

        if (numIteration < 10) {
            IO.saveInstances("data/data" + numIteration + ".arff", data);
        }

        Classifier c = getBaseLearner();
        try {
            c.buildClassifier(data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        double objective = 0;
        for (int i = 0; i < trajectories.size(); i++) {
            Trajectory trajectory = trajectories.get(i);
            objective += ratios[i][0] * trajectory.getRewards();
        }
        System.err.println(objective);
        //System.err.println(potentialFunctions.size());

        int t = alphas.size() + 1;
//        alphas.add(stepsize / Math.sqrt(t));
        alphas.add(stepsize);
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

    private double[] compuate_P_z_of_R_z(Trajectory trajectory) {
        boolean flag = numIteration < 0;
        if (flag) {
            System.out.println(trajectory.getRewards());
            int x = 1;
        }

        int T = trajectory.getSamples().size();
        double[] P_z = new double[T];
        double[] D_z = new double[T];
        double[] R_z = new double[T];

        for (int i = 0; i < T; i++) {
            Tuple tuple = trajectory.getSamples().get(i);
            double[] probabilities = getProbability(tuple.s, trajectory.getTask());
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
