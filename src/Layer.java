import java.awt.*;

public class Layer {
    private Pixel[][] layer;
    double id;

    Layer(int h, int w, Color color) {
        id = Math.random();
        layer = new Pixel[h][w];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                layer[i][j] = new Pixel(color, i, j);
            }
        }
        System.out.println(id);
    }

    Layer(int h, int w) {
        new Layer(h, w, null);
    }

    public Pixel getPixel(int y, int x) {
        return layer[y][x];
    }

    public boolean hasInitialized() {
        return (layer != null);
    }
}
