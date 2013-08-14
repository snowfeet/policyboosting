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

    public abstract PrabAction makeDecisionS(State s, Task t, double[] probabilities, Random outRand);

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

//        if (numIteration == 1) {
//            for (int i = 0; i < probabilities.length; i++) {
//                System.err.print(probabilities[i] + ",");
//            }
//            System.err.println();
//        }
        return probabilities;
    }
}
