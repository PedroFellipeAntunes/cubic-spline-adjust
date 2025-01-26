package Operation;

public class SpaceConverter {
    // Value of destination MAX needs to be inverted so positive Y goes UP
    public double convertValueSpace(double originMin, double originMax, double value, double destMin, double destMax, boolean invert) {
        if (invert) {
            double temp = destMin;
            destMin = destMax;
            destMax = temp;
        }
        
        double normalized = (value - originMin) / (originMax - originMin);
        
        return normalized * (destMax - destMin) + destMin;
    }
}