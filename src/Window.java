import javax.swing.*;
import java.awt.*;

public class Window {
    private JFrame window;
    private Canvas canvas;
    private int defaultWidth = 100;
    private int defaultHeight = 100;

    Window(int h, int w) {
        window = new JFrame();
        canvas = new Canvas(defaultHeight, defaultWidth, new Color(255, 255, 255));
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new CanvasPanel(), BorderLayout.CENTER);
        panel.add(new ControlPanel(), BorderLayout.SOUTH);

        window.getContentPane().add(panel);
        window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        window.setTitle("DrawingThing");
        window.setSize(w, h);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    private class CanvasPanel extends JPanel {
        int pixelSize;
        int offsetX;
        int offsetY;
        CanvasPanel() {
            System.out.println(getSize().width);
            setSize(800, 600);
            calculatePixelSize();
        }

        void calculatePixelSize() {
            Dimension size = getSize();
            pixelSize = (canvas.getHeight()/canvas.getWidth() > size.height/size.width) ? size.height/canvas.getHeight() : size.width/canvas.getWidth();
            offsetX = (size.width-pixelSize*canvas.getWidth())/2;
            offsetY = (size.height-pixelSize*canvas.getHeight())/2;
        }

        protected void paintComponent(Graphics pen) {
            pen.setColor(new Color(255, 0, 0));
            pen.fillRect(0, 0, getSize().width, getSize().height);
            for (int r = 0; r < canvas.getHeight(); r++) {
                for (int c = 0; c < canvas.getWidth(); c++) {
                    Pixel pixel = canvas.getPixel(r, c);
                    if (pixel != null) {
                        drawPixel(pen, pixel);
                    }
                }
            }
        }

        private void drawPixel(Graphics pen, Pixel pixel) {
            int col = pixel.getCol()*pixelSize+offsetX;
            int row = pixel.getRow()*pixelSize+offsetY;
            pen.setColor(new Color(0, 0, 0));
            pen.drawRect(col, row, pixelSize, pixelSize);
            pen.setColor(pixel.getColor());
            pen.fillRect(col+1, row+1, pixelSize-2, pixelSize-2);
        }
    }

    private class ControlPanel extends JPanel {
        JTextField width, height;
        ControlPanel() {
            width = new JTextField(defaultWidth+"", 4);
            height = new JTextField(defaultHeight+"", 4);
            setLayout(new FlowLayout());
            add(new JLabel("Width: "));
            add(width);
            add(new JLabel("Height: "));
            add(height);
        }
    }
}
