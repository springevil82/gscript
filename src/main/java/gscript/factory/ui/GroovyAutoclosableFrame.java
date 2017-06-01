package gscript.factory.ui;

import javax.swing.*;

public class GroovyAutoclosableFrame extends JFrame implements AutoCloseable {

    @Override
    public void close() throws Exception {
        dispose();
    }
}
