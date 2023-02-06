package sociocom.fuzzyannotation.ui.annotation;

import sociocom.fuzzyannotation.Annotation;
import sociocom.fuzzyannotation.GradientHighlighter;
import sociocom.fuzzyannotation.utils.XMLUtils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.Highlighter;
import javax.swing.text.NumberFormatter;

public abstract class BaseAnnotationUI {

    private static final String[] tagOptions = {"C"};

    // UI elements
    protected final JFrame frame;
    protected final JTextField docNumInputField;
    protected final JTextArea textArea;
    protected final JButton nextButton;
    protected final JButton prevButton;
    protected final JButton undoButton;
    protected final JComboBox<String> tagComboBox;

    // Annotation data
    protected List<List<Annotation>> storedAnnotations;
    protected List<String> documents;
    protected List<Annotation> annotations;
    protected Stack<Annotation> undoStack = new Stack<>();
    protected int documentNumber = 0;

    // Highlighting
    protected Highlighter highlighter;
    protected GradientHighlighter painter;
    protected final Random random = new Random();

    public BaseAnnotationUI(List<String> documents, List<List<Annotation>> storedAnnotations,
            String title) {
        this.documents = documents;
        this.storedAnnotations = storedAnnotations;

        // Create UI
        frame = new JFrame(title);

        // Text Area
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setBounds(10, 10, 1000, 600);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(false);
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
        save.addActionListener(this::saveAction);
        tempPanel.add(save);
        lowerPanel.add(tempPanel, BorderLayout.SOUTH);

        configureElements();

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

    // Override this method to configure UI elements
    protected abstract void configureElements();

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

    protected abstract void annotateAll();

    private void saveAction(ActionEvent e) {
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

    private void save(String path, List<String> documents,
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

    protected abstract String convertAnnotationsIntoTags(String document,
            List<Annotation> annotations);

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
}
