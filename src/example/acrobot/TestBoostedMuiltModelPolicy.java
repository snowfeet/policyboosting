/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package example.acrobot;

import core.State;
import core.Task;
import domain.acrobot.AcrobotTask;
import experiment.Experiment;
import java.util.Random;
import policy.BoostedMuiltModelPolicy;

/**
 *
 * @author daq
 */
public class TestBoostedMuiltModelPolicy {

    static int maxStep = 2000;
    static boolean isPara = true;

    public static void main(String[] args) throws Exception {
        Random random = new Random();
        Task task = new AcrobotTask(new Random(0));
        State initialState = task.getInitialState();

        Experiment exp = new Experiment();

        BoostedMuiltModelPolicy bmmp = new BoostedMuiltModelPolicy(new Random(random.nextInt()));
        bmmp.setStepsize(0.001);

        exp.conductExperimentTrain(bmmp, task, 100, 50, initialState, maxStep, isPara, new Random(random.nextInt()));
    }
}