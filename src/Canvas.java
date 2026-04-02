import java.awt.*;
import java.util.*;
import java.util.List;

public class Canvas {
    public Layers layerThing;
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
        layerThing = new Layers();
        layerThing.layers.clear();
        bgColor = bg;
        layerThing.layers.add(new Layer(h, w, bg, "Background"));
        layerThing.layers.add(new Layer(h, w, null, "Layer 1"));
        bgColor = bg;
        height = h;
        width = w;
        lastActions = new ArrayList<>();
        redoActions = new ArrayList<>();
    }

    Pixel getPixel(int y, int x) {
        for (int i = layerThing.layers.size()-1; i >= 0; i--) {
            if (layerThing.layers.get(i).hasInitialized()) {
                Pixel pixel = layerThing.layers.get(i).getPixel(y, x);
                if (pixel.getColor() != null) {
                    return pixel;
                }
            }
        }
        return layerThing.layers.getFirst().getPixel(y, x);
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

    public Layer getSelectedLayer() {
        return layerThing.layers.get(layerThing.getSelectedIndex());
    }

    public class Layers extends Selectable {
        List<Layer> layers = new LinkedList<>();

        Layers() {
            selectedIndex = 1;
        }
        @Override
        public List<String> getSelectables() {
            List<String> names = new ArrayList<>();
            for (Layer layer : layers) {
                names.add(layer.getName());
            }
            return names;
        }

        public void deleteSelectedLayer() {
            if (layers.size()>2) {
                layers.remove(selectedIndex);
                if (selectedIndex != 1) {
                    selectedIndex--;
                    System.out.println(selectedIndex);
                }
            }
        }

        public void addLayer() {
            layers.add(selectedIndex+1, new Layer(height, width, null, "Layer "+(layers.size())));
            selectedIndex++;
        }

        public void duplicateSelectedLayer() {
            layers.add(selectedIndex+1, new Layer(layers.get(selectedIndex)));
            selectedIndex++;
            layers.get(selectedIndex).setName(layers.get(selectedIndex).getName()+" copy");
        }
    }
}