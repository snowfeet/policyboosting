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
public class Trajectory implements Comparable<Trajectory> {

    private Task task;
    private List<Tuple> samples;
    private double rewards;
    private int maxStep;
    private boolean isSuccess;
    List<double[]> features;
    List<Double> labels;
    List<Double>[] multiModelLabels;
    private int producedIteration;

    public Trajectory(Task task, List<Tuple> samples, int maxStep, boolean isSuccess) {
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

    public void setFeatures(List<double[]> features) {
        this.features = features;
    }

    public void setLabels(List<Double> labels) {
        this.labels = labels;
    }

    public List<Double> getLabels() {
        return labels;
    }

    public List<double[]> getFeatures() {
        return features;
    }

    public int getProducedIteration() {
        return producedIteration;
    }

    public void setProducedIteration(int producedIteration) {
        this.producedIteration = producedIteration;
    }

    public List<Double>[] getMultiModelLabels() {
        return multiModelLabels;
    }

    public void setMultiModelLabels(List<Double>[] multiModelLabels) {
        this.multiModelLabels = multiModelLabels;
    }

    @Override
    public int compareTo(Trajectory o) {
        return new Double(o.getRewards()).compareTo(this.getRewards());
    }
}
