package Operation;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class AdjustColors {
    public BufferedImage adjustColors(BufferedImage input, int[] colorMap, TYPE type) {
        BufferedImage output = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);
        
        for (int y = 0; y < input.getHeight(); y++) {
            for (int x = 0; x < input.getWidth(); x++) {
                int argb = input.getRGB(x, y);
                
                int a = (argb >> 24) & 0xFF; // Alpha
                int r = (argb >> 16) & 0xFF; // Red
                int g = (argb >> 8) & 0xFF;  // Green
                int b = argb & 0xFF;         // Blue
                
                switch (type) {
                    case RGB -> {
                        r = colorMap[r];
                        g = colorMap[g];
                        b = colorMap[b];
                    }
                    case RED ->
                        r = colorMap[r];
                    case GREEN ->
                        g = colorMap[g];
                    case BLUE ->
                        b = colorMap[b];
                    case ALPHA ->
                        a = colorMap[a];
                }
                
                Color newC = new Color(r, g, b, a);
                output.setRGB(x, y, newC.getRGB());
            }
        }
        
        return output;
    }
}