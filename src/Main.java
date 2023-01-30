import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Stack;
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
import javax.swing.text.NumberFormatter;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class Main {

    public record Annotation(int start, int end, String tag)  {}

    public static class TextAreaExample {

        private final JTextField textField;
        private final JTextArea area;
        private final JButton next;
        private final JButton prev;
        private String[] documents = null;
        private int i = 0;
        private Stack<Annotation> annotations = new Stack<>();

        TextAreaExample(String file) {
            documents = readXML(file);
            JFrame f = new JFrame();
            area = new JTextArea(documents[i]);
            area.setEditable(false);
            area.setBounds(10, 10, 600, 300);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);

            JPanel panel1 = new JPanel();
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
            panel1.add(comboBox);
            panel1.add(label);
            panel1.add(textField);
            label = new JLabel("of " + documents.length);
            panel1.add(label);

            next = new JButton("Next");
            next.addActionListener(e -> {
                changeText(i + 1);
            });
            JButton reset = new JButton("Reset");
            reset.addActionListener(e -> {
                while (!annotations.isEmpty()) {
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

            JPanel panel = new JPanel(new FlowLayout());
            panel.add(prev);
            panel.add(undo);
            panel.add(reset);
            panel.add(next);

            f.setSize(620, 320);
            f.setLayout(new BorderLayout());
            f.add(panel1, BorderLayout.PAGE_START);
            f.add(area, BorderLayout.CENTER);
            f.add(panel, BorderLayout.PAGE_END);
            f.setVisible(true);
            area.addMouseListener(new MouseAdapter() {
                public void mouseReleased(MouseEvent e) {

                    if (e.getButton() == MouseEvent.BUTTON1) {
                        int start = area.getSelectionStart();
                        int end = area.getSelectionEnd();
                        String text = area.getText();

                        if (text.substring(start, end).strip().length() == 0) {
                            return;
                        }

                        String tag = (String) comboBox.getSelectedItem();
                        area.setText(text.substring(0, start) + "<" + tag + ">" +
                                text.substring(start, end) + "</" + tag + ">" +
                                text.substring(end));
                        annotations.push(new Annotation(start, end, tag));
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
            });        }

        private void undo(){
            if (!annotations.isEmpty()) {
                Annotation annotation = annotations.pop();
                area.replaceRange("", annotation.start, annotation.start + annotation.tag.length() + 2);
                area.replaceRange("", annotation.end, annotation.end + annotation.tag.length() + 3);
            }
        }

        private void changeText(int newDoc){
            if (newDoc > documents.length - 1 || newDoc < 0) {
                return;
            }
            i = newDoc;
            area.setText(documents[i]);
            textField.setText(String.valueOf(i + 1));
            if (i == 0) {
                prev.setEnabled(false);
            }
            else {
                prev.setEnabled(true);
            }

            if (i == documents.length - 1) {
                next.setEnabled(false);
            }
            else {
                next.setEnabled(true);
            }
        }

        private static String[] readXML(String path){
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

    }


    public static String fileChooser(){
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
