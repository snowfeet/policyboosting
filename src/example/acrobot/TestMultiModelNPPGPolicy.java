/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package example.acrobot;

import domain.acrobot.AcrobotTask;
import java.util.Random;
import policy.MultiModelNPPGPolicy;

/**
 *
 * @author daq
 */
public class TestMultiModelNPPGPolicy {

    static int maxIter = 2000;
    static boolean isPara = true;

    public static void main(String[] args) throws Exception {
        testNPPGPolicy();
    }

    private static void testNPPGPolicy() {
        Random random = new Random();
        AcrobotTask task = new AcrobotTask(new Random(random.nextInt()));

        MultiModelNPPGPolicy gbPolicy = new MultiModelNPPGPolicy(new Random(random.nextInt()));
        gbPolicy.setStationaryRate(0.3);
        gbPolicy.setStepsize(0.1);
        gbPolicy.setEpsionGreedy(0.1);
        gbPolicy.setEpsionGreedyDecay(1);
        gbPolicy.train(task, 100, 20, null, maxIter, true, random);
    }
}
