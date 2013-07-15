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

    public Action[] actions;

    public abstract State getInitialState();

    public abstract State transition(State s, Action a, Random outRand);

    public abstract double immediateReward(State s);

    public abstract boolean isComplete(State s);

    public double[] getSAFeature(State s, Action action) {
        double[] feature = s.extractFeature();
        double[] saFea = new double[feature.length + 1];
        System.arraycopy(feature, 0, saFea, 0, feature.length);
        saFea[saFea.length - 1] = action.a;
        return saFea;
    }
}
