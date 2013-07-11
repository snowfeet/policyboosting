/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package run;

import experiment.Experiment;
import core.Task;
import domain.mountaincar3d.MCar3DTask;
import domain.mountaincar3d.MCar3DTaskSet;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import policy.AdaptiveMultiMetaPolicy;
import policy.AdaptiveMultiMetaPolicyV2;
import policy.GBMetaPolicy;
import policy.MultiMetaPolicy;
import utills.IO;

/**
 *
 * @author daq
 */
public class TestAdaptiveK {

    static int maxIter = 500;
    static boolean isPara = true;

    public static void main(String[] args) throws Exception {
//        for (int k = 10; k <= 50; k++) {
//            for (int t = 1; t <= 30; t++) {
//                System.out.println(k + "/" + t);
//                testMMP(k, "_" + k + "_" + t);
//            }
//        }

        double[] params_r = new double[]{0.2, 0.4, 0.5, 0.6, 0.8};
        double[] params_e = new double[]{0.025, 0.0275, 0.03, 0.0325};
        double[] params_d = new double[]{0.3, 0.4, 0.5};
        for (int r = 0; r < params_r.length; r++) {
            for (int p = Integer.parseInt(args[0]); p <= Integer.parseInt(args[1]); p++) {
//            for (int q = 0; q < params_d.length; q++) {
                for (int k = 2; k <= 30; k += 3) {
                    for (int t = 1; t <= 10; t++) {
                        System.out.println(r + "/" + p + "/" + k + "/" + t);
                        testAMMP(k, params_e[p], params_r[r], "_" + r + "_" + p + "_" + k + "_" + t);

//                        System.out.println(p + "/" + q + "/" + k + "/" + t);
//                        testAMMP_V2(k, params_e[p], params_d[q], "_" + p + "_" + q + "_" + k + "_" + t);
                    }
                }
//            }
            }
        }

        int k = 5, p = 7;
//        testAMMP(k, params[p], "_for_single");
//        testMMP(4, "_for_single");
//        testMetaWithSingle(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
    }

    private static void testMMP(int K, String post) throws IOException {
        Random random = new Random();
        MCar3DTaskSet taskSet = new MCar3DTaskSet(new Random(random.nextInt()));

        List<Task> tasks = (List<Task>) IO.loadObject("task/MCar3D_2000.task");

        Experiment exp = new Experiment();
        MultiMetaPolicy mmp = new MultiMetaPolicy(K, new Random(random.nextInt()));
        mmp.setStepsize(0.3);
        mmp.train(taskSet, 100, 50, 2, MCar3DTask.getInitialState(), maxIter, true, random);
        IO.saveObject("mmp100_MCar3D.mpl", mmp);

        double[][] results = exp.conductTesting(mmp, tasks, MCar3DTask.getInitialState(), maxIter, 0, isPara, random);
        IO.matrixWrite(results, "/home/lamda/daq/matlab/gradient_boosting/MCar3D/data_adaptive/MMP_MCar3D" + post + ".txt");
    }

    private static void testAMMP(int K, double epsion, double ratio, String post) throws IOException {
        Random random = new Random();
        MCar3DTaskSet taskSet = new MCar3DTaskSet(new Random(random.nextInt()));

        List<Task> tasks = (List<Task>) IO.loadObject("task/MCar3D_2000.task");

        Experiment exp = new Experiment();
        AdaptiveMultiMetaPolicy ammp = new AdaptiveMultiMetaPolicy(K, epsion, ratio, new Random(random.nextInt()));
        ammp.setStepsize(0.3);
        ammp.train(taskSet, 100, 50, 2, MCar3DTask.getInitialState(), maxIter, true, random);
//        IO.saveObject("ammp100_MCar3D.mpl", ammp);

        double[][] results = exp.conductTesting(ammp, tasks, MCar3DTask.getInitialState(), maxIter, 0, isPara, random);

        String dir = "/home/lamda/daq/matlab/gradient_boosting/MCar3D/data_adaptive_v1_" + ratio;
//        File file = new File(dir);
//        file.mkdir();
        IO.matrixWrite(results, dir + "/AMMP_MCar3D" + post + ".txt");
    }

    private static void testAMMP_V2(int K, double epsion, double delay, String post) throws IOException {
        Random random = new Random();
        MCar3DTaskSet taskSet = new MCar3DTaskSet(new Random(random.nextInt()));

        List<Task> tasks = (List<Task>) IO.loadObject("task/MCar3D_2000.task");

        Experiment exp = new Experiment();
        AdaptiveMultiMetaPolicyV2 ammp = new AdaptiveMultiMetaPolicyV2(K, epsion, 0.8,delay, new Random(random.nextInt()));
        ammp.setStepsize(0.3);
        ammp.train(taskSet, 100, 50, 2, MCar3DTask.getInitialState(), maxIter, true, random);

        double[][] results = exp.conductTesting(ammp, tasks, MCar3DTask.getInitialState(), maxIter, 0, isPara, random);
        IO.matrixWrite(results, "/home/lamda/daq/matlab/gradient_boosting/MCar3D/data_adaptive_v2/AMMP_MCar3D" + post + ".txt");
    }

    private static void testMetaWithSingle(int start, int end) throws IOException {
        Random random = new Random();
        MCar3DTaskSet taskSet = new MCar3DTaskSet(random);
        Experiment exp = new Experiment();

        for (int k = start; k <= end; k++) {
            for (int t = 0; t < 20; t++) {
                Task task = taskSet.generateTasks();

                AdaptiveMultiMetaPolicy ammp = (AdaptiveMultiMetaPolicy) IO.loadObject("ammp100_MCar3D.mpl");
                GBMetaPolicy mp = ammp.getTaskPolicy(task);

//                MultiMetaPolicy mmp = (MultiMetaPolicy) IO.loadObject("mmp100_MCar3D.mpl");
//                GBMetaPolicy mp = mmp.getTaskPolicy(task);

                mp.setStepsize(0.3);
                int bais = mp.getNumIteration();

                GBMetaPolicy sp = new GBMetaPolicy(random);
                sp.setStepsize(0.3);

                exp.conductExperimentSingle(mp, task, 500, 10, MCar3DTask.getInitialState(), maxIter, isPara);
                exp.conductExperimentSingle(sp, task, 500, 10, MCar3DTask.getInitialState(), maxIter, isPara);

                List<Task> tasks = new ArrayList<Task>();
                for (int i = 0; i < 100; i++) {
                    tasks.add(task);
                }

                double[][] resultsMP = exp.conductTesting(mp, tasks, MCar3DTask.getInitialState(), maxIter, bais, isPara, random);
                double[][] resultsSP = exp.conductTesting(sp, tasks, MCar3DTask.getInitialState(), maxIter, 0, isPara, random);

                IO.matrixWrite(resultsMP, "/home/lamda/daq/matlab/gradient_boosting/MCar3D/single_a/dataMP_" + k + "_" + t + ".txt");
                IO.matrixWrite(resultsSP, "/home/lamda/daq/matlab/gradient_boosting/MCar3D/single_a/dataSP_" + k + "_" + t + ".txt");
            }
        }
    }
}
