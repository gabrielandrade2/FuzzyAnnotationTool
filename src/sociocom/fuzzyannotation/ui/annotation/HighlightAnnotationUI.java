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

public class HighlightAnnotationUI extends BaseAnnotationUI {

    public HighlightAnnotationUI(List<String> documents, List<List<Annotation>> storedAnnotations) {
        super(documents, storedAnnotations, "Highlight Annotation");
    }

    @Override
    protected void configureElements() {
        highlighter = new DefaultHighlighter();
        painter = new GradientHighlighter(new Color(51, 153, 255, 128));
        textArea.setHighlighter(highlighter);
        textArea.addMouseListener(new MouseEventHandler());
    }

    @Override
    protected void annotateAll() {
        Highlighter highlighter = textArea.getHighlighter();
        highlighter.removeAllHighlights();
        try {
            for (Annotation a : annotations) {
                if (a.getStartSpan() == -1 || a.getEndSpan() == -1) {
                    int span = random.nextInt(3);
                    span = 0;
                    int start = Math.max(0, a.start() - span);
                    int offseta = textArea.getText().substring(start, a.start())
                            .indexOf("\n");
                    if (offseta == -1) {
                        offseta = 0;
                    }

                    int end = Math.min(textArea.getText().length(), a.end() + span);
                    int offsetb = textArea.getText().substring(a.start(), end)
                            .indexOf("\n");
                    if (offsetb == -1) {
                        offsetb = 0;
                    }

                    a.setSpan(Math.max(a.start() - span + offseta, 0),
                            Math.min(a.end() + span - offsetb, textArea.getText().length() - 1));
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
            String startTag = "<" + a.tag() + ">";
            String endTag = "</" + a.tag() + ">";
            StringBuilder sb = new StringBuilder();
            sb.append(taggedText.substring(0, a.start() + offset));
            sb.append(startTag);
            sb.append(text.substring(a.start() + offset, a.end() + offset));
            sb.append(endTag);
            sb.append(taggedText.substring(a.end() + offset));
            offset += startTag.length() + endTag.length();
        }
        return taggedText;
    }

    private class MouseEventHandler extends MouseAdapter {

        public void mouseReleased(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                int start = textArea.getSelectionStart();
                int end = textArea.getSelectionEnd();

                String tag = (String) tagComboBox.getSelectedItem();
                Annotation annotation = new Annotation(start, end, tag);
                annotations.add(annotation);
                undoStack.push(annotation);
                undoButton.setEnabled(true);

                annotateAll();
            } else if (SwingUtilities.isRightMouseButton(e)) {
            }
        }
    }
}
