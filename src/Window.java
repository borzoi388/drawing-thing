
import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Executable;
import java.util.*;
import java.util.List;

public class Window {
    private JFrame window;
    private Canvas canvas;
    private Canvas defaultCanvas = new Canvas(50, 50, Color.white);


    private Pen pen = new Pen();


    JPanel panel = new JPanel();
    CanvasPanel canvasPanel;
    PenSettingsPanel penSettingsPanel;
    

    Window(int h, int w) {

        window = new JFrame();
        canvas = new Canvas(defaultCanvas.getHeight(), defaultCanvas.getWidth(), defaultCanvas.getBgColor());
        canvasPanel = new BigCanvasPanel(canvas);
        penSettingsPanel = new PenSettingsPanel();

        panel.setLayout(new BorderLayout());
        panel.add(canvasPanel, BorderLayout.CENTER);
        panel.add(penSettingsPanel, BorderLayout.WEST);

        window.getContentPane().add(panel);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
            pen.setColor(pixel.getColor());
            pen.fillRect(col, row, pixelSize, pixelSize);
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
            PenDownListener l = new PenDownListener();
            addMouseListener(l);
            addMouseMotionListener(l);
        }


        private class PenDownListener implements MouseListener, MouseMotionListener {
            volatile private boolean mouseDown = false;
            volatile private boolean isRunning = false;
            volatile Map<Pixel, Color> pixelsAltered = new HashMap<>();

            volatile Pixel lastPixel;

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (pen.isFilling()) {
                        Pixel pxl = checkPixel(e.getPoint());
                        if (pxl == null) return;
                        myCanvas.setLastAction(Actions.stroke(fill(pxl, pxl.getColor())));
                    } else if (!mouseDown) {
                        mouseDown = true;
                        penDown(pen.getSelectedIndex());
                    }
                }

            }

            private Pixel checkPixel(Point mousePos) {
                if (mousePos.x > offsetX && mousePos.x < getWidth()-offsetX && mousePos.y < getHeight()-offsetY && mousePos.y > offsetY) {
                    repaint();
                    return myCanvas.getSelectedLayer().getPixel((mousePos.y-offsetY)/pixelSize, (mousePos.x-offsetX)/pixelSize);
                } else {
                    return null;
                }
            }

            private Pixel checkTopPixel(Point mousePos) {
                if (mousePos.x > offsetX && mousePos.x < getWidth()-offsetX && mousePos.y < getHeight()-offsetY && mousePos.y > offsetY) {
                    repaint();
                    return myCanvas.getPixel((mousePos.y-offsetY)/pixelSize, (mousePos.x-offsetX)/pixelSize);
                } else {
                    return null;
                }
            }

            private synchronized boolean check() {
                if (isRunning) return false;
                isRunning = true;
                return true;
            }

            private void draw(Point mousePos) {
                int temp = 0;
                if (mousePos == null) {
                    return;
                }
                Pixel currPixel = checkPixel(mousePos);
                if (currPixel == null) {
                    return;
                }
                temp+=drawOnPixel(currPixel);
                fillLine(lastPixel, currPixel);
                lastPixel = currPixel;

                if (temp > 0) {
                    canvas.clearRedo();
                }
            }

            private void fillLine(Pixel lastPixel, Pixel currPixel) {
                new Thread(() -> {
                    if (lastPixel != null && lastPixel != currPixel) {
                        int changeX = currPixel.getCol() - lastPixel.getCol();
                        int changeY = currPixel.getRow() - lastPixel.getRow();
                        System.out.println(lastPixel);
                        System.out.println(currPixel);
                        LineMaker lineMaker = new LineMaker(lastPixel.getCol(), currPixel.getCol(), lastPixel.getRow(), currPixel.getRow());
                        if (changeX != 0 && changeY != 0) {
                            if (Math.abs(changeX) >= Math.abs(changeY)) {
                                for (int i = Math.min(lastPixel.getCol(), currPixel.getCol()); i < Math.max(lastPixel.getCol(), currPixel.getCol()); i++) {
                                    Pixel pixel = canvas.getSelectedLayer().getPixel(lineMaker.calcByX(i), i);
                                    drawOnPixel(pixel);
                                }
                            } else {
                                for (int i = Math.min(lastPixel.getRow(), currPixel.getRow()); i < Math.max(lastPixel.getRow(), currPixel.getRow()); i++) {
                                    Pixel pixel = canvas.getSelectedLayer().getPixel(i, lineMaker.calcByY(i));
                                    drawOnPixel(pixel);
                                }
                            }
                        } else if (changeY != 0) {
                            for (int i = Math.min(lastPixel.getRow(), currPixel.getRow()); i < Math.max(lastPixel.getRow(), currPixel.getRow()); i++) {
                                Pixel pixel = canvas.getSelectedLayer().getPixel(i, currPixel.getCol());
                                drawOnPixel(pixel);
                            }
                        } else if (changeX != 0) {
                            for (int i = Math.min(lastPixel.getCol(), currPixel.getCol()); i < Math.max(lastPixel.getCol(), currPixel.getCol()); i++) {
                                Pixel pixel = canvas.getSelectedLayer().getPixel(currPixel.getRow(), i);
                                drawOnPixel(pixel);
                            }
                        }
                    }
                }).start();
            }

            private int drawOnPixel(Pixel currPixel) {
                int temp = 0;
                for (Pixel pixel:canvas.getNeighborPixels(currPixel, (pen.getSize()-1)/2)) {
                    if (!pixelsAltered.containsKey(pixel)) {
                        pixelsAltered.put(pixel, colorPixel(pixel, pen.getPenColor()));
                    }
                    temp++;
                }
                return temp;
            }

            private Color colorPixel(Pixel pixel, Color color) {
                return pixel.setColor(color);
            }

            private HashMap<Pixel, Color> fill(Pixel pixel, Color color) {
                if (color == pen.getPenColor()) return new HashMap<>();
                HashMap<Pixel, Color> filled = new HashMap<>();
                for (Pixel pxl : canvas.getNeighborPixels(pixel, -1)) {
                    if (pxl.getColor() == color) {
                        filled.put(pxl, colorPixel(pxl, pen.getPenColor()));
                        filled.putAll(fill(pxl, color));
                    }
                }
                return filled;
            }

            private void penDown(int type) {
                if (check()) {
                    new Thread(() -> {
                        if (type < 2) {
                            mouseDown = true;
                            lastPixel = null;
                            while (mouseDown) {
                                draw(getMousePosition());
                            }
                        }
                    }).start();
                }
            }

            private void release(MouseEvent e) {
                if (isRunning && e.getButton() == MouseEvent.BUTTON1) {
                    mouseDown = false;
                    isRunning = false;
                    try {
                        Thread.sleep(10);
                    } catch (Exception ex) {}
                    myCanvas.setLastAction(Actions.stroke(pixelsAltered));
                    pixelsAltered = new HashMap<>();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                release(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (mouseDown) release(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                release(e);
            }


            @Override
            public void mouseDragged(MouseEvent e) {
                if (pen.types[pen.getSelectedIndex()].colorpick) {
                    try {
                        Color temp = checkTopPixel(e.getPoint()).getColor();
                        penSettingsPanel.colorChooser.setColor(temp);
                        pen.penColor = temp;
                    } catch (Exception _) {}
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {

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
                bgColor = defaultCanvas.getBgColor();

                add(new LabeledInput(widthTextField, "Width: "));
                add(new LabeledInput(heightTextField, "Height: "));
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
                    penSettingsPanel.layersSection.layersPanel.thing = canvas.layerThing;
                    penSettingsPanel.layersSection.layersPanel.load();
                    penSettingsPanel.repaint();
                    dispose();
                }
            }
        }
    }

    private class PenSettingsPanel extends JPanel {
        JButton undoButton, newCanvasBtn, redoBtn;
        JSlider penSizeSlider;
        SmallChooser colorChooser;
        LayersSection layersSection = new LayersSection();
        SelectablePanel penPanel = new SelectablePanel(pen, SelectablePanel.FLOW);

        PenSettingsPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            undoButton = new JButton("Undo");
            undoButton.addActionListener(new UndoListener());

            penSizeSlider = new JSlider(JSlider.HORIZONTAL,1,9,1);
            penSizeSlider.setMajorTickSpacing(2);
            penSizeSlider.setSnapToTicks(true);
            penSizeSlider.addChangeListener(_ -> pen.setSize(penSizeSlider.getValue()));

            redoBtn = new JButton("Redo");
            redoBtn.addActionListener(new RedoListener());

            newCanvasBtn = new JButton("Blank canvas...");
            newCanvasBtn.addActionListener(new NewCanvasListener());

            colorChooser = new SmallChooser(Color.black);
            colorChooser.getSelectionModel().addChangeListener(_ -> {
                pen.penColor = colorChooser.getColor();
            });
            add(colorChooser);
            add(penPanel);
            add(penSizeSlider);
            add(undoButton);
            add(redoBtn);
            add(layersSection);
            add(newCanvasBtn);
        }

        private class LayersSection extends JPanel {
            JButton newLayerBtn = new JButton("New Layer"), deleteLayerBtn = new JButton("Delete"), duplicateBtn = new JButton("Duplicate"), clearBtn = new JButton("Clear");
            JPanel buttonPanel = new JPanel();
            SelectablePanel layersPanel = new SelectablePanel(canvas.layerThing, SelectablePanel.BOX);
            LayersSection() {
                setLayout(new BorderLayout());

                newLayerBtn.addActionListener(_ -> {
                    canvas.addLayer();
                    canvas.setLastAction(Actions.createLayer(canvas.layerThing.getSelectedIndex()));
                    layersPanel.load();
                });
                deleteLayerBtn.addActionListener(_ -> {
                    canvas.setLastAction(Actions.deleteLayer(canvas.deleteSelectedLayer(), canvas.layerThing.getSelectedIndex()));
                    layersPanel.load();
                });
                duplicateBtn.addActionListener(_ -> {
                    canvas.duplicateLayer();
                    layersPanel.load();
                });
                clearBtn.addActionListener(_ -> {
                    Map<Pixel, Color> map = new HashMap<>();
                    for (int x = 0; x < canvas.getWidth(); x++) {
                        for (int y = 0; y < canvas.getHeight(); y++) {
                            Pixel pixel = canvas.getSelectedLayer().getPixel(y, x);
                            map.put(pixel, pixel.setColor(null));
                        }
                    }
                    canvas.setLastAction(Actions.stroke(map));
                    layersPanel.load();
                });

                buttonPanel.setLayout(new FlowLayout());
                for (JButton btn : new JButton[]{newLayerBtn, deleteLayerBtn, duplicateBtn, clearBtn}) {
                    buttonPanel.add(btn);
                }
                add(buttonPanel, BorderLayout.CENTER);
                add(layersPanel, BorderLayout.SOUTH);
            }
        }

        private class UndoListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    canvas.addRedoAction(canvas.getLastAction().performAction(canvas));
                    canvasPanel.repaint();
                    layersSection.layersPanel.load();
                } catch (Exception _) {}
            }
        }

        private class RedoListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    canvas.setLastAction(canvas.getRedoAction().performAction(canvas));
                    canvasPanel.repaint();
                    layersSection.layersPanel.load();
                } catch (Exception _) {}
            }
        }

        private class NewCanvasListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                new CanvasDialog().display();
            }
        }
    }

    private class Pen extends Selectable {
        public Color penColor;

        private final PenType[] types = new PenType[5];

        Pen() {
            penColor = Color.black;
            selectedIndex = 0;
            types[0] = new PenType("Pen", false, false, false);
            types[1] = new PenType("Eraser", true, false, false);
            types[2] = new PenType("Fill", false, true, false);
            types[3] = new PenType("Eraser Fill", true, true, false);
            types[4] = new PenType("Colorpick", false, false, true);
        }

        public boolean isFilling() {
            return types[selectedIndex].fill;
        }

        public Color getPenColor() {
            return types[selectedIndex].getColor();
        }

        public void setSize(int s) {
            types[selectedIndex].setSize(s);
        }

        @Override
        public List<String> getSelectables() {
            List<String> names = new ArrayList<>();
            for (PenType type : types) {
                names.add(type.name);
            }
            return names;
        }

        public int getSize() {
            return types[selectedIndex].size;
        }

        public class PenType {
            String name;
            int size;
            boolean eraser;
            boolean fill;
            boolean colorpick;

            PenType(String name, boolean eraser, boolean fill, boolean colorpick) {
                this.name = name;
                size = 1;
                this.fill = fill;
                this.eraser = eraser;
                this.colorpick = colorpick;

            }

            public Color getColor() {
                if (eraser) {
                    return null;
                } else return penColor;
            }

            void setSize(int size) {
                this.size = size;
            }

            void incrementSize(int inc) {
                this.size+=inc;
            }
        }
        public void select(int num) {
            super.select(num);
            if (penSettingsPanel == null) return;
            penSettingsPanel.penSizeSlider.setValue(types[selectedIndex].size);
            penSettingsPanel.repaint();
        }
        public int getSelectedIndex() {
            return super.getSelectedIndex();
        }
    }

    /////////// Styling n other stuff //////////

    private class SmallChooser extends JColorChooser {
        SmallChooser(Color color) {
            super(color);
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

    private class LabeledInput extends JPanel {
        JTextField textField;
        LabeledInput(JTextField textField, String text) {
            this.textField = textField;
            setLayout(new GridLayout());
            add(new JLabel(text));
            add(textField);
        }
    }

    private class SelectablePanel extends JPanel {
        static int FLOW = 0;
        static int BOX = 1;

        Selectable thing;
        LayoutManager[] mgrs = {new FlowLayout(), new BoxLayout(this, BoxLayout.Y_AXIS)};

        SelectablePanel(Selectable thing, int layout) {
            this.thing = thing;
            setLayout(mgrs[layout]);
            load();
        }

        public void load() {
            removeAll();
            for (int i = thing.getSelectables().size()-1; i >= 0; i--) {
                SelectBtn btn = new SelectBtn(thing.getSelectables().get(i), (i == thing.getSelectedIndex()));
                btn.addActionListener(new SelectListener(i));
                add(btn);
                repaint();
            }

            validate();
            repaint();
            revalidate();
            canvasPanel.repaint();
        }

        private class SelectBtn extends JButton {
            public SelectBtn(String name, boolean isSelected) {
                super(name);
                if (isSelected) {
                    setForeground(Color.blue);
                }
            }
        }

        private class SelectListener implements ActionListener {
            int index;
            SelectListener(int i) {
                index = i;
            }
            public void actionPerformed(ActionEvent e) {
                thing.select(index);
                load();
                repaint();
            }
        }
    }

    private class LineMaker {
        int x1, x2, y1, y2;
        long b;
        double slope;

        LineMaker(int x1, int x2, int y1, int y2) {
            this.x1 = x1;
            this.x2 = x2;
            this.y1 = y1;
            this.y2 = y2;
            slope = (double)(y1-y2)/(x1-x2);
            b = Math.round(-1 * (slope * x1 - y1));
        }

        int calcByX(int x) {
            return (int)Math.round(x*slope+b);
        }

        int calcByY(int y) {
            return (int)Math.round((y-b)/slope);
        }
    }
}
