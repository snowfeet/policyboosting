/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package auto;

/**
 *
 * @author daq
 */
public class RunAll {

    public static void main(String[] args) throws Exception {
       // args = new String[]{"3", "0", "99"};
        for (int trial = Integer.parseInt(args[1]); trial <= Integer.parseInt(args[2]); trial++) {
            switch (Integer.parseInt(args[0])) {
                case 0:
                    example.cw.TestPolicy.run(trial);
                    break;
                case 1:
                    example.mountaincar2d.TestPolicy.run(trial);
                    break;
                case 2:
                    example.acrobot.TestPolicy.run(trial);
                    break;
                case 3:
                    example.cw.TestPolicy.run(trial);
                    example.mountaincar2d.TestPolicy.run(trial);
                    example.acrobot.TestPolicy.run(trial);
                    break;
            }
        }
    }
}
