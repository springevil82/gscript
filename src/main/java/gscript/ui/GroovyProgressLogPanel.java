package gscript.ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Script execution progress panel with two labels (progress + summary), progress bar, log text pane and cancellation button
 */
public final class GroovyProgressLogPanel extends JPanel {

    private final JLabel progressLabel;
    private final JProgressBar progressBar;
    private final JLabel progressDetailLabel;
    private final JTextPane logEditor;
    private final Style stylePlain;
    private final Style styleWarn;
    private final Style styleError;
    private final Style styleBold;

    private boolean cancelled = false;
    private final JButton cancelButton;

    public GroovyProgressLogPanel() {
        setLayout(new BorderLayout());
        setBorder(new CompoundBorder(new LineBorder(Color.GRAY), new EmptyBorder(5, 5, 5, 5)));

        progressLabel = new JLabel("<html>&nbsp;");
        progressBar = new JProgressBar();
        progressDetailLabel = new JLabel("<html>&nbsp;");

        logEditor = new JTextPane();
        logEditor.setEditable(false);
        logEditor.getCaret().setVisible(true);

        DefaultCaret caret = (DefaultCaret) logEditor.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        stylePlain = logEditor.addStyle("plain", null);
        StyleConstants.setForeground(stylePlain, Color.black);
        styleBold = logEditor.addStyle("bold", null);
        StyleConstants.setForeground(styleBold, Color.black);
        StyleConstants.setBold(styleBold, true);
        styleWarn = logEditor.addStyle("warn", null);
        StyleConstants.setForeground(styleWarn, Color.red.darker());
        styleError = logEditor.addStyle("error", null);
        StyleConstants.setForeground(styleError, Color.red);

        cancelButton = new JButton(new AbstractAction("Cancel") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelled = true;
            }
        });

        final JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        progressPanel.add(progressLabel, BorderLayout.NORTH);
        progressPanel.add(progressBar, BorderLayout.CENTER);
        progressPanel.add(progressDetailLabel, BorderLayout.SOUTH);

        final JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.add(new JScrollPane(logEditor));

        final JPanel buttonPanel = new JPanel(new BorderLayout(5, 5));
        buttonPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        buttonPanel.add(cancelButton, BorderLayout.EAST);


        add(progressPanel, BorderLayout.NORTH);
        add(logPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public JTextPane getLogEditor() {
        return logEditor;
    }

    public JButton getCancelButton() {
        return cancelButton;
    }

    public void appendLog(String logType, String message) {
        if (message == null)
            return;

        final StyledDocument doc = logEditor.getStyledDocument();
        try {
            switch (logType) {
                case "WARN":
                    doc.insertString(doc.getLength(), message + "\n", styleWarn);
                    break;
                case "ERROR":
                case "FATAL":
                    doc.insertString(doc.getLength(), message + "\n", styleError);
                    break;
                default:
                    if (message.startsWith(" -- "))
                        doc.insertString(doc.getLength(), message + "\n", styleBold);
                    else
                        doc.insertString(doc.getLength(), message + "\n", stylePlain);
                    break;
            }
        } catch (BadLocationException ignored) {
        }
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setProgressText(String text) {
        progressLabel.setText(text);
    }

    public void setProgressDetailText(String text) {
        progressDetailLabel.setText(text);
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

}
