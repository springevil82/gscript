package gscript.factory.ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Script execution progress window with two labels (current + summary), progress bar and cancellation button
 */
public class GroovyProgressFrame {

    private final GroovyAutoclosableFrame frame;

    private final JLabel topLabel;
    private final JProgressBar progressBar;
    private final JLabel bottomLabel;
    private final JButton cancelButton;

    private boolean cancelled = false;

    public GroovyProgressFrame(boolean cancellationAllowed) {
        final JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(new CompoundBorder(new LineBorder(Color.GRAY), new EmptyBorder(5, 5, 5, 5)));

        final JPanel progressPanel = new JPanel(new BorderLayout());

        topLabel = new JLabel("<html>&nbsp;");
        progressBar = new JProgressBar();
        bottomLabel = new JLabel("<html>&nbsp;");
        cancelButton = new JButton(new AbstractAction("Cancel") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelled = true;
            }
        });

        progressPanel.add(topLabel, BorderLayout.NORTH);
        progressPanel.add(progressBar, BorderLayout.CENTER);
        progressPanel.add(bottomLabel, BorderLayout.SOUTH);

        final JPanel buttonPanel = new JPanel(new BorderLayout(5, 5));
        buttonPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        buttonPanel.add(cancelButton, BorderLayout.EAST);

        contentPanel.add(progressPanel, BorderLayout.CENTER);

        if (cancellationAllowed)
            contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame = new GroovyAutoclosableFrame();
        frame.setUndecorated(true);
        frame.setMinimumSize(new Dimension(400, 10));
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setContentPane(contentPanel);
        frame.pack();
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setTopLabelText(String text) {
        topLabel.setText(text);
    }

    public void setBottomLabelText(String text) {
        bottomLabel.setText(text);
    }

    public void setProgressMax(int maxValue) {
        progressBar.setIndeterminate(false);
        progressBar.setMaximum(maxValue);
    }

    public void moveProgress() {
        progressBar.setValue(progressBar.getValue() + 1);
    }

    public void setProgressIndeterminate(boolean indeterminate) {
        progressBar.setIndeterminate(indeterminate);
    }

    public void show() {
        frame.setVisible(true);
    }

    public void hide() {
        frame.dispose();
    }

    public GroovyAutoclosableFrame getFrame() {
        return frame;
    }
}
