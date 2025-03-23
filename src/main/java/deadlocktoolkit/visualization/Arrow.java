package deadlocktoolkit.visualization;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

public class Arrow extends Group {
    private static final double ARROW_HEAD_SIZE = 10;
    
    public Arrow(Point2D start, Point2D end, Color color) {
        Line line = new Line(start.getX(), start.getY(), end.getX(), end.getY());
        line.setStroke(color);
        line.setStrokeWidth(2);
        
        // Create arrow head
        double dx = end.getX() - start.getX();
        double dy = end.getY() - start.getY();
        double angle = Math.atan2(dy, dx);
        
        double x1 = end.getX() - ARROW_HEAD_SIZE * Math.cos(angle - Math.PI/6);
        double y1 = end.getY() - ARROW_HEAD_SIZE * Math.sin(angle - Math.PI/6);
        double x2 = end.getX() - ARROW_HEAD_SIZE * Math.cos(angle + Math.PI/6);
        double y2 = end.getY() - ARROW_HEAD_SIZE * Math.sin(angle + Math.PI/6);
        
        Polygon arrowHead = new Polygon(
            end.getX(), end.getY(),
            x1, y1,
            x2, y2
        );
        arrowHead.setFill(color);
        
        getChildren().addAll(line, arrowHead);
    }
}
