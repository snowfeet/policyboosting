/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utills;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

/**
 *
 * @author daq
 */
public class IO {

    public static void matrixWrite(double[][] X, String path) {
        try {
            FileWriter writer = new FileWriter(new File(path));
            for (int i = 0; i < X.length; i++) {
                for (int j = 0; j < X[i].length; j++) {
                    writer.write(X[i][j] + "\t");
                }
                writer.write("\n");
            }
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static Object loadObject(String pathname) throws IOException {
        Object data = null;
        ObjectInputStream objreader = null;
        try {
            objreader = new ObjectInputStream(new BufferedInputStream(new FileInputStream(pathname)));
            data = objreader.readObject();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                objreader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return data;
    }

    public static void saveObject(String pathname, Object obj) {
        ObjectOutputStream saver = null;
        try {
            saver = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(pathname)));
            saver.writeObject(obj);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                saver.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static Instances loadInstances(String path) {
        Instances data = null;

        ArffLoader loader = new ArffLoader();
        try {
            loader.setFile(new File(path));
            data = loader.getDataSet();
            data.setClassIndex(data.numAttributes() - 1);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return data;
    }

    public static Instances saveInstances(String path, Instances data) {
        ArffSaver saver = new ArffSaver();
        try {
            saver.setFile(new File(path));
            saver.setInstances(data);
            saver.writeBatch();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return data;
    }
}
