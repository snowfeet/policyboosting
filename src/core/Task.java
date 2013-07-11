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
public abstract class Task implements Serializable {

    public abstract State getInitialState();

    public abstract State transition(State s, Action a, Random outRand);

    public abstract double immediateReward(State s);

    public abstract boolean isComplete(State s);
}
