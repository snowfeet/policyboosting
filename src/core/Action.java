/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

/**
 *
 * @author daq
 */
public class Action {

    public int a;
    public double[] controls;

    public Action(double[] controls) {
        this.controls = controls;
    }

    public Action(int a) {
        this.a = a;
    }
}
