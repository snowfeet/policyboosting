/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utills;

/**
 *
 * @author daq
 */
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** 
 * Created by IntelliJ IDEA. 
 * 
 * @author leizhimin 2008-11-25 14:28:59 
 */
public class TestCachedThreadPool {

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(4);

        for (int i = 0; i < 4; i++) {
            executorService.execute(new TestRunnable());
            System.out.println("************* a" + i + " *************");
        }
        System.out.println("start!");
        executorService.shutdown();
        try {
            while (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        System.out.println("over!");
    }
}

class TestRunnable implements Runnable {

    public void run() {
        System.out.println(Thread.currentThread().getName() + "线程被调用了。");
        for(int i=0;i<3;i++) {
            try {
                Thread.sleep(1000);
                System.out.println(Thread.currentThread().getName());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}