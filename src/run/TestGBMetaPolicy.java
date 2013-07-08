/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package run;

import core.Experiment;
import core.Task;
import domain.cwk.CrwState;
import domain.cwk.CrwTask;
import domain.cwk.CrwTaskSet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import policy.GBMetaPolicy;
import utills.IO;

/**
 *
 * @author daq
 */
public class TestGBMetaPolicy {

    static boolean isPara = false;

    public static void main(String[] args) throws Exception {
        testMeta();
//        testMetaWithSingle();
    }

    private static void testMeta() throws IOException {
        Random random = new Random();
        CrwTaskSet taskSet = new CrwTaskSet(random);
        Experiment exp = new Experiment();

        GBMetaPolicy mp = new GBMetaPolicy(random);
        exp.conductExperiment(mp, taskSet, 50, 10, 5, new CrwState(0), 500, isPara, random);
//        IO.saveObject("gb50.mpl", mp);
//        GBMetaPolicy mp = (GBMetaPolicy) IO.loadObject("gb.mpl");

        int numTest = 1000;
        List<Task> tasks = new ArrayList<Task>(numTest);
        for (int i = 0; i < numTest; i++) {
            tasks.add(taskSet.generateTasks());
        }
        IO.saveObject("crw.task", tasks);
//        List<Task> tasks = (List<Task>) IO.loadObject("crw.task");

        double[][] results = exp.conductTesting(mp, tasks, new CrwState(0), 500, 0, isPara, random);

        IO.matrixWrite(results, "/home/lamda/daq/matlab/gradient_boosting/crw/dataD.txt");
    }

    private static void testMetaWithSingle() throws IOException {
        Random random = new Random();
        CrwTaskSet taskSet = new CrwTaskSet(random);
        Experiment exp = new Experiment();

        for (int k = 0; k < 30; k++) {
            Task task = taskSet.generateTasks();
            System.out.println(k + "\t" + ((CrwTask) task).g1);

            GBMetaPolicy mp = (GBMetaPolicy) IO.loadObject("gb50.mpl");
            int bais = mp.getNumIteration();
            GBMetaPolicy sp = new GBMetaPolicy(random);

            exp.conductExperimentSingle(mp, task, 50, 10, new CrwState(0), 500, isPara);
            exp.conductExperimentSingle(sp, task, 50, 10, new CrwState(0), 500, isPara);

            List<Task> tasks = new ArrayList<Task>();
            for (int i = 0; i < 100; i++) {
                tasks.add(task);
            }

            double[][] resultsMP = exp.conductTesting(mp, tasks, new CrwState(0), 500, bais, isPara, random);
            double[][] resultsSP = exp.conductTesting(sp, tasks, new CrwState(0), 500, 0, isPara, random);

            IO.matrixWrite(resultsMP, "/home/lamda/daq/matlab/gradient_boosting/crw/dataMP" + k + ".txt");
            IO.matrixWrite(resultsSP, "/home/lamda/daq/matlab/gradient_boosting/crw/dataSP" + k + ".txt");
        }
    }
}
