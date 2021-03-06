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
 * JIFI is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * JIFI is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JIFI. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package s3f.dwrs.simulation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import static java.lang.Math.*;
import java.util.ArrayList;
import java.util.Iterator;
import s3f.core.plugin.Data;
import s3f.core.plugin.Plugabble;
import s3f.core.project.Editor;
import s3f.core.project.Element;
import s3f.core.ui.tab.TabProperty;
import s3f.dwrs.robot.Robot;
import s3f.dwrs.robot.device.IRProximitySensor;
import s3f.dwrs.robot.device.ReflectanceSensorArray;
import s3f.magenta.Drawable;
import s3f.magenta.DrawingPanel;
import s3f.magenta.sidepanel.Item;
import s3f.magenta.sidepanel.SidePanel;
import s3f.util.trafficsimulator.Timer;

/**
 * Painel da simulação do robô. <### EM DESENVOLVIMENTO ###>
 */
public class SimulationPanel extends DrawingPanel implements Serializable, Editor {

    public static final BasicStroke SIMPLE_STROKE = new BasicStroke();
    public static final BasicStroke DASHED_STROKE = new BasicStroke(1.0f,
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_MITER,
            10.0f, new float[]{5}, 0.0f);

    public static final Item ITEM_LINE;
    public static final Item ITEM_LINE_POLI;
    public static final Item ITEM_OBSTACLE_LINE;
    public static final Item ITEM_OBSTACLE_POLI;
    public static final Item ITEM_REMOVE_LINE;
    public static final Item ITEM_REMOVE_AREA;

    static {
        Area myShape = new Area();
        Polygon tmpShape = new Polygon();
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

        ITEM_REMOVE_LINE = new Item("Remover", new Area(myShape), Color.red, "Remove elementos na interseção com esta linha");

        Area myShape2 = new Area();
        myShape2.exclusiveOr(new Area(new Rectangle(3, 3, 14, 14)));
        myShape2.exclusiveOr(new Area(new Rectangle(0, 0, 20, 20)));
        myShape2.add(new Area(myShape));

        ITEM_REMOVE_AREA = new Item("Remover Área", myShape2, Color.red, "Remove elementos na interseção com esta linha");

        myShape2 = new Area();
        tmpShape = new Polygon();
        tmpShape.reset();
        tmpShape.addPoint(0, 20);
        tmpShape.addPoint(10, 0);
        tmpShape.addPoint(20, 20);
        myShape2.add(new Area(tmpShape));
        tmpShape.reset();
        tmpShape.addPoint(5, 17);
        tmpShape.addPoint(10, 6);
        tmpShape.addPoint(15, 17);
        myShape2.exclusiveOr(new Area(tmpShape));

        ITEM_OBSTACLE_POLI = new Item("Parede Fechada", myShape2, Environment.getObstacleColor(), "Cria paredes em forma de um polígono");
        ITEM_LINE = new Item("Linha", new Rectangle(0, 0, 20, 4), Color.DARK_GRAY, "Linha preta colocada no chão, detectada pelo sensor de refletância");
        ITEM_LINE_POLI = new Item("Linha Fechada", myShape2, Color.DARK_GRAY, "Cria linhas no chão em forma de um polígono");
        ITEM_OBSTACLE_LINE = new Item("Parede", new Rectangle(0, 0, 20, 4), Environment.getObstacleColor(), "Parede ou obstáculo, detectado pelo sensor de distância");
    }
    private final ArrayList<Robot> robots = new ArrayList<>();
    private Environment environment = new Environment();
    private Item itemSelected;
    private Point2D.Double point = null;
    private final Ellipse2D.Double circle = new Ellipse2D.Double();
    private final Ellipse2D.Double dot = new Ellipse2D.Double();
    private final Line2D.Double radius = new Line2D.Double();
    private int poliSegments = 6;
    SidePanel sidePanel;

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
    private final Data data;

    public SimulationPanel() {

        super.midMouseButtonResetView = false;

        sidePanel = new SidePanel(this) {
            @Override
            public void itemSelected(Item item, Object ref) {
                try {
                    if (itemSelected == item) {
                        itemSelected.setSelected(false);
                        itemSelected = null;
                    } else {
                        if (itemSelected != null) {
                            itemSelected.setSelected(false);
                            itemSelected = null;
                        }
                        itemSelected = item;
                        itemSelected.setSelected(true);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

        sidePanel.setColor(Color.decode("#9DCA1D"));//FF7070
        sidePanel.add(ITEM_LINE);
        sidePanel.add(ITEM_LINE_POLI);
        sidePanel.add(ITEM_OBSTACLE_LINE);
        sidePanel.add(ITEM_OBSTACLE_POLI);
//        sp.add(ITEM_CILINDER);
        sidePanel.add(ITEM_REMOVE_LINE);
        sidePanel.add(ITEM_REMOVE_AREA);
        add(sidePanel);

        //mapeia a posição a cada x ms
        Timer timer = new Timer(300) {
            ArrayList<Robot> tmpBots = new ArrayList<>();

            @Override
            public void run() {
                tmpBots.clear();
                synchronized (robots) {
                    tmpBots.addAll(robots);
                }

                for (Robot robot : tmpBots) {
                    if (!(robot.getLeftWheelSpeed() == 0 && robot.getRightWheelSpeed() == 0)) {
                        robot.updateVirtualPerception();
                    }

//                    robot.setRightWheelSpeed(30);
//                    robot.setLeftWheelSpeed(-30);
//                    if (this.getCount() % 20 == 0) {
//                        robot.setRightWheelSpeed(Math.random() * 100);
//                        robot.setLeftWheelSpeed(Math.random() * 100);
//                    }
                }
            }
        };
        timer.setDisposable(false);
        clock.addTimer(timer);
        clock.setPaused(false);

//        Robot r = new Robot();
//        r.add(new IRProximitySensor());
//        r.add(new ReflectanceSensorArray());
//        //r.setEnvironment(p.getEnv());
//        addRobot(r);
        data = new Data("enveditorTab", "s3f.dwrs.simulation", "Editor Tab");
        TabProperty.put(data, "Editor", null, "Editor de amb", this);
    }

    public void hideSidePanel(boolean b) {
        sidePanel.setOpen(!b);
    }

    public void addRobot(Robot robot) {
        synchronized (robots) {
            robots.add(robot);
        }
        add(robot);
    }

    public void removeRobot(Robot robot) {
        synchronized (robots) {
            robots.remove(robot);
        }
        remove(robot);
    }

    public ArrayList<Robot> getRobots() {
        return robots;
    }

    @Override
    public int getDrawableLayer() {
        return DrawingPanel.BACKGROUND_LAYER | DrawingPanel.DEFAULT_LAYER | DrawingPanel.TOP_LAYER;
    }

    @Override
    public void drawBackground(Graphics2D g, GraphicAttributes ga, InputState in) {
        super.drawBackground(g, ga, in);
        environment.draw(g);
        for (Robot robot : robots) {
            robot.getPerception().draw(g);
        }
    }

    @Override
    public void drawTopLayer(Graphics2D g, GraphicAttributes ga, InputState in) {
        if (sidePanel.getObjectBouds().contains(in.getMouse())) {
            return;
        }

        if (in.isKeyPressed(KeyEvent.VK_R) || in.isKeyPressed(KeyEvent.VK_M)) {
            Robot r = null;
            int d = Integer.MAX_VALUE;
            synchronized (robots) {
                for (Robot robot : robots) {
                    int x = (int) (robot.getPosX() - in.getTransformedMouse().x);
                    int y = (int) (robot.getPosY() - in.getTransformedMouse().y);
                    int tmpD = (int) (Math.sqrt(x * x + y * y));
                    if (tmpD < d) {
                        d = tmpD;
                        r = robot;
                    }
                }
            }
            if (r != null) {
                r.setSelected(true);
                double theta = Math.atan2((in.getTransformedMouse().y - r.getPosY()), (in.getTransformedMouse().x - r.getPosX()));
                if (in.mousePressed() && in.getMouseButton() == MouseEvent.BUTTON1) {
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    if (in.isKeyPressed(KeyEvent.VK_R)) {
                        zoomEnabled = false;

                        theta -= r.getTheta();
                        r.setTheta(r.getTheta() + theta);

                    } else if (in.isKeyPressed(KeyEvent.VK_M)) {
                        r.setLocation(in.getTransformedMouse().x, in.getTransformedMouse().y);
                    }
                }

                AffineTransform o = ga.getT(g.getTransform());
                AffineTransform t = ga.getT();
                ga.applyZoom(ga.applyGlobalPosition(t));
                g.setTransform(t);
                g.setColor(Color.MAGENTA);
                g.drawLine((int) r.getPosX(), (int) r.getPosY(), in.getTransformedMouse().x, in.getTransformedMouse().y);

                g.setColor(Color.BLUE);
                double w = Math.sqrt(Math.pow(r.getPosX() - in.getTransformedMouse().x, 2) + Math.pow(r.getPosY() - in.getTransformedMouse().y, 2));
                g.fill(new Arc2D.Double(r.getPosX() - w / 2, r.getPosY() - w / 2, w, w, -Math.toDegrees(r.getTheta()), (r.getTheta() - theta) * (180 / Math.PI), Arc2D.Double.PIE));

//                g.rotate(r.getTheta());
//                g.fill(new Arc2D.Double(r.getPosX() - w / 2, r.getPosY() - w / 2, w, w, 0, -Math.toDegrees(theta - r.getTheta()), Arc2D.Double.PIE));
                g.setTransform(o);
                ga.done(t);
                ga.done(o);

//                AffineTransform o = ga.getT(g.getTransform());
//                AffineTransform t = ga.getT();
//                ga.applyZoom(ga.applyGlobalPosition(t));
//                g.setTransform(t);
//                g.setColor(Color.MAGENTA);
//                g.drawLine((int) r.getPosX(), (int) r.getPosY(), in.getTransformedMouse().x, in.getTransformedMouse().y);
//
//                t.setTransform(o);
//                ga.applyZoom(ga.applyGlobalPosition(t));
//
//                g.setTransform(t);
////                g.rotate(r.getTheta());
//
//                
//
//                g.setTransform(o);
//                ga.done(t);
//                ga.done(o);
            }
        } else {
            this.setCursor(Cursor.getDefaultCursor());
        }

        if (in.isKeyPressed(KeyEvent.VK_CONTROL)) {
            if (in.mouseClicked() && in.getMouseButton() == MouseEvent.BUTTON2) {
                resetView();
            } else if (itemSelected == ITEM_LINE_POLI || itemSelected == ITEM_OBSTACLE_POLI) {
                zoomEnabled = false;
                int wr = -in.getMouseWheelRotation();
                if (poliSegments + wr >= 3 && poliSegments + wr < 16) {
                    poliSegments += wr;
                }
            }
        } else {
            zoomEnabled = true;
        }

        if (itemSelected != null) {
            if (itemSelected == ITEM_LINE || itemSelected == ITEM_OBSTACLE_LINE || itemSelected == ITEM_REMOVE_LINE) {
                if (in.mouseClicked()) {
                    if (in.getMouseButton() == MouseEvent.BUTTON1) {
                        if (point != null) {
                            Line2D.Double line = new Line2D.Double(point, in.getTransformedMouse());

                            if (itemSelected == ITEM_LINE) {
                                environment.addFollowLine(new double[]{line.x1, line.y1, line.x2, line.y2});
                            } else if (itemSelected == ITEM_OBSTACLE_LINE) {
                                environment.addWall(new double[]{line.x1, line.y1, line.x2, line.y2});
                            } else if (itemSelected == ITEM_REMOVE_LINE) {
                                for (Iterator<Line2D.Double> it = environment.linesIterator(); it.hasNext();) {
                                    Line2D.Double s = it.next();
                                    if (s.intersectsLine(line)) {
                                        environment.removeFollowLine(s);
                                        it.remove();
                                    }
                                }
                                for (Iterator<Line2D.Double> it = environment.obstaclesIterator(); it.hasNext();) {
                                    Line2D.Double s = it.next();
                                    if (s.intersectsLine(line)) {
                                        environment.removeWall(s);
                                        it.remove();
                                    }
                                }
                            }

                        }
                        point = new Point2D.Double(in.getTransformedMouse().x, in.getTransformedMouse().y);
                    } else {
                        point = null;
                    }
                }
            } else if (itemSelected == ITEM_LINE_POLI || itemSelected == ITEM_OBSTACLE_POLI) {
                boolean obstacle = (itemSelected == ITEM_OBSTACLE_POLI);
                if (in.mouseClicked()) {
                    if (in.getMouseButton() == MouseEvent.BUTTON1) {
                        if (point != null) {
                            Point point2 = in.getTransformedMouse();
                            double r = point.distance(point2);
                            double alpha = Math.atan2(point2.y - point.y, point2.x - point.x);
                            double theta = (2 * Math.PI) / poliSegments;
                            double ix = point.x + r * cos(alpha);
                            double iy = point.y + r * sin(alpha);
                            double ox = ix;
                            double oy = iy;
                            double tx;
                            double ty;

                            for (int i = 0; i < poliSegments; i++) {
                                alpha += theta;
                                tx = point.x + r * cos(alpha);
                                ty = point.y + r * sin(alpha);
                                if (obstacle) {
                                    environment.addWall(new double[]{ox, oy, tx, ty});
                                } else {
                                    environment.addFollowLine(new double[]{ox, oy, tx, ty});
                                }
                                ox = tx;
                                oy = ty;
                            }
                            point = null;
                            return;
                        }
                        point = new Point2D.Double(in.getTransformedMouse().x, in.getTransformedMouse().y);
                    } else {
                        point = null;
                    }
                }
            } else if (itemSelected == ITEM_REMOVE_AREA) {
                if (in.mouseClicked()) {
                    if (in.getMouseButton() == MouseEvent.BUTTON1) {
                        if (point != null) {
                            double x = (in.getTransformedMouse().x < point.x) ? in.getTransformedMouse().x : point.x;
                            double y = (in.getTransformedMouse().y < point.y) ? in.getTransformedMouse().y : point.y;
                            double w = Math.abs(in.getTransformedMouse().x - point.x);
                            double h = Math.abs(in.getTransformedMouse().y - point.y);
                            Rectangle2D.Double box = new Rectangle2D.Double(x, y, w, h);
                            for (Iterator<Line2D.Double> it = environment.linesIterator(); it.hasNext();) {
                                Line2D.Double s = it.next();
                                if (box.intersectsLine(s)) {
                                    environment.removeFollowLine(s);
                                    it.remove();
                                }
                            }
                            for (Iterator<Line2D.Double> it = environment.obstaclesIterator(); it.hasNext();) {
                                Line2D.Double s = it.next();
                                if (box.intersectsLine(s)) {
                                    environment.removeWall(s);
                                    it.remove();
                                }
                            }
                        }
                        point = new Point2D.Double(in.getTransformedMouse().x, in.getTransformedMouse().y);
                    } else {
                        point = null;
                    }
                }
            }
        }
    }

    @Override
    public void draw(Graphics2D g, GraphicAttributes ga, InputState in) {
        synchronized (robots) {
            for (Robot robot : robots) {

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

                    g.setStroke(DASHED_STROKE); //linha pontilhada
                    //desenha o circulo
                    g.setColor(Color.gray);
                    circle.setFrame(x - r, y - r, r * 2, r * 2);
                    try {
                        g.draw(circle);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("circle: " + circle);
                    }
                    //desenha o raio
                    g.setColor(Color.magenta);
                    radius.setLine(robot.getObjectBouds().x, robot.getObjectBouds().y, x, y);
                    g.draw(radius);
                    g.setStroke(SIMPLE_STROKE); //fim da linha pontilhada
                    //desenha o centro
                    dot.setFrame(x - 3, y - 3, 6, 6);
                    g.fill(dot);
                }
            }
        }

//        g.setColor(Color.red);
//        synchronized (rpos) {
//            paintPoints(g, rpos, 5);
//        }
//        g.setColor(Color.GREEN.brighter());
//        synchronized (obstacle) {
//            paintPoints(g, obstacle, 5);
//        }
//        per.draw(g);
        g.setStroke(Environment.DEFAULT_STROKE);

        if (itemSelected == ITEM_REMOVE_LINE || itemSelected == ITEM_REMOVE_AREA) {
            g.setColor(Color.red);
        } else {
            g.setColor(Color.green);
        }
        if (point != null) {
            if (itemSelected == ITEM_LINE || itemSelected == ITEM_OBSTACLE_LINE || itemSelected == ITEM_REMOVE_LINE) {
                g.drawLine((int) point.x, (int) point.y, (int) in.getTransformedMouse().x, (int) in.getTransformedMouse().y);
            } else if (itemSelected == ITEM_LINE_POLI || itemSelected == ITEM_OBSTACLE_POLI) {
                Point point2 = in.getTransformedMouse();
                double r = point.distance(point2);
                double alpha = Math.atan2(point2.y - point.y, point2.x - point.x);
                double theta = (2 * Math.PI) / poliSegments;
                double ix = point.x + r * cos(alpha);
                double iy = point.y + r * sin(alpha);
                double ox = ix;
                double oy = iy;
                double tx;
                double ty;

                for (int i = 0; i < poliSegments; i++) {
                    alpha += theta;
                    tx = point.x + r * cos(alpha);
                    ty = point.y + r * sin(alpha);
                    g.drawLine((int) ox, (int) oy, (int) tx, (int) ty);
                    ox = tx;
                    oy = ty;
                }
//                g.drawLine((int) ix, (int) iy, (int) ox, (int) oy);
            } else if (itemSelected == ITEM_REMOVE_AREA) {
                double x = (in.getTransformedMouse().x < point.x) ? in.getTransformedMouse().x : point.x;
                double y = (in.getTransformedMouse().y < point.y) ? in.getTransformedMouse().y : point.y;
                double w = in.getTransformedMouse().x - point.x;
                double h = in.getTransformedMouse().y - point.y;
                g.drawRect((int) x - 2, (int) y - 2, (int) Math.abs(w) + 4, (int) Math.abs(h) + 4);
                g.drawLine((int) point.x, (int) point.y, (int) in.getTransformedMouse().x, (int) in.getTransformedMouse().y);
                g.drawLine((int) (point.x + w), (int) point.y, (int) (in.getTransformedMouse().x - w), (int) in.getTransformedMouse().y);
            }
        }

//        g.setColor(Color.BLACK);
//        for (Shape s : obstacles) {
//            g.draw(s);
//        }
        g.setStroke(SIMPLE_STROKE);
    }

    public void resetSimulation() {
        // remove as percepções dos robos e retorna-os para a posicao (0,0)
        for (Robot r : robots) {
            r.reset();
        }
        // limpa o ambiente
        environment.clearEnvironment();
    }

//    public static void main(String[] args) {
//
//        SimulationPanel p = new SimulationPanel();
//        QuickFrame.create(p, "Teste Simulação").addComponentListener(p);
//
//        Robot r = new Robot();
//        r.add(new IRProximitySensor());
//        r.add(new ReflectanceSensorArray());
//        r.setEnvironment(p.getEnv());
//        p.addRobot(r);
//
//        r = new Robot();
//        r.add(new IRProximitySensor());
//        r.add(new ReflectanceSensorArray());
//        r.setEnvironment(p.getEnv());
//        p.addRobot(r);
//    }
    @Override
    public void setContent(Element content) {
        if (content instanceof Environment) {
            environment = (Environment) content;
            data.setProperty(TabProperty.TITLE, content.getName());
            data.setProperty(TabProperty.ICON, content.getIcon());
        }
    }

    @Override
    public Element getContent() {
        return environment;
    }

    @Override
    public void update() {
        int x = 0;
        for (Object o : environment.getExternalResources()) {
            if (o instanceof Robot) {
                Robot robot = (Robot) o;
                robot.setEnvironment(environment);
                addRobot(robot);
                robot.setLocation(x, 0);
                x += 50;
            } else if (o instanceof Drawable) {
                Drawable drawable = (Drawable) o;
                add(drawable);
            }
        }
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
        return new SimulationPanel();
    }
}
