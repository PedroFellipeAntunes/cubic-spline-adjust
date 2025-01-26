package Operation;

import java.awt.geom.Point2D;
import java.util.List;

public class SplineEvaluator {
    private final Spline spline;

    // Construtor que recebe a spline
    public SplineEvaluator(Spline spline) {
        this.spline = spline;
        spline.calculateCubicCoefficients();
    }

    // Método para interpolação linear
    public double evaluate(double input) {
        // Obtém os pontos de controle da spline
        List<Point2D.Double> controlPoints = spline.getControlPoints();

        // Caso tenha menos de dois pontos de controle, não é possível calcular
        if (controlPoints.size() < 2) {
            throw new IllegalArgumentException("A spline precisa de pelo menos dois pontos de controle.");
        }

        // Valida se o input está dentro do intervalo da spline
        if (input < controlPoints.get(0).x) {
            return controlPoints.get(0).y;  // Antes do intervalo da spline
        } else if (input > controlPoints.get(controlPoints.size() - 1).x) {
            return controlPoints.get(controlPoints.size() - 1).y;  // Após o intervalo da spline
        }

        // Realiza a interpolação cúbica utilizando o método já existente
        double output = spline.interpolateCubic(input);
        
        return Math.min(spline.getMax(), Math.max(spline.getMin(), output));
    }
}