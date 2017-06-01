package gscript.factory.ui;

import gscript.Factory;

import javax.swing.*;

public final class GroovyUIFactory {

    private final Factory factory;

    protected static boolean uiInitialized = false;

    public GroovyProgressFrame createProgressFrame(boolean cancellationAllowed) {
        initUI();

        final GroovyProgressFrame groovyProgressFrame = new GroovyProgressFrame(cancellationAllowed);
        factory.getAutoCloseables().add(groovyProgressFrame.getFrame());
        return groovyProgressFrame;
    }

    public GroovyUIFactory(Factory factory) {
        this.factory = factory;
    }

    public static void initUI() {
        if (!uiInitialized) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }

            uiInitialized = true;
        }
    }
}
