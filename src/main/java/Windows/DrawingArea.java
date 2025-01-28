package Windows;

import Operation.SpaceConverter;
import Operation.Spline;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.List;

public class DrawingArea extends JPanel {
    private Spline spline;
    private final int POINT_RAIDUS, width, height, bgGridCount = 5, resolution;
    private Point2D.Double selectedPoint;
    private final SpaceConverter sc = new SpaceConverter();
    
    public void setSelectedPoint(Point2D.Double selectedPoint) {
        this.selectedPoint = selectedPoint;
    }
    
    public void setSpline(Spline spline) {
        this.spline = spline;
    }
    
    public DrawingArea(Spline spline, int width, int height, int resolution, int POINT_RADIUS, Point2D.Double selectedPoint) {
        this.spline = spline;
        this.resolution = resolution;
        this.POINT_RAIDUS = POINT_RADIUS;
        this.width = width;
        this.height = height;
        this.selectedPoint = selectedPoint;
        setPreferredSize(new Dimension(width, height));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
//        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON
        
        // Bg
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // Bg grid
        g2d.setColor(Color.GRAY);
        for (int i = 0; i < bgGridCount; i++) {
            g2d.drawLine(width / (bgGridCount - 1) * i, 0, width / (bgGridCount - 1) * i, height);
            g2d.drawLine(0, height / (bgGridCount - 1) * i, width, height / (bgGridCount - 1) * i);
        }
        
        // Border
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        
        // Polygon for area under the spline
        Path2D.Double polygon = new Path2D.Double();
        
        // Start points for bottom of area
        polygon.moveTo(width, height);
        polygon.lineTo(0, height);
        
        List<Point2D.Double> splinePoints = spline.generateSplinePoints(resolution);
        
        for (int i = 0; i < splinePoints.size(); i++) {
            Point2D.Double p = splinePoints.get(i);
            
            int screenX = (int) sc.convertValueSpace(0, 255, p.x, 0, width, false);
            int screenY = (int) sc.convertValueSpace(0, 255, p.y, 0, height, true);
            
            polygon.lineTo(screenX, screenY);
        }
        
        polygon.closePath();
        
        // Polygon
        g2d.setColor(new Color(255, 255, 255, 50));
        g2d.fill(polygon);
        
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1));
        g2d.draw(polygon);
        
        // Control points
        int border = 3;
        
        for (Point2D.Double point : spline.getControlPoints()) {
            int screenX = (int) sc.convertValueSpace(0, 255, point.x, 0, width, false);
            int screenY = (int) sc.convertValueSpace(0, 255, point.y, 0, height, true);
            
            int x = screenX - this.POINT_RAIDUS;
            int y = screenY - this.POINT_RAIDUS;
            
            g2d.setColor(Color.BLACK);
            g2d.fillOval(x, y, this.POINT_RAIDUS * 2, this.POINT_RAIDUS * 2);
            
            g2d.setColor(Color.WHITE);
            g2d.drawOval(x, y, this.POINT_RAIDUS * 2, this.POINT_RAIDUS * 2);
            
            if (point.equals(this.selectedPoint)) {
                g2d.drawOval(x - border, y - border, this.POINT_RAIDUS * 2 + border * 2, this.POINT_RAIDUS * 2 + border * 2);
            }
        }
    }
}