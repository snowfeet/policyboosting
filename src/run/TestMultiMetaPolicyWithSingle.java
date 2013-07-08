/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package run;

import core.Experiment;
import core.Task;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import policy.GBMetaPolicy;
import policy.MultiMetaPolicy;
import policy.MultiMetaPolicy;
import domain.puddle.PuddleState;
import domain.puddle.PuddleTaskSet;
import utills.IO;

/**
 *
 * @author daq
 */
public class TestMultiMetaPolicyWithSingle {

    static PuddleState initialState = new PuddleState(new Point2D.Double(0.5, 0.5));
    static int maxIter = 500;
    static public int[] sel = {5, 26, 27, 23, 19, 22, 15, 3, 13, 11,
        4, 23, 18, 11, 4, 7, 10, 28, 29, 17,};

    public static void run(String[] args) throws Exception {
//        List<Task> taskSet = (List<Task>) IO.loadObject("puddle2000.task");
//        Random random = new Random();
//        List<Task> taskSet100 = new ArrayList<Task>();
//        for(Task task : taskSet)
//            if(random.nextDouble() < 0.05)
//                taskSet100.add(task);
//        IO.saveObject("puddle100.task", taskSet100);

        for (int t = Integer.parseInt(args[0]); t < Integer.parseInt(args[1]); t++) {
            testSingle(t);
        }

        for (int k = Integer.parseInt(args[2]); k < Integer.parseInt(args[3]); k++) {
            for (int i = 0; i < 29; i++) {
                testMetaWithSingle(k, i);
            }
        }
    }

    private static void testSingle(int T) throws IOException {
        Random random = new Random();
        Experiment exp = new Experiment();

        List<Task> taskSet = (List<Task>) IO.loadObject("puddle100.task");
        for (int test = 0; test < 30; test++) {
            System.out.println("Testing " + test + ": " +T);
            Task task = taskSet.get(test);

            GBMetaPolicy sp = new GBMetaPolicy(random);
            sp.setStepsize(0.15);

            exp.conductExperimentSingle(sp, task, 100, 20, initialState, maxIter, true);

            List<Task> tasks = new ArrayList<Task>();
            for (int j = 0; j < 100; j++) {
                tasks.add(task);
            }

            double[][] resultsSP = exp.conductTesting(sp, tasks, initialState, maxIter, 0, true, random);

            IO.matrixWrite(resultsSP, "/home/lamda/daq/matlab/gradient_boosting/puddle/MWS_sp_puddle_" + test + "_" + T + ".txt");
        }
    }

    private static void testMetaWithSingle(int k, int i) throws IOException {
        Random random = new Random();
        Experiment exp = new Experiment();

        List<Task> taskSet = (List<Task>) IO.loadObject("puddle100.task");
        for (int test = 0; test < 30; test++) {
            System.out.println("Testing " + test + ": " + k + "/" + i);
            Task task = taskSet.get(test);

            MultiMetaPolicy mmp = (MultiMetaPolicy) IO.loadObject("mmp100_puddle_" + k + "_" + i + ".mpl");
            GBMetaPolicy mp = mmp.getTaskPolicy(task);
            mp.setStepsize(0.15);
            int bais = mp.getNumIteration();

            exp.conductExperimentSingle(mp, task, 100, 20, initialState, maxIter, true);

            List<Task> tasks = new ArrayList<Task>();
            for (int j = 0; j < 100; j++) {
                tasks.add(task);
            }

            double[][] resultsMP = exp.conductTesting(mp, tasks, initialState, maxIter, bais, true, random);

            IO.matrixWrite(resultsMP, "/home/lamda/daq/matlab/gradient_boosting/puddle/MWS_mmp_puddle_" + test + "_" + k + "_" + i + ".txt");
        }
    }
}
