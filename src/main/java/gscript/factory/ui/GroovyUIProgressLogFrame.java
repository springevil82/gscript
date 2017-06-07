package gscript.factory.ui;

import gscript.Factory;
import gscript.ui.GroovyProgressLogPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Script execution progress window with two labels (progress + summary), progress bar, log text pane and cancellation button
 */
public class GroovyUIProgressLogFrame extends GroovyAutoclosableFrame {

    private final GroovyProgressLogPanel groovyProgressLogPanel;

    public GroovyUIProgressLogFrame(Factory factory) {
        GroovyUIFactory.initUI();

        groovyProgressLogPanel = new GroovyProgressLogPanel();

        setTitle(factory.script.getCurrentScriptFile().getAbsolutePath() + " [" + factory.script.getCurrentProcessID() + "]");
        setIconImage(new ImageIcon(Factory.class.getResource("/icons/others/script.png")).getImage());
        setMinimumSize(new Dimension(600, 400));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                groovyProgressLogPanel.getCancelButton().doClick();
            }
        });

        setContentPane(groovyProgressLogPanel);
        pack();

        setVisible(true);
    }

    public GroovyProgressLogPanel getProgressLogPanel() {
        return groovyProgressLogPanel;
    }
}
