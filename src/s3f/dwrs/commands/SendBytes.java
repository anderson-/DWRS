/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.dwrs.commands;

import java.io.PrintStream;
import java.util.Arrays;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import s3f.core.ui.MainUI;
import s3f.dwrs.robot.Robot;
import s3f.jifi.core.commands.Command;
import s3f.jifi.core.interpreter.ExecutionException;
import s3f.jifi.core.interpreter.ResourceManager;

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
        PrintStream out = MainUI.getInstance().getConsole();
        int len = args.length - 2;
        if (len < 0) {
            out.println(">>[]");
        }
        out.print(">>[");
        for (int i = 0;; i++) {
            out.print(String.valueOf(args[i]));
            if (i == len) {
                out.println(']');
                break;
            }
            out.print(", ");
        }
        Robot robot = ((ResourceManager) args[args.length - 1]).getResource(Robot.class);
        byte data[] = new byte[args.length - 1];
        for (int i = 0; i < args.length - 1; i++) {
            data[i] = ((Number) args[i]).byteValue();
        }
        robot.getMainConnection().send(data);
    }

}
