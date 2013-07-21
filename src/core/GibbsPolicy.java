/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.util.Random;

/**
 *
 * @author daq
 */
public abstract class GibbsPolicy extends Policy {

    public abstract double[] getProbability(State s, Task t);

    public abstract PrabAction makeDecisionS(State s, Task t, double[] probabilities, Random outRand);
}
