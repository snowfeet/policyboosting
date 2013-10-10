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
public class RankBoostPoolWithoutEXPPolicy extends GibbsPolicy {

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
    private int uniformPoolCurSize;
    private int uniformPoolCount;

    public RankBoostPoolWithoutEXPPolicy(Random rand) {
        numIteration = 0;
        alphas = new ArrayList<Double>();
        potentialFunctions = new ArrayList<Classifier>();
        random = rand;
        REPTree tree = new REPTree();
        tree.setMaxDepth(100);
        base = tree;
        stepsize = 1;

        bestPoolSize = 10;
        uniformPoolSize = 10;

        bestPool = new Trajectory[bestPoolSize];
        uniformPool = new Trajectory[uniformPoolSize];

        bestPoolCurSize = 0;
        uniformPoolCurSize = 0;
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
        // gather data to learn
        List<Trajectory> trainTrajectories = new ArrayList<Trajectory>();
        // current data
        for (int i = 0; i < trajectories.size(); i++) {
            trainTrajectories.add(trajectories.get(i));
        }
        // best data
        for (int i = 0; i < bestPoolCurSize; i++) {
            trainTrajectories.add(bestPool[i]);
        }
        // uniform data
        for (int i = 0; i < uniformPoolCurSize; i++) {
            trainTrajectories.add(uniformPool[i]);
        }

        double[][] log_P_z = new double[trainTrajectories.size()][];

        int numZ = trainTrajectories.size();

        compuate_log_P_z(trainTrajectories, log_P_z);
//        for (int i = 0; i < trainTrajectories.size(); i++) {
//            Trajectory trajectory = trainTrajectories.get(i);
//            ratios[i] = compuate_P_z_of_R_z(trajectory);
//        }

        double[][] Jzz = new double[numZ][numZ];
        for (int i = 0; i < numZ; i++) {
            for (int j = i + 1; j < numZ; j++) {
                Trajectory trajectory_i = trainTrajectories.get(i);
                Trajectory trajectory_j = trainTrajectories.get(j);
                double jzz = Math.exp(-(log_P_z[i][0] - log_P_z[j][0]) * (trajectory_i.getRewards() - trajectory_j.getRewards()));
//                double jzz = 1;
//                if(ratios[i][0] == ratios[j][0]){
//                    System.err.println("non-zero!");
//                }

//                if (Double.isInfinite(jzz)) {
//                    System.out.println(0x1);
//                }

                Jzz[i][j] = Jzz[j][i] = jzz;
            }
        }

        double[] Jz = new double[numZ];
        double[] JzR = new double[numZ];
        double[] JzLogP = new double[numZ];
        for (int i = 0; i < numZ; i++) {
            Jz[i] = 0;
            JzR[i] = 0;
            JzLogP[i] = 0;
            for (int j = 0; j < numZ; j++) {
                Jz[i] += Jzz[i][j];
                JzR[i] += Jzz[i][j] * trainTrajectories.get(j).getRewards();
                JzLogP[i] += Jzz[i][j] * log_P_z[j][0];
            }
        }

        double max_abs_label = -1;
        for (int i = 0; i < trainTrajectories.size(); i++) {
            List<double[]> features = new ArrayList<double[]>();
            List<Double> labels = new ArrayList<Double>();

            Trajectory trajectory = trainTrajectories.get(i);
            Task task = trajectory.getTask();
            List<Tuple> samples = trajectory.getSamples();

            double R_z = trajectory.getRewards();

            for (int step = 0; step < samples.size(); step++) {
                Tuple sample = samples.get(step);

                features.add(task.getSAFeature(sample.s, sample.action));
                double prab = ((PrabAction) sample.action).probability;

                double label = ((Jz[i] * R_z - JzR[i]) / prab + (Jz[i] * log_P_z[i][0] - JzLogP[i]) * sample.reward) * prab * (1 - prab);
                labels.add(label);
                if (Double.isNaN(label)) {
                    System.out.println(2);
                }

                if (Math.abs(label) > max_abs_label) {
                    max_abs_label = Math.abs(label);
                }
            }

            trajectory.setFeatures(features);
            trajectory.setLabels(labels);
        }

        if (null == dataHead) {
            int na = trainTrajectories.get(0).getTask().actions.length;
            dataHead = constructDataHead(trainTrajectories.get(0).getFeatures().get(0).length, na);
        }

        Instances data = new Instances(dataHead);
        for (Trajectory trajectory : trainTrajectories) {
            List<double[]> features = trajectory.getFeatures();
            List<Double> labels = trajectory.getLabels();
            for (int j = 0; j < features.size(); j++) {
//            Instance ins = contructInstance(features.get(j), labels.get(j) / Math.max(1, max_abs_label));
                Instance ins = contructInstance(features.get(j), max_abs_label == 0 ? labels.get(j) : labels.get(j) / max_abs_label);
                data.add(ins);
            }
        }
//        if (numIteration < 10) {
//            IO.saveInstances("data/data" + numIteration + ".arff", data);
//        }

        Classifier c = getBaseLearner();
        try {
            System.out.println("max_abs_label = " + max_abs_label + ", train size: " + data.numInstances());
            c.buildClassifier(data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // sample for best and uniform
//        {
//            // best
//            Trajectory[] allTrajectory = new Trajectory[trajectories.size() + bestPoolCurSize];
//            trajectories.toArray(allTrajectory);
//            System.arraycopy(bestPool, 0, allTrajectory, trajectories.size(), bestPoolCurSize);
//            Arrays.sort(allTrajectory);
//            bestPoolCurSize = Math.min(bestPoolSize, allTrajectory.length);
//            System.arraycopy(allTrajectory, 0, bestPool, 0, bestPoolCurSize);
//
//            for (Trajectory trajectory : trajectories) {
//                // uniform
//                if (uniformPoolCurSize < uniformPoolSize) {
//                    uniformPool[uniformPoolCurSize] = trajectory;
//                    uniformPoolCurSize++;
//                } else {
//                    int repInd = random.nextInt(uniformPoolCount);
//                    if (repInd < uniformPoolSize) {
//                        uniformPool[repInd] = trajectory;
//                    }
//                }
//                uniformPoolCount++;
//            }
//        }

        double objective = 0;
        for (int i = 0; i < trainTrajectories.size(); i++) {
            Trajectory trajectory = trainTrajectories.get(i);
            objective += log_P_z[i][0] * trajectory.getRewards();
        }
        System.err.println(objective);
        //System.err.println(potentialFunctions.size());

        int t = alphas.size() + 1;
//        alphas.add(stepsize / Math.sqrt(t));

        //line search
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

    private void compuate_log_P_z(List<Trajectory> trainTrajectories, double[][] log_P_z) {
        int numTraj = trainTrajectories.size();
        double mean_log_P_z = 0, max_log_P_z = Double.NEGATIVE_INFINITY, min_log_P_z = Double.POSITIVE_INFINITY;
        for (int i = 0; i < numTraj; i++) {
            log_P_z[i] = new double[1];
            Trajectory trajectory = trainTrajectories.get(i);

            log_P_z[i][0] = 0;
            int T = trajectory.getSamples().size();
            for (int t = 0; t < T; t++) {
                Tuple tuple = trajectory.getSamples().get(t);
                double[] probabilities = getProbability(tuple.s, trajectory.getTask());
                log_P_z[i][0] += Math.log(probabilities[tuple.action.a]);
            }
//            log_P_z[i][0] = Math.exp(log_P_z[i][0]);

            if (log_P_z[i][0] > max_log_P_z) {
                max_log_P_z = log_P_z[i][0];
            }

            if (log_P_z[i][0] < min_log_P_z) {
                min_log_P_z = log_P_z[i][0];
            }

            mean_log_P_z += log_P_z[i][0];
        }
//        mean_log_P_z /= numTraj;

        for (int i = 0; i < numTraj; i++) {
//             log_P_z[i][0] = (log_P_z[i][0] - min_log_P_z) / (max_log_P_z - min_log_P_z);
            log_P_z[i][0] /= 1000;//-Math.abs(mean_log_P_z);
        }
    }
}
