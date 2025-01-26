package Operation;

import FileManager.PngReader;
import FileManager.PngSaver;
import Windows.ImageViewer;
import Windows.SplineWindow;
import java.awt.image.BufferedImage;

public class Operations {
    private static TYPE type;
    
    public static void processFile(String filePath) throws Exception {
        try {
            long startTime, endTime;
            
            // Get image from file
            startTime = System.currentTimeMillis();
            PngReader read = new PngReader();
            BufferedImage inputImage = read.readPNG(filePath, false);
            endTime = System.currentTimeMillis();
            System.out.println("IMAGE READ - Time: " + (endTime - startTime) + "ms");
            
            // Generate generic splines for each type
            Spline[] splines = new Spline[TYPE.values().length];
            
            for (int i = 0; i < TYPE.values().length; i++) {
                Spline spline = new Spline(0, 255, TYPE.values()[i]);
                
                spline.addControlPoint(0, 0, 0); // Start point
                spline.addControlPoint(1, 255, 255); // End point
                
                splines[i] = spline;
            }
            
            // Call window
            int width = 500, height = 300;
            SplineWindow splineWindow = new SplineWindow(splines, width, height, inputImage, filePath);
            splineWindow.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }
    
    public static boolean nextStep(BufferedImage image, int[] colorMap, String filePath, TYPE type) {
        Operations.type = type;
        
        long startTime, endTime;
        
        // Now apply the map to the new image
        startTime = System.currentTimeMillis();
        AdjustColors ac = new AdjustColors();
        BufferedImage newImage = ac.adjustColors(image, colorMap, type);
        endTime = System.currentTimeMillis();
        System.out.println("COLORS ADJUSTED - Time: " + (endTime - startTime) + "ms");
        
        // View images before saving
        ImageViewer viewer = new ImageViewer(newImage, filePath);

        return viewer.wentBack();
    }
    
    //Save files
    public static void saveImage(BufferedImage image, String filePath) {
        PngSaver listToImage = new PngSaver();
        
        listToImage.saveToFile("ColorAdjusted["+Operations.type+"]", filePath, image);
    }
}