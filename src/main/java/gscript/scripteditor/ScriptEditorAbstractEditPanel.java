package gscript.scripteditor;

import javax.swing.*;
import java.io.File;

public abstract class ScriptEditorAbstractEditPanel extends JPanel {

    protected File file;

    private final ScriptEditorSearchPanel searchPanel = new ScriptEditorSearchPanel(this);

    private boolean searchPanelVisible = false;

    public void startSearch() {
        searchPanel.getSearchField().setText("");
        showSearchPanel(searchPanel);
        searchPanel.getSearchField().requestFocus();
        searchPanelVisible = true;
    }

    public void cancelSearch() {
        hideSearchPanel();
        searchPanelVisible = false;
    }

    public boolean isSearchPanelVisible() {
        return searchPanelVisible;
    }

    public abstract void changeEncoding(String encoding);

    protected abstract void showSearchPanel(ScriptEditorSearchPanel searchPanel);

    protected abstract void findFirst(String searchText, boolean matchCase, boolean regularExpression, boolean searchForward, boolean wholeWord);

    protected abstract void findNext(boolean searchForward);

    protected abstract void hideSearchPanel();


}
