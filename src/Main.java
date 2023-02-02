import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.NumberFormatter;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class Main {

    private static Stack<Annotation>[] storedAnnotations;

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
        private String[] documents = null;
        private Stack<Annotation> annotations;
        private int i = 0;
        private Highlighter highlighter;
        private GradientHighlighter painter;

        TextAreaExample(String file) {
            documents = readXML(file);
            storedAnnotations = new Stack[documents.length];
            JFrame f = new JFrame();
            area = new JTextArea();
            area.setHighlighter(new DefaultHighlighter());
            area.setEditable(false);
            area.setBounds(10, 10, 1000, 600);
            area.setLineWrap(true);
            area.setWrapStyleWord(false);
            area.setSelectionColor(null);

            JPanel panel1 = new JPanel();
            FlowLayout layout = new FlowLayout();
            layout.setHgap(layout.getHgap() + 50);
            panel1.setLayout(layout);
            String[] options = {"C", "CN"};
            JComboBox<String> comboBox = new JComboBox<>(options);
            comboBox.setSelectedIndex(0);
            comboBox.setPreferredSize(new Dimension(100, 30));
            JLabel label = new JLabel("Document");

            NumberFormat format = NumberFormat.getInstance();
            NumberFormatter formatter = new NumberFormatter(format);
            formatter.setValueClass(Integer.class);
            formatter.setMinimum(1);
            formatter.setMaximum(documents.length);
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
            label = new JLabel("of " + documents.length);
            p1.add(label);
            panel1.add(p1);

            next = new JButton("Next");
            next.addActionListener(e -> {
                changeText(i + 1);
            });
            JButton reset = new JButton("Reset");
            reset.addActionListener(e -> {
                while (storedAnnotations[i] != null && !storedAnnotations[i].isEmpty()) {
                    undo();
                }
            });
            JButton undo = new JButton("Undo");
            undo.addActionListener(e -> undo());

            prev = new JButton("Prev");
            prev.setEnabled(false);
            prev.addActionListener(e -> {
                changeText(i - 1);
            });

            JPanel panel2 = new JPanel(new BorderLayout());
            JPanel panel = new JPanel(new FlowLayout());
            panel.add(prev);
            panel.add(reset);
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
            area.setHighlighter(null);
            area.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {

                    if (e.getButton() == MouseEvent.BUTTON1) {
                        int start = area.getSelectionStart();
                        String text = area.getText();

                        if (start == area.getText().length()) {
                            return;
                        }

                        String tag = (String) comboBox.getSelectedItem();
//                        area.setText(text.substring(0, start) + "<" + tag + " />" +
//                                text.substring(start));
                        area.setHighlighter(new DefaultHighlighter());
                        area.setSelectionColor(Color.white);
                        highlighter = area.getHighlighter();
                        highlighter.removeAllHighlights();
                        painter =
                                new GradientHighlighter(new Color(51, 153, 255, 128));

                        annotations.push(new Annotation(start, tag));

                        annotateAll();
                    }
                }
            });
            area.getDocument().addDocumentListener(new DocumentListener() {

                @Override
                public void removeUpdate(DocumentEvent e) {
                    documents[i] = area.getText();
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    documents[i] = area.getText();
                }

                @Override
                public void changedUpdate(DocumentEvent arg0) {
                    documents[i] = area.getText();
                }
            });
            changeText(i);
        }

        private void undo() {
            if (!annotations.isEmpty()) {
                annotations.pop();
                annotateAll();
            }
        }

        private void changeText(int newDoc) {
            if (newDoc > documents.length - 1 || newDoc < 0) {
                return;
            }
            i = newDoc;
            if (storedAnnotations.length <= i || storedAnnotations[i] == null) {
                storedAnnotations[i] = new Stack<>();
            }
            annotations = storedAnnotations[i];
            area.setText(documents[i]);
            textField.setText(String.valueOf(i + 1));

            annotateAll();

            if (i == 0) {
                prev.setEnabled(false);
            } else {
                prev.setEnabled(true);
            }

            if (i == documents.length - 1) {
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
                            Math.min(a.start + offsetb, area.getText().length()-1),
                            painter);
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }


        private static String[] readXML(String path) {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            try {
                dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(new File(path));
                // optional, but recommended
                // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
                doc.getDocumentElement().normalize();
                // get <artivlce>
                NodeList list = doc.getElementsByTagName("article");
                String[] documents = new String[list.getLength()];
                for (int i = 0; i < list.getLength(); i++) {
                    Node node = list.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;
                        documents[i] = element.getTextContent();
                    }
                }
                return documents;
            } catch (ParserConfigurationException | SAXException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private static void saveXML(String path, String[] documents,
                Stack<Annotation>[] annotations) {
            File file = new File(path);
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                writer.write("<articles>\n");
                for (int i = 0; i < documents.length; i++) {
                    String document = documents[i];
                    Stack<Annotation> ann = annotations[i];
                    writer.write("<article>\n");
                    writer.write(convertAnnotationsIntoTags(document, ann));
                    writer.write("</article>\n");
                }

                writer.write("</articles>");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private static String convertAnnotationsIntoTags(String text, Stack<Annotation> annotations) {
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
