import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.View;

public class GradientHighlighter extends DefaultHighlighter.DefaultHighlightPainter {

    /**
     * Constructs a new highlight painter. If <code>c</code> is null, the JTextComponent will be
     * queried for its selection color.
     *
     * @param c the color for the highlight
     */
    public GradientHighlighter(Color c) {
        super(c);
    }

    @Override
    public void paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c) {
        Rectangle alloc = bounds.getBounds();
        try {
            // --- determine locations ---
            TextUI mapper = c.getUI();
            Rectangle p0 = mapper.modelToView(c, offs0);
            Rectangle p1 = mapper.modelToView(c, offs1);

            // --- render ---
            Color color = getColor();

            if (color == null) {
                g.setColor(c.getSelectionColor());
            } else {
                g.setColor(color);
            }
            if (p0.y == p1.y) {
                // same line, render a rectangle
                Rectangle r = p0.union(p1);
                g.fillRect(r.x, r.y, r.width, r.height);
            } else {
                // different lines
                int p0ToMarginWidth = alloc.x + alloc.width - p0.x;
                g.fillRect(p0.x, p0.y, p0ToMarginWidth, p0.height);
                if ((p0.y + p0.height) != p1.y) {
                    g.fillRect(alloc.x, p0.y + p0.height, alloc.width,
                            p1.y - (p0.y + p0.height));
                }
                g.fillRect(alloc.x, p1.y, (p1.x - alloc.x), p1.height);
            }
        } catch (BadLocationException e) {
            // can't render
        }
    }

    @Override
    public Shape paintLayer(Graphics g, int offs0, int offs1,
            Shape bounds, JTextComponent c, View view) {
        Color color = getColor();

//        if (color == null) {
//            g.setColor(c.getSelectionColor());
//        }
//        else {
//            g.setColor(color);
//        }

//        Graphics2D g2d = (Graphics2D)g;
//        GradientPaint gradient = new GradientPaint(g.get,10,s1,30,30,e,true);
//        g2d.setPaint(gradient);

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

        if (r != null) {
            // If we are asked to highlight, we should draw something even
            // if the model-to-view projection is of zero width (6340106).
            r.width = Math.max(r.width, 1);

            Color transparent = new Color(0, 0, 0, 0);

            Graphics2D g2d = (Graphics2D) g;
            GradientPaint gradient;
            if (c.getFontMetrics(c.getFont()).getMaxAdvance() + r.x + r.width >= c.getWidth())  {
                gradient = new GradientPaint(r.x, r.y, transparent, r.x + r.width, r.y,
                        color, false);

            } else if (r.x - c.getFontMetrics(c.getFont()).getMaxAdvance() <= 0){
                gradient = new GradientPaint(r.x, r.y, color, r.x + r.width, r.y,
                        transparent, false);
            } else{
                gradient = new GradientPaint(r.x, r.y, transparent, r.x + r.width/2, r.y,
                        color, true);
            }

            g2d.setPaint(gradient);

            g.fillRect(r.x, r.y, r.width, r.height);
        }

        return r;
    }
}
