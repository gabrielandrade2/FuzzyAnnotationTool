package sociocom.fuzzyannotation;

import sociocom.fuzzyannotation.ui.FileSelectionUI;
import sociocom.fuzzyannotation.ui.annotation.HighlightAnnotationUI;
import sociocom.fuzzyannotation.ui.annotation.PointWiseAnnotationUI;
import sociocom.fuzzyannotation.utils.XMLUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

public class Main {

    public static void main(String[] args) {
        new FileSelectionUI();
    }

    public static void openWindow(WindowType type, Path file) {
        try {
            loadDocument(type, file);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to open file: " + file.toString() + "\n" +
                    e.getMessage());
            new FileSelectionUI();
        }
    }

    private static void loadDocument(WindowType type, Path file) {
        //Load documents
        List<String> documents = XMLUtils.readXML(file);
        List<List<Annotation>> storedAnnotations = convertTagsIntoAnnotations(documents);
        removeAnnotations(documents);
        switch (type) {
            case PointWiseAnnotationUI:
                new PointWiseAnnotationUI(documents, storedAnnotations);
                break;
            case HighlightAnnotationUI:
                new HighlightAnnotationUI(documents, storedAnnotations);
                break;

        }
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

    private static void removeAnnotations(List<String> documents) {
        for (int i = 0; i < documents.size(); i++) {
            String text = documents.get(i);
            text = text.replaceAll("<[^>]+>", "")
                    .replaceAll("</?\\w( [^>]*)?>", "");
            documents.set(i, text);
        }
    }
}
