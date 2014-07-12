/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.dwrs._old;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 *
 * @author antunes
 */
public class RobotManager extends JPanel implements Iterable<RobotControlPanel> {

    private ArrayList<RobotControlPanel> panels = new ArrayList<>();
    private JButton btnAddRobot;
    private final GridBagConstraints cons;

    public RobotManager() {
        
//        super.setLayout(new GridBagLayout());
        cons = new GridBagConstraints();
        cons.fill = GridBagConstraints.VERTICAL;
        cons.weightx = 1;
        cons.gridx = 0;

        btnAddRobot = new JButton("Adicionar Robô");

        btnAddRobot.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createRobot();
            }
        });
        
        btnAddRobot.setVisible(false); //temp
        
        super.add(btnAddRobot, cons);
    }

    public RobotControlPanel createRobot() {
        RobotControlPanel p = new RobotControlPanel(RobotManager.this);
        panels.add(p);
        RobotManager.this.add(p, cons);
        RobotManager.this.remove(btnAddRobot);
        RobotManager.this.add(btnAddRobot, cons);
//        gui.updateRobotList();
        return p;
    }

    public void remove(RobotControlPanel robotControlPanel) {
        panels.remove(robotControlPanel);
        super.remove(robotControlPanel);
//        gui.getSimulationPanel().removeRobot(robotControlPanel.getRobot());
    }

    @Override
    public Iterator<RobotControlPanel> iterator() {
        return panels.iterator();
    }
}
