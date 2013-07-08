/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package domain.puddle;

import core.State;
import java.awt.geom.Point2D;

/**
 *
 * @author daq
 */
public class PuddleState extends State {

    public Point2D s;

    public PuddleState(Point2D s) {
        this.s = s;
    }

    @Override
    public double[] extractFeature() {
        double[] fea = new double[5];
        fea[0] = 1;
        fea[1] = s.getX();
        fea[2] = s.getY();
        fea[3] = fea[1] * fea[1];
        fea[4] = fea[2] * fea[2];
        return fea;
    }
}
