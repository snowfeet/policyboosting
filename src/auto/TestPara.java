/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package auto;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 *
 * @author daq
 */
public class TestPara {

    public static void main(String[] args) throws Exception {
        new TestPara().run(args);
        
//        double[] stepsize = new double[]{0.01, 0.05, 0.1, 0.5, 1, 5, 10};
//        int[] treeDepth = new int[]{10, 50, 100, 150, 200};
//
//        int[] bestPoolSize = new int[]{1, 10, 100, 10};
//        int[] uniformPoolSize = new int[]{10, 1, 10, 100};
//
//        for (int trial = Integer.parseInt(args[0]); trial <= Integer.parseInt(args[1]); trial++) {
//
//            for (int i = 0; i < stepsize.length; i++) {
//                example.mountaincar2d.TestPara.testStepsize(trial, stepsize[i]);
//                example.acrobot.TestPara.testStepsize(trial, stepsize[i]);
//            }
//
//            for (int i = 0; i < treeDepth.length; i++) {
//                example.mountaincar2d.TestPara.testTreeDepth(trial, treeDepth[i]);
//                example.acrobot.TestPara.testTreeDepth(trial, treeDepth[i]);
//            }
//
//            for (int i = 0; i < bestPoolSize.length; i++) {
//                example.mountaincar2d.TestPara.testPoolSize(trial, bestPoolSize[i], uniformPoolSize[i]);
//                example.acrobot.TestPara.testPoolSize(trial, bestPoolSize[i], uniformPoolSize[i]);
//            }
//        }
    }

    public class ParallelTest implements Runnable {

        double[] stepsize = new double[]{0.01, 0.05, 0.1, 0.5, 1, 5, 10};
        int[] treeDepth = new int[]{10, 50, 100, 150, 200};
        int[] bestPoolSize = new int[]{1, 10, 100, 10};
        int[] uniformPoolSize = new int[]{10, 1, 10, 100};
        int trial;

        public ParallelTest(int trial) {
            this.trial = trial;
        }

        public void run() {
            for (int i = 0; i < stepsize.length; i++) {
                example.mountaincar2d.TestPara.testStepsize(trial, stepsize[i]);
                example.acrobot.TestPara.testStepsize(trial, stepsize[i]);
            }

            for (int i = 0; i < treeDepth.length; i++) {
                example.mountaincar2d.TestPara.testTreeDepth(trial, treeDepth[i]);
                example.acrobot.TestPara.testTreeDepth(trial, treeDepth[i]);
            }

            for (int i = 0; i < bestPoolSize.length; i++) {
                example.mountaincar2d.TestPara.testPoolSize(trial, bestPoolSize[i], uniformPoolSize[i]);
                example.acrobot.TestPara.testPoolSize(trial, bestPoolSize[i], uniformPoolSize[i]);
            }
        }
    }

    private void run(String[] args) throws InterruptedException {
        ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
        for (int trial = Integer.parseInt(args[0]); trial <= Integer.parseInt(args[1]); trial++) {
            ParallelTest run = new ParallelTest(trial);
            exec.execute(run);
        }
        exec.shutdown();
        while (!exec.awaitTermination(10, TimeUnit.SECONDS)) {
        }
    }
}
