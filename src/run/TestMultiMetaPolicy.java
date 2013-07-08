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
import policy.MultiMetaPolicy;
import domain.puddle.PuddleState;
import domain.puddle.PuddleTaskSet;
import utills.IO;

/**
 *
 * @author daq
 */
public class TestMultiMetaPolicy {

    static PuddleState initialState = new PuddleState(new Point2D.Double(0.5, 0.5));
    static int maxIter = 500;

    public static void main(String[] args) throws Exception {
        testMeta("test",3);
//        for (int i = 0; i < 100; i++) {
//            System.out.println("mmp iter = " + i);
//            testMeta(i + "");
//        }
//        TestGBMetaPolicy2.main(args);

//        for (int k = 14; k <= 20; k++) {
//            int s = k == 14 ? 25 : 0;
//            for (int i = s; i < 30; i++) {
//                System.out.println(k + "," + i);
//                testMeta(k + "_" + i, k);
//            }
//        }

//        TestMultiMetaPolicyWithSingle.run(args);
    }

    private static void testMeta(String post, int K) throws IOException {
        Random random = new Random();
        PuddleTaskSet taskSet = new PuddleTaskSet(new Random(random.nextInt()));

        int numTest = 2000;
        List<Task> tasks = new ArrayList<Task>(numTest);
        for (int i = 0; i < numTest; i++) {
            tasks.add(taskSet.generateTasks());
        }
//        IO.saveObject("puddle2000.task", tasks);
//        List<Task> tasks = (List<Task>) IO.loadObject("puddle2000.task");

        MultiMetaPolicy mmp = new MultiMetaPolicy(K, new Random(random.nextInt()));
        mmp.setStepsize(0.15);
        mmp.train(taskSet, 100, 20, 5, initialState, maxIter, true, random);
        IO.saveObject("mmp100_puddle_" + post + ".mpl", mmp);
//        MultiMetaPolicy mmp = (MultiMetaPolicy) IO.loadObject("mmp100_puddle.mpl");

        Experiment exp = new Experiment();
        double[][] results = exp.conductTesting(mmp, tasks, initialState, maxIter, 0, true, random);
        IO.matrixWrite(results, "/home/lamda/daq/matlab/gradient_boosting/puddle/mmp_puddle_" + post + ".txt");
    }
}
