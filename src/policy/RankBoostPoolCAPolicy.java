/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package policy;

import core.Action;
import core.ParallelTrain;
import core.Policy;
import core.PrabAction;
import core.State;
import core.Task;
import experiment.Trajectory;
import experiment.Tuple;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
public class RankBoostPoolCAPolicy extends Policy {

    private List<Double>[] alphas;
    private List<Classifier>[] potentialFunctions;
    private Classifier base;
    private double stepsize;
    private double sigma;
    private Instances dataHead = null;
    private Trajectory[] bestPool = null;
    private Trajectory[] uniformPool = null;
    private int bestPoolSize;
    private int uniformPoolSize;
    private int bestPoolCurSize;
    private int uniformPoolCurSize;
    private int uniformPoolCount;

    public RankBoostPoolCAPolicy(Random rand) {
        numIteration = 0;

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

        sigma = 0.05;
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

    public Action makeDecisionD(State s, Task t, Random outRand) {
        if (numIteration == 0) {
            return null;
        }

        Random thisRand = outRand == null ? random : outRand;
        return null;
    }

    public PrabAction makeDecisionS(State s, Task t, Random outRand) {
        if (numIteration == 0) {
            return null;
        }

        Random thisRand = outRand == null ? random : outRand;

        double[] utilities = getUtility(s, t);
        double[] controls = sampleFromGaussian(utilities, thisRand);
        Action action = new Action(controls);
        return new PrabAction(action, 1, true);
    }

    public PrabAction makeDecisionS(State s, Task t, double[] probabilities, Random outRand) {
        if (numIteration == 0 || probabilities == null) {
            return null;
        }

        Random thisRand = outRand == null ? random : outRand;
        return null;
    }

    public double[] getUtility(State s, Task t) {
        double[] utilities = new double[t.actionDim];
        for (int k = 0; k < utilities.length; k++) {
            double[] stateFeature = s.getfeatures();
            Instance ins = contructInstance(stateFeature, 0);
            if (null == dataHead) {
                dataHead = constructDataHead(stateFeature.length);
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

    private Instance contructInstance(double[] stateFeature, double label) {
        int D = stateFeature.length;
        double[] values = new double[D + 1];
        values[D] = label;
        System.arraycopy(stateFeature, 0, values, 0, D);
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

        double[][] Jzz = new double[numZ][numZ];
        for (int i = 0; i < numZ; i++) {
            for (int j = i + 1; j < numZ; j++) {
                Trajectory trajectory_i = trainTrajectories.get(i);
                Trajectory trajectory_j = trainTrajectories.get(j);
                double jzz = Math.exp(-(log_P_z[i][0] - log_P_z[j][0]) * (trajectory_i.getRewards() - trajectory_j.getRewards()));

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
            Trajectory trajectory = trainTrajectories.get(i);
            Task task = trajectory.getTask();
            List<Tuple> samples = trajectory.getSamples();

            List<double[]> features = new ArrayList<double[]>();
            List<Double>[] multiModelLabels = new List[task.actionDim];
            for (int k = 0; k < task.actionDim; k++) {
                multiModelLabels[k] = new ArrayList<Double>();
            }

            double R_z = trajectory.getRewards();

            for (int step = 0; step < samples.size(); step++) {
                Tuple sample = samples.get(step);

                features.add(sample.s.getfeatures());
                double prab = ((PrabAction) sample.action).probability;

                for (int k = 0; k < task.actionDim; k++) {
                    double[] rhos = getUtility(sample.s, task);
                    double label = ((Jz[i] * R_z - JzR[i]) / prab + (Jz[i] * log_P_z[i][0] - JzLogP[i]) * sample.reward) * prab * 2 * (sample.action.controls[k] - rhos[k]) / (sigma * sigma);
                    multiModelLabels[k].add(label);
                    if (Double.isNaN(label)) {
                        System.out.println(2);
                    }

                    if (Math.abs(label) > max_abs_label) {
                        max_abs_label = Math.abs(label);
                    }
                }
            }

            trajectory.setFeatures(features);
            trajectory.setMultiModelLabels(multiModelLabels);
        }

        if (null == alphas) {
            int actionDim = trainTrajectories.get(0).getTask().actionDim;
            alphas = new List[actionDim];
            potentialFunctions = new List[actionDim];
            for (int k = 0; k < actionDim; k++) {
                alphas[k] = new ArrayList<Double>();
                potentialFunctions[k] = new ArrayList<Classifier>();
            }
        }

        ExecutorService exec = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors() - 1);
        List<ParallelTrain> rList = new ArrayList<ParallelTrain>();
        for (int k = 0; k < trajectories.get(0).getTask().actionDim; k++) {
            Instances data = new Instances(dataHead);
            for (Trajectory trajectory : trainTrajectories) {
                List<double[]> features = trajectory.getFeatures();
                List<Double>[] multiModelLabels = trajectory.getMultiModelLabels();
                for (int j = 0; j < features.size(); j++) {
                    Instance ins = contructInstance(features.get(j), multiModelLabels[k].get(j) / max_abs_label);
                    data.add(ins);
                }
            }
//            if (numIteration < 10) {
//                IO.saveInstances("data/data" + numIteration + ".arff", data);
//            }

            Classifier c = getBaseLearner();
            try {
                System.out.println("train size: " + data.numInstances());
                ParallelTrain run = new ParallelTrain(c, data);
                rList.add(run);
                exec.execute(run);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // sample for best and uniform
            {
                // best
//                Trajectory[] allTrajectory = new Trajectory[trajectories.size() + bestPoolCurSize];
//                trajectories.toArray(allTrajectory);
//                System.arraycopy(bestPool, 0, allTrajectory, trajectories.size(), bestPoolCurSize);
//                Arrays.sort(allTrajectory);
//                bestPoolCurSize = Math.min(bestPoolSize, allTrajectory.length);
//                System.arraycopy(allTrajectory, 0, bestPool, 0, bestPoolCurSize);
//
//                for (Trajectory trajectory : trajectories) {
//                    // uniform
//                    if (uniformPoolCurSize < uniformPoolSize) {
//                        uniformPool[uniformPoolCurSize] = trajectory;
//                        uniformPoolCurSize++;
//                    } else {
//                        int repInd = random.nextInt(uniformPoolCount);
//                        if (repInd < uniformPoolSize) {
//                            uniformPool[repInd] = trajectory;
//                        }
//                    }
//                    uniformPoolCount++;
//                }
            }

            alphas[k].add(stepsize);

            potentialFunctions[k].add(c);
        }

        exec.shutdown();
        try {
            while (!exec.awaitTermination(10, TimeUnit.SECONDS)) {
            }
        } catch (InterruptedException ex) {
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

    public void setNumIteration(int numIteration) {
        this.numIteration = Math.min(potentialFunctions[0].size(), numIteration);
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
                double probability = getProbability(tuple.s, tuple.action, trajectory.getTask());
                log_P_z[i][0] += Math.log(probability);
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

    private double getProbability(State s, Action action, Task task) {
        double[] utilities = getUtility(s, task);

        double prob = 1 / (Math.sqrt(Math.PI * 2) * sigma);
        for (int i = 0; i < utilities.length; i++) {
            prob = prob * Math.exp(-(action.controls[i] - utilities[i]) * (action.controls[i] - utilities[i]) / (sigma * sigma));
        }
        return 1;
    }

    private double[] sampleFromGaussian(double[] utilities, Random thisRand) {
        double[] controls = new double[utilities.length];

        for (int i = 0; i < controls.length; i++) {
            controls[i] = utilities[i] + thisRand.nextGaussian() * sigma;
        }

        return controls;
    }
}
