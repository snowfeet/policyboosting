/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package domain.puddle;

import core.Action;
import core.State;
import core.Task;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Random;

/**
 *
 * @author daq
 */
public class PuddleTask extends Task {

    public List<Puddle> thePuddles;
    public Rectangle2D worldRect;
    public Rectangle2D goalRect;
    public double agentSpeed;
    public double rewardPerStep;
    public double rewardAtGoal;
    public double transitionNoise;
    public Random random;

    public PuddleTask(List<Puddle> thePuddles, Rectangle2D goalRect, Random random) {
        this.thePuddles = thePuddles;
        this.worldRect = new Rectangle2D.Double(0, 0, 1, 1);
        this.goalRect = goalRect;
        this.agentSpeed = agentSpeed = .1;
        this.rewardPerStep = -1.0d;
        this.rewardAtGoal = 0.0d;
        this.transitionNoise = 0;
        this.random = random;

        this.actionSet = new int[]{0, 1, 2, 3};
//        this.taskParameter = new double[]{goalRect.getCenterX(),
//            goalRect.getCenterY(), goalRect.getHeight(), goalRect.getWidth(),
//            goalRect.getMinX(), goalRect.getMinY(), goalRect.getMaxX(),
//            goalRect.getMaxY()};       
        this.taskParameter = new double[]{goalRect.getCenterX(),
            goalRect.getCenterY()};
         this.taskDimension = taskParameter.length;
    }

    @Override
    public State transition(State s, Action a, Random outRand) {
        Random thisRand = outRand == null ? random : outRand;
        PuddleState ps = (PuddleState) s;
        PuddleState newState = null;

        int move = actionSet[a.index];
        double nextX = ps.s.getX();
        double nextY = ps.s.getY();

        if (move == 0) {
            nextX += agentSpeed;
        }
        if (move == 1) {
            nextX -= agentSpeed;
        }
        if (move == 2) {
            nextY += agentSpeed;
        }
        if (move == 3) {
            nextY -= agentSpeed;
        }

        double XNoise = thisRand.nextGaussian() * transitionNoise * agentSpeed;
        double YNoise = thisRand.nextGaussian() * transitionNoise * agentSpeed;

        nextX += XNoise;
        nextY += YNoise;

        nextX = Math.min(nextX, worldRect.getMaxX());
        nextX = Math.max(nextX, worldRect.getMinX());
        nextY = Math.min(nextY, worldRect.getMaxY());
        nextY = Math.max(nextY, worldRect.getMinY());

        newState = new PuddleState(new Point2D.Double(nextX, nextY));
        return newState;
    }

    private double getReward(PuddleState ps) {
        double puddleReward = 0;
//        double puddleReward = getPuddleReward(ps);
        if (inGoalRegion(ps)) {
            return puddleReward + rewardAtGoal;
        } else {
            return puddleReward + rewardPerStep;
        }
    }

    private double getPuddleReward(PuddleState ps) {
        double totalPuddleReward = 0;
        for (Puddle puddle : thePuddles) {
            totalPuddleReward += puddle.getReward(ps.s);
        }
        return totalPuddleReward;
    }

    private boolean inGoalRegion(PuddleState ps) {
        return goalRect.contains(ps.s);
    }

    @Override
    public double immediateReward(State s) {
        PuddleState ps = (PuddleState) s;
        return getReward(ps);
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
        PuddleState ps = (PuddleState) s;
        return inGoalRegion(ps);
    }
}
