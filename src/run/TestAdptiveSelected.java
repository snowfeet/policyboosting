/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package run;

import core.Task;
import domain.mountaincar3d.MCar3DTaskSet;
import java.util.Random;
import policy.AdaptiveMultiMetaPolicy;
import policy.GBMetaPolicy;
import utills.IO;

/**
 *
 * @author daq
 */
public class TestAdptiveSelected {

    static int maxIter = 500;
    static boolean isPara = true;

    public static void main(String[] args) throws Exception {
        Random random = new Random();
        MCar3DTaskSet taskSet = new MCar3DTaskSet(random);
        AdaptiveMultiMetaPolicy ammp = (AdaptiveMultiMetaPolicy) IO.loadObject("ammp100_MCar3D.mpl");
        for (int k = 0; k <= 50; k++) {
            Task task = taskSet.generateTasks();
            GBMetaPolicy mp = ammp.getTaskPolicy(task);
            int bais = mp.getNumIteration();

            System.out.println(bais);
        }
    }
}
