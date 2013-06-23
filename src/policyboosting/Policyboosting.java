/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package policyboosting;
import jeigen.DenseMatrix;
import jeigen.DenseMatrix.SvdResult;
import jeigen.JeigenJna;

/**
 *
 * @author daq
 */
public class Policyboosting {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.setProperty("java.library.path", "./lib");
        JeigenJna.Jeigen.loadLibrary();

        DenseMatrix dm3 = DenseMatrix.rand(100, 100);
        SvdResult result3 = dm3.svd();
    }
}
