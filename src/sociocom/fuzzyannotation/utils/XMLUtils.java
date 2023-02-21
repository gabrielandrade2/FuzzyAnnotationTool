package sociocom.fuzzyannotation.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
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
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
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

    public static void saveXML(String path, List<String> documents) throws IOException {
        try (OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(path),
                StandardCharsets.UTF_8)) {
            os.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            os.write("<articles>\n");
            for (String document : documents) {
                os.write("<article>\n");
                os.write(document);
                os.write("\n</article>\n");
            }
            os.write("</articles>\n");
            os.flush();
        }
    }
}
