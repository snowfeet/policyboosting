/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package example.acrobot;

import experiment.Experiment;
import core.State;
import core.Task;
import domain.acrobot.AcrobotTask;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import policy.BoostedPolicy;
import utills.IO;

/**
 *
 * @author daq
 */
public class TestBoostedPolicy {

    static int maxStep = 2000;
    static boolean isPara = true;

    public static void main(String[] args) throws Exception {
        Random random = new Random();
        Task task = new AcrobotTask(new Random(random.nextInt()));
        State initialState = task.getInitialState();

        Experiment exp = new Experiment();

        BoostedPolicy bp = new BoostedPolicy(new Random(random.nextInt()));
        bp.setStepsize(0.5);

        exp.conductExperimentTrain(bp, task, 100, 50, initialState, maxStep, isPara, new Random(random.nextInt()));
    }
}
