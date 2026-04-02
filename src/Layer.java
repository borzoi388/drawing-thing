import java.awt.*;

public class Layer {
    private Pixel[][] layer;
    private String name;
    private Dimension size;

    Layer(int h, int w, Color color, String name) {
        this.name = name;
        layer = new Pixel[h][w];
        this.size = new Dimension(w, h);
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                layer[i][j] = new Pixel(color, i, j);
            }
        }
    }

    Layer(Layer layer) {
        int h = layer.getSize().height;
        int w = layer.getSize().width;
        this.name = layer.getName();
        this.layer = new Pixel[h][w];
        this.size = layer.getSize();
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                this.layer[i][j] = new Pixel(layer.getPixel(i,j).getColor(), i, j);
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

    public void setName(String name) {
        this.name = name;
    }

    public Dimension getSize() {
        return size;
    }
}
