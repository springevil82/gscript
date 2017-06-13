package gscript.scripteditor;

import gscript.factory.file.dbf.GroovyDBFReader;
import gscript.ui.Dialogs;
import gscript.ui.util.TableUtils;
import org.fife.ui.rtextarea.SearchContext;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public final class ScriptEditorDBFFileEditPanel extends ScriptEditorAbstractEditPanel {

    private final JTable table;
    private final JLabel statusLabel;

    private int searchFromRow;
    private int searchFromCol;
    private SearchContext searchContext;

    public ScriptEditorDBFFileEditPanel(File file) {
        this.file = file;

        setLayout(new BorderLayout());
        table = new JTable() {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                final JLabel label = (JLabel) super.prepareRenderer(renderer, row, column);
                if (isSearchPanelVisible())
                    highlightFound(label, row, column);
                return label;
            }
        };
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSelectionAllowed(false);
        table.setCellSelectionEnabled(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setFillsViewportHeight(true);
        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (isSearchPanelVisible()) {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                        cancelSearch();

                    if (e.getKeyCode() == KeyEvent.VK_F3 && !e.isShiftDown())
                        findNext(true);

                    if (e.getKeyCode() == KeyEvent.VK_F3 && e.isShiftDown())
                        findNext(false);
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        statusLabel = new JLabel();
        add(statusLabel, BorderLayout.SOUTH);

        loadFile("windows-1251");
    }

    private void highlightFound(JLabel cellRenderer, int row, int column) {

    }

    @Override
    protected void showSearchPanel(ScriptEditorSearchPanel searchPanel) {
        searchFromRow = table.getSelectedRow();
        searchFromCol = table.getSelectedColumn();

        removeAll();
        add(searchPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
        updateUI();
    }

    @Override
    protected void findFirst(String searchText, boolean matchCase, boolean regularExpression, boolean searchForward, boolean wholeWord) {
        if (table.getRowCount() > 0 && searchFromRow != -1 && searchFromCol != -1)
            table.changeSelection(searchFromRow, searchFromCol, false, false);

        searchContext = new SearchContext();
        if (searchText.length() == 0)
            return;

        searchContext.setSearchFor(searchText);
        searchContext.setMatchCase(matchCase);
        searchContext.setRegularExpression(regularExpression);
        searchContext.setSearchForward(searchForward);
        searchContext.setWholeWord(wholeWord);
        searchContext.setMarkAll(true);

        TableSearchEngine.find(table, searchContext);
    }

    @Override
    protected void findNext(boolean searchForward) {
        if (searchContext != null) {
            searchContext.setSearchForward(searchForward);
            TableSearchEngine.find(table, searchContext);
        }
    }

    @Override
    protected void hideSearchPanel() {
        removeAll();
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
        updateUI();
    }

    @Override
    public void changeEncoding(String encoding) {
        loadFile(encoding);
    }

    public void loadFile(String encoding) {
        try (InputStream inputStream = new FileInputStream(file)) {
            final GroovyDBFReader dbfReader = new GroovyDBFReader(inputStream, encoding);
            final GroovyDBFReader.Column[] columns = dbfReader.getColumns();

            final List<Object[]> rows = new ArrayList<>();
            while (dbfReader.hasNextRecord())
                rows.add(dbfReader.nextRecord());

            table.setModel(new DefaultTableModel() {
                @Override
                public int getColumnCount() {
                    return columns.length;
                }

                @Override
                public String getColumnName(int column) {
                    return columns[column].getName();
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columns[columnIndex].getJavaType();
                }

                @Override
                public int getRowCount() {
                    return dbfReader.getRecordCount();
                }

                @Override
                public Object getValueAt(int row, int column) {
                    return rows.get(row)[column];
                }

                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            });

            TableUtils.autoResizeAllColumns(table);
            statusLabel.setText("Record Count: " + rows.size());

        } catch (Throwable e) {
            Dialogs.showExceptionDialog("DBF File read error:\n" + e.getMessage(), e);
        }
    }

    public static final class TableSearchEngine {

        public static void find(JTable table, SearchContext searchContext) {
            if (searchContext.getSearchFor() == null || "".equals(searchContext.getSearchFor().trim()))
                return;

            final CellIterator iterator = new CellIterator(table, searchContext.getSearchForward());
            while (iterator.hasNext()) {
                final Cell cell = iterator.next();
                Object valueAt = table.getValueAt(cell.row, cell.col);

                if (matchCellText(valueAt, searchContext)) {
                    table.changeSelection(cell.row, cell.col, true, false);
                    break;
                }
            }
        }

        private static class Cell {
            int row, col;

            public Cell(int row, int col) {
                this.row = row;
                this.col = col;
            }
        }

        private static class CellIterator implements Iterator<Cell> {

            private final JTable table;
            private final boolean searchForward;
            private final Cell currentCell;

            public CellIterator(JTable table, boolean searchForward) {
                this.table = table;
                this.searchForward = searchForward;
                this.currentCell = new Cell(
                        table.getSelectedRow() != -1 ? table.getSelectedRow() : 0,
                        table.getSelectedColumn() != -1 ? table.getSelectedColumn() : 0);
            }

            @Override
            public boolean hasNext() {
                if (searchForward) {
                    if (currentCell.col < table.getColumnCount() - 1) {
                        currentCell.col++;
                        return true;
                    } else if (currentCell.row < table.getRowCount() - 1) {
                        currentCell.row++;
                        currentCell.col = 0;
                        return true;
                    }
                } else {
                    if (currentCell.col > 0) {
                        currentCell.col--;
                        return true;
                    } else if (currentCell.row > 0) {
                        currentCell.row--;
                        currentCell.col = table.getColumnCount() - 1;
                        return true;
                    }
                }

                return false;
            }

            @Override
            public Cell next() {
                return currentCell;
            }

            @Override
            public void remove() {

            }
        }

        private static boolean searchWords(String searchText, String initialString, boolean matchCase) {
            final String[] words = searchText.split(" ");
            if (words.length == 0)
                return false;
            if (words.length == 1)
                return matchCase ? initialString.contains(words[0]) : initialString.toLowerCase().contains(words[0].toLowerCase());

            for (String word : words) {
                if (matchCase) {
                    if (!initialString.contains(word))
                        return false;
                } else {
                    if (!initialString.toLowerCase().contains(word.toLowerCase()))
                        return false;
                }
            }

            return true;
        }

        private static boolean matchCellText(Object cellValue, SearchContext searchContext) {
            if (cellValue == null)
                return false;

            final String searchText = searchContext.getSearchFor();
            final boolean matchCase = searchContext.getMatchCase();
            final boolean regularExpression = searchContext.isRegularExpression();
            final boolean wholeWord = searchContext.getWholeWord();

            if (regularExpression) {
                return Pattern.compile(searchText).matcher(cellValue.toString()).find();
            } else if (wholeWord) {
                if (matchCase)
                    return cellValue.toString().contains(searchText);
                else
                    return cellValue.toString().toLowerCase().contains(searchText.toLowerCase());
            } else {
                return searchWords(searchText, cellValue.toString(), matchCase);
            }
        }

    }
}
