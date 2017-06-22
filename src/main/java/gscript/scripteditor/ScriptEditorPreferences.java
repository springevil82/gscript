package gscript.scripteditor;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class ScriptEditorPreferences {

    public static final String DEFAULT_PROPERTIES_FILE = "gscript.properties";

    private static final String PROPERTY_WINDOW_LOCATION = "windowLocation";
    private static final String PROPERTY_WINDOW_SIZE = "windowSize";
    private static final String PROPERTY_WINDOW_STATE = "windowState";
    private static final String PROPERTY_RECENT_FILE = "recentFile.";
    private static final String PROPERTY_USER_ENCODING = "encodings";
    private static final String PROPERTY_OUTPUT_DIVIDER_LOCATION = "outputDividerLocation";

    private String userEncodings;
    private Point windowLocation;
    private Dimension windowSize;
    private Integer windowState;
    private Integer outputDividerLocation;

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

    public String getUserEncodings() {
        return userEncodings;
    }

    public void setUserEncodings(String userEncodings) {
        this.userEncodings = userEncodings;
    }

    public Integer getOutputDividerLocation() {
        return outputDividerLocation;
    }

    public void setOutputDividerLocation(Integer outputDividerLocation) {
        this.outputDividerLocation = outputDividerLocation;
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

            userEncodings = properties.getProperty(PROPERTY_USER_ENCODING);

            final String outputDividerLocationValue = properties.getProperty(PROPERTY_OUTPUT_DIVIDER_LOCATION);
            if (outputDividerLocationValue != null) {
                try {
                    outputDividerLocation = Integer.parseInt(outputDividerLocationValue);
                } catch (Exception e) {
                    outputDividerLocation = null;
                }
            }

            recentFiles.clear();
            for (String propName : properties.stringPropertyNames())
                if (propName.startsWith(PROPERTY_RECENT_FILE)) {
                    final File recentFile = new File(properties.getProperty(propName));
                    if (recentFile.exists())
                        recentFiles.add(recentFile);
                }

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
        if (userEncodings != null)
            properties.setProperty(PROPERTY_USER_ENCODING, userEncodings);
        if (outputDividerLocation != null)
            properties.setProperty(PROPERTY_OUTPUT_DIVIDER_LOCATION, String.valueOf(outputDividerLocation));

        for (File recentFile : getRecentFiles())
            properties.setProperty(PROPERTY_RECENT_FILE + recentFile.getName(), recentFile.getAbsolutePath());

        try (OutputStream outputStream = new FileOutputStream(file)) {
            properties.store(outputStream, "ScriptEditor");
        } catch (IOException e) {
            throw new RuntimeException("Preferences store error", e);
        }
    }
}
