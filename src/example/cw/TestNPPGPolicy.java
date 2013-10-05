/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package example.cw;

import core.State;
import core.Task;
import domain.cw.CWTask;
import experiment.Experiment;
import java.util.Random;
import policy.NPPGPolicy;

/**
 *
 * @author daq
 */
public class TestNPPGPolicy {

    static int maxStep = 200;
    static boolean isPara = false;

    public static void main(String[] args) throws Exception {
        Random random = new Random();
        Task task = new CWTask(new Random(random.nextInt()));
        State initialState = task.getInitialState();

        NPPGPolicy gbPolicy = new NPPGPolicy(new Random(random.nextInt()));
        gbPolicy.setStationaryRate(0.5);
        gbPolicy.setStepsize(0.05);
        gbPolicy.setEpsionGreedy(0.1);
        gbPolicy.setEpsionGreedyDecay(1);
        gbPolicy.setMaxStep(maxStep);

        Experiment exp = new Experiment();
        exp.conductExperimentTrain(gbPolicy, task, 100, 30, initialState, maxStep, true, 0.1, random);
    }
}
