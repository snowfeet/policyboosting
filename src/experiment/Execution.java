/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package experiment;

import core.Action;
import core.Policy;
import core.PrabAction;
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

        State s = null;
        Action action = null;
        State sPrime = initalState;
        double reward = Double.NEGATIVE_INFINITY;

        int step = 0;
        while (step < maxStep) {
            if (!task.isComplete(sPrime)) {
                s = sPrime;

                action = isStochastic ? policy.makeDecisionS(s, task, random) : policy.makeDecisionD(s, task, random);
                sPrime = task.transition(s, action, random);
                reward = task.immediateReward(sPrime);
                samples.add(new Tuple(s, action, reward, sPrime));
            } else {
                samples.add(new Tuple(sPrime, new PrabAction(random.nextInt(task.actions.length), 1), reward, sPrime));
            }
            step = step + 1;
        }

        return samples;
    }
}
