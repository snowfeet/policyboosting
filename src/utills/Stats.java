/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utills;

/**
 *
 * @author daq
 */
public class Stats {

    public static double[] mean_std(double[] x) {
        double[] ms = new double[2];
        int N = x.length;
        
        ms[0] = 0;
        for (int i = 0; i < x.length; i++) {
            ms[0] += x[i];
        }
        ms[0] /= N;

        ms[1] = 0;
        for (int i = 0; i < x.length; i++) {
            ms[1] += (x[i] - ms[0]) * (x[i] - ms[0]);
        }
        ms[1] /= N;

        return ms;
    }
}
