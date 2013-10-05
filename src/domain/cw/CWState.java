/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package domain.cw;
import core.State;


/**
 *
 * @author daq
 */
public class CWState extends State {

    public double x;

    public CWState(double x) {
        this.x = x;
    }

    @Override
    protected void extractFeature() {
        features = new double[3];
        features[0] = 1;
        features[1] = x;
        features[2] = x * x;
    }
}
