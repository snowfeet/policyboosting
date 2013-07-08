/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package domain.cwk;

import core.Action;
import core.State;
import core.Task;
import java.util.Random;

/**
 *
 * @author daq
 */
public class CrwTask extends Task {

    public double p;
    public double l;
    public double n;
    public double g1;
    public double g2;
    private Random random;

    public CrwTask(double p, double l, double n, double g1, double g2, Random rand) {
        this.p = p;
        this.l = l;
        this.n = n;
        this.g1 = g1;
        this.g2 = g2;
        this.actionSet = new int[]{-1, 1};
        this.random = rand;

        this.taskDimension = 2;
        this.taskParameter = new double[]{g1, g2};
    }

    @Override
    public State transition(State s, Action a, Random outRand) {
        Random thisRand = outRand == null ? random : outRand;
        CrwState cs = (CrwState) s;
        CrwState newState = null;

        int move = actionSet[a.index];
        if (thisRand.nextDouble() < p) {
            newState = new CrwState(cs.x + move * (l + thisRand.nextGaussian() * n));
        } else {
            newState = new CrwState(cs.x - move * (l + thisRand.nextGaussian() * n));
        }

        return newState;
    }

    @Override
    public double immediateReward(State s) {
        CrwState cs = (CrwState) s;
        if (cs.x >= g1 && cs.x <= g1 + g2) {
            return 0;
        } else {
            return -1;
        }
    }

    @Override
    public double[] getSATFeature(State s, Action a) {
        double[] feature = s.extractFeature();
        double[] satFea = new double[feature.length + 1 + taskDimension];
        System.arraycopy(taskParameter, 0, satFea, 0, taskDimension);
        System.arraycopy(feature, 0, satFea, taskDimension, feature.length);
        satFea[satFea.length - 1] = actionSet[a.index];
        return satFea;
    }

    @Override
    public boolean isComplete(State s) {
        CrwState cs = (CrwState) s;
        if (cs.x >= g1 && cs.x <= g1 + g2) {
            return true;
        } else {
            return false;
        }
    }
}
