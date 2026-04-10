import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Actions {
    public static Action stroke(Map<Pixel, Color> map) {
        return new StrokeAction(map);
    }

    public static Action deleteLayer(Layer layer, int index) {
        return new DeleteLayer(layer, index+1, 0);
    }

    public static Action createLayer(int index) {
        return new CreateLayer(index);
    }

    private static class StrokeAction implements Action {
        Map<Pixel, Color> map;
        StrokeAction(Map<Pixel, Color> map) {
            this.map = map;
        }

        @Override
        public Action performAction(Canvas canvas) {
            Map<Pixel, Color> undoAction = new HashMap<>();

            for (Pixel pixel : map.keySet()) {
                undoAction.put(pixel, pixel.setColor(map.get(pixel)));
            }

            return new StrokeAction(undoAction);
        }

        @Override
        public String toString() {
            return "stroke";
        }
    }

    private static class DeleteLayer implements Action {
        Layer layer;
        int index;
        double temp;

        DeleteLayer(Layer layer, int index, double temp) {
            this.layer = layer;
            this.index = index;
            this.temp = temp;
        }


        @Override
        public Action performAction(Canvas canvas) {
            canvas.insertLayer(index+1, layer);
            return new CreateLayer(index);
        }

        @Override
        public String toString() {
            return index+" Del "+temp;
        }
    }

    private static class CreateLayer implements Action {
        int index;
        double temp;

        CreateLayer(int index) {
            this.index = index;
            temp = Math.random();
        }
        @Override
        public Action performAction(Canvas canvas) {
            return new DeleteLayer(canvas.deleteLayer(index+1), index, temp);
        }

        public String toString() {
            return index+" Create "+temp;
        }
    }
}
