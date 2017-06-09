package gscript.scripteditor;

import gscript.factory.file.dbf.GroovyDBFReader;
import gscript.ui.Dialogs;
import gscript.ui.util.TableUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class GroovyDBFFileEditPanel extends GroovyAbstractEditPanel {

    private final JTable table;
    private final JLabel statusLabel;

    public GroovyDBFFileEditPanel(File file) {
        this.file = file;

        setLayout(new BorderLayout());
        table = new JTable();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setFillsViewportHeight(true);

        add(new JScrollPane(table), BorderLayout.CENTER);

        statusLabel = new JLabel();
        add(statusLabel, BorderLayout.SOUTH);

        loadFile("windows-1251");
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
            Dialogs.showExceptionDialog("Ошибка при чтении файла", e);
        }
    }

}
