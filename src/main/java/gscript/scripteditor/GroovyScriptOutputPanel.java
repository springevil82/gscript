package gscript.scripteditor;

import gscript.factory.document.RegExp;
import gscript.util.SystemUtils;

import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.regex.Matcher;

public final class GroovyScriptOutputPanel extends JPanel {

    private final JTextPane logEditor;
    private final Style styleTime;
    private final Style stylePlain;
    private final Style styleBold;
    private final Style styleWarn;
    private final Style styleInfo;
    private final Style styleError;
    private final Style styleFatal;

    public GroovyScriptOutputPanel() {
        setLayout(new BorderLayout());

        logEditor = new JTextPane();
        logEditor.setEditable(false);
        logEditor.getCaret().setVisible(true);
        logEditor.setFont(new Font("monospaced", Font.PLAIN, 12));

        styleTime = logEditor.addStyle("time", null);
        StyleConstants.setForeground(styleTime, Color.gray.darker());

        stylePlain = logEditor.addStyle("plain", null);
        StyleConstants.setForeground(stylePlain, Color.black);

        styleBold = logEditor.addStyle("bold", null);
        StyleConstants.setForeground(styleBold, Color.black);
        StyleConstants.setBold(styleBold, true);

        styleInfo = logEditor.addStyle("info", null);
        StyleConstants.setForeground(styleInfo, Color.blue);

        styleWarn = logEditor.addStyle("warn", null);
        StyleConstants.setForeground(styleWarn, Color.red.darker());

        styleError = logEditor.addStyle("error", null);
        StyleConstants.setForeground(styleError, Color.red);

        styleFatal = logEditor.addStyle("fatal", null);
        StyleConstants.setForeground(styleFatal, Color.red);
        StyleConstants.setBold(styleFatal, true);

        add(new JScrollPane(logEditor));
    }

    public void appendLog(String message) {
        if (message == null)
            return;

        try {
            final StyledDocument doc = logEditor.getStyledDocument();

            final Matcher matcher = RegExp.DATETIME_PATTERN.matcher(message);
            if (matcher.find()) {
                final String datetime = matcher.group(1);
                doc.insertString(doc.getLength(), datetime, styleTime);
                message = message.substring(datetime.length());
            }

            if (message.contains("INFO"))
                doc.insertString(doc.getLength(), message, styleInfo);
            else if (message.contains("WARN"))
                doc.insertString(doc.getLength(), message, styleWarn);
            else if (message.contains("ERROR"))
                doc.insertString(doc.getLength(), message, styleError);
            else if (message.contains("FATAL"))
                doc.insertString(doc.getLength(), message, styleFatal);
            else if (message.startsWith("system:"))
                doc.insertString(doc.getLength(), message.substring("system:".length()), styleBold);
            else
                doc.insertString(doc.getLength(), message, stylePlain);

            doc.insertString(doc.getLength(), "\n", stylePlain);
        } catch (Throwable e) {
            logEditor.setText(logEditor.getText() + "\n\n" + SystemUtils.getExceptionCauses(e));
        }
    }

    public void clearLog() {
        logEditor.setText("");
    }
}
