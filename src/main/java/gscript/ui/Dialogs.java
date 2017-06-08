package gscript.ui;

import gscript.util.SystemUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public final class Dialogs {

    /**
     * Show modal dialog with error
     *
     * @param title title
     * @param e     error text
     */
    public static void showExceptionDialog(String title, Throwable e) {
        JOptionPane.showMessageDialog(null,
                "<html>" + SystemUtils.getExceptionCauses(e),
                title,
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Show file open dialog
     *
     * @param title   title
     * @param filters filename filters
     * @return selected file or null if dialog was cancelled
     */
    public static File showFileOpenDialog(String title, File currentDir, FileNameExtensionFilter... filters) {
        final JFileChooser fileChooser = new JFileChooser();

        if (title != null)
            fileChooser.setDialogTitle(title);

        for (FileNameExtensionFilter fileNameExtensionFilter : filters)
            fileChooser.addChoosableFileFilter(fileNameExtensionFilter);

        if (filters.length > 0)
            fileChooser.setFileFilter(filters[0]);

        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setCurrentDirectory(currentDir);
        fileChooser.showOpenDialog(new Frame());
        return fileChooser.getSelectedFile();
    }

    private Dialogs() {
    }
}
