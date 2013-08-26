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

    public abstract double[] getUtility(State s, Task t);

    @Override
    public Action makeDecisionD(State s, Task t, Random outRand) {
        if (numIteration == 0) {
            return null;
        }

        Random thisRand = outRand == null ? random : outRand;
        int K = t.actions.length;

        double[] probabilities = getProbability(s, t);
        int bestAction = 0, m = 2;
        for (int k = 1; k < K; k++) {
            if (probabilities[k] > probabilities[bestAction] + Double.MIN_VALUE) {
                bestAction = k;
                m = 2;
            } else if (Math.abs(probabilities[k] - probabilities[bestAction]) <= Double.MIN_VALUE) {
                if (thisRand.nextDouble() < 1.0 / m) {
                    bestAction = k;
                }
                m++;
            }
        }

        return new Action(bestAction);
    }

    @Override
    public PrabAction makeDecisionS(State s, Task t, Random outRand) {
        if (numIteration == 0) {
            return null;
        }

        Random thisRand = outRand == null ? random : outRand;

        double[] probabilities = getProbability(s, t);
        return makeDecisionS(s, t, probabilities, thisRand);
    }

    public PrabAction makeDecisionS(State s, Task t, double[] probabilities, Random outRand) {
        if (numIteration == 0 || probabilities == null) {
            return null;
        }

        Random thisRand = outRand == null ? random : outRand;
        int K = t.actions.length;

        int bestAction = 0, m = 2;
        for (int k = 1; k < K; k++) {
            if (probabilities[k] > probabilities[bestAction] + Double.MIN_VALUE) {
                bestAction = k;
                m = 2;
            } else if (Math.abs(probabilities[k] - probabilities[bestAction]) <= Double.MIN_VALUE) {
                if (thisRand.nextDouble() < 1.0 / m) {
                    bestAction = k;
                }
                m++;
            }
        }

        return new PrabAction(bestAction, probabilities[bestAction]);
    }

    public double[] getProbability(State s, Task t) {
        double[] utilities = getUtility(s, t);
        return getProbability(utilities);
    }

    public double[] getProbability(double[] utilities) {
        double[] probabilities = new double[utilities.length];
        double maxUtility = Double.NEGATIVE_INFINITY;
        for (int k = 0; k < utilities.length; k++) {
            if (utilities[k] > maxUtility) {
                maxUtility = utilities[k];
            }
        }

        double norm = 0;
        for (int k = 0; k < utilities.length; k++) {
            probabilities[k] = Math.exp(utilities[k] - maxUtility + 10);
            norm += probabilities[k];
        }


        for (int k = 0; k < probabilities.length; k++) {
            probabilities[k] /= norm;
        }

        if (numIteration == 160) {
            for (int i = 0; i < probabilities.length; i++) {
                System.err.print(probabilities[i] + ",");
            }
            System.err.println();
        }

        return probabilities;
    }
}
