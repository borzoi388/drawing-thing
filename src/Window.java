import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

public class Window {
    boolean showGrid = false;

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
            addMouseListener(new PenListener());
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

        private class PenListener implements MouseListener {
            boolean mouseDown = false;
            @Override
            public void mouseClicked(MouseEvent e) {
                mouseDown = false;
            }

            @Override
            public void mousePressed(MouseEvent e) {
                mouseDown = true;
                Map<Pixel, Color> pixelsAltered = new HashMap<>();
                while (mouseDown) {
                    Point mousePos = getMousePosition();
                    if (mousePos == null) return;
                    if (!mouseDown) return;
                    Pixel currPixel = checkPixel(mousePos);
                    if (currPixel != null && !pixelsAltered.containsKey(currPixel)) {
                        pixelsAltered.put(currPixel, currPixel.getColor());
                        System.out.println(pixelsAltered.size());
                    }
                }
                for (Pixel pixel : pixelsAltered.keySet()) {
                    System.out.println(pixel);
                }
            }

            private Pixel checkPixel(Point mousePos) {
                if (mousePos.x > offsetX && mousePos.x < getWidth()-offsetX && mousePos.y < getHeight()-offsetY && mousePos.y > offsetY) {
                    Pixel currPixel = canvas.getPixel((mousePos.y)/pixelSize, (mousePos.x-offsetX)/pixelSize);
                    currPixel.setColor(Color.black);
                    repaint();
                    return currPixel;
                } else {
                    return null;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                System.out.println("a");
                mouseDown = false;

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        }
    }

    private class ControlPanel extends JPanel {
        JTextField widthTextField, heightTextField;
        JButton newCanvasBtn, chooserToggleBtn;
        JColorChooser colorChooser;
        boolean showChooser = false;

        ControlPanel() {
            widthTextField = new JTextField(defaultWidth+"", 4);
            heightTextField = new JTextField(defaultHeight+"", 4);
            colorChooser = new JColorChooser(Color.white);

            chooserToggleBtn = new JButton("Choose color...");
            newCanvasBtn = new JButton("New Canvas");
            chooserToggleBtn.addActionListener(new chooserToggleListener());
            newCanvasBtn.addActionListener(new NewPaintingListener());
            setLayout(new FlowLayout());
            add(new JLabel("Width: "));
            add(widthTextField);
            add(new JLabel("Height: "));
            add(heightTextField);
            add(new JLabel("Background color: "));
            add(chooserToggleBtn);
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

        private class chooserToggleListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                if (showChooser) {
                    remove(colorChooser);
                } else {
                    add(colorChooser);
                }
                showChooser = !showChooser;
                repaint();
            }
        }
    }

}
