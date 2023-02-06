package sociocom.fuzzyannotation.ui;

import sociocom.fuzzyannotation.Annotation;
import sociocom.fuzzyannotation.GradientHighlighter;
import sociocom.fuzzyannotation.utils.XMLUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
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

public class PointWiseAnnotationUI extends BaseAnnotationUI {

    private static final String[] tagOptions = {"C"};

    // UI elements
    private final JFrame frame;
    private final JTextField docNumInputField;
    private final JTextArea textArea;
    private final JButton nextButton;
    private final JButton prevButton;
    private final JButton undoButton;
    private final JComboBox<String> tagComboBox;

    // Annotation data
    private List<List<Annotation>> storedAnnotations;
    private List<String> documents;
    private List<Annotation> annotations;
    private Stack<Annotation> undoStack = new Stack<>();
    private int documentNumber = 0;

    // Highlighting
    private final Highlighter highlighter;
    private final GradientHighlighter painter;

    public PointWiseAnnotationUI(List<String> documents, List<List<Annotation>> storedAnnotations) {
        this.documents = documents;
        this.storedAnnotations = storedAnnotations;

        // Create UI
        frame = new JFrame();

        // Text Area
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setBounds(10, 10, 1000, 600);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(false);
        textArea.setSelectionColor(null);
        highlighter = new DefaultHighlighter();
        painter = new GradientHighlighter(new Color(51, 153, 255, 128));
        textArea.setHighlighter(highlighter);
        textArea.addMouseListener(new MouseEventHandler());
        textArea.getDocument().addDocumentListener(new DocumentUpdater());

        // Create Upper panel
        JPanel upperPanel = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(layout.getHgap() + 50);
        upperPanel.setLayout(layout);
        tagComboBox = new JComboBox<>(tagOptions);
        tagComboBox.setPreferredSize(new Dimension(100, 30));
        NumberFormatter formatter = new NumberFormatter(NumberFormat.getInstance());
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(1);
        formatter.setMaximum(documents.size());
        docNumInputField = new JFormattedTextField(formatter);
        docNumInputField.setPreferredSize(new Dimension(50, 30));
        docNumInputField.setText(String.valueOf(documentNumber + 1));
        docNumInputField.addActionListener(
                e -> changeText(Integer.parseInt(docNumInputField.getText()) - 1));
        JPanel tempPanel = new JPanel();
        tempPanel.add(new JLabel("Tag:"));
        tempPanel.add(tagComboBox);
        upperPanel.add(tempPanel);
        tempPanel = new JPanel();
        tempPanel.add(new JLabel("Document"));
        tempPanel.add(docNumInputField);
        tempPanel.add(new JLabel("of " + documents.size()));
        upperPanel.add(tempPanel);

        // Create Buttons
        nextButton = new JButton("Next");
        nextButton.addActionListener(e -> changeText(documentNumber + 1));

        undoButton = new JButton("Undo");
        undoButton.addActionListener(e -> undo());
        undoButton.setEnabled(false);

        prevButton = new JButton("Prev");
        prevButton.setEnabled(false);
        prevButton.addActionListener(e -> changeText(documentNumber - 1));

        // Create lower panel
        JPanel lowerPanel = new JPanel(new BorderLayout());
        tempPanel = new JPanel(new FlowLayout());
        tempPanel.add(prevButton);
        tempPanel.add(undoButton);
        tempPanel.add(nextButton);
        lowerPanel.add(tempPanel, BorderLayout.NORTH);
        tempPanel = new JPanel(new FlowLayout());
        JButton save = new JButton("Save");
        save.addActionListener(new SaveAction());
        tempPanel.add(save);
        lowerPanel.add(tempPanel, BorderLayout.SOUTH);

        // Add elements to Frame
        frame.setSize(1000, 600);
        frame.setLayout(new BorderLayout());
        frame.add(upperPanel, BorderLayout.PAGE_START);
        frame.add(textArea, BorderLayout.CENTER);
        frame.add(lowerPanel, BorderLayout.PAGE_END);
        frame.setVisible(true);

        // Select first document
        changeText(documentNumber);
    }

    private void undo() {
        if (!undoStack.isEmpty()) {
            Annotation annotation = undoStack.pop();
            annotations.remove(annotation);
            annotateAll();
            undoButton.setEnabled(!undoStack.isEmpty());
        }
    }

    private void changeText(int newDoc) {
        if (newDoc > documents.size() - 1 || newDoc < 0) {
            return;
        }
        documentNumber = newDoc;
        if (storedAnnotations.size() <= documentNumber ||
                storedAnnotations.get(documentNumber) == null) {
            storedAnnotations.set(documentNumber, new Stack<>());
        }
        undoStack.clear();
        undoButton.setEnabled(false);
        annotations = storedAnnotations.get(documentNumber);
        textArea.setText(documents.get(documentNumber));
        docNumInputField.setText(String.valueOf(documentNumber + 1));

        annotateAll();

        if (documentNumber == 0) {
            prevButton.setEnabled(false);
        } else {
            prevButton.setEnabled(true);
        }

        if (documentNumber == documents.size() - 1) {
            nextButton.setEnabled(false);
        } else {
            nextButton.setEnabled(true);
        }
    }

    private void annotateAll() {
        highlighter.removeAllHighlights();
        try {
            for (Annotation a : annotations) {
                int start = Math.max(0, a.start() - 10);
                int offseta = textArea.getText().substring(start, a.start())
                        .indexOf("\n");
                if (offseta == -1) {
                    offseta = 0;
                }

                int end = Math.min(textArea.getText().length(), a.start() + 10);
                int offsetb = textArea.getText().substring(a.start(), end)
                        .indexOf("\n");
                if (offsetb == -1) {
                    offsetb = 10;
                }

                highlighter.addHighlight(Math.max(a.start() - 10 + offseta, 0),
                        Math.min(a.start() + offsetb, textArea.getText().length() - 1),
                        painter);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void save(String path, List<String> documents,
            List<List<Annotation>> annotations) {
        List<String> taggedDocuments = new ArrayList<>();
        for (int i = 0; i < documents.size(); i++) {
            String document = documents.get(i);
            List<Annotation> ann = annotations.get(i);
            taggedDocuments.add(convertAnnotationsIntoTags(document, ann));
        }
        XMLUtils.saveXML(path, taggedDocuments);
        new JOptionPane().showMessageDialog(null, "Saved to " + path);
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

    private class DocumentUpdater implements DocumentListener {

        @Override
        public void removeUpdate(DocumentEvent e) {
            documents.set(documentNumber, textArea.getText());
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            documents.set(documentNumber, textArea.getText());
        }

        @Override
        public void changedUpdate(DocumentEvent arg0) {
            documents.set(documentNumber, textArea.getText());
        }
    }

    private class SaveAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser jfc = new JFileChooser(
                    FileSystemView.getFileSystemView().getHomeDirectory());
            jfc.setDialogTitle("Specify a file to save");
            jfc.setSelectedFile(new File("annotation.xml"));
            jfc.setAcceptAllFileFilterUsed(false);
            jfc.setFileFilter(new FileNameExtensionFilter("XML file", "xml"));
            int returnValue = jfc.showSaveDialog(frame);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = jfc.getSelectedFile();
                System.out.println(selectedFile.getAbsolutePath());
                save(selectedFile.getAbsolutePath(), documents, storedAnnotations);
            }
        }
    }
}
