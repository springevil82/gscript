package gscript.scripteditor;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import java.io.File;

public final class GroovyTextFileEditPanel extends GroovySyntaxAreaEditPanel {

    public GroovyTextFileEditPanel(File file) {
        this.file = file;

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

        textArea.setEditable(false);
    }

}
