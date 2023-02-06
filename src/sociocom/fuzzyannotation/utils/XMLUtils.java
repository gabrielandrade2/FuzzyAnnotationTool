package sociocom.fuzzyannotation.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class XMLUtils {

    private XMLUtils() {
        // Private constructor to prevent instantiation
    }

    public static List<String> readXML(Path path) {
        try {
            List<String> documents = new ArrayList<>();
            List<String> lines = Files.readAllLines(path);
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

    public static void saveXML(String path, List<String> documents) {
        File file = new File(path);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<articles>\n");
            for (String document : documents) {
                writer.write("<article>\n");
                writer.write(document);
                writer.write("\n</article>\n");
            }
            writer.write("</articles>\n");
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
