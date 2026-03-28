import java.awt.*;

public class Layer {
    private Pixel[][] layer;
    private String name;

    Layer(int h, int w, Color color, String name) {
        this.name = name;
        layer = new Pixel[h][w];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                layer[i][j] = new Pixel(color, i, j);
            }
        }
    }

    public Pixel getPixel(int y, int x) {
        return layer[y][x];
    }

    public boolean hasInitialized() {
        return (layer != null);
    }

    public String getName() {
        return name;
    }
}
