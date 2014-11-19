/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.dwrs.commands;

import java.util.Arrays;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import s3f.jifi.core.commands.Command;
import s3f.jifi.core.interpreter.ExecutionException;

/**
 *
 * @author gnome3
 */
public class SendBytes implements Command {

    @Override
    public String getName() {
        return "send";
    }

    @Override
    public Class[][] getArgs() {
        return new Class[][]{{Context.class, Scriptable.class, Object[].class, Function.class}};
    }

    public static void perform(Context cx, Scriptable thisObj, Object[] args, Function funOb) throws ExecutionException {
        System.out.println(Arrays.toString(args));
//        Robot robot = ((ResourceManager) rm).getResource(Robot.class);
//        robot.getMainConnection().send(data);
    }

}
