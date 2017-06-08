package gscript.scripteditor;

import javax.swing.*;

public abstract class TabPopupMenu extends JPopupMenu {

    @Override
    protected void firePopupMenuWillBecomeVisible() {
        super.firePopupMenuWillBecomeVisible();
        popupMenuWillBecomeVisible(this);
    }

    protected abstract void popupMenuWillBecomeVisible(JPopupMenu menu);
}
