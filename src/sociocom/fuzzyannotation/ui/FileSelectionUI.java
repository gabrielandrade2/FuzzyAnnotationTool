package sociocom.fuzzyannotation.ui;

import sociocom.fuzzyannotation.Main;
import sociocom.fuzzyannotation.WindowType;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Path;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

public class FileSelectionUI {

    private final JFrame frame;

    public FileSelectionUI() {
        frame = new JFrame("File Selection");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1));

        addRadioButtons(panel);

        JButton button = new JButton("Select File");
        button.addActionListener(this::buttonPressed);

        // Add elements to Frame
        frame.add(panel, BorderLayout.NORTH);
        frame.add(button, BorderLayout.SOUTH);
        frame.setSize(200, 150);
        frame.setVisible(true);
    }

    private void addRadioButtons(JPanel panel) {
        ButtonGroup group = new ButtonGroup();
        for (WindowType type : WindowType.values()) {
            JRadioButton checkBox = new JRadioButton(type.toString());
            panel.add(checkBox);
            group.add(checkBox);
        }
    }

    private void buttonPressed(ActionEvent e) {
        Path file = fileChooser();
        if (file == null) {
            JOptionPane.showMessageDialog(null, "No file selected");
            return;
        }
        frame.dispose();
        Main.openWindow(WindowType.PointWiseAnnotationUI, file);
    }

    private static Path fileChooser() {
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
}
