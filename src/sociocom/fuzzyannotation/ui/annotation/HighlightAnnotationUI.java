package sociocom.fuzzyannotation.ui.annotation;

import sociocom.fuzzyannotation.Annotation;
import sociocom.fuzzyannotation.ui.GradientHighlighter;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;


public class HighlightAnnotationUI extends BaseAnnotationUI {

    public HighlightAnnotationUI(List<String> documents, List<List<Annotation>> storedAnnotations,
            boolean autoSave, Path file) {
        super(documents, storedAnnotations, autoSave, file, "Highlight Annotation");
    }

    private void resetSelection() {
        textArea.setSelectionStart(0);
        textArea.setSelectionEnd(0);
    }

    @Override
    protected void configureElements() {
        highlighter = new DefaultHighlighter();
        painter = new GradientHighlighter(new Color(51, 153, 255, 128));
        textArea.setHighlighter(highlighter);
        textArea.addMouseListener(new MouseEventHandler());
        DefaultCaret caret = new DefaultCaret();
        textArea.setCaret(caret);
    }

    @Override
    public void annotateAll() {
        Highlighter highlighter = textArea.getHighlighter();
        highlighter.removeAllHighlights();
        try {
            for (Annotation a : annotations) {
                if (a.getStartSpan() == -1 || a.getEndSpan() == -1) {
//                    int span = random.nextInt(getFuzzyWeight());
                    int span = 0;
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

                int start = a.getStartSpan();
                int end = a.getEndSpan();

                if ((end - start) < 2) {
                    int offset = 2 - (end - start);
                    start = start - (int) Math.ceil(offset / 2);
                    end = end + (int) Math.ceil(offset / 2);
                }
                highlighter.addHighlight(start, end, painter);
            }
        } catch (Exception ex) {
            StringJoiner joiner = new StringJoiner("\n")
                    .add("Failed to highlight text")
                    .add("Message: " + ex.getMessage());
            JOptionPane.showMessageDialog(frame, joiner.toString(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void undo() {
        super.undo();
        resetSelection();
    }

    @Override
    protected String convertAnnotationsIntoTags(String text, List<Annotation> annotations) {
        try {
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
                if (a.end() + offset >= taggedText.length()) {
                    sb.append(taggedText.substring(a.start() + offset));
                } else {
                    sb.append(taggedText.substring(a.start() + offset, a.end() + offset));
                }
                sb.append(endTag);
                sb.append(taggedText.substring(a.end() + offset));
                offset += startTag.length() + endTag.length();
                taggedText = sb.toString();
            }
            return taggedText;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Failed to save file: \n" + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return text;
        }
    }

    private class MouseEventHandler extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            if (e.getClickCount() > 1) {
                e.consume();
            }
        }

        public void mouseReleased(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                int start = textArea.getSelectionStart();
                int end = textArea.getSelectionEnd();

                if (start == end) {
                    return;
                }

                String tag = (String) tagComboBox.getSelectedItem();
                Annotation annotation = new Annotation(start, end, tag);
                addAnnotation(annotation);

                annotateAll();
                resetSelection();
            } else if (SwingUtilities.isRightMouseButton(e)) {
            }
        }
    }

    public static List<List<Annotation>> convertTagsIntoAnnotations(List<String> documents) {
        List<List<Annotation>> annotations = new ArrayList<>();
        for (String document : documents) {
            List<Annotation> ann = new ArrayList<>();
            Pattern pattern = Pattern.compile("<([^>/]+)>.+?</[^>]+>");
            Matcher matcher = pattern.matcher(document);
            int offset = 0;
            while (matcher.find()) {
                ann.add(new Annotation(matcher.start() - offset, matcher.end() - offset,
                        matcher.group(1)));
                offset += matcher.group(1).length() + 4;
            }
            annotations.add(ann);
        }
        return annotations;
    }
}
