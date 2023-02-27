package sociocom.fuzzyannotation;

import sociocom.fuzzyannotation.ui.FileSelectionUI;
import sociocom.fuzzyannotation.ui.annotation.HighlightAnnotationUI;
import sociocom.fuzzyannotation.ui.annotation.PointWiseAnnotationUI;
import sociocom.fuzzyannotation.utils.XMLUtils;

import java.nio.file.Path;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.UnsupportedLookAndFeelException;

public class Main {

    public static void main(String[] args)
            throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
//        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        new FileSelectionUI();
    }

    public static void openWindow(WindowType type, Path file, boolean autoSave) {
        try {
            loadDocument(type, file, autoSave);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to open file: " + file.toString() + "\n" +
                    e.getMessage());
            new FileSelectionUI();
        }
    }

    private static void loadDocument(WindowType type, Path file, boolean autoSave) {
        //Load documents
        List<String> documents = XMLUtils.readXML(file);
        List<List<Annotation>> storedAnnotations;
        switch (type) {
            case PointWiseAnnotationUI:
                storedAnnotations = PointWiseAnnotationUI.convertTagsIntoAnnotations(documents);
                removeAnnotations(documents);
                new PointWiseAnnotationUI(documents, storedAnnotations, autoSave, file);
                break;
            case HighlightAnnotationUI:
                storedAnnotations = HighlightAnnotationUI.convertTagsIntoAnnotations(documents);
                removeAnnotations(documents);
                new HighlightAnnotationUI(documents, storedAnnotations, autoSave, file);
                break;
        }
    }

    private static void removeAnnotations(List<String> documents) {
        for (int i = 0; i < documents.size(); i++) {
            String text = documents.get(i);
            text = text.replaceAll("<[^>]+>", "")
                    .replaceAll("</?\\w( [^>]*)?>", "");
            documents.set(i, text);
        }
    }
}
