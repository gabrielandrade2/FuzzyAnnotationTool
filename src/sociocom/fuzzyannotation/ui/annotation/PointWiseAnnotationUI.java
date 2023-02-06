package sociocom.fuzzyannotation.ui.annotation;

import sociocom.fuzzyannotation.Annotation;
import sociocom.fuzzyannotation.GradientHighlighter;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

public class PointWiseAnnotationUI extends BaseAnnotationUI {

    public PointWiseAnnotationUI(List<String> documents, List<List<Annotation>> storedAnnotations) {
        super(documents, storedAnnotations);
    }

    @Override
    protected void configureElements() {
        textArea.setSelectionColor(null);
        textArea.addMouseListener(new MouseEventHandler());
        highlighter = new DefaultHighlighter();
        painter = new GradientHighlighter(new Color(51, 153, 255, 128));
        textArea.setHighlighter(highlighter);
    }

    @Override
    protected void annotateAll() {
        Highlighter highlighter = textArea.getHighlighter();
        highlighter.removeAllHighlights();
        try {

            for (Annotation a : annotations) {
                if (a.getStartSpan() == -1 || a.getEndSpan() == -1) {
                    int span = random.nextInt(10 - 3) + 3;
                    int start = Math.max(0, a.start() - span);
                    int offseta = textArea.getText().substring(start, a.start())
                            .indexOf("\n");
                    if (offseta == -1) {
                        offseta = 0;
                    }

                    int end = Math.min(textArea.getText().length(), a.start() + span);
                    int offsetb = textArea.getText().substring(a.start(), end)
                            .indexOf("\n");
                    if (offsetb == -1) {
                        offsetb = span;
                    }

                    a.setSpan(Math.max(a.start() - span + offseta, 0),
                            Math.min(a.start() + offsetb, textArea.getText().length() - 1));
                }

                highlighter.addHighlight(a.getStartSpan(), a.getEndSpan(), painter);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected String convertAnnotationsIntoTags(String text, List<Annotation> annotations) {
        if (annotations == null || annotations.isEmpty()) {
            return text;
        }

        String taggedText = new String(text);
        List<Annotation> sortedAnnotations = annotations.stream().sorted()
                .collect(Collectors.toList());
        int offset = 0;
        for (Annotation a : sortedAnnotations) {
            String tag = "<" + a.tag() + " />";
            taggedText = taggedText.substring(0, a.start() + offset) + tag +
                    taggedText.substring(a.start() + offset);
            offset += tag.length();
        }
        return taggedText;
    }

    private class MouseEventHandler extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                addAnnotation();
            } else if (SwingUtilities.isRightMouseButton(e)) {
                removeAnnotation();
            }
        }

        private void addAnnotation() {
            int start = textArea.getSelectionStart();
            String text = textArea.getText();

            if (start == text.length()) {
                return;
            }

            String tag = (String) tagComboBox.getSelectedItem();
            textArea.setSelectionColor(Color.white);
            highlighter.removeAllHighlights();

            Annotation annotation = new Annotation(start, tag);
            annotations.add(annotation);
            undoStack.push(annotation);
            undoButton.setEnabled(true);

            annotateAll();
        }

        private void removeAnnotation() {
            int start = textArea.getSelectionStart();
            Highlighter.Highlight[] highlights = textArea.getHighlighter()
                    .getHighlights();
            for (Highlighter.Highlight highlight : highlights) {
                if (highlight.getStartOffset() <= start &&
                        highlight.getEndOffset() >= start) {
                    break;
                }
            }
        }
    }
}
