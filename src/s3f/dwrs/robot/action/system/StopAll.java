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
public class StopAll extends Action {

    public StopAll() {
        super(true); //uma só mensagem de confimação
    }

    @Override
    public void putMessage(ByteBuffer data, Robot robot) {
        data.put(Robot.CMD_STOP);
        data.put(Robot.XTRA_ALL);
    }
    
}
