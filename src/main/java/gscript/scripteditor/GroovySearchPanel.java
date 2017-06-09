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

public final class GroovySearchPanel extends JPanel implements ActionListener {

    private GroovyAbstractEditPanel editPanel;
    private final JTextField searchField;
    private final JCheckBox regexComboBox;
    private final JCheckBox matchCaseComboBox;

    public GroovySearchPanel(final GroovyAbstractEditPanel editPanel) {
        this.editPanel = editPanel;

        final JToolBar toolBar = new JToolBar();
        searchField = new JTextField(30);
        toolBar.add(new JLabel("Search for: "));
        toolBar.add(searchField);

        final JButton nextButton = new JButton("Find Next");
        nextButton.setToolTipText("Next occurrence (F3)");
        nextButton.setActionCommand("FindNext");
        nextButton.addActionListener(this);
        toolBar.add(nextButton);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                doSearch();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                doSearch();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                doSearch();
            }
        });


        final JButton prevButton = new JButton("Find Previous");
        prevButton.setToolTipText("Previous Occurrence (Shift+F3)");
        prevButton.setActionCommand("FindPrev");
        prevButton.addActionListener(this);
        toolBar.add(prevButton);
        regexComboBox = new JCheckBox("Regex");
        toolBar.add(regexComboBox);

        matchCaseComboBox = new JCheckBox("Match Case");
        toolBar.add(matchCaseComboBox);

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                    closeSearch();

                if (e.getKeyCode() == KeyEvent.VK_F3 && !e.isShiftDown())
                    nextButton.doClick();

                if (e.getKeyCode() == KeyEvent.VK_F3 && e.isShiftDown())
                    prevButton.doClick();
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

    public JTextField getSearchField() {
        return searchField;
    }

    private void doSearch() {
        actionPerformed(new ActionEvent(this, 0, "FindNext"));
    }

    private void closeSearch() {
        editPanel.hideSearchPanel();
    }

    public void actionPerformed(ActionEvent e) {
        editPanel.doSearchNext(
                searchField.getText(),
                matchCaseComboBox.isSelected(),
                regexComboBox.isSelected(),
                "FindNext".equals(e.getActionCommand()),
                false);
    }
}
