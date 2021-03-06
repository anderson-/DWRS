/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.dwrs.simulation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import javax.swing.ImageIcon;
import s3f.core.plugin.Plugabble;
import s3f.core.project.ComplexElement;
import s3f.core.project.Editor;
import s3f.core.project.Element;
import s3f.core.project.Resource;
import s3f.core.project.editormanager.TextFile;
import s3f.dwrs.robot.Robot;
import s3f.dwrs.robot.device.IRProximitySensor;
import s3f.util.LineIterator;

/**
 *
 * @author antunes
 */
public class Environment extends ComplexElement implements TextFile {

    public static final Element ENV_FILE = new Environment();
    public static final Element.CategoryData ENV_FILES = new Element.CategoryData("Environments", "env", new ImageIcon(Environment.class.getResource("/resources/icons/fugue/maps-stack.png")), ENV_FILE);

    public static final Stroke DEFAULT_STROKE = new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.CAP_ROUND);

    private final ArrayList<Line2D.Double> followLines = new ArrayList<>();
    private final ArrayList<Line2D.Double> walls = new ArrayList<>();
    private final ArrayList<Line2D.Double> followLinesTmp = new ArrayList<>();
    private final ArrayList<Line2D.Double> wallsTmp = new ArrayList<>();
    private final ArrayList<double[]> wallsData = new ArrayList<>();
    private final ArrayList<double[]> followLinesData = new ArrayList<>();
    private final Color obstacleColor = Color.decode("#BA9C3A");

    public static Color getObstacleColor() {
        return Color.decode("#BA9C3A");
    }

    public Environment() {
        super("envirr", "/resources/icons/fugue/map.png", ENV_FILES, new Class[]{SimulationPanel.class}, Robot.R_FILES.getName());
    }

    @Deprecated
    private void alienCode() {
        HashMap<Character, Integer> map = new HashMap<>();
        map.put(' ', 0);
        map.put('t', 1);
        map.put('e', 2);
        map.put('r', 4);
        map.put('s', 8);
        map.put('o', 16);

        String str = "terrestres terrosos";
        int i = 0;
        for (char c : str.toCharArray()) {
            addCodedLetter(i, map.get(c));
            i++;
        }
    }

    @Deprecated
    private void addCodedLetter(int index, int code) {
        double w = 50;
        double x = index * 80 + 60;
        for (int i = 0; i < 5; i++) {
            if ((code & (1 << i)) != 0) {
                double h = i * 7 - 14;
                addFollowLine(new double[]{x, h, x + w, h});
            }
        }
    }

    public void addWall(double[] line) {
        wallsData.add(line);

        walls.add(new Line2D.Double(line[0], line[1], line[2], line[3]));
    }

    public void addFollowLine(double[] line) {
        followLinesData.add(line);
        followLines.add(new Line2D.Double(line[0], line[1], line[2], line[3]));
    }

    public void removeWall(Shape s) {
        int i = walls.indexOf(s);
        if (i != -1) {
            wallsData.remove(i);
        }
    }

    public void removeFollowLine(Shape s) {
        int i = followLines.indexOf(s);
        if (i != -1) {
            followLinesData.remove(i);
        }
    }

    public double beamDistance(double x, double y, double theta) {
        double df = IRProximitySensor.MAX_DISTANCE;
        double dt;
        double x2 = x + IRProximitySensor.MAX_DISTANCE * cos(theta);
        double y2 = y + IRProximitySensor.MAX_DISTANCE * sin(theta);
        Line2D.Double line = new Line2D.Double(x, y, x2, y2);
        Point2D p;
        wallsTmp.clear();
        wallsTmp.addAll(walls);
        for (Line2D.Double wall : wallsTmp) {
            Line2D.Double tmpLine = new Line2D.Double();
            for (Iterator<Point2D> lineIt = new LineIterator(line); lineIt.hasNext();) {
                p = lineIt.next();
                tmpLine.setLine(x, y, p.getX(), p.getY());
                if (tmpLine.intersectsLine(wall)) {
                    dt = sqrt(pow(x - p.getX(), 2) + pow(y - p.getY(), 2));
                    if (df > dt && dt > 0) {
                        df = dt;
                    }
                }
            }
        }
        return df / 2;
    }

    public boolean isOver(double x, double y) {
        followLinesTmp.clear();
        followLinesTmp.addAll(followLines);

        for (Shape s : followLinesTmp) {
            if (s.intersects(x - 3, y - 3, 6, 6)) {
                return true;
            }
        }
        return false;
    }

    public void draw(Graphics2D g) {

        Stroke str = g.getStroke();

        g.setStroke(DEFAULT_STROKE);

        g.setColor(Color.BLACK);
        for (Shape s : followLines) {
            g.draw(s);
        }

        g.setColor(obstacleColor);
        for (Shape s : walls) {
            g.draw(s);
        }

        g.setStroke(str);
    }

    public Iterator<Line2D.Double> linesIterator() {
        return followLines.iterator();
    }

    public Iterator<Line2D.Double> obstaclesIterator() {
        return walls.iterator();
    }

    public void clearEnvironment() {
        wallsData.clear();
        walls.clear();
        followLinesData.clear();
        followLines.clear();
    }

    @Override
    public Plugabble createInstance() {
        return new Environment();
    }

    @Override
    public void addResource(Resource resource) {
        super.addResource(resource);
        if (resource.getPrimary() instanceof Environment) {
            Environment e = (Environment) resource.getPrimary();
            e.addExternalResource(resource.getSecondary());
            Editor currentEditor = getCurrentEditor();
            if (currentEditor != null) {
                currentEditor.update();
            }
        }
    }

    @Override
    public void removeResource(Resource resource) {
        super.removeResource(resource);
        if (resource.getPrimary() instanceof Environment) {
            Environment e = (Environment) resource.getPrimary();
            e.removeExternalResource(resource.getSecondary());
            Editor currentEditor = getCurrentEditor();
            if (currentEditor != null) {
                currentEditor.update();
            }
        }
    }

    @Override
    public void setText(String text) {
        clearEnvironment();
        BufferedReader reader = new BufferedReader(new StringReader(text));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("#") && !line.trim().isEmpty()) {
                    String str = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
                    String[] argv = str.split(",");
                    if (line.contains("wall") && argv.length == 4) {
                        argv[0] = argv[0].trim();
                        argv[1] = argv[1].trim();
                        argv[2] = argv[2].trim();
                        argv[3] = argv[3].trim();

                        double x1 = Double.parseDouble(argv[0]);
                        double y1 = Double.parseDouble(argv[1]);
                        double x2 = Double.parseDouble(argv[2]);
                        double y2 = Double.parseDouble(argv[3]);

                        addWall(new double[]{x1, y1, x2, y2});

                    } else if (line.contains("line") && argv.length == 4) {
                        argv[0] = argv[0].trim();
                        argv[1] = argv[1].trim();
                        argv[2] = argv[2].trim();
                        argv[3] = argv[3].trim();

                        double x1 = Double.parseDouble(argv[0]);
                        double y1 = Double.parseDouble(argv[1]);
                        double x2 = Double.parseDouble(argv[2]);
                        double y2 = Double.parseDouble(argv[3]);

                        addFollowLine(new double[]{x1, y1, x2, y2});
                    }
                }
            }
            reader.close();
        } catch (IOException ex) {

        }
    }

    @Override
    public String getText() {
        StringBuilder sb = new StringBuilder();
        NumberFormat format = NumberFormat.getInstance(Locale.US);
        format.setGroupingUsed(false);
        if (format instanceof DecimalFormat) {
            ((DecimalFormat) format).applyPattern("#.00");
        }

        sb.append("# Environment ").append(System.currentTimeMillis()).append(" #\n");

        if (!wallsData.isEmpty()) {
            sb.append("# Obstacles #\n");
            sb.append("\n");
            for (double[] data : wallsData) {
                sb.append("wall(").append(format.format(data[0])).append(", ").append(format.format(data[1])).append(", ").append(format.format(data[2])).append(", ").append(format.format(data[3])).append(")\n");
            }
            sb.append("\n");
        }

        if (!followLinesData.isEmpty()) {
            sb.append("# Followable Lines #\n");
            sb.append("\n");
            for (double[] data : followLinesData) {
                sb.append("line(").append(format.format(data[0])).append(", ").append(format.format(data[1])).append(", ").append(format.format(data[2])).append(", ").append(format.format(data[3])).append(")\n");
            }
        }

        sb.append("\n");
        return sb.toString();
    }

}
