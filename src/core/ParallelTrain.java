/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 *
 * @author daq
 */
public class ParallelTrain implements Runnable {

    private Classifier c;
    private Instances data;

    public ParallelTrain(Classifier c, Instances data) {
        this.c = c;
        this.data = data;
    }

    public void run() {
        try {
            c.buildClassifier(data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Classifier getC() {
        return c;
    }
}
