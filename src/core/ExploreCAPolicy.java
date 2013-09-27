/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import experiment.Trajectory;
import java.util.List;
import java.util.Random;
import policy.RandomCAPolicy;

/**
 *
 * @author daq
 */
public class ExploreCAPolicy extends ExplorePolicy {

    private Policy rp;
    private Policy policy;
    private double epsion;

    public ExploreCAPolicy(Policy policy, double epsion, Random random) {
        this.rp = new RandomCAPolicy(new Random(random.nextInt()));
        this.policy = policy;
        this.epsion = epsion;
        this.random = random;
    }

    @Override
    public PrabAction makeDecisionS(State s, Task t, Random outRand) {
        Random thisRand = outRand == null ? random : outRand;
        PrabAction policyAction = policy.makeDecisionS(s, t, thisRand);
        if (policyAction == null) {
            policyAction = rp.makeDecisionS(s, t, random);
        }
        return policyAction;
    }

    @Override
    public Action makeDecisionD(State s, Task t, Random outRand) {
        throw new UnsupportedOperationException("Not supported yet.");
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
