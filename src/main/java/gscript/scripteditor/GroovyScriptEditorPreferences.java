package gscript.scripteditor;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class GroovyScriptEditorPreferences {

    public static final String DEFAULT_PROPERTIES_FILE = "gscript.properties";

    private static final String PROPERTY_WINDOW_LOCATION = "windowLocation";
    private static final String PROPERTY_WINDOW_SIZE = "windowSize";
    private static final String PROPERTY_WINDOW_STATE = "windowState";
    private static final String PROPERTY_RECENT_FILE = "recentFile.";

    private Point windowLocation;
    private Dimension windowSize;
    private Integer windowState;

    private final List<File> recentFiles = new ArrayList<>();

    public Dimension getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(Dimension windowSize) {
        this.windowSize = windowSize;
    }

    public Integer getWindowState() {
        return windowState;
    }

    public void setWindowState(Integer windowState) {
        this.windowState = windowState;
    }

    public void setWindowLocation(Point windowLocation) {
        this.windowLocation = windowLocation;
    }

    public Point getWindowLocation() {
        return windowLocation;
    }

    public List<File> getRecentFiles() {
        return recentFiles;
    }

    public void load(File file) {
        if (!file.exists())
            return;

        final Properties properties = new Properties();
        try (InputStream inputStream = new FileInputStream(file)) {
            properties.load(inputStream);

            final String windowLocationValue = properties.getProperty(PROPERTY_WINDOW_LOCATION);
            if (windowLocationValue != null)
                windowLocation = new Point(Integer.parseInt(windowLocationValue.split(",")[0]), Integer.parseInt(windowLocationValue.split(",")[1]));

            final String windowSizeValue = properties.getProperty(PROPERTY_WINDOW_SIZE);
            if (windowSizeValue != null)
                windowSize = new Dimension(Integer.parseInt(windowSizeValue.split(",")[0]), Integer.parseInt(windowSizeValue.split(",")[1]));

            final String windowStateValue = properties.getProperty(PROPERTY_WINDOW_STATE);
            if (windowStateValue != null)
                windowState = Integer.parseInt(windowStateValue);

            recentFiles.clear();
            for (String propName : properties.stringPropertyNames())
                if (propName.startsWith(PROPERTY_RECENT_FILE))
                    recentFiles.add(new File(propName.substring(PROPERTY_RECENT_FILE.length())));

        } catch (Exception e) {
            System.out.println("Preferences load error");
        }
    }

    public void save(File file) {
        final Properties properties = new Properties();

        if (windowLocation != null)
            properties.setProperty(PROPERTY_WINDOW_LOCATION, windowLocation.x + "," + windowLocation.y);
        if (windowSize != null)
            properties.setProperty(PROPERTY_WINDOW_SIZE, windowSize.width + "," + windowSize.height);
        if (windowState != null)
            properties.setProperty(PROPERTY_WINDOW_STATE, String.valueOf(windowState));

        for (File recentFile : getRecentFiles())
            properties.setProperty(PROPERTY_RECENT_FILE + recentFile.getName(), recentFile.getAbsolutePath());

        try (OutputStream outputStream = new FileOutputStream(file)) {
            properties.store(outputStream, "GroovyScriptEditor");
        } catch (IOException e) {
            throw new RuntimeException("Preferences store error", e);
        }
    }
}
