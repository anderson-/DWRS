/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.dwrs;

import s3f.core.plugin.ConfigurableObject;
import s3f.core.plugin.PluginBuilder;
import s3f.dwrs.commands.Move;
import s3f.dwrs.commands.Print;
import s3f.dwrs.commands.ReadDevice;
import s3f.dwrs.commands.Rotate;
import s3f.dwrs.commands.SendBytes;
import s3f.dwrs.commands.Wait;
import s3f.dwrs.robot.Robot;
import s3f.dwrs.robot.device.IRProximitySensor;
import s3f.dwrs.robot.device.ReflectanceSensorArray;
import s3f.dwrs.simulation.Environment;

/**
 *
 * @author antunes
 */
public class Builder extends PluginBuilder {

    public Builder() {
        super("DWRS");
    }

    @Override
    public void init() {
        //dispositivos
        ConfigurableObject o;
//        o = new ConfigurableObject("s3f.dwrs.device");
//        o.getData().setProperty("device", new LED());
//        pm.registerFactory(o);
        o = new ConfigurableObject("s3f.dwrs.device");
        o.getData().setProperty("device", new IRProximitySensor());
        pm.registerFactory(o);
        o = new ConfigurableObject("s3f.dwrs.device");
        o.getData().setProperty("device", new ReflectanceSensorArray());
        pm.registerFactory(o);
        
        //comandos
        o = new ConfigurableObject("s3f.jifi.cmd");
        o.getData().setProperty("procedure", new Move());
        pm.registerFactory(o);
        o = new ConfigurableObject("s3f.jifi.cmd");
        o.getData().setProperty("procedure", new Rotate());
        pm.registerFactory(o);
        o = new ConfigurableObject("s3f.jifi.cmd");
        o.getData().setProperty("procedure", new Wait());
        pm.registerFactory(o);
        o = new ConfigurableObject("s3f.jifi.cmd");
        o.getData().setProperty("procedure", new ReadDevice());
        pm.registerFactory(o);
        o = new ConfigurableObject("s3f.jifi.cmd");
        o.getData().setProperty("procedure", new SendBytes());
        pm.registerFactory(o);
        o = new ConfigurableObject("s3f.jifi.cmd");
        o.getData().setProperty("procedure", new Print());
        pm.registerFactory(o);
        
//        o = new ConfigurableObject("s3f.jifi.constants");
//        o.getData().setProperty("provider", new ConstantsProvider("Distancia", "Refletancia", "Bussola"));
//        pm.registerFactory(o);
        
        pm.registerFactory(Environment.ENV_FILES);
        pm.registerFactory(Robot.R_FILES);
    }

}
