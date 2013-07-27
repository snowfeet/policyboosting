/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package policy;

import core.Action;
import core.GibbsPolicy;
import core.Policy;
import core.PrabAction;
import core.State;
import core.Task;
import experiment.Rollout;
import experiment.Tuple;
import java.util.ArrayList;
import java.util.LinkedList;
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
public class BoostedMuiltModelPolicy extends GibbsPolicy {
    
    private List<Double>[] alphas;
    private List<Classifier>[] potentialFunctions;
    private Classifier base;
    private double stepsize;
    private Instances dataHead = null;
    private List<Instance>[] dataPool = null;
    private int[] dataCounts = null;
    private int poolSize;
    
    public BoostedMuiltModelPolicy(Random rand) {
        numIteration = 0;
        random = rand;
        stepsize = 1;
        poolSize = 5000;
        
        REPTree tree = new REPTree();
        tree.setMaxDepth(100);
        base = tree;
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
        int A = t.actions.length;
        
        double[] utilities = getUtility(s, t);
        int bestAction = 0, m = 2;
        for (int k = 1; k < A; k++) {
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
        
        double[] utilities = getUtility(s, t);
        double[] probabilities = getProbability(utilities);
        return makeDecisionS(s, t, probabilities, thisRand);
    }
    
    public PrabAction makeDecisionS(State s, Task t, double[] probabilities, Random outRand) {
        if (numIteration == 0 || probabilities == null) {
            return null;
        }
        
        Random thisRand = outRand == null ? random : outRand;
        int A = t.actions.length;
        
        int bestAction = 0, m = 2;
        for (int k = 1; k < A; k++) {
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
    
    public double[] getProbability(State s, Task t) {
        return getProbability(getUtility(s, t));
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
        
        return probabilities;
    }
    
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
    public void update(List<Rollout> rollouts) {
        int A = rollouts.get(0).getTask().actions.length;
        
        if (potentialFunctions == null) {
            potentialFunctions = new ArrayList[A];
            alphas = new ArrayList[A];
            for (int k = 0; k < A; k++) {
                potentialFunctions[k] = new ArrayList<Classifier>();
                alphas[k] = new ArrayList<Double>();
            }
            
            dataPool = new LinkedList[A];
            for (int k = 0; k < A; k++) {
                dataPool[k] = new LinkedList<Instance>();
            }
            dataCounts = new int[A];
        }
        
        List<double[]> features = new ArrayList<double[]>();//same features for all model
        List<Double>[] labels = new List[A]; // different labels for different model, so an array needed here
        for (int k = 0; k < A; k++) {
            labels[k] = new ArrayList<Double>();
        }

        // extract features
        for (Rollout rollout : rollouts) {
            List<Tuple> samples = rollout.getSamples();
            
            double[][] utilities = getRolloutUtilities(rollout);
            double[][] probabilities = getRolloutProbabilities(utilities);
            
            double[] ratio = compuate_P_z_of_D_z(rollout, probabilities);
            double R_z = rollout.getRewards();
            
            for (int step = 0; step < samples.size(); step++) {
                Tuple sample = samples.get(step);
                
                features.add(sample.s.getfeatures());
                
                double labelConstant = ratio[step] * R_z * (1 + sample.reward / (R_z + 0.5));
                
                for (int k = 0; k < A; k++) {
                    if (sample.action.a == k) {
                        labels[k].add(labelConstant
                                * probabilities[step][sample.action.a] * (1 - probabilities[step][sample.action.a]));
                        if (k > 0) {
                            //  System.out.println(labels[k].get(labels[k].size() - 1));
                        }
                    } else {
                        labels[k].add(-labelConstant
                                * probabilities[step][sample.action.a] * probabilities[step][sample.action.a]
                                * utilities[step][k] / utilities[step][sample.action.a]);
                        if (k > 0) {
                            // System.out.println(labels[k].get(labels[k].size() - 1));
                        }
                    }
                }
                
                R_z -= sample.reward;
            }
        }

        // construct datahead when 1st time used
        if (null == dataHead) {
            dataHead = constructDataHead(features.get(0).length);
        }

        // collect examples for regression
        Instance[][] dataTmp = new Instance[A][];
        for (int k = 0; k < A; k++) {
            dataTmp[k] = new Instance[features.size()];
            for (int i = 0; i < features.size(); i++) {
                dataTmp[k][i] = contructInstance(features.get(i), labels[k].get(i));
            }
        }

        // sampling training data
        for (int k = 0; k < A; k++) {
            for (int i = 0; i < dataTmp[k].length; i++) {
                if (dataPool[k].size() < poolSize) {
                    dataPool[k].add(dataTmp[k][i]);
                } else {
                    int j = random.nextInt(dataCounts[k]);
                    if (j < poolSize) {
                        dataPool[k].remove(j);
                        dataPool[k].add(dataTmp[k][i]);
                    }
                }
                dataCounts[k]++;
            }
        }
        
        Instances[] dataTrain = new Instances[A];
        for (int k = 0; k < A; k++) {
            dataTrain[k] = new Instances(dataHead, dataPool[k].size());
            for (Instance ins : dataPool[k]) {
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
    
    private double[][] getRolloutUtilities(Rollout rollout) {
        Task task = rollout.getTask();
        List<Tuple> samples = rollout.getSamples();
        
        double[][] utilities = new double[samples.size()][];
        for (int i = 0; i < samples.size(); i++) {
            utilities[i] = getUtility(samples.get(i).s, task);
        }
        return utilities;
    }
    
    private double[][] getRolloutProbabilities(double[][] utilities) {
        double[][] probabilities = new double[utilities.length][];
        for (int i = 0; i < utilities.length; i++) {
            // System.out.println( utilities[i][0]+","+utilities[i][1]+","+utilities[i][2]);
            probabilities[i] = getProbability(utilities[i]);
            // System.out.println( probabilities[i][0]);
        }
        return probabilities;
    }
    
    private double[] compuate_P_z_of_D_z(Rollout rollout, double[][] probabilities) {
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
            P_z[i] = probabilities[i][tuple.action.a];
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
}
