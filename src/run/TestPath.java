/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package run;

import core.Execution;
import core.Task;
import core.Tuple;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import policy.GBMetaPolicy;
import domain.puddle.PuddleState;
import domain.puddle.PuddleTask;
import domain.puddle.PuddleTaskSet;
import utills.IO;

/**
 *
 * @author daq
 */
public class TestPath {

    static PuddleState initialState = new PuddleState(new Point2D.Double(0.5, 0.5));
    static int maxIter = 500;

    public static void main(String[] args) throws Exception {
        System.out.println("111");
        testPuddlePath();
    }

    private static void testPuddlePath() throws IOException {
        Random random = new Random();
        PuddleTaskSet taskSet = new PuddleTaskSet(new Random(random.nextInt()));

        int numTest = 10;
        List<Task> tasks = new ArrayList<Task>(numTest);
        for (int i = 0; i < numTest; i++) {
            tasks.add(taskSet.generateTasks());
        }

        GBMetaPolicy mp = (GBMetaPolicy) IO.loadObject("gb100_puddle.mpl");
        System.out.println("loaded!");

        for (int i = 0; i < numTest; i++) {
            PuddleTask task = (PuddleTask) tasks.get(i);
            List<Tuple> samples = Execution.runTask(task, initialState, mp, maxIter, false, random);

            double[][] results = new double[samples.size() + 1][2];
            results[0][0] = task.goalRect.getCenterX();
            results[0][1] = task.goalRect.getCenterY();

            for (int j = 0; j < samples.size(); j++) {
                Tuple tuple = samples.get(j);
                results[j + 1][0] = ((PuddleState) tuple.s).s.getX();
                results[j + 1][1] = ((PuddleState) tuple.s).s.getY();
            }
            IO.matrixWrite(results, "/home/lamda/daq/matlab/gradient_boosting/puddle/path_puddle" + i + ".txt");
            System.out.println(i + "th's done!");
        }
    }
}
