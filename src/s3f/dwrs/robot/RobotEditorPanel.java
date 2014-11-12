/**
 * @file .java
 * @author Anderson Antunes <anderson.utf@gmail.com>
 *         *seu nome* <*seu email*>
 * @version 1.0
 *
 * @section LICENSE
 *
 * Copyright (C) 2013 by Anderson Antunes <anderson.utf@gmail.com>
 *                       *seu nome* <*seu email*>
 *
 * RobotInterface is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * RobotInterface is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * RobotInterface. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package s3f.dwrs.robot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import static java.lang.Math.*;
import java.util.ArrayList;
import java.util.List;
import s3f.core.plugin.Data;
import s3f.core.plugin.EntityManager;
import s3f.core.plugin.Plugabble;
import s3f.core.plugin.PluginManager;
import s3f.core.project.Editor;
import s3f.core.project.Element;
import s3f.core.ui.tab.TabProperty;
import s3f.dwrs.robot.Robot;
import s3f.dwrs.robot.device.Device;
import s3f.magenta.Drawable;
import s3f.magenta.DrawingPanel;
import s3f.magenta.Rotable;
import s3f.magenta.Selectable;
import s3f.magenta.sidepanel.Classifiable;
import s3f.magenta.sidepanel.Configurable;
import s3f.magenta.sidepanel.Item;
import s3f.magenta.sidepanel.SidePanel;
import s3f.util.trafficsimulator.Timer;

/**
 * Painel da simulação do robô. <### EM DESENVOLVIMENTO ###>
 */
public class RobotEditorPanel extends DrawingPanel implements Editor {

    private final Item ITEM_ADD_DEVICE;
    private final Item ITEM_GO_BACK;
    private final Item ITEM_REMOVE;
    public static Color SELECTED_COLOR = Color.decode("#6BE400");
    private final Data data;

    {
        Area myShape = new Area();
        Polygon tmpShape = new Polygon();
        tmpShape.addPoint(16, 6);
        tmpShape.addPoint(16, 10);
        tmpShape.addPoint(0, 10);
        tmpShape.addPoint(0, 6);
        myShape.add(new Area(tmpShape));

        tmpShape.reset();
        tmpShape.addPoint(6, 16);
        tmpShape.addPoint(10, 16);
        tmpShape.addPoint(10, 0);
        tmpShape.addPoint(6, 0);
        myShape.add(new Area(tmpShape));

        ITEM_ADD_DEVICE = new Item("Novo Dispositivo", myShape, Color.decode("#0969A2"), "");

        tmpShape.reset();
        tmpShape.addPoint(0, 10);
        tmpShape.addPoint(10, 0);
        tmpShape.addPoint(10, 5);
        tmpShape.addPoint(20, 5);

        tmpShape.addPoint(20, 8);
        tmpShape.addPoint(9, 8);
        tmpShape.addPoint(9, 5);

        tmpShape.addPoint(4, 10);

        tmpShape.addPoint(9, 15);
        tmpShape.addPoint(9, 12);
        tmpShape.addPoint(20, 12);

        tmpShape.addPoint(20, 15);
        tmpShape.addPoint(10, 15);
        tmpShape.addPoint(10, 20);

        ITEM_GO_BACK = new Item("Voltar", tmpShape, Color.decode("#FF7800"), "");

        myShape = new Area();
        tmpShape = new Polygon();
        tmpShape.addPoint(2, 0);
        tmpShape.addPoint(20, 18);
        tmpShape.addPoint(18, 20);
        tmpShape.addPoint(0, 2);
        myShape.add(new Area(tmpShape));

        tmpShape.reset();
        tmpShape.addPoint(18, 0);
        tmpShape.addPoint(20, 2);
        tmpShape.addPoint(2, 20);
        tmpShape.addPoint(0, 18);
        myShape.add(new Area(tmpShape));

        ITEM_REMOVE = new Item("Remover", new Area(myShape), Color.red, "");

    }
    private Item itemSelected;
    private Point2D.Double point = null;
    private final Ellipse2D.Double circle = new Ellipse2D.Double();
    private final Ellipse2D.Double dot = new Ellipse2D.Double();
    private final Line2D.Double radius = new Line2D.Double();
    private int poliSegments = 6;
    private SidePanel sidePanel;
    private Robot robot;

    public static Shape create(int i, double x, double y, double r, Path2D.Double poly) {

        if (poly == null) {
            poly = new Path2D.Double();
        } else {
            poly.reset();
        }

        double alpha = 1;
        double theta = (2 * Math.PI) / i;
        double tx = x + r * cos(alpha);
        double ty = y + r * sin(alpha);

        poly.moveTo(tx, ty);

        for (int j = 0; j < i; j++) {
            alpha += theta;
            tx = x + r * cos(alpha);
            ty = y + r * sin(alpha);
            poly.lineTo((int) tx, (int) ty);
        }

        return poly;
    }

    public RobotEditorPanel() {
        super.midMouseButtonResetView = false;

        gridSize = 30;
        zoom = 3.2;
        sidePanel = new SidePanel(this) {

            void goBack() {
                if (itemSelected != null && itemSelected.getRef() instanceof Selectable) {
                    itemSelected.setSelected(false);
                    ((Selectable) itemSelected.getRef()).setSelected(false);
                }
                itemSelected = null;
                sidePanel.clearPanel();

                Robot robot = RobotEditorPanel.this.robot;
                if (robot != null) {
                    for (Device d : robot.getDevices()) {
                        if (d instanceof Classifiable) {
                            Item newItem = ((Classifiable) d).getItem().copy();
                            newItem.setName("[" + (d.getID()) + "] " + newItem.getName());
                            sidePanel.add(newItem);
                        }
                    }
                }

                sidePanel.add(ITEM_ADD_DEVICE);
                sidePanel.switchAnimLeft();
            }

            @Override
            public void itemSelected(Item item, Object ref) {
                if (item == null) {

                } else if (item == ITEM_ADD_DEVICE) {
                    sidePanel.clearTempPanel();
                    sidePanel.addTmp(ITEM_GO_BACK);

                    EntityManager em = PluginManager.getInstance().createFactoryManager(null);
                    List<Classifiable> list = em.getAllProperties("s3f.dwrs.device.*", "device", Classifiable.class);
                    if (list != null) {
                        for (Classifiable o : list) {
                            try {
                                Item newItem = ((Classifiable) o.getClass().newInstance()).getItem();
                                newItem.setRef(o.getClass());
                                sidePanel.addTmp(newItem);
                            } catch (InstantiationException ex) {

                            } catch (IllegalAccessException ex) {

                            }
                        }
                    }

//                    for (Class c : RobotControlPanel.getAvailableDevices()) {
//                        if (Classifiable.class.isAssignableFrom(c)) {
//                            try {
//                                Item newItem = ((Classifiable) c.newInstance()).getItem();
//                                newItem.setRef(c);
//                                sidePanel.addTmp(newItem);
//                            } catch (InstantiationException ex) {
//
//                            } catch (IllegalAccessException ex) {
//
//                            }
//                        }
//                    }
                    sidePanel.switchAnimRight();

                } else if (item == ITEM_GO_BACK) {
                    goBack();
                } else if (item == ITEM_REMOVE) {
                    RobotEditorPanel.this.robot.remove((Device) itemSelected.getRef());
                    goBack();
                } else {
                    if (item.getRef() instanceof Class) {
                        try {
                            RobotEditorPanel.this.robot.add((Device) (((Class) item.getRef()).newInstance()));
                        } catch (Exception ex) {
                        }

                        goBack();
                    } else {
                        sidePanel.clearTempPanel();
                        sidePanel.addTmp(ITEM_GO_BACK);

                        if (item.getRef() instanceof Configurable) {
                            Configurable c = (Configurable) item.getRef();
                            sidePanel.addTmp(c.getConfigurationPanel());
                        }

                        sidePanel.addTmp(ITEM_REMOVE);
                        sidePanel.switchAnimRight();

                        if (item.getRef() instanceof Selectable) {
                            item.setSelected(true);
                            ((Selectable) item.getRef()).setSelected(true);
                        }
                    }

                }
                itemSelected = item;
            }
        };

        sidePanel.itemSelected(ITEM_GO_BACK, null);

        sidePanel.setColor(Color.decode("#4D4388"));//FF7070

        add(sidePanel);

        //mapeia a posição a cada x ms
        Timer timer = new Timer(300) {
            ArrayList<Robot> tmpBots = new ArrayList<>();

            @Override
            public void run() {
                tmpBots.clear();
                if (robot != null) {
                    if (!(robot.getLeftWheelSpeed() == 0 && robot.getRightWheelSpeed() == 0)) {
                        robot.updateVirtualPerception();
                    }

//                    robot.setRightWheelSpeed(30);
//                    robot.setLeftWheelSpeed(-30);
                    if (this.getCount() % 20 == 0) {
//                        robot.setRightWheelSpeed(Math.random() * 100);
//                        robot.setLeftWheelSpeed(Math.random() * 100);
                    }
                }
            }
        };
        timer.setDisposable(false);
        clock.addTimer(timer);
        clock.setPaused(false);
        data = new Data("ReditorTab", "s3f.dwrs.robot", "REditor Tab");
        TabProperty.put(data, "REditor", null, "Editor de robo", this);
    }

    public void hideSidePanel(boolean b) {
        sidePanel.setOpen(!b);
    }

    @Override
    public int getDrawableLayer() {
        return DrawingPanel.BACKGROUND_LAYER | DrawingPanel.DEFAULT_LAYER | DrawingPanel.TOP_LAYER;
    }

    @Override
    public void drawBackground(Graphics2D g, GraphicAttributes ga, InputState in) {
        super.drawBackground(g, ga, in);
    }

    @Override
    public void drawTopLayer(Graphics2D g, GraphicAttributes ga, InputState in) {
        robot.setTheta(0);
        robot.setLocation(0, 0);
        if (itemSelected != null && itemSelected.getRef() instanceof Drawable) {
            //desenha a origem
            Drawable d = ((Drawable) itemSelected.getRef());
            if (d != null) {
                g.setColor(Color.MAGENTA);
                AffineTransform transform = g.getTransform();
                ga.applyGlobalPosition(transform);
                ga.applyZoom(transform);
                g.setTransform(transform);
                g.drawOval((int) d.getPosX() - 1, (int) d.getPosY() - 1, 2, 2);
                //g.fillRect((int) d.getPosX() - 2, (int) d.getPosY() - 2, 4, 4);
            }
        }
        if (sidePanel.getObjectBouds().contains(in.getMouse())) {
            return;
        }

        if (itemSelected != null && itemSelected.getRef() instanceof Drawable) {
            //desenha a origem
            Drawable d = ((Drawable) itemSelected.getRef());

            if (in.mousePressed() && in.getMouseButton() == MouseEvent.BUTTON1) {
                this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                if (in.isKeyPressed(KeyEvent.VK_R) && d instanceof Rotable) {
                    Rotable rotable = (Rotable) d;
                    zoomEnabled = false;

                    double theta = Math.atan2((in.getTransformedMouse().y - d.getPosY()), (in.getTransformedMouse().x - d.getPosX()));
                    g.setColor(Color.MAGENTA);
                    g.drawLine((int) d.getPosX(), (int) d.getPosY(), in.getTransformedMouse().x, in.getTransformedMouse().y);
                    rotable.setTheta(theta);
                } else if (in.isKeyPressed(KeyEvent.VK_M)) {
                    d.setLocation(in.getTransformedMouse().x, in.getTransformedMouse().y);
                }
            } else {
                this.setCursor(Cursor.getDefaultCursor());
            }

            if (in.isKeyPressed(KeyEvent.VK_CONTROL)) {
                if (in.mouseClicked() && in.getMouseButton() == MouseEvent.BUTTON2) {
                    resetView();
                }

//                if (in.isKeyPressed(KeyEvent.VK_R) && d instanceof Rotable) {
//                    Rotable rotable = (Rotable) d;
//                    zoomEnabled = false;
//                    int wr = -in.getMouseWheelRotation();
//                    rotable.setTheta(rotable.getTheta() + wr / 5.0);
//                } else if (in.mousePressed() && in.getMouseButton() == MouseEvent.BUTTON1) {
//                    d.setLocation(in.getTransformedMouse().x, in.getTransformedMouse().y);
//                }
            } else {
                zoomEnabled = true;
            }
        }
    }
    public static final BasicStroke defaultStroke = new BasicStroke();
    public static final BasicStroke dashedStroke = new BasicStroke(1.0f,
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_MITER,
            10.0f, new float[]{5}, 0.0f);

    @Override
    public void draw(Graphics2D g, GraphicAttributes ga, InputState in) {

        if (robot != null) {

            if (in.mouseClicked() && in.getMouseButton() == MouseEvent.BUTTON2) {
                if (!in.isKeyPressed(KeyEvent.VK_CONTROL)) {
                    robot.setLocation(0, 0);
                    robot.setTheta(0);
                }
            }

            double v1 = robot.getLeftWheelSpeed();
            double v2 = robot.getRightWheelSpeed();

            //desenha o caminho
            if (v1 != v2) {
                //calcula o raio
                double r = Robot.size / 2 * ((v1 + v2) / (v1 - v2));
                //calcula o centro (ortogonal à direção atual do robô)
                double x, y;
                if (r < 0) {
                    r *= -1;
                    x = (cos(-robot.getTheta() + PI / 2) * r + robot.getObjectBouds().x);
                    y = (-sin(-robot.getTheta() + PI / 2) * r + robot.getObjectBouds().y);
                } else {
                    x = (cos(-robot.getTheta() - PI / 2) * r + robot.getObjectBouds().x);
                    y = (-sin(-robot.getTheta() - PI / 2) * r + robot.getObjectBouds().y);
                }

                g.setStroke(dashedStroke); //linha pontilhada
                //desenha o circulo
                g.setColor(Color.gray);
                circle.setFrame(x - r, y - r, r * 2, r * 2);
                try {
                    g.draw(circle);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(circle);
                    System.exit(0);
                }
                //desenha o raio
                g.setColor(Color.magenta);
                radius.setLine(robot.getObjectBouds().x, robot.getObjectBouds().y, x, y);
                g.draw(radius);
                g.setStroke(defaultStroke); //fim da linha pontilhada
                //desenha o centro
                dot.setFrame(x - 3, y - 3, 6, 6);
                g.fill(dot);
            }
        }

        g.setStroke(new BasicStroke(5));
        g.setColor(Color.green);
        g.setStroke(defaultStroke);
    }

//    public static void main(String[] args) {
//
//        Robot r = new Robot();
//        r.add(new IRProximitySensor());
//        r.add(new ReflectanceSensorArray());
//        r.add(new LED());
//        r.add(new Button());
//        r.add(new Button());
//        r.add(new Button());
//
//        RobotEditorPanel p = new RobotEditorPanel(r);
//        QuickFrame.create(p, "Teste Simulação").addComponentListener(p);
//
//    }
    @Override
    public void setContent(Element content) {
        if (content instanceof Robot) {
            robot = (Robot) content;
            if (robot != null) {
                remove(robot);
            }
            add(robot);
            data.setProperty(TabProperty.TITLE, content.getName());
            data.setProperty(TabProperty.ICON, content.getIcon());
        }
    }

    @Override
    public Element getContent() {
        return robot;
    }

    @Override
    public void update() {

    }

    @Override
    public void selected() {

    }

    @Override
    public Data getData() {
        return data;
    }

    @Override
    public void init() {

    }

    @Override
    public Plugabble createInstance() {
        return new RobotEditorPanel();
    }
}
