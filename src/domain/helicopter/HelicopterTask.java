/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package domain.helicopter;

import core.Action;
import core.State;
import core.Task;
import java.util.Random;

/**
 *
 * @author daq
 */
public class HelicopterTask extends Task {
    /* some constants indexing into the helicopter's state */

    final int state_size = 13;
    static final int NUMOBS = 12;
    // note: observation returned is not the state itself, but the "error state" expressed in the helicopter's frame (which allows for a simpler mapping from observation to inputs)
    // observation consists of:
    // u, v, w  : velocities in helicopter frame
    // xerr, yerr, zerr: position error expressed in frame attached to helicopter [xyz correspond to ned when helicopter is in "neutral" orientation, i.e., level and facing north]
    // p, q, r
    // qx, qy, qz
    // upper bounds on values state variables can take on (required by rl_glue to be put into a string at environment initialization)
    static double MAX_VEL = 5.0; // m/s
    static double MAX_POS = 20.0;
    static double MAX_RATE = 2 * 3.1415 * 2;
    static double MAX_QUAT = 1.0;
    static double MIN_QW_BEFORE_HITTING_TERMINAL_STATE = Math.cos(30.0 / 2.0 * Math.PI / 180.0);
    static double MAX_ACTION = 1.0;
    static double WIND_MAX = 5.0; // 
    static double mins[] = {-MAX_VEL, -MAX_VEL, -MAX_VEL, -MAX_POS, -MAX_POS, -MAX_POS, -MAX_RATE, -MAX_RATE, -MAX_RATE, -MAX_QUAT, -MAX_QUAT, -MAX_QUAT, -MAX_QUAT};
    static double maxs[] = {MAX_VEL, MAX_VEL, MAX_VEL, MAX_POS, MAX_POS, MAX_POS, MAX_RATE, MAX_RATE, MAX_RATE, MAX_QUAT, MAX_QUAT, MAX_QUAT, MAX_QUAT};
    // very crude helicopter model, okay around hover:
    final double heli_model_u_drag = 0.18;
    final double heli_model_v_drag = 0.43;
    final double heli_model_w_drag = 0.49;
    final double heli_model_p_drag = 12.78;
    final double heli_model_q_drag = 10.12;
    final double heli_model_r_drag = 8.16;
    final double heli_model_u0_p = 33.04;
    final double heli_model_u1_q = -33.32;
    final double heli_model_u2_r = 70.54;
    final double heli_model_u3_w = -42.15;
    final double heli_model_tail_rotor_side_thrust = -0.54;
    final double DT = .1; // simulation time scale  [time scale for control --- internally we integrate at 100Hz for simulating the dynamics]
    final static int NUM_SIM_STEPS_PER_EPISODE = 8000; // after 6000 steps we automatically enter the terminal state
    double wind[] = new double[2];
    Random randomNumberGenerator = new Random();

    public HelicopterTask(Random random) {
        randomNumberGenerator = random;
        actionDim = 4;
    }

    @Override
    public State getInitialState() {
        return new HelicopterState();
    }

    @Override
    public State transition(State s, Action a, Random outRand) {
        HelicopterState hs = (HelicopterState) s;

        // for the next state
        boolean env_terminal = hs.env_terminal;
        int num_sim_steps = hs.num_sim_steps;
        HeliVector velocity = new HeliVector(hs.velocity);
        HeliVector position = new HeliVector(hs.position);
        HeliVector angular_rate = new HeliVector(hs.angular_rate);
        Quaternion q = new Quaternion(hs.q);
        double[] noise = new double[6];
        System.arraycopy(hs.noise, 0, noise, 0, noise.length);

        double[] controls = new double[4];
        // saturate all the actions, b/c the actuators are limited: 
        //[real helicopter's saturation is of course somewhat different, depends on swash plate mixing etc ... ]
        for (int i = 0; i < 4; ++i) {
            controls[i] = Math.min(Math.max(a.controls[i], -1.0), +1.0);
        }

        final double noise_mult = 2.0;
        final double noise_std[] = {0.1941, 0.2975, 0.6058, 0.1508, 0.2492, 0.0734}; // u, v, w, p, q, r
        double noise_memory = .8;
        //generate Gaussian random numbers
        for (int i = 0; i < 6; ++i) {
            noise[i] = noise_memory * noise[i] + (1.0d - noise_memory) * box_mull() * noise_std[i] * noise_mult;
        }

        double dt = .01;  //integrate at 100Hz [control at 10Hz]
        for (int t = 0; t < 10; ++t) {

            // Euler integration:

            // *** position ***
            position.x += dt * velocity.x;
            position.y += dt * velocity.y;
            position.z += dt * velocity.z;

            /*System.out.println("New position: [" + Double.toString(position.x) + "," + 
             Double.toString(position.y) + "," +
             Double.toString(position.z) + "]");*/
            // *** velocity ***
            HeliVector uvw = velocity.express_in_quat_frame(q);
            /*System.out.println("uvw: [" + Double.toString(uvw.x) + "," + 
             Double.toString(uvw.y) + "," +
             Double.toString(uvw.z) + "]");*/
            HeliVector wind_ned = new HeliVector(wind[0], wind[1], 0.0);
            HeliVector wind_uvw = wind_ned.express_in_quat_frame(q);
            HeliVector uvw_force_from_heli_over_m = new HeliVector(-heli_model_u_drag * (uvw.x + wind_uvw.x) + noise[0],
                    -heli_model_v_drag * (uvw.y + wind_uvw.y) + heli_model_tail_rotor_side_thrust + noise[1],
                    -heli_model_w_drag * uvw.z + heli_model_u3_w * controls[3] + noise[2]);

            HeliVector ned_force_from_heli_over_m = uvw_force_from_heli_over_m.rotate(q);
            velocity.x += dt * ned_force_from_heli_over_m.x;
            velocity.y += dt * ned_force_from_heli_over_m.y;
            velocity.z += dt * (ned_force_from_heli_over_m.z + 9.81d);

            /*System.out.println("New velocity: [" + Double.toString(velocity.x) + "," + 
             Double.toString(velocity.y) + "," +
             Double.toString(velocity.z) + "]");*/

            // *** orientation ***
            HeliVector axis_rotation = new HeliVector(angular_rate.x * dt,
                    angular_rate.y * dt,
                    angular_rate.z * dt);
            Quaternion rot_quat = axis_rotation.to_quaternion();
            q = q.mult(rot_quat);

            /*System.out.println("New orientation: [" + Double.toString(this.q.x) + "," + 
             Double.toString(q.y) + "," +
             Double.toString(q.z) + "," + 
             Double.toString(q.w) + "]");*/


            // *** angular rate ***

            double p_dot = -heli_model_p_drag * angular_rate.x + heli_model_u0_p * controls[0] + noise[3];
            double q_dot = -heli_model_q_drag * angular_rate.y + heli_model_u1_q * controls[1] + noise[4];
            double r_dot = -heli_model_r_drag * angular_rate.z + heli_model_u2_r * controls[2] + noise[5];

            angular_rate.x += dt * p_dot;
            angular_rate.y += dt * q_dot;
            angular_rate.z += dt * r_dot;

            /*System.out.println("New angular rate: [" + Double.toString(this.angular_rate.x) + "," + 
             Double.toString(angular_rate.y) + "," +
             Double.toString(angular_rate.z) + "]");*/

            if (!env_terminal && (Math.abs(position.x) > MAX_POS
                    || Math.abs(position.y) > MAX_POS
                    || Math.abs(position.y) > MAX_POS
                    || Math.abs(velocity.x) > MAX_VEL
                    || Math.abs(velocity.y) > MAX_VEL
                    || Math.abs(velocity.z) > MAX_VEL
                    || Math.abs(angular_rate.x) > MAX_RATE
                    || Math.abs(angular_rate.y) > MAX_RATE
                    || Math.abs(angular_rate.z) > MAX_RATE
                    || Math.abs(q.w) < MIN_QW_BEFORE_HITTING_TERMINAL_STATE)) {
                env_terminal = true;
            }
        }

        num_sim_steps = num_sim_steps + 1;;
//        env_terminal = env_terminal || (num_sim_steps == NUM_SIM_STEPS_PER_EPISODE);
        env_terminal = env_terminal || (num_sim_steps == 8000);

        return new HelicopterState(env_terminal, num_sim_steps, velocity,
                position, angular_rate, q, noise);
    }

    @Override
    public double immediateReward(State s) {
        HelicopterState hs = (HelicopterState) s;

        double reward = 0;
        if (!hs.env_terminal) { // not in terminal state
            reward -= hs.velocity.x * hs.velocity.x;
            reward -= hs.velocity.y * hs.velocity.y;
            reward -= hs.velocity.z * hs.velocity.z;
            reward -= hs.position.x * hs.position.x;
            reward -= hs.position.y * hs.position.y;
            reward -= hs.position.z * hs.position.z;
            reward -= hs.angular_rate.x * hs.angular_rate.x;
            reward -= hs.angular_rate.y * hs.angular_rate.y;
            reward -= hs.angular_rate.z * hs.angular_rate.z;
            reward -= hs.q.x * hs.q.x;
            reward -= hs.q.y * hs.q.y;
            reward -= hs.q.z * hs.q.z;
        } else { // in terminal state, obtain very negative reward b/c the agent will exit, we have to give out reward for all future times
            reward = -3.0f * MAX_POS * MAX_POS
                    + -3.0f * MAX_RATE * MAX_RATE
                    + -3.0f * MAX_VEL * MAX_VEL
                    - (1.0f - MIN_QW_BEFORE_HITTING_TERMINAL_STATE * MIN_QW_BEFORE_HITTING_TERMINAL_STATE);
            reward *= (float) (NUM_SIM_STEPS_PER_EPISODE - hs.num_sim_steps);

            //System.out.println("Final reward is: "+reward+" NUM_SIM_STEPS_PER_EPISODE="+HelicopterState.NUM_SIM_STEPS_PER_EPISODE +"  hs.num_sim_steps="+ hs.num_sim_steps);
        }
        return reward; //-Math.log(-reward);
    }

    @Override
    public boolean isComplete(State s) {
        HelicopterState hs = (HelicopterState) s;
        return hs.env_terminal;
    }

    public final Random getRandom() {
        return randomNumberGenerator;
    }

    public double box_mull() {
        double x1 = randomNumberGenerator.nextDouble();
        double x2 = randomNumberGenerator.nextDouble();
        return Math.sqrt(-2.0f * Math.log(x1)) * Math.cos(2.0f * Math.PI * x2);
    }
}
