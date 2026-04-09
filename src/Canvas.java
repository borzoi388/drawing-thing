import java.awt.*;
import java.util.*;
import java.util.List;

public class Canvas {
    public Layers layerThing;
    private Color bgColor;
    private int height;
    private int width;
    private List<Action> lastActions;
    private List<Action> redoActions;
    private Map<Integer, Boolean> layerNames;


    Canvas(int h, int w, Color bg) {
        resetCanvas(h,w,bg);
    }

    public void resetCanvas(int h, int w, Color bg) {
        if (h < 1 || w < 1) return;
        bgColor = bg;
        height = h;
        width = w;
        layerThing = new Layers();
        layerNames = new HashMap<>();
        layerThing.layers.clear();
        bgColor = bg;
        layerThing.layers.add(new Layer(h, w, bg, "Background"));
        layerThing.addLayer();
        lastActions = new ArrayList<>();
        redoActions = new ArrayList<>();
    }

    void addLayer() {
        layerThing.addLayer();
    }

    Layer deleteSelectedLayer() {
        return layerThing.deleteSelectedLayer();
    }

    void duplicateLayer() {
        layerThing.duplicateSelectedLayer();
    }

    Pixel getPixel(int y, int x) {
        return getPixelHelper(y, x, layerThing.layers);
    }

    private Pixel getPixelHelper(int y, int x, List<Layer> layers) {
        for (int i = layers.size()-1; i >= 0; i--) {
            if (layers.get(i).hasInitialized()) {
                Pixel pixel = layers.get(i).getPixel(y, x);
                if (pixel != null) {
                    if (pixel.getColor() != null) {
                        return pixel;
                    }
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

    void setLastAction(Action action) {
        System.out.println(action.toString());
        lastActions.add(action);
    }

    Action getLastAction() {
        try {
            System.out.println(lastActions.getLast().toString());
            return lastActions.removeLast();
        } catch (Exception e) {
            return null;
        }
    }

    public void addRedoAction(Action action) {
        redoActions.add(action);
    }

    public void clearRedo() {
        redoActions.clear();
    }

    public Action getRedoAction() {
        try {
            return redoActions.removeLast();
        } catch (Exception e) {
            return null;
        }
    }

    public Layer getSelectedLayer() {
        return layerThing.layers.get(layerThing.getSelectedIndex()+1);
    }

    public Layer getLayer(int i) {
        return layerThing.layers.get(i);
    }

    public Layer deleteLayer(int i) {
        if (layerThing.getSelectedIndex() == i) {

        }
        return layerThing.layers.remove(i);
    }

    public void clearLayer() {
        layerThing.clearLayer();
    }

    public void insertLayer(int index, Layer layer) {
        layerThing.insertLayer(index, layer);
    }

    public List<Pixel> getNeighborPixels(Pixel pixel, int size) {
        if (size == 0) return new ArrayList<>(List.of(new Pixel[]{pixel}));
        List<Pixel> pixels = new ArrayList<>();
        int row = pixel.getRow();
        int col = pixel.getCol();
        if (size == -1) {
            for (int r = row-1; r < row+2; r++) {
                try {

                    Pixel temp = pixel.getLayer().getPixel(r, col);
                    if (temp == null) throw new Exception();
                    pixels.add(temp);
                } catch (Exception ex) {}
            }
            for (int c = col-1; c < col+2; c++) {
                if (c != col) {
                    try {

                        Pixel temp = pixel.getLayer().getPixel(row, c);
                        if (temp == null) throw new Exception();
                        pixels.add(temp);
                    } catch (Exception ex) {}
                }
            }
            return pixels;
        } else {
            for (int r = row - size; r <= row + size; r++) {
                for (int c = col - size; c <= col + size; c++) {
                    try {
                        Pixel temp = pixel.getLayer().getPixel(r, c);
                        if (temp == null) throw new Exception();
                        pixels.add(temp);
                    } catch (Exception _) {
                    }
                }
            }
            return pixels;
        }
    }

    public class Layers extends Selectable {
        List<Layer> layers = new LinkedList<>();

        Layers() {
            selectedIndex = 0;
        }
        @Override
        public List<String> getSelectables() {
            List<String> names = new ArrayList<>();
            for (int i = 1; i < layers.size(); i++) {
                names.add(layers.get(i).getName());
            }
            return names;
        }

        public void clearLayer() {
            layers.set(selectedIndex, new Layer(height, width, null, layers.get(selectedIndex).getName()));
        }

        public void insertLayer(int index, Layer layer) {
            layers.add(index, layer);
        }

        public Layer deleteSelectedLayer() {
            if (layers.size()>2) {
                int num = checkRenaming(layers.get(selectedIndex).getName());
                if (num != 0) { layerNames.remove(num); }
                Layer temp = layers.remove(selectedIndex);
                if (selectedIndex != 1) {
                    selectedIndex--;
                }
                return temp;
            }
            return null;
        }

        private int checkRenaming(String name) {
            if (name.indexOf("Layer ") == 0) {
                try {
                    return Integer.parseInt(name.substring(6));
                } catch (NumberFormatException ex) {
                    return 0;
                }
            } else {
                return 0;
            }
        }

        public void addLayer() {
            int num = 1;
            while (layerNames.containsKey(num)) num++;
            layerNames.put(num, true);
            layers.add(selectedIndex+1, new Layer(height, width, null, "Layer "+num));
            selectedIndex++;
        }

        public void duplicateSelectedLayer() {
            layers.add(selectedIndex+1, new Layer(layers.get(selectedIndex)));
            selectedIndex++;
            layers.get(selectedIndex).setName(layers.get(selectedIndex).getName()+" copy");
        }


        @Override
        public void select(int num) {
            super.select(num+1);
        }

        @Override
        public int getSelectedIndex() {
            return super.getSelectedIndex()-1;
        }
    }
}