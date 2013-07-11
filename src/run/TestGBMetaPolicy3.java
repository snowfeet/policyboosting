/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package run;

import experiment.Experiment;
import core.Task;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import domain.mountaincar3d.MCar3DTask;
import domain.mountaincar3d.MCar3DTaskSet;
import policy.GBMetaPolicy;
import policy.MultiMetaPolicy;
import utills.IO;

/**
 *
 * @author daq
 */
public class TestGBMetaPolicy3 {

    static int maxIter = 500;
    static boolean isPara = true;

    public static void main(String[] args) throws Exception {
//        int tryStart = Integer.parseInt(args[0]);
//        int tryEnd = Integer.parseInt(args[1]);
//        int count = 1;
//        for (double p = 0.26; p <= 0.34; p += 0.01) {
//            for (int i = tryStart; i <= tryEnd; i++) {
//                System.out.println(count + ":" + i);
//                testMeta(p, "_" + count + "_" + i);
//            }
//            count++;
//        }
        testMultiTask();
    }

    private static void testMeta(double lamda, String post) throws IOException {
        Random random = new Random();
        MCar3DTaskSet taskSet = new MCar3DTaskSet(new Random(random.nextInt()));

//        int numTest = 2000;
//        List<Task> tasks = new ArrayList<Task>(numTest);
//        for (int i = 0; i < numTest; i++) {
//            tasks.add(taskSet.generateTasks());
//        }
//        IO.saveObject("MCar3D_2000.task", tasks);
        List<Task> tasks = (List<Task>) IO.loadObject("task/MCar3D_2000.task");

        Experiment exp = new Experiment();
        GBMetaPolicy mp = new GBMetaPolicy(new Random(random.nextInt()));
        mp.setStepsize(lamda);

        MultiMetaPolicy mmp = new MultiMetaPolicy(2, new Random(random.nextInt()));
        mmp.setStepsize(lamda);
//         exp.conductExperimentSingle(mp, tasks.get(0), 20, 20, MCar3DTask.getInitialState(), maxIter, isPara);
//        exp.conductExperiment(mp, taskSet, 50, 10, 5, MCar3DTask.getInitialState(), maxIter, isPara, random);
        mmp.train(taskSet, 100, 50, 2, MCar3DTask.getInitialState(), maxIter, true, random);
//        IO.saveObject("gb100_MCar3D_" + post + ".mpl", mp);
//        GBMetaPolicy mp = (GBMetaPolicy) IO.loadObject("gb50_puddle.mpl");

        double[][] results = exp.conductTesting(mmp, tasks, MCar3DTask.getInitialState(), maxIter, 0, isPara, random);
        IO.matrixWrite(results, "/home/lamda/daq/matlab/gradient_boosting/MCar3D/data_c1/data_MCar3D" + post + ".txt");
    }

    private static void testMultiTask() throws IOException {
        Random random = new Random();
//        MCar3DTaskSet taskSet = new MCar3DTaskSet(new Random(random.nextInt()));

//        int numTest = 50;
//        List<Task> tasks = new ArrayList<Task>(numTest);
//        for (int i = 0; i < numTest; i++) {
//            tasks.add(taskSet.generateTasks());
//        }
//        IO.saveObject("task/mt_50.task", tasks);
        List<Task> tasks = (List<Task>) IO.loadObject("task/mt_50.task");

        for (int i = 1; i <= 30; i++) {
            MultiMetaPolicy mmp = new MultiMetaPolicy(4, new Random(random.nextInt()));
            mmp.setStepsize(0.3);
            mmp.trainMT(tasks, 125, 2, MCar3DTask.getInitialState(), maxIter, true, random);
            Experiment exp = new Experiment();
            double[][] results = exp.conductTesting(mmp, tasks, MCar3DTask.getInitialState(), maxIter, 0, isPara, random);
//            IO.matrixWrite(results, "/home/lamda/Storage_SV52/daq/164/matlab/mpl/data_mt2/data_MCar3D_" + i + ".txt");
            IO.matrixWrite(results, "data_mt5/data_MCar3D_" + i + ".txt");
        }

//        for (int t = 0; t < tasks.size(); t++) {
//            Task task = tasks.get(t);
//            for (int i = 1; i <= 30; i++) {
//                GBMetaPolicy mp = new GBMetaPolicy(new Random(random.nextInt()));
//                mp.setStepsize(0.3);
//                Experiment exp = new Experiment();
//                exp.conductExperimentSingle(mp, task, 250, 100, MCar3DTask.getInitialState(), maxIter, isPara);
//
//                List<Task> testTasks = new ArrayList<Task>();
//                for (int j = 0; j < 50; j++) {
//                    testTasks.add(task);
//                }
//
//                double[][] results = exp.conductTesting(mp, testTasks, MCar3DTask.getInitialState(), maxIter, 0, isPara, random);
//                IO.matrixWrite(results, "/home/lamda/Storage_SV52/daq/164/matlab/mpl/data_single4/data_MCar3D_" + (t + 1) + "_" + i + ".txt");
//            }
//        }
    }
}
