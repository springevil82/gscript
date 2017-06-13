package gscript.scripteditor;

import gscript.ui.Dialogs;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.security.MessageDigest;

public class GroovySyntaxAreaEditPanel extends GroovyAbstractEditPanel {

    protected final RSyntaxTextArea textArea;
    protected String lastChecksum;
    private String encoding = "UTF-8";

    private int searchFromCaretPosition;
    private SearchContext searchContext;

    public GroovySyntaxAreaEditPanel() {
        setLayout(new BorderLayout());

        textArea = new RSyntaxTextArea(20, 40);
        textArea.setBracketMatchingEnabled(false);
        textArea.setCaretPosition(0);
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (isSearchPanelVisible() && e.getKeyCode() == KeyEvent.VK_ESCAPE)
                    cancelSearch();
            }
        });

        add(new RTextScrollPane(textArea), BorderLayout.CENTER);
    }

    @Override
    protected void showSearchPanel(GroovySearchPanel searchPanel) {
        searchFromCaretPosition = textArea.getCaretPosition();

        removeAll();
        add(searchPanel, BorderLayout.NORTH);
        add(new RTextScrollPane(textArea), BorderLayout.CENTER);
        updateUI();
    }

    @Override
    protected void findFirst(String searchText, boolean matchCase, boolean regularExpression, boolean searchForward, boolean wholeWord) {
        textArea.setCaretPosition(searchFromCaretPosition);

        searchContext = new SearchContext();
        if (searchText.length() == 0)
            return;

        searchContext.setSearchFor(searchText);
        searchContext.setMatchCase(matchCase);
        searchContext.setRegularExpression(regularExpression);
        searchContext.setSearchForward(searchForward);
        searchContext.setWholeWord(wholeWord);
        searchContext.setMarkAll(true);

        SearchEngine.find(textArea, searchContext);
    }

    @Override
    protected void findNext(boolean searchForward) {
        if (searchContext != null) {
            searchContext.setSearchForward(searchForward);
            SearchEngine.find(textArea, searchContext);
        }
    }

    @Override
    protected void hideSearchPanel() {
        final SearchContext context = new SearchContext();
        context.setMarkAll(false);
        SearchEngine.find(textArea, context);
        searchContext = null;

        removeAll();
        add(new RTextScrollPane(textArea), BorderLayout.CENTER);
        updateUI();
    }

    @Override
    public void changeEncoding(String encoding) {
        this.encoding = encoding;

        if (file != null)
            loadFile(file);
    }

    public void loadFile(File file) {
        this.file = file;

        try {
            final String text = new String(Files.readAllBytes(file.toPath()), encoding);
            textArea.setText(text);
        } catch (Throwable e) {
            Dialogs.showExceptionDialog("File load error", e);
        }

        lastChecksum = calcSHA1();
        textArea.setCaretPosition(0);
    }

    public void saveFile(File file) {
        try (PrintStream printStream = new PrintStream(file, encoding)) {
            printStream.print(getText());
        } catch (Throwable e) {
            Dialogs.showExceptionDialog("File save error", e);
        }

        lastChecksum = calcSHA1();
    }

    public String calcSHA1() {
        try {
            final MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            try (InputStream input = new ByteArrayInputStream(getText().getBytes())) {

                byte[] buffer = new byte[8192];
                int len = input.read(buffer);

                while (len != -1) {
                    sha1.update(buffer, 0, len);
                    len = input.read(buffer);
                }

                return new HexBinaryAdapter().marshal(sha1.digest()).toLowerCase();
            }
        } catch (Throwable e) {
            throw new RuntimeException("Script checksum calculation error", e);
        }
    }

    public String getText() {
        return textArea.getText();
    }

    public boolean isChanged() {
        return !calcSHA1().equals(lastChecksum);
    }

}
