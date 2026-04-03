
import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class Window {
    boolean showGrid = false;

    private JFrame window;
    private Canvas canvas;
    private Canvas defaultCanvas = new Canvas(20, 20, Color.white);


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


            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (!mouseDown) {
                        mouseDown = true;
                        penDown(pen.selectedIndex);
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

            private void draw(Point mousePos, boolean isLast) {
                new Thread(() -> {
                    int temp = 0;
                    if (mousePos == null) return;
                    Pixel currPixel = checkPixel(mousePos);
                    if (currPixel != null && !pixelsAltered.containsKey(currPixel)) {
                        pixelsAltered.put(currPixel, currPixel.getColor());
                        currPixel.setColor(pen.getPenColor());
                        temp++;
                    }
                    if (temp > 0) {
                        canvas.clearRedo();
                    }
                    if (isLast) {
                        myCanvas.setLastAction(pixelsAltered);
                        pixelsAltered = new HashMap<>();
                    }
                }).start();
            }

            private void penDown(int type) {
                if (check()) {
                    new Thread(() -> {
                        if (type < 2) {
                            mouseDown = true;
                            while (mouseDown) {
                                draw(getMousePosition(), false);
                            }
                            draw(getMousePosition(), true);
                        }
                    }).start();
                }
            }

            private void release(MouseEvent e) {
                if (isRunning && e.getButton() == MouseEvent.BUTTON1) {
                    mouseDown = false;
                    isRunning = false;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                release(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {
                release(e);
            }


            @Override
            public void mouseDragged(MouseEvent e) {
                if (pen.types[pen.selectedIndex].colorpick) {
                    try {
                        Color temp = checkTopPixel(e.getPoint()).getColor();
                        System.out.println(temp);
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
        SmallChooser colorChooser;
        LayersSection layersSection = new LayersSection();
        SelectablePanel penPanel = new SelectablePanel(pen, SelectablePanel.FLOW);

        PenSettingsPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            undoButton = new JButton("Undo");
            undoButton.addActionListener(new UndoListener());

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
            add(undoButton);
            add(redoBtn);
            add(layersSection);
            add(newCanvasBtn);
        }

        private class LayersSection extends JPanel {
            JButton newLayerBtn = new JButton("New Layer"), deleteLayerBtn = new JButton("Delete"), duplicateBtn = new JButton("Duplicate");
            JPanel buttonPanel = new JPanel();
            SelectablePanel layersPanel = new SelectablePanel(canvas.layerThing, SelectablePanel.BOX);
            LayersSection() {
                setLayout(new BorderLayout());

                newLayerBtn.addActionListener(_ -> {
                    canvas.addLayer();
                    layersPanel.load();
                });
                deleteLayerBtn.addActionListener(_ -> {
                    canvas.deleteLayer();
                    layersPanel.load();
                });
                duplicateBtn.addActionListener(_ -> {
                    canvas.duplicateLayer();
                    layersPanel.load();
                });

                buttonPanel.setLayout(new FlowLayout());
                for (JButton btn : new JButton[]{newLayerBtn, deleteLayerBtn, duplicateBtn}) {
                    buttonPanel.add(btn);
                }
                add(buttonPanel, BorderLayout.CENTER);
                add(layersPanel, BorderLayout.SOUTH);
            }
        }

        private Map<Pixel, Color> performAction(Map<Pixel, Color> map) {
            Map<Pixel, Color> undoAction = new HashMap<>();

            for (Pixel pixel : map.keySet()) {
                Color prevColor = pixel.getColor();
                pixel.setColor(map.get(pixel));
                undoAction.put(pixel, prevColor);
            }

            return undoAction;
        }

        private class UndoListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                Map<Pixel, Color> lastAction = canvas.getLastAction();
                if (!lastAction.isEmpty()) {
                    canvas.addRedoAction(performAction(lastAction));
                }

                canvasPanel.repaint();
            }
        }

        private class RedoListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                Map<Pixel, Color> redoAction = canvas.getRedoAction();
                if (!redoAction.isEmpty()) {
                    canvas.setLastAction(performAction(redoAction));
                }
                canvasPanel.repaint();
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
        public int size;

        private PenType[] types = new PenType[5];

        Pen() {
            penColor = Color.black;
            size = 1;
            types[0] = new PenType("Pen", false, false, false);
            types[1] = new PenType("Eraser", true, false, false);
            types[2] = new PenType("Fill", false, true, false);
            types[3] = new PenType("Eraser Fill", true, true, false);
            types[4] = new PenType("Colorpick", false, false, true);
        }

        public Color getPenColor() {
            return types[selectedIndex].getColor();
        }

        @Override
        public List<String> getSelectables() {
            List<String> names = new ArrayList<>();
            for (PenType type : types) {
                names.add(type.name);
            }
            return names;
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
    }

    /////////// Styling n other abstract stuff //////////

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
}
