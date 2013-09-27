/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

/**
 *
 * @author daq
 */
public class PrabAction extends Action {

    public double probability;

    public PrabAction(double[] controls, double probability) {
        super(controls);
        this.probability = probability;
    }

    public PrabAction(int a, double probability) {
        super(a);
        this.probability = probability;
    }

    public PrabAction(Action action, double probability) {
        super(action.a);
        this.probability = probability;
    }

    public PrabAction(Action action, double probability, boolean isCA) {
        super(action.controls);
        this.probability = probability;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }
}
