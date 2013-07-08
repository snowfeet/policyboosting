/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package domain.mountaincar3d;

import core.State;

/**
 *
 * @author daq
 */
public class MCar3DState extends State {

    double mcar_Xposition;
    double mcar_Yposition;
    double mcar_Xvelocity;
    double mcar_Yvelocity;

    public MCar3DState(double p1, double p2, double p3, double p4) {
        this.mcar_Xposition = p1;
        this.mcar_Yposition = p2;
        this.mcar_Xvelocity = p3;
        this.mcar_Yvelocity = p4;
    }

    @Override
    public double[] extractFeature() {
        double[] fea = new double[4];
        int m = 0;
//        fea[m++] = 1;
        fea[m++] = mcar_Xposition;
        fea[m++] = mcar_Yposition;
        fea[m++] = mcar_Xvelocity;
        fea[m++] = mcar_Yvelocity;

//        fea[5] = mcar_Xposition * mcar_Yposition;
//        fea[6] = mcar_Xvelocity * mcar_Yvelocity;
//
//        fea[7] = mcar_Xposition * mcar_Xposition;
//        fea[8] = mcar_Yposition * mcar_Yposition;
//        fea[9] = mcar_Xvelocity * mcar_Xvelocity;
//        fea[10] = mcar_Yvelocity * mcar_Yvelocity;


        return fea;
    }
}
