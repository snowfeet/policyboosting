/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package domain.mountaincar3d;

import core.Action;
import core.State;
import core.Task;
import java.util.Random;

/**
 *
 * @author daq
 */
public class MCar3DTask extends Task {

    public double mcar_min_position = -1.2;
    public double mcar_max_position = 0.6;
    public double mcar_max_velocity = 0.07 * 1;
    public double gravity = -0.0025;
    public double acceleration = 0.001 * 1;
    public double mcar_goal_position_x;
    public double mcar_goal_position_y;
    private Random random;

    public MCar3DTask(double p1, double p2, Random rand) {
        this.mcar_goal_position_x = p1;
        this.mcar_goal_position_y = p2;

        this.actionSet = new int[]{0, 1, 2, 3, 4};
        this.random = rand;

        this.taskDimension = 2;
        this.taskParameter = new double[]{mcar_goal_position_x, mcar_goal_position_y};
    }

    public static MCar3DState getInitialState() {
        double m_offset = 0;//Math.random();
        m_offset -= 0.5;
        m_offset /= 100.0;
        return new MCar3DState(-Math.PI / 6.0 + m_offset, -Math.PI / 6.0 + m_offset, 0, 0);
    }

    @Override
    public State transition(State s, Action a, Random outRand) {
        Random thisRand = outRand == null ? random : outRand;
        MCar3DState ms = (MCar3DState) s;
        MCar3DState newState = null;

        double mcar_Xposition = ms.mcar_Xposition;
        double mcar_Yposition = ms.mcar_Yposition;
        double mcar_Xvelocity = ms.mcar_Xvelocity;
        double mcar_Yvelocity = ms.mcar_Yvelocity;

        int move = actionSet[a.index];
        switch (move) {
            case 0:
                mcar_Xvelocity += Math.cos(3 * mcar_Xposition) * gravity;
                mcar_Yvelocity += Math.cos(3 * mcar_Yposition) * gravity;
                break;
            case 1:
                mcar_Xvelocity += -acceleration + Math.cos(3 * mcar_Xposition) * gravity;
                mcar_Yvelocity += Math.cos(3 * mcar_Yposition) * gravity;
                break;
            case 2:
                mcar_Xvelocity += +acceleration + Math.cos(3 * mcar_Xposition) * gravity;
                mcar_Yvelocity += Math.cos(3 * mcar_Yposition) * gravity;
                break;
            case 3:
                mcar_Xvelocity += Math.cos(3 * mcar_Xposition) * gravity;
                mcar_Yvelocity += -acceleration + Math.cos(3 * mcar_Yposition) * gravity;
                break;
            case 4:
                mcar_Xvelocity += Math.cos(3 * mcar_Xposition) * gravity;
                mcar_Yvelocity += +acceleration + Math.cos(3 * mcar_Yposition) * gravity;
                break;
        }

        if (mcar_Xvelocity > mcar_max_velocity) {
            mcar_Xvelocity = mcar_max_velocity;
        } else if (mcar_Xvelocity < -mcar_max_velocity) {
            mcar_Xvelocity = -mcar_max_velocity;
        }
        if (mcar_Yvelocity > mcar_max_velocity) {
            mcar_Yvelocity = mcar_max_velocity;
        } else if (mcar_Yvelocity < -mcar_max_velocity) {
            mcar_Yvelocity = -mcar_max_velocity;
        }

        mcar_Xposition += mcar_Xvelocity;
        mcar_Yposition += mcar_Yvelocity;

        if (mcar_Xposition > mcar_max_position) {
            mcar_Xposition = mcar_max_position;
        }
        if (mcar_Xposition < mcar_min_position) {
            mcar_Xposition = mcar_min_position;
        }
        if (mcar_Xposition == mcar_max_position && mcar_Xvelocity > 0) {
            mcar_Xvelocity = 0;
        }
        if (mcar_Xposition == mcar_min_position && mcar_Xvelocity < 0) {
            mcar_Xvelocity = 0;
        }

        if (mcar_Yposition > mcar_max_position) {
            mcar_Yposition = mcar_max_position;
        }
        if (mcar_Yposition < mcar_min_position) {
            mcar_Yposition = mcar_min_position;
        }
        if (mcar_Yposition == mcar_max_position && mcar_Yvelocity > 0) {
            mcar_Yvelocity = 0;
        }
        if (mcar_Yposition == mcar_min_position && mcar_Yvelocity < 0) {
            mcar_Yvelocity = 0;
        }

        newState = new MCar3DState(mcar_Xposition, mcar_Yposition, mcar_Xvelocity, mcar_Yvelocity);
        return newState;
    }

    @Override
    public double immediateReward(State s) {
        if (isComplete(s)) {
            return 1000;
        } else {
            return -1;
        }
    }

    @Override
    public double[] getSATFeature(State s, Action a) {
        double[] feature = s.extractFeature();
        double[] satFea = new double[feature.length + 1 + taskDimension];
        System.arraycopy(taskParameter, 0, satFea, 0, taskDimension);
        System.arraycopy(feature, 0, satFea, taskDimension, feature.length);
        satFea[satFea.length - 1] = actionSet[a.index];
        return satFea;
    }

    @Override
    public boolean isComplete(State s) {
        MCar3DState ms = (MCar3DState) s;
        if ((ms.mcar_Xposition - mcar_goal_position_x) * mcar_goal_position_x > 0
                && (ms.mcar_Yposition - mcar_goal_position_y) * mcar_goal_position_y > 0) {
            return true;
        } else {
            return false;
        }
    }
}
