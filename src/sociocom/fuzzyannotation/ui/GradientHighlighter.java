package sociocom.fuzzyannotation.ui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.View;


public class GradientHighlighter extends DefaultHighlighter.DefaultHighlightPainter {

    public static final Map<String, Color> COLORS = new LinkedHashMap<>();

    static {
        COLORS.put("Blue", new Color(51, 153, 255, 128));
        COLORS.put("Red", new Color(255, 51, 51, 128));
        COLORS.put("Green", new Color(51, 255, 51, 128));
        COLORS.put("Yellow", new Color(255, 255, 51, 128));
        COLORS.put("Orange", new Color(255, 153, 51, 128));
        COLORS.put("Purple", new Color(153, 51, 255, 128));
    }

    private Iterator<Color> colorIterator;

    private Color color;
    private int fuzziness = 0;

    /**
     * Constructs a new highlight painter. If <code>c</code> is null, the JTextComponent will be
     * queried for its selection color.
     *
     * @param c the color for the highlight
     */
    public GradientHighlighter(Color c) {
        super(c);
        color = c;
    }

    public GradientHighlighter() {
        super(null);
    }

    @Override
    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String getColorName() {
        for (Map.Entry<String, Color> entry : COLORS.entrySet()) {
            if (entry.getValue().equals(color)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public Shape paintLayer(Graphics g, int offs0, int offs1,
            Shape bounds, JTextComponent c, View view) {
        Color color = getColor();
        if (color == null) {
            color = COLORS.get("Blue");
        }

        Rectangle r;

        if (offs0 == view.getStartOffset() &&
                offs1 == view.getEndOffset()) {
            // Contained in view, can just use bounds.
            if (bounds instanceof Rectangle) {
                r = (Rectangle) bounds;
            } else {
                r = bounds.getBounds();
            }
        } else {
            // Should only render part of View.
            try {
                // --- determine locations ---
                Shape shape = view.modelToView(offs0, Position.Bias.Forward,
                        offs1, Position.Bias.Backward,
                        bounds);
                r = (shape instanceof Rectangle) ?
                        (Rectangle) shape : shape.getBounds();
            } catch (BadLocationException e) {
                // can't render
                r = null;
            }
        }
        try {

            if (r != null) {
                // If we are asked to highlight, we should draw something even
                // if the model-to-view projection is of zero width (6340106).
                r.width = Math.max(r.width, 1);

                Color transparent = new Color(color.getRed(), color.getGreen(), color.getBlue(),
                        0 - fuzziness);

                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient;
                if (c.getFontMetrics(c.getFont()).charWidth(c.getText().charAt(offs1 + 1)) + r.x +
                        r.width > c.getWidth()) {
                    gradient = new GradientPaint(r.x, r.y, transparent, r.x + r.width, r.y,
                            color, false);

                } else if (
                        r.x - c.getFontMetrics(c.getFont())
                                .charWidth(c.getText().charAt(offs0 - 1)) <
                                0) {
                    gradient = new GradientPaint(r.x, r.y, color, r.x + r.width, r.y,
                            transparent, false);
                } else {
                    gradient = new GradientPaint(r.x, r.y, transparent, r.x + r.width / 2, r.y,
                            color, true);
                }

                g2d.setPaint(gradient);

                g.fillRect(r.x, r.y, r.width, r.height);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return r;
    }

    public void setFuzziness(int fuzz) {
        this.fuzziness = fuzz;
    }
//
//    private Color nextColor() {
//        if (colorIterator == null || !colorIterator.hasNext()) {
//            colorIterator = colors.iterator();
//        }
//        return colorIterator.next();
//    }
}
