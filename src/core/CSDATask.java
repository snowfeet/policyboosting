/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

/**
 *
 * @author daq
 */
public abstract class CSDATask extends Task {

    public Action[] actions;

    public double[] getSAFeature(State s, Action action) {
        double[] feature = s.extractFeature();
        double[] saFea = new double[feature.length + 1];
        System.arraycopy(feature, 0, saFea, 0, feature.length);
        saFea[saFea.length - 1] = ((DiscreteAction) action).a;
        return saFea;
    }
}
