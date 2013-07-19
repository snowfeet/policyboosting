/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import experiment.Rollout;
import java.util.List;
import java.util.Random;
import policy.RandomPolicy;

/**
 *
 * @author daq
 */
public class EpsionGreedyExplorePolicy extends ExplorePolicy {

    private Policy rp;
    private Policy policy;
    private double epsion;

    public EpsionGreedyExplorePolicy(Policy policy, double epsion, Random random) {
        this.rp = new RandomPolicy(new Random(random.nextInt()));
        this.policy = policy;
        this.epsion = epsion;
        this.random = random;
    }

    @Override
    public PrabAction makeDecisionS(State s, Task t, Random outRand) {
        Random thisRand = outRand == null ? random : outRand;
        PrabAction action = null;
        if (thisRand.nextDouble() < epsion) {
            action = rp.makeDecisionS(s, t, thisRand);
        } else {
            action = policy.makeDecisionS(s, t, thisRand);
            if (action == null) {
                action = rp.makeDecisionS(s, t, thisRand);
            }
        }
        return action;
    }

    @Override
    public Action makeDecisionD(State s, Task t, Random outRand) {
        Random thisRand = outRand == null ? random : outRand;

        if (thisRand.nextDouble() < epsion) {
            return rp.makeDecisionD(s, t, thisRand);
        } else {
            Action action = policy.makeDecisionS(s, t, thisRand);
            if (action == null) {
                return rp.makeDecisionD(s, t, thisRand);
            } else {
                return action;
            }
        }
    }

    @Override
    public void update(List<Rollout> rollouts) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setNumIteration(int numIteration) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
