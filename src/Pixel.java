import java.awt.*;

public class Pixel {
    private Color color;
    private int row;
    private int col;
    private Layer layer;

    Pixel(Color color, int r, int c, Layer layer) {
        this.color = color;
        this.row = r;
        this.col = c;
        this.layer = layer;
    }
    Color setColor(Color newColor) {
        Color temp = color;
        color = newColor;
        return temp;
    }

    Layer getLayer() {
        return layer;
    }

    public Color getColor() {
        return color;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    @Override
    public String toString() {
        return "Row: "+row+" Col: "+col+" Color: "+color;
    }
}
