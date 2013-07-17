/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package experiment;

import core.Task;
import java.util.List;

/**
 *
 * @author daq
 */
public class Rollout {

    public Task task;
    public List<Tuple> samples;

    public Rollout(Task task, List<Tuple> samples) {
        this.task = task;
        this.samples = samples;
    }

    public double getAvaReward() {
        return getReward() / samples.size();
    }

    double getReward() {
        double rewards = 0;
        for (Tuple t : samples) {
            rewards += t.reward;
        }
        return rewards;
    }
}
