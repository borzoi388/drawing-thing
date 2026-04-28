import java.awt.*;

public class Pixel extends Coord{
    private Color color;

    Pixel(Color color, int r, int c) {
        super(c, r);
        this.color = color;
    }
    Color setColor(Color newColor) {
        Color temp = color;
        color = newColor;
        return temp;
    }


    public Color getColor() {
        return color;
    }

    @Override
    public String toString() {
        return "Row: "+row+" Col: "+col+" Color: "+color;
    }
}
