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

    public int index;
    public double value;

    public Action(int a) {
        this.index = a;
    }

    public Action(int a, double v) {
        this.index = a;
        this.value = v;
    }
}
