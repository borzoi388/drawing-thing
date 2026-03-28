import java.awt.*;
import java.util.*;
import java.util.List;

public class Canvas {
    private List<Layer> layers = new LinkedList<>();
    private int selectedLayer;
    private Color bgColor;
    private int height;
    private int width;
    private List<Map<Pixel, Color>> lastActions;
    private List<Map<Pixel, Color>> redoActions;


    Canvas(int h, int w, Color bg) {
        resetCanvas(h,w,bg);
    }

    public void resetCanvas(int h, int w, Color bg) {
        if (h < 1 || w < 1) return;
        layers.clear();
        bgColor = bg;
        layers.add(new Layer(h, w, bg, "Background"));
        layers.add(new Layer(h, w, null, "Layer 1"));
        selectedLayer = 1;
        bgColor = bg;
        height = h;
        width = w;
        lastActions = new ArrayList<>();
        redoActions = new ArrayList<>();
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
            return lastActions.removeLast();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    public void addRedoAction(Map<Pixel, Color> map) {
        redoActions.add(map);
    }

    public void clearRedo() {
        redoActions.clear();
    }

    public Map<Pixel, Color> getRedoAction() {
        try {
            return redoActions.removeLast();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    public List<Layer> getLayers() {
        return layers;
    }

    public void selectLayer(int i) {
        selectedLayer = i;
    }

    public Layer getSelectedLayer() {
        return layers.get(selectedLayer);
    }

    public int getSelectedIndex() {
        return selectedLayer;
    }
}