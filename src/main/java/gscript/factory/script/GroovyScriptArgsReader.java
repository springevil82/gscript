package gscript.factory.script;

import gscript.Factory;
import gscript.GroovyException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class GroovyScriptArgsReader {

    private final Factory factory;

    public GroovyScriptArgsReader(Factory factory) {
        this.factory = factory;
    }

    public String[] getAllArgs() {
        if (factory.getThisScript() != null)
            return (String[]) factory.getThisScript().getBinding().getVariable("args");

        if (factory.getThisScriptFile() != null)
            return factory.getThisScriptFile().getArgs();

        throw new GroovyException("Script is not defined");
    }

    public Map<String, String> getArgsMap() {
        final String[] args = getAllArgs();
        final Map<String, String> argMap = new LinkedHashMap<>();

        for (String a : args) {
            if (a.startsWith("-V")) {
                final String arg = a.substring(2);
                final int indexOf = arg.indexOf("=");
                if (indexOf != -1)
                    argMap.put(arg.substring(0, indexOf), arg.substring(indexOf + 1));
                else
                    argMap.put(arg, null);
            }
        }

        return argMap;
    }

    public String[] getArgNames() {
        final Set<String> set = getArgsMap().keySet();
        return set.toArray(new String[set.size()]);
    }

    public boolean containsArg(String argName) {
        return getArgsMap().containsKey(argName);
    }

    public String getArgValue(String argName) {
        return getArgsMap().get(argName);
    }

}
