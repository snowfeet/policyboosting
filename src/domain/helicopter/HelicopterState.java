/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package domain.helicopter;

import core.State;
import static domain.helicopter.HelicopterTask.MAX_VEL;

/**
 *
 * @author daq
 */
public class HelicopterState extends State {

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
    static final int ndot_idx = 0; // north velocity
    static final int edot_idx = 1; // east velocity
    static final int ddot_idx = 2; // down velocity
    static final int n_idx = 3; // north
    static final int e_idx = 4; // east
    static final int d_idx = 5; // down
    static final int p_idx = 6; // angular rate around forward axis
    static final int q_idx = 7; // angular rate around sideways (to the right) axis
    static final int r_idx = 8; // angular rate around vertical (downward) axis
    static final int qx_idx = 9; // quaternion entries, x,y,z,w   q = [ sin(theta/2) * axis; cos(theta/2)]
    static final int qy_idx = 10; // where axis = axis of rotation; theta is amount of rotation around that axis
    static final int qz_idx = 11;  // [recall: any rotation can be represented by a single rotation around some axis]
    final static int qw_idx = 12;
    public boolean env_terminal = false;
    public int num_sim_steps = 0;
    public HeliVector velocity = new HeliVector(0.0d, 0.0d, 0.0d);
    public HeliVector position = new HeliVector(0.0d, 0.0d, 0.0d);
    public HeliVector angular_rate = new HeliVector(0.0d, 0.0d, 0.0d);
    public Quaternion q = new Quaternion(0.0d, 0.0d, 0.0d, 1.0d);
    public double[] noise = new double[6];

    public HelicopterState() {
        reset();
    }

    public HelicopterState(boolean env_terminal, int num_sim_steps,
            HeliVector velocity, HeliVector position, HeliVector angular_rate,
            Quaternion q, double[] noise) {
        this.env_terminal = env_terminal;
        this.num_sim_steps = num_sim_steps;
        this.velocity = velocity;
        this.position = position;
        this.angular_rate = angular_rate;
        this.q = q;
        this.noise = noise;
    }

    private void checkObservationConstraints(double observationDoubles[]) {
        for (int i = 0; i < NUMOBS; i++) {
            if (observationDoubles[i] > maxs[i]) {
                observationDoubles[i] = maxs[i];
            }
            if (observationDoubles[i] < mins[i]) {
                observationDoubles[i] = mins[i];
            }
        }
    }

    @Override
    protected void extractFeature() {
        HeliVector ned_error_in_heli_frame = this.position.express_in_quat_frame(this.q);
        HeliVector uvw = this.velocity.express_in_quat_frame(this.q);

        features = new double[state_size];
        features[ndot_idx] = uvw.x;
        features[edot_idx] = uvw.y;
        features[ddot_idx] = uvw.z;

        features[n_idx] = ned_error_in_heli_frame.x;
        features[e_idx] = ned_error_in_heli_frame.y;
        features[d_idx] = ned_error_in_heli_frame.z;
        features[p_idx] = angular_rate.x;
        features[q_idx] = angular_rate.y;
        features[r_idx] = angular_rate.z;

        // the error quaternion gets negated, b/c we consider the rotation required to bring the helicopter back to target in the helicopter's frame
        features[qx_idx] = q.x;
        features[qy_idx] = q.y;
        features[qz_idx] = q.z;

        checkObservationConstraints(features);
    }

    public void reset() {
        this.velocity = new HeliVector(0.0d, 0.0d, 0.0d);
        this.position = new HeliVector(0.0d, 0.0d, 0.0d);
        this.angular_rate = new HeliVector(0.0d, 0.0d, 0.0d);
        this.q = new Quaternion(0.0d, 0.0d, 0.0d, 1.0);
        this.num_sim_steps = 0;
        this.env_terminal = false;
    }
}
