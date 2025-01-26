package Operation;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Spline {
    private final TYPE type;
    private List<Point2D.Double> controlPoints;
    private double[] a, b, c, d;
    private double[] h;
    private final int min, max;
    
    public TYPE getType() {
        return this.type;
    }
    
    public int getMin() {
        return this.min;
    }
    
    public int getMax() {
        return this.max;
    }
    
    public Spline(int min, int max, TYPE type) {
        controlPoints = new ArrayList<>();
        this.min = min;
        this.max = max;
        this.type = type;
    }
    
    public List<Point2D.Double> getControlPoints() {
        return controlPoints;
    }
    
    public void addControlPoint(int index, double x, double y) {
        Point2D.Double newPoint = new Point2D.Double(x, y);
        controlPoints.add(index, newPoint);
    }
    
    public void addControlPoint(int index, Point2D.Double newPoint) {
        controlPoints.add(index, newPoint);
    }
    
    public List<Point2D.Double> generateSplinePoints(double resolution) {
        if (controlPoints.size() < 2) {
            throw new IllegalArgumentException("A spline requires at least 2 control points.");
        }
        
        calculateCubicCoefficients();
        
        List<Point2D.Double> splinePoints = new ArrayList<>();
        double step = (this.max - this.min) / resolution;
        
        Point2D.Double start = controlPoints.get(0);
        Point2D.Double end = controlPoints.get(controlPoints.size() - 1);
        
        for (double x = this.min; x <= this.max; x += step) {
            double y;
            
            // Clamp values outside of control points
            if (x <= start.x) {
                y = start.y;
            } else if (x >= end.x) {
                y = end.y;
            } else {
                y = interpolateCubic(x);
            }
            
            // Clamp values
            y = Math.min(max, Math.max(min, y));
            
            splinePoints.add(new Point2D.Double(x, y));
        }
        
        return splinePoints;
    }
    
    private void resolveDuplicateX() {
        double epsilon = 1e-5; // Deslocation
        
        for (int i = 1; i < controlPoints.size(); i++) {
            if (controlPoints.get(i).x == controlPoints.get(i - 1).x) {
                controlPoints.get(i).x += epsilon;
            }
        }
    }
    
    public void calculateCubicCoefficients() {
        resolveDuplicateX();
        
        int n = controlPoints.size() - 1;
        a = new double[n + 1];
        b = new double[n];
        c = new double[n + 1];
        d = new double[n];
        h = new double[n];
        
        // Stage 1: Distance between points
        for (int i = 0; i < n; i++) {
            h[i] = controlPoints.get(i + 1).x - controlPoints.get(i).x;
            a[i] = controlPoints.get(i).y;
        }
        
        a[n] = controlPoints.get(n).y;
        
        // Stage 2:
        double[] alpha = new double[n];
        
        for (int i = 1; i < n; i++) {
            alpha[i] = (3 / h[i]) * (a[i + 1] - a[i]) - (3 / h[i - 1]) * (a[i] - a[i - 1]);
        }
        
        double[] l = new double[n + 1];
        double[] mu = new double[n];
        double[] z = new double[n + 1];
        
        l[0] = 1;
        mu[0] = 0;
        z[0] = 0;
        
        for (int i = 1; i < n; i++) {
            l[i] = 2 * (controlPoints.get(i + 1).x - controlPoints.get(i - 1).x) - h[i - 1] * mu[i - 1];
            mu[i] = h[i] / l[i];
            z[i] = (alpha[i] - h[i - 1] * z[i - 1]) / l[i];
        }
        
        l[n] = 1;
        z[n] = 0;
        c[n] = 0;
        
        // Stage 3: Coeficients
        for (int j = n - 1; j >= 0; j--) {
            c[j] = z[j] - mu[j] * c[j + 1];
            b[j] = (a[j + 1] - a[j]) / h[j] - h[j] * (c[j + 1] + 2 * c[j]) / 3;
            d[j] = (c[j + 1] - c[j]) / (3 * h[j]);
        }
    }
    
    public double interpolateCubic(double x) {
        int i = findInterval(x);
        
        double dx = x - controlPoints.get(i).x;
        
        return a[i] + b[i] * dx + c[i] * dx * dx + d[i] * dx * dx * dx;
    }
    
    private int findInterval(double x) {
        for (int i = 0; i < controlPoints.size() - 1; i++) {
            if (x >= controlPoints.get(i).x && x <= controlPoints.get(i + 1).x) {
                return i;
            }
        }
        
        throw new IllegalArgumentException("X is outside the range of the spline.");
    }
}