import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class Window {
    boolean showGrid = true;

    private JFrame window;
    private Canvas canvas;
    private int defaultWidth = 10;
    private int defaultHeight = 10;
    private int width;
    private int height;


    JPanel panel = new JPanel();
    CanvasPanel canvasPanel = new CanvasPanel();
    ControlPanel controlPanel = new ControlPanel();

    Window(int h, int w) {
        window = new JFrame();
        canvas = new Canvas(defaultHeight, defaultWidth, new Color(255, 255, 255));
        panel.setLayout(new BorderLayout());
        panel.add(canvasPanel, BorderLayout.CENTER);
        panel.add(controlPanel, BorderLayout.NORTH);

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
            addComponentListener(new ResizeListener());
        }

        void calculatePixelSize() {
            if (canvas == null) {return;}
            Dimension size = getSize();
            pixelSize = (canvas.getHeight()/canvas.getWidth() > size.height/size.width) ? size.height/canvas.getHeight() : size.width/canvas.getWidth();
            offsetX = (size.width-pixelSize*canvas.getWidth())/2;
            offsetY = (size.height-pixelSize*canvas.getHeight())/2;
        }

        protected void paintComponent(Graphics pen) {
            pen.setColor(new Color(100, 100, 100));
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
            if (showGrid) {
                pen.setColor(pixel.getColor());
                pen.fillRect(col + 1, row + 1, pixelSize - 2, pixelSize - 2);
            } else {
                pen.setColor(pixel.getColor());
                pen.fillRect(col, row, pixelSize, pixelSize);
            }
        }

        private class ResizeListener implements ComponentListener {

            @Override
            public void componentResized(ComponentEvent e) {
                calculatePixelSize();
                repaint();
            }

            public void componentMoved(ComponentEvent e) {}

            @Override
            public void componentShown(ComponentEvent e) {}

            @Override
            public void componentHidden(ComponentEvent e) {}
        }
    }

    private class ControlPanel extends JPanel {
        JTextField widthTextField, heightTextField;
        JButton newCanvasBtn;
        JColorChooser colorChooser;
        ControlPanel() {
            widthTextField = new JTextField(defaultWidth+"", 4);
            heightTextField = new JTextField(defaultHeight+"", 4);
            colorChooser = new JColorChooser(Color.white);
            colorChooser.

            newCanvasBtn = new JButton("New Canvas");
            newCanvasBtn.addActionListener(new NewPaintingListener());
            setLayout(new FlowLayout());
            add(new JLabel("Width: "));
            add(widthTextField);
            add(new JLabel("Height: "));
            add(heightTextField);
            add(new JLabel("Background color: "));
            add(colorChooser);
            add(newCanvasBtn);
        }

        private class NewPaintingListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    width = Integer.parseInt(widthTextField.getText());
                    height = Integer.parseInt(heightTextField.getText());
                    canvas = new Canvas(width, height, colorChooser.getColor());
                    canvasPanel.calculatePixelSize();
                    canvasPanel.repaint();
                } catch (Exception ex) {
                    System.out.println("Fialure");
                }
            }
        }
    }

}
