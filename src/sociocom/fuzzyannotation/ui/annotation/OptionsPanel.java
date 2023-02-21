package sociocom.fuzzyannotation.ui.annotation;

import sociocom.fuzzyannotation.ui.GradientHighlighter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;

public class OptionsPanel extends JFrame {

    private static final int MIN_FONT_SIZE = 8;
    private static final int MAX_FONT_SIZE = 24;

    private final BaseAnnotationUI annotationUI;
    private final JSlider fontSizeSlider;
    private final JTextField fontSizeTextField;

    public OptionsPanel(BaseAnnotationUI annotationUI) {
        super("Options");
        this.annotationUI = annotationUI;
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(new Dimension(400, 320));

        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setLayout(new BorderLayout());

        JLabel title = new JLabel("Options");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18));

        JLabel fuzzyWeightLabel = new JLabel("Fuzzy Weight");
        JSlider fuzzyWeightSlider = new JSlider(JSlider.HORIZONTAL, 1, 20,
                annotationUI.getFuzzyWeight());
        fuzzyWeightSlider.addChangeListener(this::setFuzzyWeight);

        JLabel highlighterMinSpanLabel = new JLabel("Minimum Highlight Size");
        JSlider highlighterMinSpanSlider = new JSlider(JSlider.HORIZONTAL, 2, 20,
                annotationUI.getMinHighlightSpan());
        highlighterMinSpanSlider.addChangeListener(this::setHighlighterMinSpan);

        JLabel highlighterMaxSpanLabel = new JLabel("Maximum Highlight Size");
        JSlider highlighterMaxSpanSlider = new JSlider(JSlider.HORIZONTAL, 2, 30,
                annotationUI.getMaxHighlightSpan());
        highlighterMaxSpanSlider.addChangeListener(this::setHighlighterMaxSpan);

        JLabel highlighterColorLabel = new JLabel("Highlighter Color");
        JComboBox<String> highlighterColorComboBox = new JComboBox<>(
                GradientHighlighter.COLORS.toArray(new String[0]));
        highlighterColorComboBox.setSelectedIndex(annotationUI.getPainter().getColorIndex());
        highlighterColorComboBox.addActionListener(this::setHighlighterColor);

        JLabel fuzzinessLabel = new JLabel("Fuzziness");
        JSlider fuzzinessSlider = new JSlider(JSlider.HORIZONTAL, -128, 0,
                annotationUI.getFuzziness());
        fuzzinessSlider.addChangeListener(this::setFuzziness);

        JLabel fontTypeLabel = new JLabel("Font Type");
        List<String> fonts = new ArrayList<>();
        fonts.add("Default");
        fonts.addAll(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        JComboBox<String> fontTypeComboBox = new JComboBox<>(fonts.toArray(new String[0]));
        int index = fonts.indexOf(annotationUI.getFontType());
        fontTypeComboBox.setSelectedIndex(index == -1 ? 0 : index);
        fontTypeComboBox.addActionListener(this::setFontType);

        JLabel fontSizeLabel = new JLabel("Font Size");
        fontSizeSlider = new JSlider(JSlider.HORIZONTAL, MIN_FONT_SIZE, MAX_FONT_SIZE,
                annotationUI.getFontSize());
        fontSizeSlider.addChangeListener(this::setFontSizeSlider);
        fontSizeTextField = new JTextField();
        fontSizeTextField.setText(String.valueOf(fontSizeSlider.getValue()));
        fontSizeTextField.addActionListener(this::setFontSizeTextField);
        JPanel fontSizePanel = new JPanel();
        fontSizePanel.setLayout(new BorderLayout());
        fontSizePanel.add(fontSizeSlider, BorderLayout.CENTER);
        fontSizePanel.add(fontSizeTextField, BorderLayout.EAST);

        JPanel tempPanel = new JPanel();
        tempPanel.setLayout(new BorderLayout());
        tempPanel.add(title, BorderLayout.CENTER);
        tempPanel.add(new JLabel(" "), BorderLayout.SOUTH);
        mainPanel.add(tempPanel, BorderLayout.NORTH);

        JButton importPrefs = new JButton("Import Preferences");
        importPrefs.addActionListener(
                actionEvent -> JOptionPane.showMessageDialog(null, "Not implemented yet"));
        JButton exportPrefs = new JButton("Export Preferences");
        exportPrefs.addActionListener(
                actionEvent -> JOptionPane.showMessageDialog(null, "Not implemented yet"));

        if (annotationUI instanceof HighlightAnnotationUI) {
            highlighterMinSpanLabel.setEnabled(false);
            highlighterMinSpanSlider.setEnabled(false);
            highlighterMaxSpanLabel.setEnabled(false);
            highlighterMaxSpanSlider.setEnabled(false);
        }

        if (annotationUI instanceof PointWiseAnnotationUI) {
            fuzzinessLabel.setEnabled(false);
            fuzzinessSlider.setEnabled(false);
        }

        JPanel centerPanel = new JPanel();
        GridLayout layout = new GridLayout(8, 2);
        centerPanel.setLayout(layout);
        centerPanel.add(highlighterColorLabel);
        centerPanel.add(highlighterColorComboBox);
        centerPanel.add(fuzzinessLabel);
        centerPanel.add(fuzzinessSlider);
        centerPanel.add(fuzzyWeightLabel);
        centerPanel.add(fuzzyWeightSlider);
        centerPanel.add(highlighterMinSpanLabel);
        centerPanel.add(highlighterMinSpanSlider);
        centerPanel.add(highlighterMaxSpanLabel);
        centerPanel.add(highlighterMaxSpanSlider);
        centerPanel.add(fontTypeLabel);
        centerPanel.add(fontTypeComboBox);
        centerPanel.add(fontSizeLabel);
        centerPanel.add(fontSizePanel);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        JButton close = new JButton("Close");
        close.addActionListener(actionEvent -> {
            setVisible(false);
            dispose();
        });
        bottomPanel.add(close, BorderLayout.EAST);
        bottomPanel.add(importPrefs, BorderLayout.WEST);
        bottomPanel.add(exportPrefs, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(false);
    }

    private void setFontType(ActionEvent actionEvent) {
        JComboBox<String> source = (JComboBox<String>) actionEvent.getSource();
        String fontType = (String) source.getSelectedItem();
        if (fontType.equals("Default")) {
            fontType = null;
        }
        annotationUI.setFontType(fontType);
    }

    private void setFontSizeTextField(ActionEvent actionEvent) {
        try {
            int size = Integer.parseInt(fontSizeTextField.getText());
            if (size < MIN_FONT_SIZE) {
                size = MIN_FONT_SIZE;
            } else if (size > MAX_FONT_SIZE) {
                size = MAX_FONT_SIZE;
            }
            fontSizeSlider.setValue(size);
            annotationUI.setFontSize(size);
        } catch (NumberFormatException e) {
            fontSizeTextField.setText(String.valueOf(fontSizeSlider.getValue()));
        }
    }

    private void setFontSizeSlider(ChangeEvent changeEvent) {
        JSlider source = (JSlider) changeEvent.getSource();
        int size = source.getValue();
        fontSizeTextField.setText(String.valueOf(size));
        if (!source.getValueIsAdjusting()) {
            annotationUI.setFontSize(size);
        }
    }

    private void setFuzziness(ChangeEvent changeEvent) {
        JSlider source = (JSlider) changeEvent.getSource();
        if (!source.getValueIsAdjusting()) {
            int size = source.getValue();
            annotationUI.setFuzziness(size);
        }
    }

    private void setFuzzyWeight(ChangeEvent changeEvent) {
        JSlider source = (JSlider) changeEvent.getSource();
        if (!source.getValueIsAdjusting()) {
            int size = source.getValue();
            annotationUI.setFuzzyWeight(size);
        }
    }

    private void setHighlighterMinSpan(ChangeEvent changeEvent) {
        JSlider source = (JSlider) changeEvent.getSource();
        if (!source.getValueIsAdjusting()) {
            int size = source.getValue();
            annotationUI.setMinHighlightSpan(size);
        }
    }

    private void setHighlighterMaxSpan(ChangeEvent changeEvent) {
        JSlider source = (JSlider) changeEvent.getSource();
        if (!source.getValueIsAdjusting()) {
            int size = source.getValue();
            annotationUI.setMaxHighlightSpan(size);
        }
    }

    @SuppressWarnings("unchecked")
    private void setHighlighterColor(ActionEvent actionEvent) {
        JComboBox<String> source = (JComboBox<String>) actionEvent.getSource();
        String color = (String) source.getSelectedItem();
        annotationUI.setHighlighterColor(color);
    }
}
