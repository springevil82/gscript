package gscript.scripteditor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public final class ScriptEditorSearchPanel extends JPanel {

    private ScriptEditorAbstractEditPanel editPanel;
    private final JTextField searchField;
    private final JCheckBox regexCheckBox;
    private final JCheckBox matchCaseCheckBox;

    private Timer findFirstTimer;

    public ScriptEditorSearchPanel(final ScriptEditorAbstractEditPanel editPanel) {
        this.editPanel = editPanel;

        final JToolBar toolBar = new JToolBar();
        searchField = new JTextField(30);
        toolBar.add(new JLabel("Search for: "));
        toolBar.add(searchField);

        final JButton nextButton = new JButton(new AbstractAction("Find Next") {
            @Override
            public void actionPerformed(ActionEvent e) {
                findNext(true);
            }
        });
        nextButton.setToolTipText("Next occurrence (F3)");
        toolBar.add(nextButton);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                runFindFirstTimer();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                runFindFirstTimer();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                runFindFirstTimer();
            }
        });

        final JButton prevButton = new JButton(new AbstractAction("Find Previous") {
            @Override
            public void actionPerformed(ActionEvent e) {
                findNext(false);
            }
        });
        prevButton.setToolTipText("Previous Occurrence (Shift+F3)");

        toolBar.add(prevButton);
        regexCheckBox = new JCheckBox(new AbstractAction("Regex") {
            @Override
            public void actionPerformed(ActionEvent e) {
                findFirst();
            }
        });
        toolBar.add(regexCheckBox);

        matchCaseCheckBox = new JCheckBox(new AbstractAction("Match Case") {
            @Override
            public void actionPerformed(ActionEvent e) {
                findFirst();
            }
        });
        toolBar.add(matchCaseCheckBox);

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                    closeSearch();

                if (e.getKeyCode() == KeyEvent.VK_F3 && !e.isShiftDown())
                    findNext(true);

                if (e.getKeyCode() == KeyEvent.VK_F3 && e.isShiftDown())
                    findNext(false);
            }
        });

        final JButton closeSearchPanelButton = new JButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeSearch();
            }
        });
        closeSearchPanelButton.setBorder(new EmptyBorder(0, 5, 0, 5));
        closeSearchPanelButton.setIcon(new ImageIcon(getClass().getResource("/icons/close.png")));
        closeSearchPanelButton.setToolTipText("Close output");
        closeSearchPanelButton.setOpaque(false);

        toolBar.add(new JLabel("  "));
        toolBar.add(closeSearchPanelButton);

        setLayout(new BorderLayout());
        add(toolBar, BorderLayout.NORTH);
    }

    private void runFindFirstTimer() {
        if (findFirstTimer == null) {
            findFirstTimer = new Timer(500, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    findFirst();
                }
            });
            findFirstTimer.setRepeats(false);
            findFirstTimer.start();
        } else {
            findFirstTimer.restart();
        }
    }

    public JTextField getSearchField() {
        return searchField;
    }

    private void findFirst() {
        editPanel.findFirst(
                searchField.getText(),
                matchCaseCheckBox.isSelected(),
                regexCheckBox.isSelected(),
                true,
                false);
    }

    private void findNext(boolean searchForward) {
        editPanel.findNext(searchForward);
    }

    private void closeSearch() {
        editPanel.hideSearchPanel();
    }

}
