/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.dwrs.commands;

import s3f.dwrs.robot.Robot;
import s3f.dwrs.robot.device.Device;
import s3f.dwrs.robot.device.HBridge;
import s3f.jifi.core.commands.Command;
import s3f.jifi.core.interpreter.ExecutionException;
import s3f.jifi.core.interpreter.ResourceManager;
import s3f.util.trafficsimulator.Clock;

/**
 *
 * @author gnome3
 */
public class Move implements Command {

    @Override
    public String getName() {
        return "move";
    }

    @Override
    public Class[][] getArgs() {
        return new Class[][]{{Integer.class, Integer.class, Object.class}};
    }

    public static void perform(Integer v1, Integer v2, Object rm) throws ExecutionException {
        Robot robot = ((ResourceManager) rm).getResource(Robot.class);
        HBridge hBridge = robot.getDevice(HBridge.class);

        if (hBridge != null) {

            byte t1 = (byte) v1.byteValue();
            byte t2 = (byte) v2.byteValue();

            hBridge.setWaiting();
            hBridge.setFullState(t1, t2);
            robot.setRightWheelSpeed(t2);
            robot.setLeftWheelSpeed(t1);
        }

        Clock clock = ((ResourceManager) rm).getResource(Clock.class);
        while (true) {
            clock.increase();
            try {
                if (hBridge != null && hBridge.isValidRead()) {
                    break;
                }
            } catch (Device.TimeoutException ex) {
                perform(v1, v2, rm);
            }
            try {
                Thread.sleep(5);
            } catch (InterruptedException ex) {
            }
        }
    }

}
