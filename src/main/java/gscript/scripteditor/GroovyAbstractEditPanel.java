package gscript.scripteditor;

import javax.swing.*;
import java.io.File;

public abstract class GroovyAbstractEditPanel extends JPanel {

    protected File file;

    private final GroovySearchPanel searchPanel = new GroovySearchPanel(this);

    public void doSearch() {
        showSearchPanel(searchPanel);
    }

    public abstract void changeEncoding(String encoding);

    protected abstract void showSearchPanel(GroovySearchPanel searchPanel);

    protected abstract void doSearchNext(String searchText, boolean matchCase, boolean regularExpression, boolean searchForward, boolean wholeWord);

    protected abstract void hideSearchPanel();

}
