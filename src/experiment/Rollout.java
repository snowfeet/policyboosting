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
    private int maxStep;
    private boolean isSuccess;

    public Rollout(Task task, List<Tuple> samples, int maxStep, boolean isSuccess) {
        this.task = task;
        this.samples = samples;
        this.maxStep = maxStep;
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

    public double getRZ() {
        double RZ = 0;
        for (int i = 0; i < samples.size(); i++) {
            RZ += (i + 1) * samples.get(i).reward;
        }
        if (isSuccess) {
            RZ += samples.get(samples.size() - 1).reward * (maxStep - (samples.size() * (samples.size() - 1)) / 2);
        } else {
            RZ += rewards * (samples.size() - 1) / 2;
        }
        return RZ;
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
