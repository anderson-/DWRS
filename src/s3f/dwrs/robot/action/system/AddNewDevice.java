/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.dwrs.robot.action.system;

import java.nio.ByteBuffer;
import s3f.dwrs.robot.Robot;
import s3f.dwrs.robot.action.Action;

/**
 *
 * @author antunes
 */
public class AddNewDevice extends Action {

    private byte deviceId;
    private byte [] deviceData;

    public void setDeviceId(byte deviceId) {
        this.deviceId = deviceId;
    }

    public void setDeviceData(byte[] deviceData) {
        this.deviceData = deviceData;
    }
    
    public AddNewDevice() {
        super(true); //uma só mensagem de confimação
    }

    @Override
    public void putMessage(ByteBuffer data, Robot robot) {
        data.put(Robot.CMD_ADD);
        data.put(deviceId);
        data.put((byte)deviceData.length);
        data.put(deviceData);
    }
}
