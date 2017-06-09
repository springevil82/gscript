package gscript.scripteditor;

import javax.swing.*;
import java.io.File;

public abstract class GroovyAbstractEditPanel extends JPanel {

    protected File file;

    public abstract void changeEncoding(String encoding);

}
