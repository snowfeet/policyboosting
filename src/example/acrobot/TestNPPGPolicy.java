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
import utills.IO;

/**
 *
 * @author daq
 */
public class TestNPPGPolicy {

    static int maxIter = 2000;
    static boolean isPara = true;

    public static void main(String[] args) throws Exception {
        testNPPGPolicy();
    }

    private static void testNPPGPolicy() {
        Random random = new Random();
        AcrobotTask task = new AcrobotTask(new Random(random.nextInt()));

        NPPGPolicy gbPolicy = new NPPGPolicy(new Random(random.nextInt()));
        gbPolicy.setStationaryRate(0.3);
        gbPolicy.setStepsize(0.1);
        gbPolicy.setEpsionGreedy(0.1);
        gbPolicy.setEpsionGreedyDecay(1);
        gbPolicy.train(task, 100, 20, null, maxIter, true, random);
    }
}
