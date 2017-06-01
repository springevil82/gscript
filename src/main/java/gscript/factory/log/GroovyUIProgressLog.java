package gscript.factory.log;

import gscript.Factory;
import gscript.factory.ui.GroovyUIProgressLogFrame;

public class GroovyUIProgressLog extends GroovyFileProgressLog {

    private final GroovyUIProgressLogFrame logFrame;

    public GroovyUIProgressLog(Factory factory) {
        super(factory);

        logFrame = new GroovyUIProgressLogFrame(factory);
        logFrame.getProgressLogPanel().setProgressIndeterminate(true);
    }

    @Override
    protected void writeToLog(String prefix, String message) {
        super.writeToLog(prefix, message);

        if (logFrame != null)
            logFrame.getProgressLogPanel().appendLog(prefix, message);
    }

    @Override
    public void setProgressText(String text) {
        super.setProgressText(text);
        logFrame.getProgressLogPanel().setProgressText(text);
    }

    @Override
    public void setProgressDetailText(String text) {
        super.setProgressDetailText(text);
        logFrame.getProgressLogPanel().setProgressDetailText(text);
    }

    @Override
    public void setProgressMax(int maxValue) {
        super.setProgressMax(maxValue);
        logFrame.getProgressLogPanel().setProgressMax(maxValue);
    }

    @Override
    public void moveProgress() {
        super.moveProgress();
        logFrame.getProgressLogPanel().moveProgress();
    }

    @Override
    public void setProgressInfinity(boolean infinityProgress) {
        super.setProgressInfinity(infinityProgress);
        logFrame.getProgressLogPanel().setProgressIndeterminate(infinityProgress);
    }

    @Override
    public boolean isCancelled() {
        return logFrame.getProgressLogPanel().isCancelled();
    }

    @Override
    public void close() throws Exception {
        super.close();
        logFrame.close();
    }
}
