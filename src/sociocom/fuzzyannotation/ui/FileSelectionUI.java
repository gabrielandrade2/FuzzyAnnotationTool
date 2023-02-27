package sociocom.fuzzyannotation.ui;

import sociocom.fuzzyannotation.Main;
import sociocom.fuzzyannotation.WindowType;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;

public class FileSelectionUI {

    private final JFrame frame;
    private final JButton openButton;
    private final JTextField fileNameField;
    private final JCheckBox autoSaveCheckBox;
    private ButtonGroup buttonGroup;

    public FileSelectionUI() {
        frame = new JFrame("File Selection");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new FlowLayout());
        JLabel title = new JLabel("Fuzzy Annotation Tool");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        titlePanel.add(title);
        JLabel version = new JLabel("v0.2.3");
        titlePanel.add(version);

        JPanel panel = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setAlignment(FlowLayout.CENTER);
        panel.setLayout(layout);
        panel.setBorder(new EmptyBorder(20, 10, 10, 10));
        addRadioButtons(panel);

        JPanel lowerPanel = new JPanel();
        lowerPanel.setLayout(new GridLayout(3, 1));
        JPanel filePanel = new JPanel();
        filePanel.setLayout(new FlowLayout());
        JButton selectFileButton = new JButton("Select File");
        selectFileButton.addActionListener(this::chooseFile);
        fileNameField = new JTextField();
        fileNameField.setPreferredSize(new Dimension(350, 20));
        fileNameField.setEditable(false);
        filePanel.add(selectFileButton);
        filePanel.add(fileNameField);
        lowerPanel.add(filePanel);

        openButton = new JButton("Start");
        openButton.addActionListener(this::openFile);
        openButton.setEnabled(false);
        autoSaveCheckBox = new JCheckBox(
                "Auto Save? (Saves annotations automatically when switching documents)");
        autoSaveCheckBox.setSelected(true);
        lowerPanel.add(autoSaveCheckBox);
        lowerPanel.add(openButton);

        // Add elements to Frame
        frame.add(titlePanel, BorderLayout.NORTH);
        frame.add(panel, BorderLayout.CENTER);
        frame.add(lowerPanel, BorderLayout.SOUTH);

        frame.setSize(500, 250);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    private void addRadioButtons(JPanel panel) {
        buttonGroup = new ButtonGroup();
        for (WindowType type : WindowType.values()) {
            JRadioButton checkBox = new JRadioButton(type.getName());
            checkBox.setActionCommand(type.toString());
            panel.add(checkBox);
            buttonGroup.add(checkBox);
        }
        buttonGroup.setSelected(buttonGroup.getElements().nextElement().getModel(), true);
    }

    private void chooseFile(ActionEvent e) {
        Path file = fileChooser();
        if (file == null) {
            JOptionPane.showMessageDialog(null, "No file selected");
            if (fileNameField.getText().isEmpty()) {
                openButton.setEnabled(false);
            }
            return;
        }
        fileNameField.setText(file.toString());
        openButton.setEnabled(true);
    }

    private void openFile(ActionEvent e) {
        Path file = new File(fileNameField.getText()).toPath();
        if (file == null) {
            JOptionPane.showMessageDialog(null, "No file selected");
            return;
        }
        if (!file.toFile().exists()) {
            JOptionPane.showMessageDialog(null, "File does not exist");
            return;
        }

        if (autoSaveCheckBox.isSelected()) {
            if (JOptionPane.showConfirmDialog(null,
                    "Auto-save is selected. This will overwrite the original file when saving.\n" +
                            "Do you want to proceed?", "Select an Option...",
                    JOptionPane.YES_NO_OPTION) != 0) {
                return;
            }
        }

        frame.dispose();
        String command = buttonGroup.getSelection().getActionCommand();
        Main.openWindow(WindowType.valueOf(command), file, autoSaveCheckBox.isSelected());
    }

    private Path fileChooser() {
        FileDialog fd = new FileDialog(frame, "Choose a file", FileDialog.LOAD);
        fd.setDirectory(FileSystemView.getFileSystemView().getHomeDirectory().toString());
        fd.setFile("*.xml;*.txt");
        fd.setFilenameFilter(
                (File dir, String name) -> name.endsWith(".xml") || name.endsWith(".txt"));
        fd.setVisible(true);
        String directory = fd.getDirectory();
        String file = fd.getFile();
        if (directory == null || file == null) {
            return null;
        }
        return Paths.get(directory).resolve(file);
    }
}
