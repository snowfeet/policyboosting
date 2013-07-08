/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author daq
 */
public class Execution {

    public static List<Tuple> runTask(Task task, State initalState, MetaPolicy mp, int maxStep, boolean isStochastic, Random random) {
        List<Tuple> samples = new ArrayList<Tuple>();

        State s = initalState;
        int step = 0;
        while (step < maxStep) {
            Action action = isStochastic ? mp.makeDecisionS(s, task, random) : mp.makeDecisionD(s, task, random);
            State sPrime = task.transition(s, action, random);
            double reward = task.immediateReward(sPrime);
            samples.add(new Tuple(s, action, reward, sPrime));

            s = sPrime;
            step = step + 1;
            
            if(task.isComplete(s))
                break;
        }

        return samples;
    }
}
