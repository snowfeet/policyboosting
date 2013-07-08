/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package domain.cwk;

import core.State;

/**
 *
 * @author daq
 */
public final class CrwState extends State {

    public double x;

    public CrwState(double x) {
        this.x = x;
    }

    @Override
    public double[] extractFeature() {
        double[] fea = new double[3];
        fea[0] = 1;
        fea[1] = x;
        fea[2] = x * x;
        return fea;
    }
}
