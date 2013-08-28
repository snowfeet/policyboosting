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
public class RankBoostPoolInnerIterPolicy extends GibbsPolicy {

    private List<Double> alphas;
    private List<Classifier> potentialFunctions;
    private Classifier base;
    private double stepsize;
    private Instances dataHead = null;
    private List<Instance> dataPool = null;
    private int poolSize;
    private int dataCounts;

    public RankBoostPoolInnerIterPolicy(Random rand) {
        numIteration = 0;
        alphas = new ArrayList<Double>();
        potentialFunctions = new ArrayList<Classifier>();
        random = rand;
        REPTree tree = new REPTree();
        tree.setMaxDepth(30);
        base = tree;
        stepsize = 1;

        dataPool = new ArrayList<Instance>();
        poolSize = 5000;
        dataCounts = 0;
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

    public PrabAction makeDecisionS(State s, Task t, double[] probabilities, Random outRand) {
        if (numIteration == 0 || probabilities == null) {
            return null;
        }

        Random thisRand = outRand == null ? random : outRand;
        int K = t.actions.length;

//        int bestAction = -1;
//        double p = thisRand.nextDouble(), totalShare = 0;
//        for (int k = 0; k < K; k++) {
//            totalShare += probabilities[k];
//            if (p <= totalShare) {
//                bestAction = k;
//                break;
//            }
//        }

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

//        if(numIteration == 2){
//            for(int i=0;i<K;i++)
//                System.err.print(utilities[i]+",");
//            System.err.println();
//        }
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
    public void update(List<Trajectory> rollouts) {
        for (int i = 0; i < 10; i++) {
            updateInner(rollouts);
        }
    }

    public void updateInner(List<Trajectory> rollouts) {
        List<double[]> features = new ArrayList<double[]>();
        List<Double> labels = new ArrayList<Double>();
        List<Double> weight = new ArrayList<Double>();

        double[][] ratios = new double[rollouts.size()][];

        int numZ = rollouts.size();
        double RZ = 0, tildeP = 0;
        //double rrrr = 0;
        for (int i = 0; i < rollouts.size(); i++) {
            Trajectory rollout = rollouts.get(i);
            RZ += rollout.getRZ();
            ratios[i] = compuate_P_z_of_R_z(rollout);
            tildeP += ratios[i][0];
        }
        //System.out.println(">>>>>"+tildeP+">>>>>"+rrrr/tildeP);

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

                features.add(task.getSAFeature(sample.s, sample.action));
                double prab = ((PrabAction) sample.action).probability;
                double tilde_R_z = R_z - accumulated_rewards_sofar, tilde_RZ = RZ;

                // 补偿rewards带来的修改
                if (rollout.isIsSuccess()) {
                    tilde_R_z += step * samples.get(samples.size() - 1).reward;
                } else {
                    tilde_R_z += step * mean_r_z;
                }
                //tilde_RZ += (tilde_R_z - R_z);

                double label = (ratios[i][step] * (numZ * tilde_R_z - tilde_RZ) / prab + (ratios[i][step] * numZ - tildeP) * sample.reward) * prab * (1 - prab);
                labels.add(label);
                weight.add(sample.reward);

                if (Math.abs(label) > max_abs_label) {
                    max_abs_label = Math.abs(label);
                }

                accumulated_rewards_sofar += sample.reward;
            }
        }

        if (null == dataHead) {
            int na = rollouts.get(0).getTask().actions.length;
            dataHead = constructDataHead(features.get(0).length, na);
        }

        Instances data = new Instances(dataHead, features.size());
        for (int i = 0; i < features.size(); i++) {
            Instance ins = contructInstance(features.get(i), labels.get(i) / Math.max(1, 0.1*max_abs_label));
            data.add(ins);
        }

        Instances dataTrain = data;
//        Instances dataTrain = new Instances(dataHead, dataPool.size());
        for (Instance ins : dataPool) {
            dataTrain.add(ins);
        }

        IO.saveInstances("data/data" + numIteration + ".arff", dataTrain);

        Classifier c = getBaseLearner();
        try {
            c.buildClassifier(dataTrain);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (numIteration % 2 == 0) {
            dataPool.clear();
            dataCounts = 0;
        }
        for (int i = 0; i < features.size(); i++) {
            Instance ins = data.instance(i);

            if (dataPool.size() < poolSize) {
                dataPool.add(ins);
            } else {
                int j = random.nextInt(dataCounts);
                if (j < poolSize && weight.get(i) > 200) {
                    dataPool.remove(j);
                    dataPool.add(ins);
                }
            }
            dataCounts++;
        }

        double objective = 0;
        for (int i = 0; i < rollouts.size(); i++) {
            Trajectory rollout = rollouts.get(i);
            objective += ratios[i][0] * rollout.getRewards();
        }
        System.err.println(objective);
        //System.err.println(potentialFunctions.size());

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
