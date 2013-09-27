/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package policy;

import core.Action;
import core.Policy;
import core.PrabAction;
import experiment.Trajectory;
import core.State;
import core.Task;
import java.util.List;
import java.util.Random;

/**
 *
 * @author daq
 */
public class RandomCAPolicy extends Policy {

    Random random;

    public RandomCAPolicy(Random rand) {
        random = rand;
    }

    @Override
    public PrabAction makeDecisionS(State s, Task t, Random outRand) {
        Random thisRand = outRand == null ? random : outRand;

        double[] controls = new double[t.actionDim];
        for (int i = 0; i < controls.length; i++) {
            controls[i] = -0.1 + 0.2 * thisRand.nextDouble();
        }

        return new PrabAction(controls, 1);
    }

    @Override
    public Action makeDecisionD(State s, Task t, Random outRand) {
        return makeDecisionS(s, t, outRand);
    }

    @Override
    public void update(List<Trajectory> rollouts) {
    }

    @Override
    public void setNumIteration(int numIteration) {
    }
}
