/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

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
        double avaReward = 0;
        for (Tuple t : samples) {
            avaReward += t.reward;
        }
        avaReward /= samples.size();
        return avaReward;
    }
}
