/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package run;

import core.Execution;
import core.State;
import core.Task;
import core.TaskSet;
import core.Tuple;
import java.util.List;
import java.util.Random;
import domain.mountaincar3d.MCar3DTask;
import domain.mountaincar3d.MCar3DTaskSet;
import policy.GBMetaPolicy;
import policy.RandomPolicy;

/**
 *
 * @author daq
 */
public class TestRandomPolicy {

    public static void main(String[] args) throws Exception {
        Random random = new Random();
//        TaskSet taskSet = new CrwTaskSet(random);
//        State initialState = new CrwState(0);

//        TaskSet taskSet = new PuddleTaskSet(random);
//        State initialState = new PuddleState(new Point2D.Double(0.5, 0.5));

        TaskSet taskSet = new MCar3DTaskSet(random);
        State initialState = MCar3DTask.getInitialState();

        double mean = 0;
        for (int i = 0; i < 2000; i++) {
            Task task = taskSet.generateTasks();
            List<Tuple> samples = Execution.runTask(task, initialState, new RandomPolicy(random), 4000, true, random);
            double t = 0;
            for (Tuple tuple : samples) {
                t += tuple.reward;
            }
            mean += samples.size();
            System.out.println(samples.size() + "\t" + (t / samples.size()));
        }
        mean /= 2000;
        System.out.println(mean);
    }
}
