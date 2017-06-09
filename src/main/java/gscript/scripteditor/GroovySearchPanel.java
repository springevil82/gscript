package gscript.scripteditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class GroovySearchPanel extends JPanel implements ActionListener {

    private GroovyAbstractEditPanel editPanel;
    private final JTextField searchField;
    private final JCheckBox regexComboBox;
    private final JCheckBox matchCaseComboBox;

    public GroovySearchPanel(GroovyAbstractEditPanel editPanel) {
        this.editPanel = editPanel;

        final JToolBar toolBar = new JToolBar();
        searchField = new JTextField(30);
        toolBar.add(searchField);

        final JButton nextButton = new JButton("Find Next");
        nextButton.setActionCommand("FindNext");
        nextButton.addActionListener(this);
        toolBar.add(nextButton);
        searchField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                nextButton.doClick();
            }
        });

        final JButton prevButton = new JButton("Find Previous");
        prevButton.setActionCommand("FindPrev");
        prevButton.addActionListener(this);
        toolBar.add(prevButton);
        regexComboBox = new JCheckBox("Regex");
        toolBar.add(regexComboBox);

        matchCaseComboBox = new JCheckBox("Match Case");
        toolBar.add(matchCaseComboBox);

        setLayout(new BorderLayout());
        add(toolBar, BorderLayout.NORTH);
    }

    public void actionPerformed(ActionEvent e) {
        editPanel.doSearch(
                searchField.getText(),
                matchCaseComboBox.isSelected(),
                regexComboBox.isSelected(),
                "FindNext".equals(e.getActionCommand()),
                false);
    }
}
