/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package experiment;

import core.Action;
import core.State;

/**
 *
 * @author daq
 */
public class Tuple {

    public State s;
    public Action action;
    public double reward;
    public State sPrime;

    Tuple(State s, Action a, double reward, State sPrime) {
        this.s = s;
        this.action = a;
        this.reward = reward;
        this.sPrime = sPrime;
    }
}
