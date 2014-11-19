/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.dwrs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.MenuSelectionManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicRadioButtonMenuItemUI;
import s3f.core.plugin.EntityManager;
import s3f.core.plugin.PluginManager;
import s3f.core.project.Project;
import s3f.core.ui.GUIBuilder;
import s3f.core.ui.MainUI;
import s3f.core.ui.ToolBarButton;
import s3f.dwrs.robot.Robot;
import s3f.dwrs.robot.VirtualConnection;
import s3f.dwrs.robot.connection.Connection;
import s3f.dwrs.robot.connection.Serial2;
import s3f.dwrs.robot.device.Device;
import s3f.util.ColorUtils;

/**
 *
 * @author antunes
 */
public class UI extends GUIBuilder {

    private class ConnectionStatusGraph extends JPanel {

        private final float size = 5;
        private ArrayList<Integer> sendedArray = new ArrayList<>();
        private ArrayList<Integer> lostArray = new ArrayList<>();
        private ArrayList<Integer> receivedArray = new ArrayList<>();
        private int sended = 0;
        private int lost = 0;
        private int received = 0;
        private Color sendedColor = Color.decode("#4ecdc4");
        private Color lostColor = Color.decode("#ff6b6b");
        private Color receivedColor = Color.decode("#c7f464");

        public ConnectionStatusGraph() {
        }

        public void step() {
            int nbars = (int) (getWidth() / size);
            int newSended = 0;
            int newReceived = 0;
            int newLost = 0;
            if (serial != null && serial.isConnected()) {
                int s = serial.getSendedPackages();
                int r = serial.getReceivedPackages();
                int l = Device.getLostPackages();

                newSended = s - sended;
                newReceived = r - received;
                newLost = l - lost;

                sendedArray.add(newSended);
                receivedArray.add(newReceived);
                lostArray.add(newLost);

                sended = s;
                received = r;
                lost = l;
            }

            //atualiza botão
            if (button != null) {
                if (connection == null || !connection.isConnected()) {
                    button.setIcon(ICON_OFFLINE);
                    button.setRolloverIcon(new ImageIcon(ColorUtils.imageHSBAchange(ICON_OFFLINE.getImage(), 0, 0, .1f, 0)));
                } else {
                    boolean oldLost = true;

                    for (int i = lostArray.size() - 1; i > lostArray.size() - 6 && i >= 0; i--) {
                        oldLost &= (lostArray.get(i) > 0 || sendedArray.get(i) == 0);
                        oldLost &= (receivedArray.get(i) == 0);
                    }

                    if (newReceived == 0 && newLost >= 0) {
                        if (newSended > 0) {
                            if (oldLost) {
                                button.setIcon(ICON_ERROR);
                                button.setRolloverIcon(new ImageIcon(ColorUtils.imageHSBAchange(ICON_ERROR.getImage(), 0, 0, .1f, 0)));
                            } else {
                                button.setIcon(ICON_TRANSMIT);
                                button.setRolloverIcon(new ImageIcon(ColorUtils.imageHSBAchange(ICON_TRANSMIT.getImage(), 0, 0, .1f, 0)));
                            }
                        } else {
                            button.setIcon(ICON_IDLE);
                            button.setRolloverIcon(new ImageIcon(ColorUtils.imageHSBAchange(ICON_IDLE.getImage(), 0, 0, .1f, 0)));
                        }
                    } else if (newSended == 0) {
                        if (newReceived > 0) {
                            button.setIcon(ICON_RECEIVE);
                            button.setRolloverIcon(new ImageIcon(ColorUtils.imageHSBAchange(ICON_RECEIVE.getImage(), 0, 0, .1f, 0)));
                        } else {
                            if (virtual) {
                                button.setIcon(ICON_RECEIVE_TRANSMITV);
                                button.setRolloverIcon(new ImageIcon(ColorUtils.imageHSBAchange(ICON_RECEIVE_TRANSMITV.getImage(), 0, 0, .1f, 0)));
                            } else {
                                button.setIcon(ICON_IDLE);
                                button.setRolloverIcon(new ImageIcon(ColorUtils.imageHSBAchange(ICON_IDLE.getImage(), 0, 0, .1f, 0)));
                            }
                        }
                    } else {
                        if (virtual) {
                            button.setIcon(ICON_RECEIVE_TRANSMITV);
                            button.setRolloverIcon(new ImageIcon(ColorUtils.imageHSBAchange(ICON_RECEIVE_TRANSMITV.getImage(), 0, 0, .1f, 0)));
                        } else {
                            button.setIcon(ICON_RECEIVE_TRANSMIT);
                            button.setRolloverIcon(new ImageIcon(ColorUtils.imageHSBAchange(ICON_RECEIVE_TRANSMIT.getImage(), 0, 0, .1f, 0)));
                        }
                    }
                }
            }

            float ping = Device.getPingEstimative();
            if (!Float.isNaN(ping)) {
                statusLabel2.setText("Ping: " + (int) ping + " ms");
            } else {
                statusLabel2.setText(" - ");
            }

            //statusLabel3.setText("Lost: " + lost);
            statusLabel3.setText(sended + "|" + received + "|" + lost);
            statusLabel3.setToolTipText("Enviado|Recebido|Perdido");

            while (sendedArray.size() > nbars) {
                sendedArray.remove(0);
                lostArray.remove(0);
                receivedArray.remove(0);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {

            int maxS = 0;
            int maxL = 0;
            int maxR = 0;
            int max;

            for (int i : sendedArray) {
                if (i > maxS) {
                    maxS = i;
                }
            }

            for (int i : lostArray) {
                if (i > maxR) {
                    maxR = i;
                }
            }

            for (int i : receivedArray) {
                if (i > maxR) {
                    maxR = i;
                }
            }

            max = maxS + maxL + maxR;
            float height = getHeight();
            float c = height / max;
            float h, t;

            if (g == null) {
                return;
            }

            g.setColor(Color.black);
            g.fillRect(0, 0, getWidth(), getHeight());

            for (int i = 0; i < sendedArray.size(); i++) {
                t = 0;
                h = lostArray.get(i) * c;
                if (h > 0) {
                    g.setColor(lostColor);
                    g.fillRect((int) (i * size), (int) (height - h - t), (int) size, (int) h + 3);
                }
                t += h;
                h = receivedArray.get(i) * c;
                if (h > 0) {
                    g.setColor(receivedColor);
                    g.fillRect((int) (i * size), (int) (height - h - t), (int) size, (int) h + 3);
                }
                t += h;
                h = sendedArray.get(i) * c;
                if (h > 0) {
                    g.setColor(sendedColor);
                    g.fillRect((int) (i * size), (int) (height - h - t), (int) size, (int) h + 3);
                }
            }
        }
    }

    private static final ImageIcon ICON_OFFLINE;
    private static final ImageIcon ICON_IDLE;
    private static final ImageIcon ICON_ERROR;
    private static final ImageIcon ICON_FULL_ERROR;
    private static final ImageIcon ICON_RECEIVE;
    private static final ImageIcon ICON_TRANSMIT;
    private static final ImageIcon ICON_RECEIVE_TRANSMIT;
    private static final ImageIcon ICON_RECEIVEV;
    private static final ImageIcon ICON_TRANSMITV;
    private static final ImageIcon ICON_RECEIVE_TRANSMITV;

    static {
        ICON_OFFLINE = new ImageIcon(UI.class.getResource("/resources/tango/24x24/status/network-offline.png"));
        ICON_IDLE = new ImageIcon(UI.class.getResource("/resources/tango/24x24/status/network-idle.png"));
        ICON_ERROR = new ImageIcon(UI.class.getResource("/resources/tango/24x24/status/network-error.png"));
        ICON_FULL_ERROR = new ImageIcon(UI.class.getResource("/resources/tango/24x24/status/software-update-urgent.png"));

        ICON_RECEIVE = new ImageIcon(UI.class.getResource("/resources/tango/24x24/status/network-receive.png"));
        ICON_TRANSMIT = new ImageIcon(UI.class.getResource("/resources/tango/24x24/status/network-transmit.png"));
        ICON_RECEIVE_TRANSMIT = new ImageIcon(UI.class.getResource("/resources/tango/24x24/status/network-transmit-receive.png"));

        ICON_RECEIVEV = new ImageIcon(UI.class.getResource("/resources/tango/24x24/status/virtual-network-receive.png"));
        ICON_TRANSMITV = new ImageIcon(UI.class.getResource("/resources/tango/24x24/status/virtual-network-transmit.png"));
        ICON_RECEIVE_TRANSMITV = new ImageIcon(UI.class.getResource("/resources/tango/24x24/status/virtual-network-transmit-receive.png"));
    }

    public static final String VIRTUAL_CONNECTION = "Virtual";
    private TitledBorder border;
    private Serial2 serial;
    private Connection connection = null;
    private boolean connected = false;
    private Robot robot = null;
    private MouseListener ml;
    private JButton button = null;
    private boolean virtual = false;
    private JComboBox connectionComboBox = new JComboBox();
    private JPanel connectionStatusGraph = new ConnectionStatusGraph();
    private JLabel statusLabel = new JLabel("Desconectado");
    private JLabel statusLabel2 = new JLabel();
    private JLabel statusLabel3 = new JLabel();
    private JLabel statusLabel4 = new JLabel();

    public UI() {
        super("DWRS");
    }

    @Override
    public void init() {

        serial = new Serial2(57600);
//        robot = new Robot();
//        robot.add(new HBridge());
//        robot.add(new Compass());
//        robot.add(new IRProximitySensor());
//        robot.add(new ReflectanceSensorArray());
//        robot.add(new LED());
//        robot.add(new Button());
//        robot.add(new Action() { //ação 0
//            @Override
//            public void putMessage(ByteBuffer data, Robot robot) {
//
//            }
//        });
//        robot.add(new RotateAction());//ação 1 (como na biblioteca em cpp)

//        connectionStatusGraph.setVisible(false);
        connectionComboBox.setModel(new DefaultComboBoxModel());
        statusLabel2.setText("");
        statusLabel3.setText("");

        new Thread("Repaint Thread- " + Thread.activeCount()) {
            @Override
            public void run() {
                try {
                    while (true) {
                        if (connectionStatusGraph instanceof ConnectionStatusGraph) {
                            ((ConnectionStatusGraph) connectionStatusGraph).step();
                        }
                        connectionStatusGraph.repaint();
                        Thread.sleep(500);
                    }
                } catch (InterruptedException ex) {
                }

            }
        }.start();

        ml = new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!connected) {
                    int i = connectionComboBox.getSelectedIndex();
                    connectionComboBox.removeAllItems();
                    connectionComboBox.addItem(VIRTUAL_CONNECTION);
                    Collection<String> availableDevices = serial.getAvailableDevices();
                    for (String str : availableDevices) {
                        connectionComboBox.addItem(str);
                    }
                    if (i != -1) {
                        connectionComboBox.setSelectedIndex(i);
                    }

                    if (button != null) {
                        if (e == null || e.getSource() == button) {
                            if (availableDevices.size() > 0) {
                                button.setEnabled(true);
                            } else {
                                button.setEnabled(false);
                            }
                        }
                    }
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        };

        for (Component c : connectionComboBox.getComponents()) {
            c.addMouseListener(ml);
        }

        ToolBarButton tbb = new ToolBarButton() {

            @Override
            public JPopupMenu getJPopupMenu() {
                JPopupMenu popup = new JPopupMenu();
                if (robot == null || !connected) {
                    ArrayList<Robot> availableRobots = new ArrayList<>();
                    EntityManager em = PluginManager.getInstance().createFactoryManager(null);
                    Project project = (Project) em.getProperty("s3f.core.project.tmp", "project");
                    for (s3f.core.project.Element e : project.getElements()) {
                        if (e instanceof Robot) {
                            Robot robot = (Robot) e;
                            availableRobots.add(robot);
                        }
                    }

                    if (availableRobots.isEmpty()) {
                        //TODO
                    } else if (availableRobots.size() == 1) {
                        robot = availableRobots.get(0);
                    } else {
                        ButtonGroup group = new ButtonGroup();
                        JRadioButtonMenuItem select = null;
                        for (final Robot r : availableRobots) {
                            JRadioButtonMenuItem item = new JRadioButtonMenuItem(r.getName());
                            if (select == null) {
                                select = item;
                                robot = r;
                            }
                            if (r == robot) {
                                item.setSelected(true);
                                select = item;
                                robot = r;
                            } else {
                                item.setSelected(false);
                            }
                            group.add(item);
                            popup.add(item).addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    robot = r;
                                    statusLabel4.setText(robot.getName());
                                    System.out.println(robot + " ddsds");
                                }
                            });
                            item.setUI(new BasicRadioButtonMenuItemUI() {
                                @Override
                                protected void doClick(MenuSelectionManager msm) {
                                    menuItem.doClick(0);
                                }
                            });
                            group.setSelected(select.getModel(), true);
                        }
                    }
                }
                if (robot != null && connected) {
                    popup.add(statusLabel);
                    popup.add(statusLabel2);
                    popup.add(statusLabel3);
                    popup.add(statusLabel4);
                    popup.add(connectionStatusGraph);
                    connectionStatusGraph.setBorder(new EmptyBorder(10, 10, 10, 10));
                }
                return popup;

            }
        };

        button = MainUI.createToolbarButton(tbb.getJComponent(), null, "adas", ICON_IDLE);

        button.addMouseListener(ml);

        tbb.setActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (robot != null) {
                            simpleConectButtonActionPerformed();
                        }
                    }
                }
        );
        ml.mouseEntered(null);

        connectionStatusGraph.setPreferredSize(new Dimension(80, 35));
//        

        this.addToolbarComponent(button, 600);
//        button.setRolloverEnabled(false);
//        this.addToolbarComponent(connectionStatusGraph, 600);
//        this.addToolbarComponent(new ToolBarButton().getJComponent(), 0);

    }

    public boolean tryConnect() {
        if (connected) {
            connected = false;

            if (connection != null) {
                connection.closeConnection();
                connection = null;
            }

            connectionComboBox.setEnabled(true);
            statusLabel.setForeground(Color.black);
            statusLabel.setText("Desconectado");
            statusLabel2.setText("");
            statusLabel3.setText("");

            robot.setMainConnection(null);

            if (button != null) {
                button.setIcon(ICON_OFFLINE);
                button.setToolTipText("Conectar");
            }

            connected = false;

            return true;
        } else {
            String str = (String) connectionComboBox.getSelectedItem();
            if (str.equals(VIRTUAL_CONNECTION)) {
                connection = new VirtualConnection();
                virtual = true;
            } else {
                serial.setDefaultPort(str);
                connection = new VirtualConnection(serial);
                virtual = false;
            }
            statusLabel.setForeground(Color.gray);
            statusLabel.setText("Conectando...");
            statusLabel2.setText("");
            connected = connection.establishConnection();
            if (connected) {
                statusLabel.setForeground(Color.green.darker());
                statusLabel.setText("Conectado");

                if (button != null) {
                    button.setIcon(ICON_IDLE);
                    button.setToolTipText("Desconectar");
                }

                connectionComboBox.setEnabled(false);
            } else {
                statusLabel.setForeground(Color.red.darker());
                statusLabel.setText("Falha");
                statusLabel2.setText("");
                statusLabel3.setText("");
                if (button != null) {
                    button.setIcon(ICON_FULL_ERROR);
                    button.setText("Tentar Novamente");
                }
                connectionComboBox.setEnabled(true);
                return false;
            }

            robot.setMainConnection(connection);

            if (connection instanceof VirtualConnection) {
                VirtualConnection v = (VirtualConnection) connection;
                v.setRobot(robot);
            }
            return true;
        }
    }

    public void simpleConectButtonActionPerformed() {
        if (connectionComboBox.getItemCount() == 2) {
            if (connected) {
                if (virtual) {
                    tryConnect();
                    connectionComboBox.setSelectedIndex(1);
                    tryConnect();
                } else {
                    tryConnect();
                    connectionComboBox.setSelectedIndex(0);
                    tryConnect();
                }
            } else {
                connectionComboBox.setSelectedIndex(1);
                tryConnect();
            }
        } else if (connectionComboBox.getItemCount() == 1) {
            connectionComboBox.setSelectedIndex(0);
            tryConnect();
        } else {

        }
    }
}
