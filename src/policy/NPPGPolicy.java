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
import experiment.Execution;
import experiment.Experiment;
import experiment.Trajectory;
import experiment.Tuple;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import weka.classifiers.Classifier;
import weka.classifiers.meta.Bagging;
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
    private RandomPolicy rp;
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
        rp = new RandomPolicy(new Random(rand.nextInt()));
        numIteration = 0;
        alphas = new ArrayList<Double>();
        potentialFunctions = new ArrayList<Classifier>();
        random = rand;

        REPTree reptree = new REPTree();
        reptree.setMaxDepth(100);

        Bagging bag = new Bagging();
        bag.setClassifier(reptree);
        bag.setNumIterations(10);

        base = bag;

        stepsize = 1;
        stationaryRate = 0.8;
        epsionGreedy = 0.1;
        epsionGreedyDecay = 1;
    }

    @Override
    public PrabAction makeDecisionS(State s, Task t, double[] probabilities, Random outRand) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    class ParallelExecute implements Runnable {

        private Trajectory rollout;
        private Task task;
        private Policy policy;
        private State initialState;
        private int maxStep;
        private Random random;

        public ParallelExecute(Task task, Policy policy, State initialState, int maxStep, int seed) {
            this.task = task;
            this.policy = policy;
            this.initialState = initialState;
            this.maxStep = maxStep;
            this.random = new Random(seed);
        }

        public void run() {
            rollout = Execution.runTaskWithFixedStep(task,
                    initialState, policy, maxStep, true, random);//task, initialState, policy, maxStep, true, random);
        }

        public Trajectory getRollout() {
            return rollout;
        }
    }

    public long[] train(Task task, int iteration, int trialsPerIter, State initialState, int maxStep, boolean isPara, Random random) {
        this.maxStep = maxStep;
        long[] time = new long[iteration];
        for (int iter = 0; iter < iteration; iter++) {
            System.out.println("iter=" + iter);
            System.out.println("collecting samples...");

            long check1 = System.currentTimeMillis();
            ParallelExecute[] runs = new ParallelExecute[trialsPerIter];
            ExecutorService exec = Executors.newFixedThreadPool(23);
            for (int i = 0; i < trialsPerIter; i++) {
                runs[i] = new ParallelExecute(task, this, task.getInitialState(), maxStep, random.nextInt());
                if (isPara) {
                    exec.execute(runs[i]);
                } else {
                    runs[i].run();
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
            long check2 = System.currentTimeMillis();
            System.out.println("collecting samples is done! Updating meta-policy...");

            List<Trajectory> rollouts = new ArrayList<Trajectory>();
            int avaStep = 0;
            for (int i = 0; i < trialsPerIter; i++) {
                rollouts.add(runs[i].getRollout());
                System.out.print(runs[i].getRollout().getSamples().size() + " ");
                avaStep += runs[i].getRollout().getSamples().size();
            }
            System.out.println("\n -> " + avaStep / trialsPerIter);

            double[] obj = Experiment.calcObjective(rollouts, this);
            System.out.println("objective value of iter " + iter + " before updating is " + obj[0] + "," + obj[1] + "," + obj[2]);

            long check3 = System.currentTimeMillis();
            update(rollouts);
            long check4 = System.currentTimeMillis();

            obj = Experiment.calcObjective(rollouts, this);
            System.out.println("objective value of iter " + iter + " after updating is " + obj[0] + "," + obj[1] + "," + obj[2]);
            
            time[iter] = check2 - check1 + check4 - check3;
        }
        return time;
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
            Instance ins = contructInstance(features.get(i), Q * pi * (1 - pi), 1.0);
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
        Random thisRand = outRand == null ? random : outRand;
        int A = t.actions.length;

        if (numIteration == 0) {
            return rp.makeDecisionS(s, t, thisRand);
        } else {
            double[] probabilities = getProbability(s, t);

            int bestAction = 0, num_ties = 1;
            for (int a = 1; a < A; a++) {
                double value = probabilities[a];
                if (value >= probabilities[bestAction]) {
                    if (value > probabilities[bestAction] + Double.MIN_VALUE) {
                        bestAction = a;
                    } else {
                        num_ties++;
                        if (0 == thisRand.nextInt(num_ties)) {
                            bestAction = a;
                        }
                    }
                }
            }

            return new PrabAction(bestAction, probabilities[bestAction]);
        }
    }

    @Override
    public PrabAction makeDecisionS(State s, Task t, Random outRand) {
        Random thisRand = outRand == null ? random : outRand;
        if (numIteration == 0 || thisRand.nextDouble() < epsionGreedy) {
            return rp.makeDecisionS(s, t, thisRand);
        } else {
            return (PrabAction) (makeDecisionD(s, t, outRand));
        }
    }

    public double[] getUtility(State s, Task t) {
        int A = t.actions.length;
        double[] utilities = new double[A];
        for (int a = 0; a < A; a++) {
            double[] stateActionFeature = t.getSAFeature(s, new Action(a));
            Instance ins = contructInstance(stateActionFeature, 0, 1.0);
            if (null == dataHead) {
                int na = t.actions.length;
                dataHead = constructDataHead(stateActionFeature.length, na);
            }
            ins.setDataset(dataHead);
            utilities[a] = 0;
            for (int j = 0; j < numIteration; j++) {
                try {
                    utilities[a] += alphas.get(j) * potentialFunctions.get(j).classifyInstance(ins);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        return utilities;
    }

    public static Instance contructInstance(double[] stateActionTaskFeature, double label, double weight) {
        int D = stateActionTaskFeature.length;
        double[] values = new double[D + 1];
        values[D] = label;
        System.arraycopy(stateActionTaskFeature, 0, values, 0, D);
        Instance ins = new Instance(weight, values);
        return ins;
    }

    public static Instances constructDataHead(int D, int na) {
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
}