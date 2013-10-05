/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package domain.cw;

import java.util.Random;
import core.Action;
import core.State;
import core.Task;

/**
 *
 * @author daq
 */
public class CWTask extends Task {

    public double noise_mean;
    public double noise_sigma;
    private Random random;

    public CWTask(Random rand) {
        this.noise_mean = 0;
        this.noise_sigma = .2;
        this.actions = new Action[2];
        for (int a = 0; a < actions.length; a++) {
            actions[a] = new Action(a);
        }
        this.random = rand;
    }

    @Override
    public State transition(State s, Action action, Random outRand) {
        Random thisRand = outRand == null ? random : outRand;
        CWState cs = (CWState) s;
        int move = action.a == 0 ? -1 : 1;
        return new CWState(cs.x + move * 0.2 + (noise_mean + thisRand.nextGaussian()) * noise_sigma);
    }

    @Override
    public double immediateReward(State s) {
        if (isComplete(s)) {
            return 0;
        } else {
            return -1;
        }
    }

    @Override
    public boolean isComplete(State s) {
        CWState cs = (CWState) s;
        if (cs.x >= 10 || cs.x <= 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public State getInitialState() {
        return new CWState((random.nextDouble() - 0.5) * 2 + 5);
    }
}
