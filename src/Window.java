
import javax.print.Doc;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

public class Window {
    boolean showGrid = false;

    private JFrame window;
    private Canvas canvas;
    private Canvas defaultCanvas = new Canvas(20, 20, Color.white);

    private Color penColor = Color.black;


    JPanel panel = new JPanel();
    CanvasPanel canvasPanel;
    PenSettingsPanel penSettingsPanel = new PenSettingsPanel();
    

    Window(int h, int w) {

        window = new JFrame();
        canvas = new Canvas(defaultCanvas.getHeight(), defaultCanvas.getWidth(), defaultCanvas.getBgColor());
        canvasPanel = new BigCanvasPanel(canvas);

        panel.setLayout(new BorderLayout());
        panel.add(canvasPanel, BorderLayout.CENTER);
        panel.add(penSettingsPanel, BorderLayout.WEST);

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
        Canvas myCanvas;

        CanvasPanel(Canvas myCanvas) {
            addComponentListener(new ResizeListener());
            this.myCanvas = myCanvas;
        }

        void rerouteCanvas(Canvas myCanvas) {
            this.myCanvas = myCanvas;
            calculatePixelSize();
            repaint();
        }

        void calculatePixelSize() {
            if (myCanvas == null) {return;}
            Dimension size = getSize();
            if (size.height < 1 || size.width < 1) return;
            pixelSize = (myCanvas.getHeight()/myCanvas.getWidth() > size.height/size.width) ? size.height/myCanvas.getHeight() : size.width/myCanvas.getWidth();
            offsetX = (size.width-pixelSize*myCanvas.getWidth())/2;
            offsetY = (size.height-pixelSize*myCanvas.getHeight())/2;
        }

        protected void paintComponent(Graphics pen) {
            pen.setColor(new Color(100, 100, 100));
            pen.fillRect(0, 0, getSize().width, getSize().height);
            for (int r = 0; r < myCanvas.getHeight(); r++) {
                for (int c = 0; c < myCanvas.getWidth(); c++) {
                    Pixel pixel = myCanvas.getPixel(r, c);
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

    private class BigCanvasPanel extends CanvasPanel {
        BigCanvasPanel(Canvas myCanvas) {
            super(myCanvas);
            addMouseListener(new PenListener());
        }

        private class PenListener implements MouseListener {
            boolean mouseDown = false;
            Map<Pixel, Color> pixelsAltered = new HashMap<>();


            @Override
            public void mouseClicked(MouseEvent e) {
                mouseDown = false;
            }

            @Override
            public void mousePressed(MouseEvent e) {
                mouseDown = true;
                while (mouseDown) {
                    Point mousePos = getMousePosition();
                    if (mousePos == null) return;
                    Pixel currPixel = checkPixel(mousePos);
                    if (currPixel != null && !pixelsAltered.containsKey(currPixel)) {
                        pixelsAltered.put(currPixel, new Color(currPixel.getColor().getRGB()));
                        currPixel.setColor(penColor);
                    }
                }
            }

            private Pixel checkPixel(Point mousePos) {
                if (mousePos.x > offsetX && mousePos.x < getWidth()-offsetX && mousePos.y < getHeight()-offsetY && mousePos.y > offsetY) {
                    repaint();
                    return myCanvas.getPixel((mousePos.y-offsetY)/pixelSize, (mousePos.x-offsetX)/pixelSize);
                } else {
                    return null;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mouseDown = false;
                myCanvas.setLastAction(pixelsAltered);
                pixelsAltered = new HashMap<>();

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        }
    }

    private class CanvasDialog extends JDialog {
        JTextField widthTextField, heightTextField;
        SmallChooser colorChooser;
        Canvas newCanvas;
        Color bgColor;
        CanvasPanel smallCanvasPanel;
        int isValid = 0;

        CanvasDialog() {
            widthTextField = new JTextField(defaultCanvas.getWidth()+"", 4);
            heightTextField = new JTextField(defaultCanvas.getHeight()+"", 4);
            colorChooser = new SmallChooser(defaultCanvas.getBgColor());
            newCanvas = defaultCanvas;
            smallCanvasPanel = new CanvasPanel(newCanvas);

            for (JTextField textField : new JTextField[]{widthTextField, heightTextField}) {
                textField.getDocument().addDocumentListener(new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        updateCanvas();

                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        updateCanvas();

                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        updateCanvas();
                    }
                }
                );
            }

            colorChooser.getSelectionModel().addChangeListener(_ -> {
                System.out.println("Hi");
                updateCanvas();
            });

            setModal(true);
//            setResizable(false);

            add(new SettingsPanel(), BorderLayout.WEST);
            add(new SubmitPanel(), BorderLayout.SOUTH);
            add(smallCanvasPanel, BorderLayout.CENTER);
        }

        public void display() {
            setSize(1000, 400);
            setVisible(true);
        }

        private void updateCanvas() {
            isValid = 0;
            int tempWidth = 0, tempHeight = 0;
            try {
                tempWidth = Integer.parseInt(heightTextField.getText());
                if (tempWidth < 1 || tempWidth > 100) {
                    throw new Exception();
                }
                heightTextField.setBorder(BorderFactory.createEmptyBorder());
            } catch (Exception ex) {
                heightTextField.setBorder(BorderFactory.createLineBorder(Color.red));
                isValid++;
            }
            try {
                tempHeight = Integer.parseInt(widthTextField.getText());
                if (tempHeight < 1 || tempHeight > 100) {
                    throw new Exception();
                }
                widthTextField.setBorder(BorderFactory.createEmptyBorder());
            } catch (Exception ex) {
                widthTextField.setBorder(BorderFactory.createLineBorder(Color.red));
                isValid++;
            }
            if (isValid > 0) return;
            bgColor = colorChooser.getColor();
            newCanvas.resetCanvas(tempWidth, tempHeight, bgColor);
            smallCanvasPanel.calculatePixelSize();
            smallCanvasPanel.repaint();
        }

        private class SettingsPanel extends JPanel {
            SettingsPanel() {
                setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
                bgColor = Color.white;

                add(new JLabel("Width: "));
                add(widthTextField);
                add(new JLabel("Height: "));
                add(heightTextField);
                add(new JLabel("Background color: "));
                add(colorChooser);
            }
        }

        private class SubmitPanel extends JPanel {
            JButton submitBtn, cancelBtn;
            SubmitPanel() {
                setLayout(new FlowLayout());
                submitBtn = new JButton("Submit");
                cancelBtn = new JButton("Cancel");
                submitBtn.addActionListener(new SubmitListener());
                cancelBtn.addActionListener(_ -> dispose());
                add(submitBtn);
                add(cancelBtn);
            }

            private class SubmitListener implements ActionListener {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (isValid > 0) return;
                    canvas = new Canvas(Integer.parseInt(heightTextField.getText()), Integer.parseInt(widthTextField.getText()), bgColor);
                    canvasPanel.rerouteCanvas(canvas);
                    dispose();
                }
            }
        }
    }

    private class PenSettingsPanel extends JPanel {
        JButton undoButton, newCanvasBtn;
        SmallChooser colorChooser;

        PenSettingsPanel() {
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            undoButton = new JButton("Undo");
            undoButton.addActionListener(new undoListener());
            newCanvasBtn = new JButton("Blank canvas...");
            newCanvasBtn.addActionListener(new newCanvasListener());
            colorChooser = new SmallChooser(Color.black);
            colorChooser.getSelectionModel().addChangeListener(_ -> {
                penColor = colorChooser.getColor();

            });
            add(colorChooser);
            add(undoButton);
            add(newCanvasBtn);
        }

        private class undoListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                Map<Pixel, Color> lastAction = canvas.getLastAction();
                for (Pixel pixel : lastAction.keySet()) {
                    pixel.setColor(lastAction.get(pixel));
                }
                canvasPanel.repaint();
            }
        }

        private class newCanvasListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                new CanvasDialog().display();
            }
        }
    }

    private class SmallChooser extends JColorChooser {
        SmallChooser(Color color) {
            super(color);
            Dimension maxSize = new Dimension();
            maxSize.setSize(400, 400);
            setMaximumSize(maxSize);
            setPreviewPanel(new JPanel());
            AbstractColorChooserPanel thingy = getChooserPanels()[2];
            for (AbstractColorChooserPanel panel : getChooserPanels()) {
                removeChooserPanel(panel);
            }
            addChooserPanel(thingy);
            setBackground(Color.yellow);
            setOpaque(true);
            revalidate();
            repaint();
        }
    }

}
