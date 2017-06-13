package gscript.scripteditor;

import javax.swing.*;

public abstract class ScriptEditorTabPopupMenu extends JPopupMenu {

    @Override
    protected void firePopupMenuWillBecomeVisible() {
        super.firePopupMenuWillBecomeVisible();
        popupMenuWillBecomeVisible(this);
    }

    protected abstract void popupMenuWillBecomeVisible(JPopupMenu menu);
}
