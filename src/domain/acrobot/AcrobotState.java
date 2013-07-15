/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package domain.acrobot;

import core.State;

/**
 *
 * @author daq
 */
public class AcrobotState extends State {

    public double theta1, theta2, theta1Dot, theta2Dot;

    public AcrobotState(double theta1, double theta2, double theta1Dot, double theta2Dot) {
        this.theta1 = theta1;
        this.theta2 = theta2;
        this.theta1Dot = theta1Dot;
        this.theta2Dot = theta2Dot;
    }

    @Override
    public double[] extractFeature() {
        double[] fea = new double[4];
        int m = 0;
        fea[m++] = theta1;
        fea[m++] = theta2;
        fea[m++] = theta1Dot;
        fea[m++] = theta2Dot;
        return fea;
    }
}