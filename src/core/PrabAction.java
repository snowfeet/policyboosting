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

    public PrabAction(int a, double probability) {
        super(a);
        this.probability = probability;
    }
}
