package gscript.scripteditor;

import javax.swing.*;
import java.io.File;

public abstract class GroovyAbstractEditPanel extends JPanel {

    protected File file;

    public abstract void changeEncoding(String encoding);

    public abstract void showSearchPanel(JPanel searchPanel);

    protected abstract void doSearch(String searchText, boolean matchCase, boolean regularExpression, boolean searchForward, boolean wholeWord);

    public abstract void hideSearchPanel();

}
