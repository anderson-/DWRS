/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.dwrs;

import javax.swing.ImageIcon;
import s3f.core.plugin.ConfigurableObject;
import s3f.core.plugin.PluginBuilder;
import s3f.core.project.Project;
import s3f.core.project.ProjectTemplateCategory;
import s3f.core.project.Resource;
import s3f.dwrs.commands.Move;
import s3f.jifi.core.commands.Print;
import s3f.dwrs.commands.ReadDevice;
import s3f.dwrs.commands.Rotate;
import s3f.dwrs.commands.SendBytes;
import s3f.jifi.core.commands.Wait;
import s3f.dwrs.robot.Robot;
import s3f.dwrs.robot.device.IRProximitySensor;
import s3f.dwrs.robot.device.ReflectanceSensorArray;
import s3f.dwrs.simulation.Environment;
import s3f.jifi.core.FlowScript;

/**
 *
 * @author antunes
 */
public class Builder extends PluginBuilder {

    public static final ProjectTemplateCategory DWRS_TEMPLATES = new ProjectTemplateCategory("Robotcs", new ImageIcon(Builder.class.getResource("/resources/icons/fugue/robot-new.png")));

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
        o.getData().setProperty("procedure", new ReadDevice());
        pm.registerFactory(o);
        o = new ConfigurableObject("s3f.jifi.cmd");
        o.getData().setProperty("procedure", new SendBytes());
        pm.registerFactory(o);

//        o = new ConfigurableObject("s3f.jifi.constants");
//        o.getData().setProperty("provider", new ConstantsProvider("Distancia", "Refletancia", "Bussola"));
//        pm.registerFactory(o);
        pm.registerFactory(Environment.ENV_FILES);
        pm.registerFactory(Robot.R_FILES);

        pm.registerFactory(DWRS_TEMPLATES);
        DWRS_TEMPLATES.addTemplate(createBasicRobotProject());
    }

    private static Project createBasicRobotProject() {
        Project p = new Project("Basic Robot Project");
        Environment environment = new Environment();
        environment.setName("Environment");
        p.addElement(environment);
        Robot robot = new Robot();
        robot.setName("Robot");
        p.addElement(robot);
        FlowScript flowScript = new FlowScript();
        flowScript.setName("Control");
        p.addElement(flowScript);
        environment.addResource(new Resource(environment, robot));
        flowScript.addResource(new Resource(flowScript, robot));
        return p;
    }

}
