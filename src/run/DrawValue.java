/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package run;

import core.Task;
import java.awt.geom.Point2D;
import java.util.Random;
import policy.GBMetaPolicy;
import domain.puddle.PuddleState;
import domain.puddle.PuddleTask;
import domain.puddle.PuddleTaskSet;
import utills.IO;

/**
 *
 * @author daq
 */
public class DrawValue {

    public static void main(String[] args) throws Exception {
        System.out.println(Double.NEGATIVE_INFINITY);
//        Random random = new Random();
//        PuddleTaskSet taskSet = new PuddleTaskSet(new Random(random.nextInt()));
//
//        Task task = taskSet.generateTasks();
//
//        GBMetaPolicy mp = (GBMetaPolicy) IO.loadObject("gb100_puddle.mpl");
//        System.out.println("loaded!");
//
//        int N = 500;
//        double step = 1.0d / N;
//        double[][][] value = new double[4][N][N];
//
//        for (int i = 0; i < N; i++) {
//            for (int j = 0; j < N; j++) {
//                double[] utility = mp.getUtility(new PuddleState(new Point2D.Double(i * step, j * step)), task);
//                for (int k = 0; k < 4; k++) {
//                    value[k][i][j] = utility[k];
//                }
//            }
//        }
//
//        for (int k = 0; k < 4; k++) {
//            IO.matrixWrite(value[k], "/home/lamda/daq/matlab/gradient_boosting/puddle/value_puddle" + k + ".txt");
//        }
//
//        double[][] taskPara = new double[1][2];
//        taskPara[0][0] = ((PuddleTask) task).goalRect.getCenterX();
//        taskPara[0][1] = ((PuddleTask) task).goalRect.getCenterY();
//        IO.matrixWrite(taskPara, "/home/lamda/daq/matlab/gradient_boosting/puddle/value_puddle_task.txt");
    }
}
