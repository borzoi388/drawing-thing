import java.awt.*;
import java.util.*;
import java.util.List;

public class Canvas {
    private List<Layer> layers = new LinkedList<>();
    private Layer selectedLayer;
    private Color bgColor;
    private int height;
    private int width;
    private List<Map<Pixel, Color>> lastActions;


    Canvas(int h, int w, Color bg) {
        resetCanvas(h,w,bg);
    }

    public void resetCanvas(int h, int w, Color bg) {
        if (h < 1 || w < 1) return;
        layers.clear();
        bgColor = bg;
        layers.add(new Layer(h, w, bg));
        layers.add(new Layer(h, w));
        selectedLayer = layers.get(1);
        bgColor = bg;
        height = h;
        width = w;
        lastActions = new ArrayList<>();
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

    Color getBgColor() {
        return bgColor;
    }

    void setLastAction(Map<Pixel, Color> map) {
        lastActions.add(map);
    }

    Map<Pixel, Color> getLastAction() {
        try {
            System.out.println(lastActions.size());
            return lastActions.removeLast();
        } catch (Exception e) {
            return new HashMap<Pixel, Color>();
        }
    }
}