/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.dwrs.commands;

import s3f.dwrs.robot.Robot;
import s3f.dwrs.robot.VirtualConnection;
import s3f.dwrs.robot.action.RotateAction;
import s3f.dwrs.robot.device.Compass;
import s3f.dwrs.robot.device.HBridge;
import s3f.jifi.core.commands.Command;
import s3f.jifi.core.interpreter.ExecutionException;
import s3f.jifi.core.interpreter.ResourceManager;
import s3f.util.trafficsimulator.Clock;

/**
 *
 * @author gnome3
 */
public class Rotate implements Command {

    private static final int THRESHOLD = 0;

    @Override
    public String getName() {
        return "rotate";
    }

    @Override
    public Class[][] getArgs() {
        return new Class[][]{{Integer.class, Object.class}};
    }

    public static boolean rotate(Robot robot, RotateVars vars, HBridge hbridge) {
        int currAngle = (int) Math.toDegrees(robot.getTheta());
        int diff = currAngle - vars.lastAngle;
        if (diff < -180) {
            diff += 360;
        } else if (diff > 180) {
            diff -= 360;
        }
        vars.turnRemaining -= diff;
        vars.lastAngle = currAngle;

        if ((vars.turnRemaining >= -THRESHOLD) && (vars.turnRemaining <= THRESHOLD)) { // se ja esta dentro do erro limite
            hbridge.setFullState((byte) 0, (byte) 0);
        } else {
            byte speed;
            if (vars.turnRemaining > THRESHOLD) { // se esta a direita do objetivo
                speed = (byte) Math.max(30, (int) (Math.min(127, vars.turnRemaining * 0.71))); // velocidade proporcional ao erro, 0.71 = 128/180°
            } else {
                speed = (byte) Math.min(-30, (int) (Math.max(-127, vars.turnRemaining * 0.71))); // velocidade proporcional ao erro, 0.71 = 128/180°
            }
            hbridge.setFullState(speed, (byte) -speed);
            return true;
        }

        return false;
    }

    public static void perform(Integer angle, Object rm) throws ExecutionException {
        RotateVars vars = new RotateVars();
        RotateAction rotateAction = null;
        Robot robot = ((ResourceManager) rm).getResource(Robot.class);
        VirtualConnection vc = (VirtualConnection) robot.getMainConnection();
        HBridge hbridge = robot.getDevice(HBridge.class);
        Compass compass = robot.getDevice(Compass.class);
        if (vc.serial()) {
            rotateAction = robot.getAction(RotateAction.class);
            if (rotateAction != null) {
                rotateAction.setAngle(angle);
                rotateAction.begin(robot);
            }
        } else {
            int turnAngle = angle;
            vars.destAngle = turnAngle;
            if (hbridge != null && compass != null) {
                vars.turnRemaining = turnAngle;
                vars.lastAngle = (int) Math.toDegrees(robot.getTheta());
                vars.destAngle = vars.lastAngle + turnAngle;
                vars.destAngle = (vars.destAngle + 1080) % 360; // limite máximo de +-1080
                //System.out.println( "theta = " + robot.getTheta() + 
                //					"; degrees = " + lastAngle +
                //					"; destAngle = " + destAngle);
                rotate(robot, vars, hbridge);
            }
        }
        Clock clock = ((ResourceManager) rm).getResource(Clock.class);
        boolean running = true;
        while (running) {
            clock.increase();
            if (vc.serial() && rotateAction != null) {
                running = rotateAction.perform(robot);
                continue;
            } else {
                if (hbridge != null && compass != null) {
                    running = rotate(robot, vars, hbridge);
                    continue;
                }
            }
            running = true;
            try {
                Thread.sleep(5);
            } catch (InterruptedException ex) {
            }
        }
    }

    public static class RotateVars {

        public int destAngle;
        public int lastAngle;
        public int turnRemaining;
    }

}
