package gscript.scripteditor;

import gscript.scripteditor.completion.GroovyAutoCompletionProvider;
import gscript.scripteditor.completion.GroovyCompletion;
import org.fife.ui.autocomplete.*;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import java.util.ArrayList;
import java.util.List;

public final class GroovyScriptEditPanel extends GroovySyntaxAreaEditPanel {

    private static final String FACTORY = "@groovy.transform.Field gscript.Factory factory = new gscript.Factory(this)";

    private final GroovyAutoCompletionProvider groovyAutoCompletionProvider;

    public GroovyScriptEditPanel() {
        textArea.append(FACTORY + "\n\n");

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

}
