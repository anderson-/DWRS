/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.dwrs.commands;

import java.nio.ByteBuffer;
import s3f.dwrs.robot.Robot;
import s3f.dwrs.robot.connection.message.Message;
import s3f.dwrs.robot.device.Device;
import s3f.jifi.core.interpreter.ExecutionException;
import s3f.jifi.core.interpreter.ResourceManager;
import s3f.util.trafficsimulator.Clock;

/**
 *
 * @author gnome3
 */
public class ReadDevice implements Command {

    @Override
    public String getName() {
        return "read";
    }

    @Override
    public Class[][] getArgs() {
        return new Class[][]{{Integer.class, Object.class}};
    }

    public static Object perform(Integer deviceIndex, Object rm) throws ExecutionException {
        Robot robot = ((ResourceManager) rm).getResource(Robot.class);
        System.out.println("t:" + deviceIndex);
        Device device = robot.getDevice(deviceIndex);
        if (device != null) {
            //mensagem get padrão 
            byte[] msg = device.defaultGetMessage();
            device.setWaiting();
            if (msg.length > 0) {
                //cria um buffer para a mensagem
                ByteBuffer GETmessage = ByteBuffer.allocate(64);
                //header do comando set
                GETmessage.put(Robot.CMD_GET);
                //id
                GETmessage.put(device.getID());
                //tamanho da mensagem
                GETmessage.put((byte) msg.length);
                //mensagem
                GETmessage.put(msg);
                //flip antes de enviar
                GETmessage.flip();
                robot.getMainConnection().send(GETmessage);
            } else {
                msg = new byte[]{Robot.CMD_GET, device.getID(), 0};
                robot.getMainConnection().send(msg);
            }
        }

        Clock clock = ((ResourceManager) rm).getResource(Clock.class);
        while (true) {
            clock.increase();
            try {
                if (device != null && device.isValidRead()) {
                    return device.getState();
                }
            } catch (Message.TimeoutException ex) {
                return perform(deviceIndex, rm);
            }
            try {
                Thread.sleep(5);
            } catch (InterruptedException ex) {
            }
        }
    }
}
