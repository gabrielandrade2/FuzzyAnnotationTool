package sociocom.fuzzyannotation;

import sociocom.fuzzyannotation.ui.FileSelectionUI;
import sociocom.fuzzyannotation.ui.PointWiseAnnotationUI;
import sociocom.fuzzyannotation.utils.XMLUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

public class Main {

    public static void main(String[] args) {
        new FileSelectionUI();
    }


    public static void openWindow(WindowType type, Path file) {
        loadDocument(file);
    }

    private static void loadDocument(Path file) {
        //Load documents
        List<String> documents = XMLUtils.readXML(file);
        List<List<Annotation>> storedAnnotations = convertTagsIntoAnnotations(documents);
        removeAnnotations(documents);
        new PointWiseAnnotationUI(documents, storedAnnotations);
    }

    public static Path fileChooser() {
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        FileFilter filter = new FileNameExtensionFilter("Text files", "txt", "xml");
        jfc.addChoosableFileFilter(filter);
        jfc.setFileFilter(filter);
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.setAcceptAllFileFilterUsed(false);
        int returnValue = jfc.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            System.out.println(selectedFile.getAbsolutePath());
            return selectedFile.toPath();
        }
        return null;
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
