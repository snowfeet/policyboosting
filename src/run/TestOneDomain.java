/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package run;

import experiment.Experiment;
import core.State;
import core.Task;
import domain.mountaincar3d.MCar3DTask;
import domain.mountaincar3d.MCar3DTaskSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import policy.GBMetaPolicy;
import utills.IO;

/**
 *
 * @author daq
 */
public class TestOneDomain {

    static int maxIter = 5000;
    static boolean isPara = true;

    public static void main(String[] args) throws Exception {
        Random random = new Random();
        MCar3DTaskSet taskSet = new MCar3DTaskSet(new Random(random.nextInt()));
        State initialState = MCar3DTask.getInitialState();

        int numTest = 20;
        List<Task> tasks = new ArrayList<Task>(numTest);
        for (int i = 0; i < numTest; i++) {
            tasks.add(taskSet.generateTasks());
        }

        Experiment exp = new Experiment();

        GBMetaPolicy mp = new GBMetaPolicy(random);
        mp.setStepsize(1);

        exp.conductTrainAndTest(mp, taskSet, tasks, 100, 20, 1, initialState, maxIter, isPara, random);
        System.out.println();
//        double[][] results = exp.conductTesting2(mp, tasks, initialState, maxIter, 0, isPara, random);
//        System.out.println();
        double[][] results = exp.conductTesting(mp, tasks, initialState, maxIter, 0, isPara, random);
        IO.matrixWrite(results, "/home/lamda/daq/matlab/gradient_boosting/MCar3D/MCar3D_origin.txt");
    }
}
