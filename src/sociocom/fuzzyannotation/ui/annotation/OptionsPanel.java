package sociocom.fuzzyannotation.ui.annotation;

import sociocom.fuzzyannotation.ui.GradientHighlighter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;

public class OptionsPanel extends JFrame {

    private final BaseAnnotationUI annotationUI;

    public OptionsPanel(BaseAnnotationUI annotationUI) {
        super("Options");
        this.annotationUI = annotationUI;
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        JLabel title = new JLabel("Options");
        title.setFont(new Font("Serif", Font.BOLD, 14));

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
                GradientHighlighter.COLORS.keySet().toArray(new String[0]));
        highlighterColorComboBox.addActionListener(this::setHighlighterColor);

        JLabel fuzzinessLabel = new JLabel("Fuzziness");
        JSlider fuzzinessSlider = new JSlider(JSlider.HORIZONTAL, -128, 0,
                0);
        fuzzinessSlider.addChangeListener(this::setFuzziness);

        setLayout(new BorderLayout());
        JPanel tempPanel = new JPanel();
        tempPanel.setLayout(new BorderLayout());
        tempPanel.add(title, BorderLayout.CENTER);
        add(tempPanel, BorderLayout.NORTH);

        if (annotationUI instanceof HighlightAnnotationUI) {
            highlighterMinSpanLabel.setEnabled(false);
            highlighterMinSpanSlider.setEnabled(false);
            highlighterMaxSpanLabel.setEnabled(false);
            highlighterMaxSpanSlider.setEnabled(false);
        }

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(5, 2));
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
        add(centerPanel, BorderLayout.CENTER);

        setSize(new Dimension(350, 180));
        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(false);
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

    private void setHighlighterColor(ActionEvent actionEvent) {
        JComboBox<String> source = (JComboBox<String>) actionEvent.getSource();
        String color = (String) source.getSelectedItem();
        annotationUI.setHighlighterColor(GradientHighlighter.COLORS.get(color));
    }
}
