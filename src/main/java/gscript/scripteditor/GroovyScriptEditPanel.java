package gscript.scripteditor;

import gscript.scripteditor.completion.GroovyAutoCompletionProvider;
import gscript.scripteditor.completion.GroovyCompletion;
import gscript.ui.Dialogs;
import org.fife.ui.autocomplete.*;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public final class GroovyScriptEditPanel extends GroovyAbstractEditPanel {

    private static final String FACTORY = "@groovy.transform.Field gscript.Factory factory = new gscript.Factory(this)";

    private final RSyntaxTextArea textArea;
    private String lastChecksum;
    private String encoding = "UTF-8";
    private final GroovyAutoCompletionProvider groovyAutoCompletionProvider;

    public GroovyScriptEditPanel() {
        setLayout(new BorderLayout());

        textArea = new RSyntaxTextArea(20, 40);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_GROOVY);
        textArea.setBracketMatchingEnabled(false);
        textArea.setCaretPosition(0);
        textArea.append(FACTORY + "\n\n");

        add(new RTextScrollPane(textArea));

        groovyAutoCompletionProvider = new GroovyAutoCompletionProvider();

        final DefaultCompletionProvider completionProvider = new DefaultCompletionProvider();
        final AutoCompletion autoCompletion = new AutoCompletion(new DefaultCompletionProvider() {
            @Override
            public List getCompletions(JTextComponent comp) {
                return getCompletionsFor(getCurrentText(comp), getAllTextBeforeCaret(comp), completionProvider);
            }
        });

        autoCompletion.install(textArea);
    }

    public String getCurrentText(JTextComponent comp) {
        final Document doc = comp.getDocument();
        final int dot = comp.getCaretPosition();
        final Element root = doc.getDefaultRootElement();
        final int index = root.getElementIndex(dot);
        final Element elem = root.getElement(index);
        final int start = elem.getStartOffset();

        try {
            return doc.getText(start, dot - start);
        } catch (BadLocationException e) {
            return "";
        }
    }

    public String getAllTextBeforeCaret(JTextComponent comp) {
        final Document doc = comp.getDocument();
        final int dot = comp.getCaretPosition();

        try {
            return doc.getText(0, dot);
        } catch (BadLocationException e) {
            return "";
        }
    }

    public List<Completion> getCompletionsFor(String enteredText, String allTextBeforeCaret, CompletionProvider completionProvider) {
        final List<Completion> completions = new ArrayList<>();

        final List<GroovyCompletion> completionList = groovyAutoCompletionProvider.getCompletionsFor(enteredText, allTextBeforeCaret);
        if (completionList == null)
            return new ArrayList<>();

        for (GroovyCompletion groovyCompletion : completionList)
            completions.add(new BasicCompletion(completionProvider, groovyCompletion.getCompletion(), groovyCompletion.getType().getSimpleName()));

        return completions;
    }

    public String calcSHA1() {
        try {
            final MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            try (InputStream input = new ByteArrayInputStream(getScriptText().getBytes())) {

                byte[] buffer = new byte[8192];
                int len = input.read(buffer);

                while (len != -1) {
                    sha1.update(buffer, 0, len);
                    len = input.read(buffer);
                }

                return new HexBinaryAdapter().marshal(sha1.digest()).toLowerCase();
            }
        } catch (Throwable e) {
            throw new RuntimeException("Script checksum calculation error", e);
        }
    }

    public String getScriptText() {
        return textArea.getText();
    }

    public boolean isChanged() {
        return !calcSHA1().equals(lastChecksum);
    }

    @Override
    public void changeEncoding(String encoding) {
        this.encoding = encoding;

        if (file != null)
            loadFile(file);
    }

    public void loadFile(File file) {
        this.file = file;

        try {
            final String text = new String(Files.readAllBytes(file.toPath()), encoding);
            textArea.setText(text);
        } catch (Throwable e) {
            Dialogs.showExceptionDialog("File load error", e);
        }

        lastChecksum = calcSHA1();
    }

    public void saveFile(File file) {
        try (PrintStream printStream = new PrintStream(file, encoding)) {
            printStream.print(getScriptText());
        } catch (Throwable e) {
            Dialogs.showExceptionDialog("File save error", e);
        }

        lastChecksum = calcSHA1();
    }

}
