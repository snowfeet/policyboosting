package run;

import experiment.Experiment;
import core.Task;
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
public class TestGBMetaPolicy2 {

    static PuddleState initialState = new PuddleState(new Point2D.Double(0.5, 0.5));
    static int maxIter = 500;
    static boolean isPara = false;

    public static void main(String[] args) throws Exception {
        for (int i = 24; i < 100; i++) {
            System.out.println("gb iter = " + i);
            testMeta(i + "");
        }
//        testMetaWithSingle();
    }

    private static void testMeta(String post) throws IOException {
        Random random = new Random();
        PuddleTaskSet taskSet = new PuddleTaskSet(new Random(random.nextInt()));

//        int numTest = 2000;
//        List<Task> tasks = new ArrayList<Task>(numTest);
//        for (int i = 0; i < numTest; i++) {
//            tasks.add(taskSet.generateTasks());
//        }
//        IO.saveObject("puddle.task", tasks);
        List<Task> tasks = (List<Task>) IO.loadObject("puddle2000.task");

        Experiment exp = new Experiment();
        GBMetaPolicy mp = new GBMetaPolicy(new Random(random.nextInt()));
        mp.setStepsize(0.15);
        exp.conductExperiment(mp, taskSet, 100, 20, 5, initialState, maxIter, isPara, random);
        IO.saveObject("gb100_puddle_" + post + ".mpl", mp);
//        GBMetaPolicy mp = (GBMetaPolicy) IO.loadObject("gb50_puddle.mpl");

        double[][] results = exp.conductTesting(mp, tasks, initialState, maxIter, 0, isPara, random);
        IO.matrixWrite(results, "/home/lamda/daq/matlab/gradient_boosting/puddle/data_puddle" + post + ".txt");
    }

    private static void testMetaWithSingle() throws IOException {
        Random random = new Random();
        PuddleTaskSet taskSet = new PuddleTaskSet(random);
        Experiment exp = new Experiment();

        for (int k = 0; k < 30; k++) {
            Task task = taskSet.generateTasks();

            GBMetaPolicy mp = (GBMetaPolicy) IO.loadObject("gb100_puddle.mpl");
            mp.setStepsize(0.15);
            int bais = mp.getNumIteration();
            GBMetaPolicy sp = new GBMetaPolicy(random);
            sp.setStepsize(0.15);

            exp.conductExperimentSingle(mp, task, 500, 10, initialState, maxIter, isPara);
            exp.conductExperimentSingle(sp, task, 500, 10, initialState, maxIter, isPara);

            List<Task> tasks = new ArrayList<Task>();
            for (int i = 0; i < 100; i++) {
                tasks.add(task);
            }

            double[][] resultsMP = exp.conductTesting(mp, tasks, initialState, maxIter, bais, isPara, random);
            double[][] resultsSP = exp.conductTesting(sp, tasks, initialState, maxIter, 0, isPara, random);

            IO.matrixWrite(resultsMP, "/home/lamda/daq/matlab/gradient_boosting/puddle/dataMP_puddle" + k + ".txt");
            IO.matrixWrite(resultsSP, "/home/lamda/daq/matlab/gradient_boosting/puddle/dataSP_puddle" + k + ".txt");
        }
    }
}
