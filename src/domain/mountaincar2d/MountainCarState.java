/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package domain.mountaincar2d;

import core.State;

/**
 *
 * @author daq
 */
public final class MountainCarState extends State {

    public double position;
    public double velocity;

    public MountainCarState(double p, double v) {
        this.position = p;
        this.velocity = v;
    }

    @Override
    public void extractFeature() {
        features = new double[2];
        features[0] = position;
        features[1] = velocity;
    }
}
