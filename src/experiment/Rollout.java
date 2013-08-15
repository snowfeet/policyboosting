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

    private Task task;
    private List<Tuple> samples;
    private double rewards;
    private boolean isSuccess;

    public Rollout(Task task, List<Tuple> samples, boolean isSuccess) {
        this.task = task;
        this.samples = samples;
        this.isSuccess = isSuccess;
    }

    public Task getTask() {
        return task;
    }

    public List<Tuple> getSamples() {
        return samples;
    }

    public double getRewards() {
        return rewards;
    }

    public void setRewards(double rewards) {
        this.rewards = rewards;
    }

    public boolean isIsSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }
}
