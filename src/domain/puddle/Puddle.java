/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package domain.puddle;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.Serializable;

/**
 *
 * @author btanner
 */
public class Puddle implements Serializable{

    private final Line2D centerLine;
    private final double puddleRadius;

    public Puddle(double x1, double y1, double x2, double y2, double puddleRadius) {
        Point2D start = new Point2D.Double(x1, y1);
        Point2D end = new Point2D.Double(x2, y2);
        centerLine = new Line2D.Double(start, end);
        this.puddleRadius = puddleRadius;
    }

    public double getReward(Point2D agentPosition) {
        double distance = centerLine.ptSegDist(agentPosition);
        if (distance < puddleRadius) {
            return -1d * (puddleRadius - distance);
        }
        return 0.0d;
    }
}
