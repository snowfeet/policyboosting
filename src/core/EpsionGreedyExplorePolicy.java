/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import experiment.Trajectory;
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
        double[] probabilities = ((GibbsPolicy) policy).getProbability(s, t);
        Action policyAction = ((GibbsPolicy) policy).makeDecisionS(s, t, probabilities, thisRand);

        PrabAction action = null;
        if (thisRand.nextDouble() < epsion || policyAction == null) {
            action = new PrabAction(rp.makeDecisionD(s, t, thisRand), -1);
            action.setProbability(epsion);
        } else {
            action = new PrabAction(policyAction, -1);
        }

        if (policyAction == null) {
            action.setProbability(1.0 / t.actions.length);
        } else {
            action.setProbability(epsion / t.actions.length + (1 - epsion) * probabilities[action.a]);
        }
        return action;
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

        if (policyAction == null) {
            action.setProbability(1.0 / t.actions.length);
        } else if (action.a == policyAction.a) {
            action.setProbability(epsion / t.actions.length + (1 - epsion));
        } else {
            action.setProbability(epsion / t.actions.length);
        }
        return action;
    }

    @Override
    public void update(List<Trajectory> rollouts) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setNumIteration(int numIteration) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
