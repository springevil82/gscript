package gscript.scripteditor;

import gscript.Factory;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import java.util.ArrayList;
import java.util.List;

public final class ScriptEditorScriptEditPanel extends ScriptEditorSyntaxAreaEditPanel {

    private final ScriptEditorAutoCompletionProvider autoCompletionProvider;

    public ScriptEditorScriptEditPanel(Class<? extends Factory> factoryClass) {
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_GROOVY);
        textArea.append(buildFactoryDefinition(factoryClass) + "\n\n");

        autoCompletionProvider = new ScriptEditorAutoCompletionProvider();
        final DefaultCompletionProvider completionProvider = new DefaultCompletionProvider();
        final org.fife.ui.autocomplete.AutoCompletion autoCompletion = new org.fife.ui.autocomplete.AutoCompletion(new DefaultCompletionProvider() {
            @Override
            public List getCompletions(JTextComponent comp) {
                return getCompletionsFor(getCurrentText(comp), getAllTextBeforeCaret(comp), completionProvider);
            }
        });

        autoCompletion.install(textArea);
    }

    private String buildFactoryDefinition(Class<? extends Factory> factoryClass) {
        return "@groovy.transform.Field " + factoryClass.getCanonicalName() + " factory = new " + factoryClass.getCanonicalName() + "(this)";
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

        final List<ScriptEditorAutoCompletion> completionList = autoCompletionProvider.getCompletionsFor(enteredText, allTextBeforeCaret);
        if (completionList == null)
            return new ArrayList<>();

        for (ScriptEditorAutoCompletion autoCompletion : completionList)
            completions.add(new BasicCompletion(completionProvider, autoCompletion.getCompletion(), autoCompletion.getType().getSimpleName()));

        return completions;
    }

}
