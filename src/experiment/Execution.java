/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package experiment;

import core.Action;
import core.Policy;
import core.State;
import core.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author daq
 */
public class Execution {

    public static List<Tuple> runTaskWithFixedStep(Task task, State initalState, Policy policy, int maxStep, boolean isStochastic, Random random) {
        List<Tuple> samples = new ArrayList<Tuple>();

        State s = initalState;
        int step = 0;
        while (step < maxStep) {
            Action action = isStochastic ? policy.makeDecisionS(s, task, random) : policy.makeDecisionD(s, task, random);
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
