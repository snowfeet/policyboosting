/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package policy;

import core.Action;
import core.Policy;
import experiment.Rollout;
import core.State;
import core.Task;
import java.util.List;
import java.util.Random;

/**
 *
 * @author daq
 */
public class RandomPolicy extends Policy {

    Random random;

    public RandomPolicy(Random rand) {
        random = rand;
    }

    @Override
    public Action makeDecisionS(State s, Task t, Random outRand) {
        Random thisRand = outRand == null ? random : outRand;
        int K = t.actionSet.length;

        double[] utilities = new double[K];
        double norm = 0;
        for (int k = 0; k < K; k++) {
            utilities[k] = 1;
            norm += utilities[k];
        }
        for (int k = 0; k < K; k++) {
            utilities[k] /= norm;
        }

        int bestAction = -1;
        double p = thisRand.nextDouble(), totalShare = 0;
        for (int k = 0; k < K; k++) {
            totalShare += utilities[k];
            if (p <= totalShare) {
                bestAction = k;
                break;
            }
        }

        return new Action(bestAction, utilities[bestAction]);
    }

    @Override
    public Action makeDecisionD(State s, Task t, Random outRand) {
        Random thisRand = outRand == null ? random : outRand;
        int K = t.actionSet.length;
        return new Action(thisRand.nextInt(K), 1);
    }

    @Override
    public void update(List<Rollout> rollouts) {
    }

    @Override
    public void setNumIteration(int numIteration) {
    }
}
