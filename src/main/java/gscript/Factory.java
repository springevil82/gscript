package gscript;

import groovy.lang.Script;
import gscript.factory.database.GroovyDatabaseFactory;
import gscript.factory.date.GroovyDateFactory;
import gscript.factory.document.GroovyDocumentFactory;
import gscript.factory.file.GroovyFileFactory;
import gscript.factory.log.GroovyLogFactory;
import gscript.factory.number.GroovyNumberFactory;
import gscript.factory.process.GroovyProcessFactory;
import gscript.factory.proxy.GroovyProxyFactory;
import gscript.factory.random.GroovyRandomFactory;
import gscript.factory.report.GroovyReportFactory;
import gscript.factory.script.GroovyScriptFactory;
import gscript.factory.security.GroovySecurityFactory;
import gscript.factory.string.GroovyStringFactory;
import gscript.factory.transport.GroovyTransportFactory;
import gscript.factory.ui.GroovyUIFactory;

import java.util.LinkedHashSet;
import java.util.Set;

public class Factory {

    private Script thisScript;
    private ScriptFile thisScriptFile;

    final Set<AutoCloseable> autoCloseables = new LinkedHashSet<>();

    public Script getThisScript() {
        return thisScript;
    }

    public ScriptFile getThisScriptFile() {
        return thisScriptFile;
    }

    public void registerAutoCloseable(AutoCloseable autoCloseable) {
        autoCloseables.add(autoCloseable);
    }

    public Set<AutoCloseable> getAutoCloseables() {
        return autoCloseables;
    }

    public final GroovyDocumentFactory document = new GroovyDocumentFactory(this);
    public final GroovyFileFactory file = new GroovyFileFactory(this);
    public final GroovyLogFactory log = new GroovyLogFactory(this);
    public final GroovyDateFactory date = new GroovyDateFactory(this);
    public final GroovyScriptFactory script = new GroovyScriptFactory(this);
    public final GroovyProcessFactory process = new GroovyProcessFactory(this);
    public final GroovyTransportFactory net = new GroovyTransportFactory(this);
    public final GroovyDatabaseFactory database = new GroovyDatabaseFactory(this);
    public final GroovyUIFactory ui = new GroovyUIFactory(this);
    public final GroovySecurityFactory security = new GroovySecurityFactory(this);
    public final GroovyReportFactory report = new GroovyReportFactory(this);
    public final GroovyRandomFactory random = new GroovyRandomFactory(this);
    public final GroovyStringFactory string = new GroovyStringFactory(this);
    public final GroovyNumberFactory number = new GroovyNumberFactory(this);
    public final GroovyProxyFactory proxy = new GroovyProxyFactory(this);

    public Factory(Script script) {
        this.thisScript = script;
    }

    Factory(ScriptFile scriptFile) {
        this.thisScriptFile = scriptFile;
    }
}
