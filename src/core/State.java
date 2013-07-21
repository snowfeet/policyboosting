/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

/**
 *
 * @author daq
 */
public abstract class State {

    private double[] features;

    protected abstract double[] extractFeature();

    public double[] getfeatures() {
        if (null == features) {
            extractFeature();
        }
        return features;
    }
}
