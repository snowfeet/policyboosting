/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.io.Serializable;
import java.util.Random;

/**
 *
 * @author daq
 */
public abstract class Task implements Serializable{

    public int taskDimension;
    public double[] taskParameter;
    public int[] actionSet;

    public abstract State transition(State s, Action a, Random outRand);

    public abstract double immediateReward(State s);

    public abstract double[] getSATFeature(State s, Action a);

    public abstract boolean isComplete(State s);
}
