/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package run;

import weka.classifiers.meta.AdditiveRegression;
import weka.classifiers.trees.M5P;
import weka.classifiers.trees.REPTree;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author daq
 */
public class TestWekaRegression {

    public static void main(String[] args) throws Exception {
        int D = 10;
        double[] w = new double[D];
        for (int i = 0; i < D; i++) {
            w[i] = Math.random();
        }

        int N = 10000;
        FastVector attInfo_x = new FastVector();
        for (int i = 0; i < D; i++) {
            attInfo_x.addElement(new Attribute("att_" + i, i));
        }
        attInfo_x.addElement(new Attribute("class", D));
        Instances dataHead = new Instances("dataHead", attInfo_x, 0);
        dataHead.setClassIndex(dataHead.numAttributes() - 1);
        Instances data = new Instances(dataHead, N);
        for (int i = 0; i < N; i++) {
            double label = 0;
            double[] atts = new double[D + 1];
            for (int j = 0; j < D; j++) {
                atts[j] = Math.random();
                label += w[j] * atts[j];
            }
            atts[D] = label;
            Instance ins = new Instance(1.0, atts);
            data.add(ins);
        }

        AdditiveRegression ar = new AdditiveRegression();
        ar.buildClassifier(data);

        REPTree repTree = new REPTree();
        repTree.buildClassifier(data);

        M5P m5p = new M5P();
        m5p.buildClassifier(data);

        for (int i = 0; i < N; i++) {
            Instance ins = data.instance(i);
            if (i % 100 == 0) {
                System.out.println(ins.classValue() + "\t"
                        + ar.classifyInstance(ins) + "\t"
                        + repTree.classifyInstance(ins) + "\t"
                        + m5p.classifyInstance(ins));
            }
        }

//        for (int i = 0; i < 10; i++) {
//            double[] atts = new double[D + 1];
//            double label = 0;
//            for (int j = 0; j < D; j++) {
//                atts[j] = Math.random();
//                label += w[j] * atts[j];
//            }
//            Instances test = new Instances(dataHead, 1);
//            Instance ins = new Instance(1.0, atts);
//            test.add(ins);
//            ins = test.instance(0);
////            ins.setDataset(dataHead);
//            System.out.println(label + "\t" + ar.classifyInstance(ins));
//        }
    }
}
