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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Action makeDecisionD(State s, Task t, Random outRand) {
        Random thisRand = outRand == null ? random : outRand;
        Action policyAction = policy.makeDecisionD(s, t, thisRand);

        PrabAction action = null;
        if (thisRand.nextDouble() < epsion || policyAction == null) {
            action = new PrabAction(rp.makeDecisionD(s, t, thisRand), -1);
            action.setProbability(epsion);
        } else {
            action = new PrabAction(policyAction, -1);
        }

        if (action.a == policyAction.a) {
            action.setProbability(epsion / t.actions.length + (1 - epsion));
        } else {
            action.setProbability(epsion / t.actions.length);
        }
        return action;
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
