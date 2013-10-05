/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package example.mountaincar2d;

import example.acrobot.*;
import core.Policy;
import core.State;
import core.Task;
import domain.acrobot.AcrobotTask;
import domain.mountaincar2d.MountainCarTask;
import experiment.Experiment;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import policy.NPPGPolicy;
import utills.IO;

/**
 *
 * @author daq
 */
public class TestNPPGPolicy {

    static int maxStep = 2000;
    static boolean isPara = true;

    public static void main(String[] args) throws Exception {
        testNPPGPolicy();
    }

    private static void testNPPGPolicy() {
        Random random = new Random();
        Task task = new MountainCarTask(new Random(random.nextInt()));
        State initialState = task.getInitialState();

        NPPGPolicy gbPolicy = new NPPGPolicy(new Random(random.nextInt()));
        gbPolicy.setStationaryRate(0.7);
        gbPolicy.setStepsize(0.1);
        gbPolicy.setEpsionGreedy(0.2);
        gbPolicy.setMaxStep(maxStep);
        
        Experiment exp = new Experiment();
        exp.conductExperimentTrain(gbPolicy,task, 100, 50, initialState, maxStep, true, 0.2, random);
    }
}
