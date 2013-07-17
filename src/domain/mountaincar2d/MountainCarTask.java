/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package domain.mountaincar2d;

import core.Action;
import core.Task;
import core.State;
import core.Task;
import java.util.Random;

/**
 *
 * @author daq
 */
public class MountainCarTask extends Task {

    final public double minPosition = -1.2;
    final public double maxPosition = 0.6;
    final public double minVelocity = -0.07 * 1;
    final public double maxVelocity = 0.07 * 1;
    final public double goalPosition = 0.5;
    final public double accelerationFactor = 0.001 * 1;
    final public double gravityFactor = -0.0025;
    final public double hillPeakFrequency = 3.0;
    //This is the middle of the valley (no slope)
    final public double defaultInitPosition = -0.5d;
    final public double defaultInitVelocity = 0.0d;
    final public double rewardPerStep = -1.0d;
    final public double rewardAtGoal = 0.0d;
    private double transitionNoise = 0.0d;
    private Random random;

    public MountainCarTask(Random random) {
        this.random = random;
        this.actions = new Action[3];
        for (int a = 0; a < 3; a++) {
            actions[a] = new Action(a);
        }
    }

    @Override
    public State transition(State s, Action action, Random outRand) {
        Random thisRand = outRand == null ? random : outRand;
        MountainCarState cs = (MountainCarState) s;
        int move = action.a;

        double acceleration = accelerationFactor;
        double position = cs.position;
        double velocity = cs.velocity;
        
        double thisNoise = 2.0d * accelerationFactor * transitionNoise * (thisRand.nextDouble() - .5d);

        velocity += (thisNoise + ((move - 1)) * (acceleration)) + getSlope(position) * (gravityFactor);
        if (velocity > maxVelocity) {
            velocity = maxVelocity;
        }
        if (velocity < minVelocity) {
            velocity = minVelocity;
        }
        position += velocity;
        if (position > maxPosition) {
            position = maxPosition;
        }
        if (position < minPosition) {
            position = minPosition;
        }
        if (position == minPosition && velocity < 0) {
            velocity = 0;
        }
//        System.out.println(move+" "+position);
        return new MountainCarState(position, velocity);
    }

    public double getSlope(double queryPosition) {
        return Math.cos(hillPeakFrequency * queryPosition);
    }

    @Override
    public double immediateReward(State s) {
        if (isComplete(s)) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public boolean isComplete(State s) {
        MountainCarState cs = (MountainCarState) s;
        if (cs.position >= goalPosition) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public State getInitialState() {
        return new MountainCarState(defaultInitPosition + 0.5d * (random.nextDouble() - .5d),
                defaultInitVelocity + .05d * (random.nextDouble() - .5d));
    }
}
