/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package example.mountaincar2d;

import example.acrobot.*;
import core.State;
import core.Task;
import domain.acrobot.AcrobotTask;
import domain.mountaincar2d.MountainCarTask;
import experiment.Experiment;
import java.util.Random;
import policy.BoostedMuiltModelPolicy;

/**
 *
 * @author daq
 */
public class TestBoostedMuiltModelPolicy {

    static int maxStep = 1000;
    static boolean isPara = true;

    public static void main(String[] args) throws Exception {
        Random random = new Random();
        Task task = new MountainCarTask(new Random(random.nextInt()));
        State initialState = task.getInitialState();

        Experiment exp = new Experiment();

        BoostedMuiltModelPolicy bmmp = new BoostedMuiltModelPolicy(new Random(random.nextInt()));
        bmmp.setStepsize(1);

        exp.conductExperimentTrain(bmmp, task, 100, 50, initialState, maxStep, isPara, new Random(random.nextInt()));
    }
}