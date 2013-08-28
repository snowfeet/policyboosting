/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import experiment.Trajectory;
import java.io.Serializable;
import java.util.List;
import java.util.Random;

/**
 *
 * @author daq
 */
public abstract class Policy implements Serializable {

    protected int numIteration;
    protected Random random;

    public abstract PrabAction makeDecisionS(State s, Task t, Random random);

    public abstract Action makeDecisionD(State s, Task t, Random random);

    public abstract void update(List<Trajectory> rollouts);

    public int getNumIteration() {
        return numIteration;
    }

    public abstract void setNumIteration(int numIteration);

    public Random getRandom() {
        return random;
    }
}
