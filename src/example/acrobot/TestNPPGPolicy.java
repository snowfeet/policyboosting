/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package example.acrobot;

import core.Policy;
import core.State;
import core.Task;
import domain.acrobot.AcrobotTask;
import experiment.Experiment;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import policy.NPPGPolicy;
import policy.RankBoostPoolCAPolicy;
import utills.IO;

/**
 *
 * @author daq
 */
public class TestNPPGPolicy {

    static int maxStep = 2000;
    static boolean isPara = false;

    public static void main(String[] args) throws Exception {
        testNPPGPolicy();
    }

    private static void testNPPGPolicy() {
        Random random = new Random();
        Task task = new AcrobotTask(new Random(random.nextInt()));
        State initialState = task.getInitialState();

        NPPGPolicy gbPolicy = new NPPGPolicy(new Random(random.nextInt()));
        gbPolicy.setStationaryRate(0.3);
        gbPolicy.setStepsize(0.08);
        gbPolicy.setEpsionGreedy(0.1);
        gbPolicy.setMaxStep(maxStep);
        
        Experiment exp = new Experiment();
        exp.conductExperimentTrain(gbPolicy,task, 100, 50, initialState, maxStep, true, 0.1, random);
    }
}
