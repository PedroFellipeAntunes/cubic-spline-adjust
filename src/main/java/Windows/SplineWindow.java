package Windows;

import Operation.Operations;
import Operation.SpaceConverter;
import Operation.Spline;
import Operation.SplineEvaluator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import javax.swing.plaf.basic.ComboPopup;

public class SplineWindow extends JDialog {
    private final SpaceConverter sc = new SpaceConverter();
    
    // Spline
    private Spline[] splines;
    private Spline currentSpline;
    private Point2D.Double selectedPoint;
    private final int POINT_RADIUS = 5, width, height, resolution = 1000;
    
    // Components
    private JComboBox<String> splineSelector;
    private final DrawingArea drawingArea;
    private JTextField inputTA = new JTextField(), outputTA = new JTextField();

    public SplineWindow(Spline[] splines, int width, int height, BufferedImage image, String filePath) {
        super((Frame) null, "Spline Editor", true);
        this.setResizable(false);
        this.splines = splines;
        this.currentSpline = splines[0];
        this.selectedPoint = this.currentSpline.getControlPoints().getFirst();
        
        this.width = width;
        this.height = height;
        
        // BG color
        setBackground(Color.BLACK);
        
        // ComboBox
        splineSelector = new JComboBox<>();
        splineSelector.setBackground(Color.BLACK);
        splineSelector.setForeground(Color.WHITE);
        
        // ComboBox visuals
        splineSelector.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected ComboBoxEditor createEditor() {
                ComboBoxEditor editor = super.createEditor();
                editor.getEditorComponent().setBackground(Color.BLACK);
                editor.getEditorComponent().setForeground(Color.WHITE);
                return editor;
            }

            @Override
            protected ComboPopup createPopup() {
                ComboPopup popup = super.createPopup();
                popup.getList().setBackground(Color.BLACK);
                popup.getList().setForeground(Color.WHITE);
                
                popup.getList().setSelectionBackground(Color.WHITE);
                popup.getList().setSelectionForeground(Color.BLACK);
                
                return popup;
            }
            
            @Override
            protected JButton createArrowButton() {
                JButton arrowButton = super.createArrowButton();
                arrowButton.setBackground(Color.BLACK);
                arrowButton.setForeground(Color.WHITE);
                
                arrowButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
                
                return arrowButton;
            }
            
            @Override
            public void paint(Graphics g, JComponent c) {
                super.paint(g, c);
                if (splineSelector.hasFocus()) {
//                    c.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
                }
            }
        });
        
        for (Spline spline : splines) {
            splineSelector.addItem(spline.getType().name());
        }
        
        splineSelector.addActionListener(e -> onSplineSelected());
        
        // Set up the layout
        JPanel topPanel = new JPanel();
        topPanel.setBackground(Color.BLACK);
        topPanel.setLayout(new BorderLayout());
        topPanel.add(splineSelector, BorderLayout.NORTH);
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));
        
        add(topPanel, BorderLayout.NORTH);
        
        // Set up the drawing area
        drawingArea = new DrawingArea(currentSpline, width, height, resolution, POINT_RADIUS, selectedPoint);
        drawingArea.setFocusable(true);
        
        add(drawingArea, BorderLayout.CENTER);
        
        // Bottom panel configuration (input, output, and finish button)
        configureBottomPanel(image, filePath);
        
        // Configure events (mouse and key listeners)
        configureEvents();
        
        // Window settings
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }
    
    private void configureBottomPanel(BufferedImage image, String filePath) {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.BLACK);
        bottomPanel.setLayout(new GridLayout(3, 2));
        
        JLabel inputLabel = new JLabel("Input:");
        inputLabel.setForeground(Color.WHITE);
        inputLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        inputTA.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        inputTA.addActionListener(e -> updateSelectedPointFromInput(inputTA, outputTA));
        
        JScrollPane inputScroll = new JScrollPane(inputTA);
        
        JLabel outputLabel = new JLabel("Output:");
        outputLabel.setForeground(Color.WHITE);
        outputLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        outputTA.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        outputTA.addActionListener(e -> updateSelectedPointFromInput(inputTA, outputTA));
        
        JScrollPane outputScroll = new JScrollPane(outputTA);
        
        updateTextFields(); // Update starting values
        
        bottomPanel.add(inputLabel);
        bottomPanel.add(inputScroll);
        bottomPanel.add(outputLabel);
        bottomPanel.add(outputScroll);
        
        JButton finishBt = new JButton("Finish");
        finishBt.setBackground(Color.BLACK);
        finishBt.setForeground(Color.WHITE);
        finishBt.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        finishBt.addActionListener(e -> finishProcessing(image, filePath));
        
        bottomPanel.add(new JLabel()); // Empty space for alignment
        bottomPanel.add(finishBt);
        
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0)); // Margin
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void updateSelectedPointFromInput(JTextField inputTA, JTextField outputTA) {
        try {
            double splineX = Double.parseDouble(inputTA.getText().trim());
            double splineY = Double.parseDouble(outputTA.getText().trim());
            
            // Convert spline space to screen space (will be converted back in checkBounds)
            splineX = sc.convertValueSpace(currentSpline.getMin(), currentSpline.getMax(), splineX, 0, width, false);
            splineY = sc.convertValueSpace(currentSpline.getMin(), currentSpline.getMax(), splineY, 0, height, true);
            
            checkCoordBounds(splineX, splineY);
            
            updateTextFields();
            
            drawingArea.repaint();
        } catch (NumberFormatException e) {
            updateTextFields();
            System.out.println("Error updating input field: " + e.getMessage());
        }
        
        drawingArea.requestFocusInWindow();
    }
    
    private void configureEvents() {
        drawingArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePress(e);
            }
        });
        
        drawingArea.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseDrag(e);
            }
        });
        
        drawingArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    removeSelectedPoint();
                }
            }
        });
    }
    
    private void finishProcessing(BufferedImage image, String filePath) {
        SplineEvaluator ev = new SplineEvaluator(currentSpline);
        int[] colorMap = new int[256];

        for (int i = 0; i < 256; i++) {
            colorMap[i] = (int) Math.ceil(ev.evaluate(i));
        }
        
        Operations.nextStep(image, colorMap, filePath, currentSpline.getType());
        drawingArea.requestFocusInWindow();
    }
    
    // Update when spline selected changes
    private void onSplineSelected() {
        int selectedIndex = splineSelector.getSelectedIndex();
        currentSpline = splines[selectedIndex];
        selectedPoint = currentSpline.getControlPoints().getFirst();
        
        drawingArea.setSpline(currentSpline);
        drawingArea.setSelectedPoint(selectedPoint);
        drawingArea.repaint();
        drawingArea.requestFocusInWindow();
        
        updateTextFields();
    }
    
    private void updateTextFields() {
        inputTA.setText(String.valueOf((int) selectedPoint.x));
        outputTA.setText(String.valueOf((int) selectedPoint.y));
    }

    private void handleMousePress(MouseEvent e) {
        drawingArea.requestFocusInWindow();
        
        Point panelPoint = e.getPoint();
        selectedPoint = findClosestPoint(panelPoint.x, panelPoint.y);
        
        // Get point or add new
        if (selectedPoint != null) {
            drawingArea.setSelectedPoint(selectedPoint);
            drawingArea.repaint();
            
            updateTextFields();
        } else {
            addNewPoint(panelPoint.x, panelPoint.y);
            
            updateTextFields();
        }
    }
    
    private void handleMouseDrag(MouseEvent e) {
        drawingArea.requestFocusInWindow();

        if (selectedPoint != null) {
            Point panelPoint = e.getPoint();
            
            checkCoordBounds(panelPoint.x, panelPoint.y);
            updateTextFields();
            
            drawingArea.repaint();
        }
    }
    
    private void checkCoordBounds(double x, double y) {
        // Convert screen space to spline space
        double splineX = sc.convertValueSpace(0, width, x, currentSpline.getMin(), currentSpline.getMax(), false);
        double splineY = sc.convertValueSpace(0, height, y, currentSpline.getMin(), currentSpline.getMax(), true);
        
        int index = currentSpline.getControlPoints().indexOf(selectedPoint);
        
        // Clamp X value to spline space
        selectedPoint.x = Math.max(currentSpline.getMin(), Math.min(splineX, currentSpline.getMax()));
        
        // Don't go beyond previous point (exception for 1st point)
        if (index > 0) {
            selectedPoint.x = Math.max(selectedPoint.x, currentSpline.getControlPoints().get(index - 1).x);
        }
        
        // Don't go beyond next point (exception for last point)
        if (index < currentSpline.getControlPoints().size() - 1) {
            selectedPoint.x = Math.min(selectedPoint.x, currentSpline.getControlPoints().get(index + 1).x);
        }
        
        // Clamp Y value to spline space
        selectedPoint.y = Math.max(currentSpline.getMin(), Math.min(splineY, currentSpline.getMax()));
    }
    
    private void removeSelectedPoint() {
        if (currentSpline.getControlPoints().size() <= 2) {
            return;
        }
        
        int index = currentSpline.getControlPoints().indexOf(selectedPoint);
        currentSpline.getControlPoints().remove(selectedPoint);
        
        if (index > 0) {
            selectedPoint = currentSpline.getControlPoints().get(index - 1);
        } else {
            selectedPoint = currentSpline.getControlPoints().get(0);
        }
        
        updateTextFields();
        
        drawingArea.setSelectedPoint(selectedPoint);
        drawingArea.repaint();
    }
    
    private Point2D.Double findClosestPoint(int x, int y) {
        Point2D.Double closestPoint = null;
        double minDistance = Double.MAX_VALUE;
        
        double splineX = sc.convertValueSpace(0, width, x, currentSpline.getMin(), currentSpline.getMax(), false);
        double splineY = sc.convertValueSpace(0, height, y, currentSpline.getMin(), currentSpline.getMax(), true);
        
        for (Point2D.Double point : currentSpline.getControlPoints()) {
            double distance = point.distance(splineX, splineY);
            
            if (distance <= POINT_RADIUS * 2 && distance < minDistance) {
                closestPoint = point;
                minDistance = distance;
            }
        }
        
        return closestPoint;
    }
    
    private void addNewPoint(int x, int y) {
        double splineX = sc.convertValueSpace(0, width, x, currentSpline.getMin(), currentSpline.getMax(), false);
        double splineY = sc.convertValueSpace(0, height, y, currentSpline.getMin(), currentSpline.getMax(), true);
        
        Point2D.Double newPoint = new Point2D.Double(splineX, splineY);
        
        int index = 0;
        
        for (Point2D.Double point : currentSpline.getControlPoints()) {
            if (splineX < point.x) {
                break;
            }
            
            index++;
        }
        
        currentSpline.addControlPoint(index, newPoint);
        selectedPoint = newPoint;
        
        drawingArea.setSelectedPoint(selectedPoint);
        drawingArea.repaint();
    }
}