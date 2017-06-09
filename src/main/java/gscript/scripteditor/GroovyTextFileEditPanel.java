package gscript.scripteditor;

import gscript.ui.Dialogs;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import java.awt.*;
import java.io.File;
import java.nio.file.Files;

public final class GroovyTextFileEditPanel extends GroovyAbstractEditPanel {

    private final RSyntaxTextArea textArea;
    private String lastChecksum;

    public GroovyTextFileEditPanel(File file) {
        this.file = file;

        setLayout(new BorderLayout());

        textArea = new RSyntaxTextArea(20, 40);
        if (file.getName().toLowerCase().endsWith(".xml"))
            textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
        else if (file.getName().toLowerCase().endsWith(".html"))
            textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
        else if (file.getName().toLowerCase().endsWith(".json"))
            textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        else if (file.getName().toLowerCase().endsWith(".sql"))
            textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        else if (file.getName().toLowerCase().endsWith(".bat"))
            textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH);
        else
            textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);

        textArea.setCaretPosition(0);
        textArea.setEditable(false);

        add(new RTextScrollPane(textArea));
    }

    @Override
    public void changeEncoding(String encoding) {
        loadFile(encoding);
    }

    public void loadFile(String encoding) {
        try {
            final String text = new String(Files.readAllBytes(file.toPath()), encoding);
            textArea.setText(text);
        } catch (Throwable e) {
            Dialogs.showExceptionDialog("Load file error", e);
        }
    }

    public void loadFile() {
        loadFile("UTF-8");
    }
}
