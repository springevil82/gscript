package gscript.scripteditor;

import gscript.ui.Dialogs;
import gscript.ui.TitledPanel;
import gscript.util.DateUtils;
import gscript.util.SystemUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class GroovyScriptEditor extends JFrame {

    private final JToolBar toolbar;
    private final JPanel documentPanel;
    private final JPanel outputPanel;
    private JPanel contentPanel;
    private JSplitPane mainSplitPane;
    private boolean outputShown;
    private final JTabbedPane documentPane;
    private final GroovyScriptOutputPanel scriptOutputPanel;

    private final Map<String, File> openedFiles = new LinkedHashMap<>();
    private File lastFile;
    private JMenuItem menuItemSave;
    private JMenuItem menuItemRun;
    private final JButton btnSave;
    private final JButton btnRun;

    public GroovyScriptEditor() throws HeadlessException {

        contentPanel = new JPanel(new BorderLayout());
        documentPanel = new JPanel(new BorderLayout());
        outputPanel = new JPanel(new BorderLayout());

        final JButton btnNew = new JButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doNew();
            }
        });
        btnNew.setIcon(new ImageIcon(getClass().getResource("/icons/new.png")));
        btnNew.setToolTipText("Create new script (Ctrl+N)");

        final JButton btnOpen = new JButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doOpen();
            }
        });
        btnOpen.setIcon(new ImageIcon(getClass().getResource("/icons/open_file.png")));
        btnOpen.setToolTipText("Open file (Ctrl+O)");

        btnSave = new JButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doSave();
            }
        });
        btnSave.setIcon(new ImageIcon(getClass().getResource("/icons/save.png")));
        btnSave.setToolTipText("Save changes (Ctrl+S)");

        btnRun = new JButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doRun();
            }
        });
        btnRun.setIcon(new ImageIcon(getClass().getResource("/icons/run.png")));
        btnRun.setToolTipText("Run script (F9)");

        toolbar = new JToolBar();
        toolbar.setRollover(true);
        toolbar.setFloatable(false);
        toolbar.add(btnNew);
        toolbar.add(btnOpen);
        toolbar.add(btnSave);
        toolbar.addSeparator();
        toolbar.add(btnRun);

        documentPane = new JTabbedPane();
        documentPane.setComponentPopupMenu(new TabPopupMenu() {
            @Override
            protected void popupMenuWillBecomeVisible(JPopupMenu menu) {
                buildPopupMenu(menu);
            }
        });

        documentPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateToolbarButtons();
            }
        });

        documentPanel.add(documentPane);

        scriptOutputPanel = new GroovyScriptOutputPanel();

        final JButton btnCloseOutput = new JButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeOutput();
            }
        });
        btnCloseOutput.setBorder(new EmptyBorder(0, 5, 0, 5));
        btnCloseOutput.setIcon(new ImageIcon(getClass().getResource("/icons/close.png")));
        btnCloseOutput.setToolTipText("Close output");
        btnCloseOutput.setOpaque(false);

        final TitledPanel titlePanel = new TitledPanel(null, "Output");
        titlePanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        final JPanel outputPanelTitle = new JPanel(new BorderLayout());
        outputPanelTitle.add(titlePanel, BorderLayout.CENTER);
        outputPanelTitle.add(btnCloseOutput, BorderLayout.EAST);

        outputPanel.add(outputPanelTitle, BorderLayout.NORTH);
        outputPanel.add(scriptOutputPanel, BorderLayout.CENTER);

        contentPanel.add(toolbar, BorderLayout.NORTH);
        contentPanel.add(documentPanel, BorderLayout.CENTER);

        initMenu();

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                doExit();
            }
        });

        updateToolbarButtons();
    }

    private void buildPopupMenu(JPopupMenu menu) {
        menu.removeAll();

        if (documentPane.getSelectedIndex() != -1 && documentPane.getTitleAt(documentPane.getSelectedIndex()).toLowerCase().endsWith(".groovy")) {
            final JMenuItem menuItemRun = new JMenuItem(new AbstractAction("Run script") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    doRun();
                }
            });
            menuItemRun.setIcon(new ImageIcon(getClass().getResource("/icons/run.png")));
            menu.add(menuItemRun);
        }

        final JMenu menuItemEncoding = new JMenu("Encoding");
        menu.add(menuItemEncoding);
        menuItemEncoding.add(new JMenuItem(new AbstractAction("utf-8") {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeEncoding("utf-8");
            }
        }));
        menuItemEncoding.add(new JMenuItem(new AbstractAction("windows-1251") {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeEncoding("windows-1251");
            }
        }));
        menuItemEncoding.add(new JMenuItem(new AbstractAction("cp866") {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeEncoding("cp866");
            }
        }));
        menuItemEncoding.addSeparator();
        menuItemEncoding.add(new JMenuItem(new AbstractAction("Set another encoding...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                final String encoding = JOptionPane.showInputDialog(GroovyScriptEditor.this, "Type your encoding");
                if (encoding != null) {
                    changeEncoding(encoding);
                }
            }
        }));

        final JMenuItem menuItemClose = new JMenuItem(new AbstractAction("Close tab") {
            @Override
            public void actionPerformed(ActionEvent e) {
                doCloseActiveTab();
            }
        });
        menu.add(menuItemClose);
    }

    private void initMenu() {
        final JMenuBar menubar = new JMenuBar();
        final JMenu menuFile = new JMenu("File");
        final JMenuItem menuItemNew = new JMenuItem(new AbstractAction("New script") {
            @Override
            public void actionPerformed(ActionEvent e) {
                doNew();
            }
        });
        menuItemNew.setIcon(new ImageIcon(getClass().getResource("/icons/new.png")));
        menuItemNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        menuFile.add(menuItemNew);

        final JMenuItem menuItemOpen = new JMenuItem(new AbstractAction("Open...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                doOpen();
            }
        });
        menuItemOpen.setIcon(new ImageIcon(getClass().getResource("/icons/open_file.png")));
        menuItemOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        menuFile.add(menuItemOpen);

        menuItemSave = new JMenuItem(new AbstractAction("Save") {
            @Override
            public void actionPerformed(ActionEvent e) {
                doSave();
            }
        });
        menuItemSave.setIcon(new ImageIcon(getClass().getResource("/icons/save.png")));
        menuItemSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        menuFile.add(menuItemSave);
        menuFile.addSeparator();

        final JMenuItem menuItemExit = new JMenuItem(new AbstractAction("Exit") {
            @Override
            public void actionPerformed(ActionEvent e) {
                doExit();
            }
        });
        menuFile.add(menuItemExit);

        final JMenu menuView = new JMenu("View");
        final JCheckBoxMenuItem menuItemOutput = new JCheckBoxMenuItem(new AbstractAction("Toggle output") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (outputShown)
                    closeOutput();
                else
                    showOutput();
            }
        });
        menuItemOutput.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
        menuItemOutput.setState(false);
        menuView.add(menuItemOutput);

        final JMenu menuRun = new JMenu("Run");
        menuItemRun = new JMenuItem(new AbstractAction("Run script") {
            @Override
            public void actionPerformed(ActionEvent e) {
                doRun();
            }
        });
        menuItemRun.setIcon(new ImageIcon(getClass().getResource("/icons/run.png")));
        menuItemRun.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0));
        menuRun.add(menuItemRun);

        menubar.add(menuFile);
        menubar.add(menuView);
        menubar.add(menuRun);

        setJMenuBar(menubar);
    }

    private void showOutput() {
        contentPanel.removeAll();

        mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, documentPanel, outputPanel);
        mainSplitPane.setResizeWeight(0.4d);

        contentPanel.add(toolbar, BorderLayout.NORTH);
        contentPanel.add(mainSplitPane, BorderLayout.CENTER);
        contentPanel.updateUI();

        outputShown = true;
    }

    private void closeOutput() {
        contentPanel.removeAll();
        contentPanel.add(toolbar, BorderLayout.NORTH);
        contentPanel.add(documentPanel, BorderLayout.CENTER);
        contentPanel.updateUI();

        outputShown = false;
    }

    private String getNewScriptName() {
        for (int i = 1; i < Integer.MAX_VALUE; i++) {
            final String name = "script" + i + ".groovy";
            if (!openedFiles.containsKey(name))
                return name;
        }

        throw new RuntimeException("Could't lookup new script name");
    }

    private void updateToolbarButtons() {
        boolean isGroovyDocument = documentPane.getSelectedIndex() != -1 &&
                documentPane.getTitleAt(documentPane.getSelectedIndex()).toLowerCase().endsWith(".groovy");

        menuItemSave.setEnabled(isGroovyDocument);
        btnSave.setEnabled(isGroovyDocument);
        menuItemRun.setEnabled(isGroovyDocument);
        btnRun.setEnabled(isGroovyDocument);
    }


    public void doNew() {
        final String newScriptName = getNewScriptName();
        openedFiles.put(newScriptName, null);
        documentPane.addTab(newScriptName, new ImageIcon(getClass().getResource("/icons/groovy.png")), new GroovyScriptEditPanel());
    }

    public void doOpen() {
        final File file = Dialogs.showFileOpenDialog("Open file", lastFile != null ? lastFile.getAbsoluteFile().getParentFile() : new File("."),
                new FileNameExtensionFilter("All Formats (*.groovy;*.txt;*.log;*.dbf;*.csv;*.json;*.xml;*.html;*.sql;*.bat)", "groovy", "txt", "log", "dbf", "csv", "json", "xml", "html", "sql", "bat"),
                new FileNameExtensionFilter("Groovy Script Files (*.groovy)", "groovy"),
                new FileNameExtensionFilter("Text Files (*.txt)", "txt"),
                new FileNameExtensionFilter("Log Files (*.log)", "log"),
                new FileNameExtensionFilter("DBF Files (*.dbf)", "dbf"),
                new FileNameExtensionFilter("CSV Files (*.csv)", "csv"),
                new FileNameExtensionFilter("XML Files (*.xml)", "xml"),
                new FileNameExtensionFilter("HTML Files (*.html)", "html"),
                new FileNameExtensionFilter("JSON Files (*.json)", "json"),
                new FileNameExtensionFilter("SQL Files (*.sql)", "sql"),
                new FileNameExtensionFilter("BAT Files (*.bat)", "bat"),
                new FileNameExtensionFilter("All Files", "*")
        );

        if (file == null)
            return;

        this.lastFile = file;
        doOpen(file);
    }

    private void doOpen(File file) {
        for (int i = 0; i < documentPane.getTabCount(); i++)
            if (file.getName().equals(documentPane.getTitleAt(i))) {
                documentPane.setSelectedIndex(i);
                return;
            }

        if (file.getName().toLowerCase().endsWith(".groovy"))
            doOpenScriptFile(file);
        else if (file.getName().toLowerCase().endsWith(".dbf"))
            doOpenDBFFile(file);
        else
            doOpenTextFile(file);
    }


    private void doOpenScriptFile(File file) {
        openedFiles.put(file.getName(), file);
        final GroovyScriptEditPanel scriptEditPanel = new GroovyScriptEditPanel();
        scriptEditPanel.loadFile(file);

        documentPane.addTab(file.getName(), new ImageIcon(getClass().getResource("/icons/groovy.png")), scriptEditPanel);
    }

    private void doOpenTextFile(File file) {
        openedFiles.put(file.getName(), file);
        final GroovyTextFileEditPanel textFileEditPanel = new GroovyTextFileEditPanel(file);
        textFileEditPanel.loadFile();

        documentPane.addTab(file.getName(), new ImageIcon(getClass().getResource("/icons/txt.png")), textFileEditPanel);
    }

    private void doOpenDBFFile(File file) {
        openedFiles.put(file.getName(), file);
        final GroovyDBFFileEditPanel dbfFileEditPanel = new GroovyDBFFileEditPanel(file);

        documentPane.addTab(file.getName(), new ImageIcon(getClass().getResource("/icons/dbf.png")), dbfFileEditPanel);
    }

    private void doSave() {
        if (documentPane.getSelectedComponent() == null)
            return;

        final GroovyScriptEditPanel scriptEditPanel = (GroovyScriptEditPanel) documentPane.getSelectedComponent();
        String fileName = documentPane.getTitleAt(documentPane.getSelectedIndex());

        if (fileName.toLowerCase().endsWith(".groovy")) {
            final File scriptFile = openedFiles.get(fileName);
            if (scriptFile == null) {
                final File file = chooseFileSave(fileName);
                if (file != null)
                    scriptEditPanel.saveFile(file);
            } else {
                scriptEditPanel.saveFile(scriptFile);
            }
        }
    }

    private File chooseFileSave(String presetFileName) {
        if (presetFileName.endsWith(".groovy"))
            presetFileName = presetFileName.substring(0, presetFileName.length() - ".groovy".length());

        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save script");
        final FileNameExtensionFilter filter = new FileNameExtensionFilter("Groovy Script Files (*.groovy)", "groovy");
        fileChooser.addChoosableFileFilter(filter);
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setCurrentDirectory(new File("."));
        fileChooser.setSelectedFile(new File(presetFileName));
        final int option = fileChooser.showSaveDialog(new Frame());

        if (option == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            if (selectedFile != null && !selectedFile.getName().contains(".")) {
                final String[] extensions = ((FileNameExtensionFilter) fileChooser.getFileFilter()).getExtensions();
                if (extensions.length > 0)
                    selectedFile = new File(selectedFile.getAbsolutePath() + "." + extensions[0]);
            }

            return selectedFile;
        }

        return null;
    }

    private int findTabByTitle(String tabTitle) {
        for (int i = 0; i < documentPane.getTabCount(); i++)
            if (tabTitle.equals(documentPane.getTitleAt(i)))
                return i;

        return -1;
    }

    protected void doExit() {
        for (String fileName : openedFiles.keySet()) {
            if (fileName.toLowerCase().endsWith(".groovy")) {
                int tabIndex = findTabByTitle(fileName);
                if (tabIndex != -1) {
                    final GroovyScriptEditPanel scriptEditPanel = (GroovyScriptEditPanel) documentPane.getComponentAt(tabIndex);
                    if (scriptEditPanel.isChanged()) {

                        documentPane.setSelectedIndex(tabIndex);

                        final int buttonIndex = JOptionPane.showOptionDialog(this,
                                "Save changes?",
                                "Attention",
                                JOptionPane.YES_NO_CANCEL_OPTION,
                                JOptionPane.PLAIN_MESSAGE,
                                null,
                                null,
                                null);

                        if (buttonIndex == 0) {
                            final File file = openedFiles.get(fileName);
                            if (file != null)
                                scriptEditPanel.saveFile(file);
                            else
                                doSave();
                        }

                        if (buttonIndex == -1 || buttonIndex == 2)
                            return;

                    }
                }
            }
        }

        setVisible(false);
        dispose();
    }

    private void doRun() {
        int tabIndex = documentPane.getSelectedIndex();
        if (tabIndex == -1)
            return;

        String fileName = documentPane.getTitleAt(tabIndex);

        if (fileName.toLowerCase().endsWith(".groovy")) {
            final File file = openedFiles.get(fileName);

            if (file != null)
                doSave();

            showOutput();
            scriptOutputPanel.clearLog();

            final ScriptRunner.Logger logger = new ScriptRunner.Logger() {
                @Override
                public void logMessage(String message) {
                    scriptOutputPanel.appendLog(message);
                }

                @Override
                public void logError(Throwable e) {
                    scriptOutputPanel.appendLog("FATAL: " + SystemUtils.getExceptionCauses(e));
                }
            };

            long startTime = System.currentTimeMillis();
            scriptOutputPanel.appendLog("system:Script launched");

            boolean success;
            if (file != null) {
                success = ScriptRunner.runScript(file, logger);
            } else {
                final GroovyScriptEditPanel scriptEditPanel = (GroovyScriptEditPanel) documentPane.getComponentAt(tabIndex);
                success = ScriptRunner.runScript(scriptEditPanel.getScriptText(), logger);
            }

            if (success)
                scriptOutputPanel.appendLog("system:Script executed successfully (" + DateUtils.millisToString(System.currentTimeMillis() - startTime) + ")");
            else
                scriptOutputPanel.appendLog("system:Script executed with errors (" + DateUtils.millisToString(System.currentTimeMillis() - startTime) + ")");
        }
    }

    private void doCloseActiveTab() {
        int tabIndex = documentPane.getSelectedIndex();
        if (tabIndex == -1)
            return;

        String fileName = documentPane.getTitleAt(tabIndex);

        if (fileName.toLowerCase().endsWith(".groovy")) {
            final File file = openedFiles.get(fileName);
            if (file != null) {

                final GroovyScriptEditPanel scriptEditPanel = (GroovyScriptEditPanel) documentPane.getComponentAt(tabIndex);

                if (scriptEditPanel.isChanged()) {
                    final int buttonIndex = JOptionPane.showOptionDialog(this,
                            "Save changes?",
                            "Attention",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.PLAIN_MESSAGE,
                            null,
                            null,
                            null);

                    if (buttonIndex == 0)
                        scriptEditPanel.saveFile(file);

                    if (buttonIndex == -1 || buttonIndex == 2)
                        return;
                }
            }
        }

        documentPane.removeTabAt(tabIndex);
        openedFiles.remove(fileName);
    }

    private void changeEncoding(String encoding) {
        int tabIndex = documentPane.getSelectedIndex();
        if (tabIndex == -1)
            return;

        final Component activeDocument = documentPane.getComponentAt(tabIndex);
        if (activeDocument != null) {
            if (activeDocument instanceof GroovyDBFFileEditPanel) {
                final GroovyDBFFileEditPanel groovyDBFFileEditPanel = (GroovyDBFFileEditPanel) activeDocument;
                groovyDBFFileEditPanel.loadFile(encoding);
            }

            if (activeDocument instanceof GroovyTextFileEditPanel) {
                final GroovyTextFileEditPanel textFileEditPanel = (GroovyTextFileEditPanel) activeDocument;
                textFileEditPanel.loadFile(encoding);
            }
        }
    }

    public void showWindow() {
        showWindow(null);
    }

    public void showWindow(GroovyScriptEditorPreferences preferences) {
        if (isVisible())
            return;

        setIconImage(new ImageIcon(getClass().getResource("/icons/script.png")).getImage());
        setTitle("Script Editor");
        setContentPane(contentPanel);

        if (preferences != null) {

            if (preferences.getWindowState() != null)
                setExtendedState(preferences.getWindowState());

            if (getExtendedState() == NORMAL) {
                if (preferences.getWindowLocation() != null && preferences.getWindowSize() != null) {
                    setLocation(preferences.getWindowLocation());
                    setSize(preferences.getWindowSize());
                } else {
                    setSize(new Dimension(800, 600));
                    setLocationRelativeTo(null);
                }
            }

            for (File file : preferences.getRecentFiles())
                if (file.exists())
                    doOpen(file);

        } else {
            setSize(new Dimension(800, 600));
            setLocationRelativeTo(null);
        }

        if (openedFiles.isEmpty())
            doNew();

        setVisible(true);
    }

    public GroovyScriptEditorPreferences getPreferences() {
        final GroovyScriptEditorPreferences preferences = new GroovyScriptEditorPreferences();
        preferences.setWindowSize(getSize());
        preferences.setWindowLocation(getLocation());
        preferences.setWindowState(getExtendedState());

        for (File file : openedFiles.values())
            if (file != null && file.exists())
                preferences.getRecentFiles().add(file);

        return preferences;
    }


}
