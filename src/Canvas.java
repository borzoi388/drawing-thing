import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class Canvas {
    private List<Layer> layers = new LinkedList<>();
    private Layer selectedLayer;
    private Color bgColor;
    private int height;
    private int width;

    Canvas(int h, int w, Color bg) {
        bgColor = bg;
        layers.add(new Layer(h, w, bg));
        layers.add(new Layer(h, w));
        System.out.println(layers.getFirst().id);
        selectedLayer = layers.get(1);
        bgColor = bg;
        height = h;
        width = w;
    }

    Pixel getPixel(int y, int x) {
        for (int i = layers.size()-1; i >= 0; i--) {
            if (layers.get(i).hasInitialized()) {
                Pixel pixel = layers.get(i).getPixel(y, x);
                if (pixel.getColor() != null) {
                    return pixel;
                }
            }
        }
        return layers.getFirst().getPixel(y, x);
    }

    int getHeight() {
        return height;
    }

    int getWidth() {
        return width;
    }

}