/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package domain.acrobot;

import java.util.Random;
import core.Action;
import core.State;
import core.Task;

/**
 *
 * @author daq
 */
public class AcrobotTask extends Task {

    private final static double maxTheta1Dot = 4 * Math.PI;
    private final static double maxTheta2Dot = 9 * Math.PI;
    private final static double m1 = 1.0;
    private final static double m2 = 1.0;
    private final static double l1 = 1.0;
    private final static double l2 = 1.0;
    private final static double lc1 = 0.5;
    private final static double lc2 = 0.5;
    private final static double I1 = 1.0;
    private final static double I2 = 1.0;
    private final static double g = 9.8;
    private final static double dt = 0.05;
    private final static double acrobotGoalPosition = 1.0;
    private Random random;
    private double transitionNoise = 0.0d;

    public AcrobotTask(Random random) {
        this.random = random;
        this.actions = new Action[3];
        for(int a=0;a<3;a++)
            actions[a] = new Action(a);
    }

    @Override
    public State transition(State s, Action action, Random outRand) {
        Random thisRand = outRand == null ? random : outRand;
        AcrobotState as = (AcrobotState) s;

        double theta1 = as.theta1;
        double theta2 = as.theta2;
        double theta1Dot = as.theta1Dot;
        double theta2Dot = as.theta2Dot;

        int theAction = action.a;
        double torque = theAction - 1.0d;
        double d1;
        double d2;
        double phi_2;
        double phi_1;

        double theta2_ddot;
        double theta1_ddot;

        //torque is in [-1,1]
        //We'll make noise equal to at most +/- 1
        double theNoise = transitionNoise * 2.0d * (thisRand.nextDouble() - .5d);

        torque += theNoise;

        int count = 0;
        while (!isComplete(s) && count < 4) {
            count++;

            d1 = m1 * Math.pow(lc1, 2) + m2 * (Math.pow(l1, 2) + Math.pow(lc2, 2) + 2 * l1 * lc2 * Math.cos(theta2)) + I1 + I2;
            d2 = m2 * (Math.pow(lc2, 2) + l1 * lc2 * Math.cos(theta2)) + I2;

            phi_2 = m2 * lc2 * g * Math.cos(theta1 + theta2 - Math.PI / 2.0);
            phi_1 = -(m2 * l1 * lc2 * Math.pow(theta2Dot, 2) * Math.sin(theta2) - 2 * m2 * l1 * lc2 * theta1Dot * theta2Dot * Math.sin(theta2)) + (m1 * lc1 + m2 * l1) * g * Math.cos(theta1 - Math.PI / 2.0) + phi_2;

            theta2_ddot = (torque + (d2 / d1) * phi_1 - m2 * l1 * lc2 * Math.pow(theta1Dot, 2) * Math.sin(theta2) - phi_2) / (m2 * Math.pow(lc2, 2) + I2 - Math.pow(d2, 2) / d1);
            theta1_ddot = -(d2 * theta2_ddot + phi_1) / d1;

            theta1Dot += theta1_ddot * dt;
            theta2Dot += theta2_ddot * dt;

            theta1 += theta1Dot * dt;
            theta2 += theta2Dot * dt;
        }
        if (Math.abs(theta1Dot) > maxTheta1Dot) {
            theta1Dot = Math.signum(theta1Dot) * maxTheta1Dot;
        }

        if (Math.abs(theta2Dot) > maxTheta2Dot) {
            theta2Dot = Math.signum(theta2Dot) * maxTheta2Dot;
        }
        /* Put a hard constraint on the Acrobot physics, thetas MUST be in [-PI,+PI]
         * if they reach a top then angular velocity becomes zero
         */
        if (Math.abs(theta2) > Math.PI) {
            theta2 = Math.signum(theta2) * Math.PI;
            theta2Dot = 0;
        }
        if (Math.abs(theta1) > Math.PI) {
            theta1 = Math.signum(theta1) * Math.PI;
            theta1Dot = 0;
        }

        return new AcrobotState(theta1, theta2, theta1Dot, theta2Dot);
    }

    @Override
    public double immediateReward(State s) {
        if (isComplete(s)) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public boolean isComplete(State s) {
        AcrobotState as = (AcrobotState) s;
        double theta1 = as.theta1;
        double theta2 = as.theta2;
        double feet_height = -(l1 * Math.cos(theta1) + l2 * Math.cos(theta2));
        double firstJointEndHeight = l1 * Math.cos(theta1);
        double secondJointEndHeight = l2 * Math.sin(Math.PI / 2 - theta1 - theta2);
        feet_height = -(firstJointEndHeight + secondJointEndHeight);
        return (feet_height > acrobotGoalPosition);
    }

    @Override
    public State getInitialState() {
//        return new AcrobotState(0.3, 0.3, 0.3, 0.3);
        return new AcrobotState(random.nextDouble() - .5d,
                random.nextDouble() - .5d,
                random.nextDouble() - .5d,
                random.nextDouble() - .5d);
    }

    public State getRandomState() {
        double[] fea = new double[4];
        fea[0] = -Math.PI + random.nextDouble() * 2 * Math.PI;
        fea[1] = -Math.PI + random.nextDouble() * 2 * Math.PI;
        fea[2] = -4 * Math.PI + random.nextDouble() * 8 * Math.PI;
        fea[3] = -9 * Math.PI + random.nextDouble() * 18 * Math.PI;
        return new AcrobotState(fea[0], fea[1], fea[2], fea[3]);
    }
}
