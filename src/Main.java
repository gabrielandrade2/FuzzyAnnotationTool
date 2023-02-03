import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.NumberFormatter;

public class Main {

    private static List<List<Annotation>> storedAnnotations;

    public record Annotation(int start, String tag) implements Comparable<Annotation> {

        @Override
        public int compareTo(Annotation o) {
            return Integer.compare(start, o.start);
        }
    }

    public static class TextAreaExample {

        private final JTextField textField;
        private final JTextArea area;
        private final JButton next;
        private final JButton prev;
        private final JButton undo;
        private List<String> documents;
        private List<Annotation> annotations;
        private Stack<Annotation> undoStack = new Stack<>();
        private int i = 0;
        private Highlighter highlighter;
        private GradientHighlighter painter;

        TextAreaExample(String file) {
            documents = readXML(file);
            storedAnnotations = convertTagsIntoAnnotations(documents);
            documents = cleanText(documents);
            JFrame f = new JFrame();
            area = new JTextArea();
            area.setEditable(false);
            area.setBounds(10, 10, 1000, 600);
            area.setLineWrap(true);
            area.setWrapStyleWord(false);
            area.setSelectionColor(null);
            highlighter = new DefaultHighlighter();
            painter = new GradientHighlighter(new Color(51, 153, 255, 128));
            area.setHighlighter(highlighter);

            JPanel panel1 = new JPanel();
            FlowLayout layout = new FlowLayout();
            layout.setHgap(layout.getHgap() + 50);
            panel1.setLayout(layout);
            String[] options = {"C"};
            JComboBox<String> comboBox = new JComboBox<>(options);
            comboBox.setSelectedIndex(0);
            comboBox.setPreferredSize(new Dimension(100, 30));
            JLabel label = new JLabel("Document");

            NumberFormat format = NumberFormat.getInstance();
            NumberFormatter formatter = new NumberFormatter(format);
            formatter.setValueClass(Integer.class);
            formatter.setMinimum(1);
            formatter.setMaximum(documents.size());
//            formatter.setAllowsInvalid(false);
//            // If you want the value to be committed on each keystroke instead of focus lost
//            formatter.setCommitsOnValidEdit(true);
            textField = new JFormattedTextField(formatter);

//            textField = new JTextField();
            textField.setPreferredSize(new Dimension(50, 30));
            textField.setText(String.valueOf(i + 1));
            textField.addActionListener(e -> {
                changeText(Integer.parseInt(textField.getText()) - 1);
            });
            JPanel p1 = new JPanel();
            p1.add(new JLabel("Tag:"));
            p1.add(comboBox);
            panel1.add(p1);
            p1 = new JPanel();
            p1.add(label);
            p1.add(textField);
            label = new JLabel("of " + documents.size());
            p1.add(label);
            panel1.add(p1);

            next = new JButton("Next");
            next.addActionListener(e -> {
                changeText(i + 1);
            });
            undo = new JButton("Undo");
            undo.addActionListener(e -> undo());
            undo.setEnabled(false);

            prev = new JButton("Prev");
            prev.setEnabled(false);
            prev.addActionListener(e -> {
                changeText(i - 1);
            });

            JPanel panel2 = new JPanel(new BorderLayout());
            JPanel panel = new JPanel(new FlowLayout());
            panel.add(prev);
            panel.add(undo);
            panel.add(next);
            panel2.add(panel, BorderLayout.NORTH);
            panel = new JPanel(new FlowLayout());
            JButton save = new JButton("Save");
            save.addActionListener(e -> {
                JFileChooser jfc = new JFileChooser(
                        FileSystemView.getFileSystemView().getHomeDirectory());
                jfc.setDialogTitle("Specify a file to save");
                int returnValue = jfc.showSaveDialog(f);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = jfc.getSelectedFile();
                    System.out.println(selectedFile.getAbsolutePath());
                    saveXML(selectedFile.getAbsolutePath(), documents, storedAnnotations);
                }

            });
            panel.add(save);
            panel2.add(panel, BorderLayout.SOUTH);

            f.setSize(1000, 600);
            f.setLayout(new BorderLayout());
            f.add(panel1, BorderLayout.PAGE_START);
            f.add(area, BorderLayout.CENTER);
            f.add(panel2, BorderLayout.PAGE_END);
            f.setVisible(true);
            area.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    int start = area.getSelectionStart();
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        String text = area.getText();

                        if (start == text.length()) {
                            return;
                        }

                        String tag = (String) comboBox.getSelectedItem();
//                        area.setText(text.substring(0, start) + "<" + tag + " />" +
//                                text.substring(start));
                        area.setSelectionColor(Color.white);
                        highlighter.removeAllHighlights();

                        Annotation annotation = new Annotation(start, tag);
                        annotations.add(annotation);
                        undoStack.push(annotation);
                        undo.setEnabled(true);

                        annotateAll();
                    }

                    if (SwingUtilities.isRightMouseButton(e)) {
                        Highlighter.Highlight[] highlights = area.getHighlighter().getHighlights();
                        for (Highlighter.Highlight highlight : highlights) {
                            if (highlight.getStartOffset() <= start && highlight.getEndOffset() >= start) {

                                break;
                            }
                        }
                    }
                }
            });
            area.getDocument().addDocumentListener(new DocumentListener() {

                @Override
                public void removeUpdate(DocumentEvent e) {
                    documents.set(i, area.getText());
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    documents.set(i, area.getText());
                }

                @Override
                public void changedUpdate(DocumentEvent arg0) {
                    documents.set(i, area.getText());
                }
            });
            changeText(i);
        }

        private void undo() {
            if (!undoStack.isEmpty()) {
                Annotation annotation = undoStack.pop();
                annotations.remove(annotation);
                annotateAll();
                undo.setEnabled(!undoStack.isEmpty());
            }
        }

        private void changeText(int newDoc) {
            if (newDoc > documents.size() - 1 || newDoc < 0) {
                return;
            }
            i = newDoc;
            if (storedAnnotations.size() <= i || storedAnnotations.get(i) == null) {
                storedAnnotations.set(i, new Stack<>());
            }
            undoStack.clear();
            undo.setEnabled(false);
            annotations = storedAnnotations.get(i);
            area.setText(documents.get(i));
            textField.setText(String.valueOf(i + 1));

            annotateAll();

            if (i == 0) {
                prev.setEnabled(false);
            } else {
                prev.setEnabled(true);
            }

            if (i == documents.size() - 1) {
                next.setEnabled(false);
            } else {
                next.setEnabled(true);
            }
        }

        private void annotateAll() {
            highlighter.removeAllHighlights();
            try {
                for (Annotation a : annotations) {
                    int start = Math.max(0, a.start - 10);
                    int offseta = area.getText().substring(start, a.start)
                            .indexOf("\n");
                    if (offseta == -1) {
                        offseta = 0;
                    }

                    int end = Math.min(area.getText().length(), a.start + 10);
                    int offsetb = area.getText().substring(a.start, end)
                            .indexOf("\n");
                    if (offsetb == -1) {
                        offsetb = 10;
                    }

                    highlighter.addHighlight(Math.max(a.start - 10 + offseta, 0),
                            Math.min(a.start + offsetb, area.getText().length() - 1),
                            painter);
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }


        private static List<String> readXML(String path) {
            try {
                List<String> documents = new ArrayList<>();
                List<String> lines = Files.readAllLines(Paths.get(path));
                for (int i = 0; i < lines.size(); i++) {
                    if (lines.get(i).matches("^<article( [^>]*)?>$")) {
                        StringJoiner sj = new StringJoiner("\n");
                        while (!lines.get(++i).equals("</article>")) {
                            sj.add(lines.get(i));
                        }
                        documents.add(sj.toString());
                    }
                }
                return documents;

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private static void saveXML(String path, List<String> documents,
                List<List<Annotation>> annotations) {
            File file = new File(path);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                writer.write("<articles>\n");
                int i;
                for (i = 0; i < documents.size(); i++) {
                    String document = documents.get(i);
                    List<Annotation> ann = annotations.get(i);
                    writer.write("<article>\n");
                    writer.write(convertAnnotationsIntoTags(document, ann));
                    writer.write("\n</article>\n");
                }

                writer.write("</articles>\n");
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            new JOptionPane().showMessageDialog(null, "Saved to " + path);
        }
    }

    private static String convertAnnotationsIntoTags(String text, List<Annotation> annotations) {
        if (annotations == null || annotations.isEmpty()) {
            return text;
        }

        String taggedText = new String(text);
        List<Annotation> sortedAnnotations = annotations.stream().sorted()
                .collect(Collectors.toList());
        int offset = 0;
        for (Annotation a : sortedAnnotations) {
            String tag = "<" + a.tag + " />";
            taggedText = taggedText.substring(0, a.start + offset) + tag +
                    taggedText.substring(a.start + offset);
            offset += tag.length();
        }
        return taggedText;
    }

    private static List<List<Annotation>> convertTagsIntoAnnotations(List<String> documents) {
        List<List<Annotation>> annotations = new ArrayList<>();
        for (String document : documents) {
            List<Annotation> ann = new ArrayList<>();
            Pattern pattern = Pattern.compile("<([^>]+) />");
            Matcher matcher = pattern.matcher(document);
            int offset = 0;
            while (matcher.find()) {
                ann.add(new Annotation(matcher.start() - offset, matcher.group(1)));
                offset += matcher.group(1).length() + 4;
            }
            annotations.add(ann);
        }
        return annotations;
    }

    private static List<String> cleanText(List<String> documents) {
        for (int i = 0; i < documents.size(); i++) {
            String text = documents.get(i);
            text = text.replaceAll("<[^>]+>", "")
                    .replaceAll("</?\\w( [^>]*)?>", "");
            documents.set(i, text);
        }
        return documents;
    }

    public static String fileChooser() {
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        jfc.setFileFilter(new FileNameExtensionFilter("Text files", "txt", "xml"));
        jfc.setAcceptAllFileFilterUsed(false);
        int returnValue = jfc.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            System.out.println(selectedFile.getAbsolutePath());
            return selectedFile.getAbsolutePath();
        }
        return null;
    }

    public static void main(String[] args) {
        String file = fileChooser();
        if (file == null || file.isEmpty()) {
            return;
        }
        new TextAreaExample(file);
    }
}
