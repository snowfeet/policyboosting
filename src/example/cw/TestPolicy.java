/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package example.cw;

import core.State;
import core.Task;
import domain.cw.CWTask;
import static example.cw.TestNPPGPolicy.maxStep;
import experiment.Experiment;
import java.util.Random;
import policy.NPPGPolicy;
import policy.RankBoostPoolPolicy;
import policy.RankBoostPoolWithoutEXPPolicy;
import utills.IO;

/**
 *
 * @author daq
 */
public class TestPolicy {

    static int maxStep = 200;
    static boolean isPara = true;

    public static void run(int trial) throws Exception {
        Random random = new Random();
        Task task = new CWTask(new Random(random.nextInt()));
        State initialState = task.getInitialState();

        Experiment exp = null;

//        NPPGPolicy gbPolicy = new NPPGPolicy(new Random(random.nextInt()));
//        gbPolicy.setStationaryRate(0.5);
//        gbPolicy.setStepsize(0.05);
//        gbPolicy.setEpsionGreedy(0.1);

//        gbPolicy.setStationaryRate(0.5);
//        gbPolicy.setStepsize(1);
//        gbPolicy.setEpsionGreedy(0);
//
//        gbPolicy.setEpsionGreedyDecay(1);
//        gbPolicy.setMaxStep(maxStep);
//        Experiment exp = new Experiment();
//        double[][] resultsNPPG = exp.conductExperimentTrain(gbPolicy, task, 100, 30, initialState, maxStep, true, 0, new Random(random.nextInt()));

//        RankBoostPoolPolicy bp = new RankBoostPoolPolicy(new Random(random.nextInt()));
//        bp.setStepsize(1);
//        exp = new Experiment();
//        double[][] resultsPB = exp.conductExperimentTrain(bp, task, 100, 30, initialState, maxStep, isPara, 0, new Random(random.nextInt()));
//
        RankBoostPoolWithoutEXPPolicy pbNoExp = new RankBoostPoolWithoutEXPPolicy(new Random(random.nextInt()));
        pbNoExp.setStepsize(1);
        exp = new Experiment();
        double[][] resultsPBNoExp = exp.conductExperimentTrain(pbNoExp, task, 100, 30, initialState, maxStep, isPara, 0, new Random(random.nextInt()));

//        IO.matrixWrite(resultsNPPG, "results/cw/NPPGF_trial_" + trial + ".txt");
//        IO.matrixWrite(resultsPB, "results/cw/PB_trial_" + trial + ".txt");
        IO.matrixWrite(resultsPBNoExp, "results/cw/PBNoPool_trial_" + trial + ".txt");
    }
}
