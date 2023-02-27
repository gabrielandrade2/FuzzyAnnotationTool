package sociocom.fuzzyannotation.ui.annotation;

import sociocom.fuzzyannotation.Annotation;
import sociocom.fuzzyannotation.Main;
import sociocom.fuzzyannotation.ui.GradientHighlighter;
import sociocom.fuzzyannotation.utils.XMLUtils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.Highlighter;
import javax.swing.text.NumberFormatter;

public abstract class BaseAnnotationUI {

    private static final String[] tagOptions = {"C"};
    private static final Font FONT;
    private static final int DEFAULT_FONT_SIZE = 14;

    static {
        try {
            FONT = Font.createFont(Font.TRUETYPE_FONT, Main.class.getClassLoader()
                            .getResourceAsStream("fonts/YuGothM.ttc"))
                    .deriveFont(Font.PLAIN, DEFAULT_FONT_SIZE);
        } catch (FontFormatException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // UI elements
    protected final JFrame frame;
    protected final JTextField docNumInputField;
    protected final JTextArea textArea;
    protected final JButton nextButton;
    protected final JButton prevButton;
    protected final JButton undoButton;
    protected final JComboBox<String> tagComboBox;
    private final Preferences preferences;
    private final boolean autoSave;
    private final Path file;

    // Annotation data
    protected List<List<Annotation>> storedAnnotations;
    protected List<String> documents;
    protected List<Annotation> annotations;
    protected Stack<Annotation> undoStack = new Stack<>();
    protected int documentNumber = 0;

    // Highlighting
    protected Highlighter highlighter;

    public GradientHighlighter getPainter() {
        return painter;
    }

    protected GradientHighlighter painter;
    protected final Random random = new Random();
    private int fuzzyWeight = 2;
    private int minHighlightSpan = 3;
    private int maxHighlightSpan = 15;

    // Options Panel
    private JFrame optionsPanel;

    public BaseAnnotationUI(List<String> documents, List<List<Annotation>> storedAnnotations,
            boolean autoSave, Path file, String title) {
        this.documents = documents;
        this.storedAnnotations = storedAnnotations;
        this.autoSave = autoSave;
        this.file = file;

        preferences = Preferences.userRoot();

        // Create UI
        frame = new JFrame(title);

        // Text Area
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setBounds(10, 10, 1000, 600);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(false);
        textArea.getDocument().addDocumentListener(new DocumentUpdater());
        String fontType = preferences.get("fontType", null);
        Font font = fontType == null ? FONT : new Font(fontType, Font.PLAIN, DEFAULT_FONT_SIZE);
        textArea.setFont(font);
        setFontSize(preferences.getFloat("fontSize", DEFAULT_FONT_SIZE));
        JScrollPane scrollPane = new JScrollPane(textArea);

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
        tempPanel = new JPanel();
        JButton optionsButton = new JButton("Options");
        optionsButton.addActionListener(e -> {
            if (optionsPanel != null) {
                optionsPanel.dispose();
            }
            optionsPanel = new OptionsPanel(this);
        });
        tempPanel.add(optionsButton);
        upperPanel.add(tempPanel);

        // Create Buttons
        nextButton = new JButton("Next");
        nextButton.addActionListener(e -> changeText(documentNumber + 1));

        undoButton = new JButton("Undo");
        undoButton.addActionListener(e -> undo());
        undoButton.setEnabled(false);

        prevButton = new JButton("Previous");
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
        JButton save = new JButton("Export file");
        save.addActionListener(this::saveAction);
        tempPanel.add(save);
        lowerPanel.add(tempPanel, BorderLayout.SOUTH);

        configureElements();

        // Add elements to Frame
        frame.setSize(1000, 600);
        frame.setLayout(new BorderLayout());
        frame.add(upperPanel, BorderLayout.PAGE_START);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(lowerPanel, BorderLayout.PAGE_END);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // load Preferences
        loadPreferences();

        // Select first document
        changeText(documentNumber);
    }

    public void setFontSize(float fontSize) {
        textArea.setFont(textArea.getFont().deriveFont(fontSize));
        preferences.putFloat("fontSize", fontSize);
    }

    // Override this method to configure UI elements
    protected abstract void configureElements();

    private void loadPreferences() {
        fuzzyWeight = preferences.getInt("fuzzyWeight", fuzzyWeight);
        minHighlightSpan = preferences.getInt("minHighlightSpan", minHighlightSpan);
        maxHighlightSpan = preferences.getInt("maxHighlightSpan", maxHighlightSpan);
        painter.setFuzziness(preferences.getInt("fuzziness", 0));
        painter.setColor(preferences.get("color", "Blue"));
    }

    private void undo() {
        if (!undoStack.isEmpty()) {
            Annotation annotation = undoStack.pop();
            annotations.remove(annotation);
            annotateAll();
            undoButton.setEnabled(!undoStack.isEmpty());
            if (autoSave) {
                save(file.toString(), false);
            }
        }
    }

    private void changeText(int newDoc) {
        if (newDoc > documents.size() - 1 || newDoc < 0) {
            return;
        }

        if (autoSave) {
            save(file.toString(), false);
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

        textArea.setCaretPosition(0);
        prevButton.setEnabled(documentNumber != 0);
        nextButton.setEnabled(documentNumber != documents.size() - 1);
    }

    public abstract void annotateAll();

    public int getFuzzyWeight() {
        return this.fuzzyWeight;
    }

    public void setFuzzyWeight(int fuzzyWeight) {
        updateAnnotationSpan(fuzzyWeight - this.fuzzyWeight);
        this.fuzzyWeight = fuzzyWeight;
        preferences.putInt("fuzzyWeight", fuzzyWeight);
        annotateAll();
    }

    public int getMinHighlightSpan() {
        return Math.min(minHighlightSpan, maxHighlightSpan);
    }

    public void setMinHighlightSpan(int minHighlightSpan) {
        this.minHighlightSpan = minHighlightSpan;
        preferences.putInt("minHighlightSpan", minHighlightSpan);
        annotateAll();
    }

    public int getMaxHighlightSpan() {
        return Math.max(maxHighlightSpan, minHighlightSpan);
    }

    public void setMaxHighlightSpan(int maxHighlightSpan) {
        this.maxHighlightSpan = maxHighlightSpan;
        preferences.putInt("maxHighlightSpan", maxHighlightSpan);
        annotateAll();
    }


    public void setHighlighterColor(String color) {
        painter.setColor(color);
        preferences.put("color", color);
        annotateAll();
    }

    public void setFuzziness(int fuzz) {
        painter.setFuzziness(fuzz);
        preferences.putInt("fuzziness", fuzz);
        annotateAll();
    }

    public int getFuzziness() {
        return painter.getFuzziness();
    }

    public int getFontSize() {
        return textArea.getFont().getSize();
    }

    public String getFontType() {
        return textArea.getFont().getFontName();
    }

    public void setFontType(String fontType) {
        try {
            if (fontType == null) {
                textArea.setFont(FONT.deriveFont(Font.PLAIN, textArea.getFont().getSize()));
                preferences.remove("fontType");
            } else {
                textArea.setFont(new Font(fontType, Font.PLAIN, textArea.getFont().getSize()));
                preferences.put("fontType", fontType);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Font not found", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    protected void updateAnnotationSpan(int offset) {
        for (Annotation annotation : annotations) {
            annotation.setSpan(annotation.getStartSpan() - offset,
                    annotation.getEndSpan() + offset);
        }
    }

    private void saveAction(ActionEvent e) {
        FileDialog fd = new FileDialog(frame, "Specify a file to save", FileDialog.SAVE);
        fd.setDirectory(FileSystemView.getFileSystemView().getHomeDirectory().toString());
        fd.setFile("annotation.xml");
        fd.setVisible(true);
        String directory = fd.getDirectory();
        String file = fd.getFile();
        if (directory == null || file == null) {
            return;
        }
        save(Paths.get(directory).resolve(file).toAbsolutePath().toString(), true);
    }

    private void save(String path, boolean showMessage) {
        List<String> taggedDocuments = new ArrayList<>();
        for (int i = 0; i < documents.size(); i++) {
            String document = documents.get(i);
            List<Annotation> ann = storedAnnotations.get(i);
            taggedDocuments.add(convertAnnotationsIntoTags(document, ann));
        }
        try {
            XMLUtils.saveXML(path, taggedDocuments);
            if (showMessage) {
                JOptionPane.showMessageDialog(null, "Saved to " + path);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error while saving file", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    protected final void addAnnotation(Annotation annotation) {
        annotations.add(annotation);
        undoStack.push(annotation);
        undoButton.setEnabled(true);
        if (autoSave) {
            save(file.toString(), false);
        }
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
