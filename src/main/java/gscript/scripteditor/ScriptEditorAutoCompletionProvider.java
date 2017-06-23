package gscript.scripteditor;

import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;
import gscript.Factory;
import gscript.factory.format.GroovyStringJoiner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ScriptEditorAutoCompletionProvider {

    private static Pattern DEF_VARIABLE = Pattern.compile("^(\\s+|)+def\\s+(.+)\\s+=\\s+(.+)(\\s+|)$");

    private Class<? extends Factory> factoryClass;
    private static final String FACTORY = "factory";
    private static final String OBJECT = "Object";

    private final Map<Class, ClassMeta> classesMetaData = new HashMap<>();

    private class ClassMeta {
        final Class c;
        final List<Field> fields;
        final List<Method> methods;
        final Map<String, List<MethodParams>> methodParamsMap = new HashMap<>();

        ClassMeta(Class c, List<Field> fields, List<Method> methods) {
            this.c = c;
            this.fields = fields;
            this.methods = methods;

            for (Method method : methods) {
                List<MethodParams> methodParamsList = methodParamsMap.get(method.getName());
                if (methodParamsList == null) {
                    methodParamsList = new ArrayList<>();
                    methodParamsMap.put(method.getName(), methodParamsList);
                }

                final MethodParams methodParams = new MethodParams();
                final Class<?>[] parameterTypes = method.getParameterTypes();
                final Paranamer paranamer = new CachingParanamer(new BytecodeReadingParanamer());
                final String[] parameterNames = paranamer.lookupParameterNames(method);

                for (int i = 0; i < parameterTypes.length; i++)
                    methodParams.add(new MethodParam(parameterNames[i], parameterTypes[i]));

                methodParamsList.add(methodParams);
            }
        }

    }

    private class MethodParam {
        String name;
        Class type;

        MethodParam(String name, Class type) {
            this.name = name;
            this.type = type;
        }
    }

    private class MethodParams extends ArrayList<MethodParam> {
    }

    public ScriptEditorAutoCompletionProvider(Class<? extends Factory> factoryClass) {
        this.factoryClass = factoryClass;
    }

    private ClassMeta getMeta(Class c) {
        if (classesMetaData.containsKey(c))
            return classesMetaData.get(c);

        final List<Field> publicFields = getAllPublicFields(c);
        final List<Method> publicMethods = getAllPublicMethods(c);

        final ClassMeta classMeta = new ClassMeta(c, publicFields, publicMethods);
        classesMetaData.put(c, classMeta);
        return classMeta;
    }

    public List<ScriptEditorAutoCompletion> getCompletionsForFields(List<Field> fields) {
        final List<ScriptEditorAutoCompletion> completions = new ArrayList<>();
        for (Field field : fields)
            completions.add(new ScriptEditorAutoCompletion(field.getName(), field.getType()));

        return completions;
    }

    public List<ScriptEditorAutoCompletion> getCompletionsForMethods(List<Method> methods) {
        final List<ScriptEditorAutoCompletion> completions = new ArrayList<>();

        for (Method method : methods) {
            final Class<?>[] parameterTypes = method.getParameterTypes();
            final GroovyStringJoiner stringJoiner = new GroovyStringJoiner(", ", "(", ")");
            for (Class aClass : parameterTypes)
                stringJoiner.add(aClass.getSimpleName());

            completions.add(new ScriptEditorAutoCompletion(method.getName() + stringJoiner, method.getReturnType()));
        }

        return completions;
    }

    public List<ScriptEditorAutoCompletion> getCompletionsFor(ClassMeta classMeta) {
        final List<ScriptEditorAutoCompletion> completions = new ArrayList<>();
        completions.addAll(getCompletionsForFields(classMeta.fields));
        completions.addAll(getCompletionsForMethods(classMeta.methods));
        return completions;
    }

    public static List<Field> getAllPublicFields(Class aClass) {
        final List<Field> list = new ArrayList<>();
        Field[] fields = aClass.getDeclaredFields();

        for (Field field : fields)
            if (Modifier.isPublic(field.getModifiers()))
                list.add(field);

        Class superClass = aClass.getSuperclass();
        if (superClass != null && !superClass.getSimpleName().equals(OBJECT)) {
            List<Field> superClassFields = getAllPublicFields(superClass);
            for (int i = 0; i < superClassFields.size(); i++) {
                final Field field = superClassFields.get(i);
                if (Modifier.isPublic(field.getModifiers()))
                    list.add(i, field);
            }
        }

        return list;
    }

    public static List<Method> getAllPublicMethods(Class aClass) {
        final List<Method> list = new ArrayList<>();
        Method[] methods = aClass.getDeclaredMethods();

        for (Method method : methods)
            if (Modifier.isPublic(method.getModifiers()))
                list.add(method);

        Class superClass = aClass.getSuperclass();
        if (superClass != null && !superClass.getSimpleName().equals(OBJECT)) {
            List<Method> superClassMethods = getAllPublicMethods(superClass);
            for (int i = 0; i < superClassMethods.size(); i++) {
                final Method method = superClassMethods.get(i);
                if (Modifier.isPublic(method.getModifiers()))
                    list.add(i, method);
            }
        }

        return list;
    }

    private int calcParamCount(String params) {
        return 0;
    }


    private Map<String, Class> getVariables(String allText) {
        final Map<String, Class> variables = new LinkedHashMap<>();
        final String[] lines = allText.split("\n");

        for (String line : lines) {
            final Matcher varMatcher = DEF_VARIABLE.matcher(line);
            if (varMatcher.find()) {
                final String varName = varMatcher.group(2);
                final String varDef = varMatcher.group(3);

                variables.put(varName, computeVariableType(varDef, variables));
            }
        }

        return variables;
    }

    private Class computeVariableType(String varDef, Map<String, Class> variables) {
        final List<String> tokens = parseTokens(varDef);

        String currentToken;
        Class currentTokenClass = null;
        for (int i = tokens.size(); i > 0; i--) {
            currentToken = tokens.get(i - 1).trim();

            if (FACTORY.equals(currentToken)) {
                currentTokenClass = factoryClass;
            } else if (variables.containsKey(currentToken)) {
                currentTokenClass = variables.get(currentToken);
            } else if (currentTokenClass != null) {
                if (currentToken.startsWith("."))
                    currentToken = currentToken.substring(1);

                if ("".equals(currentToken))
                    break;

                final ClassMeta meta = getMeta(currentTokenClass);

                if (currentToken.contains("(") && currentToken.contains(")")) {
                    final int indexOfStartParams = currentToken.indexOf("(");
                    final int indexOfEndParams = currentToken.indexOf(")");
                    final String methodName = currentToken.substring(0, indexOfStartParams);
                    final String params = currentToken.substring(indexOfStartParams + 1, indexOfEndParams);
                    final int paramCount = calcParamCount(params);

                    for (Method method : meta.methods) {
                        if (method.getName().equals(methodName)) {
                            currentTokenClass = method.getReturnType();
                            break;
                        }
                    }
                } else {
                    for (Field field : meta.fields) {
                        if (field.getName().equals(currentToken)) {
                            currentTokenClass = field.getType();
                            break;
                        }
                    }
                }
            }
        }

        return currentTokenClass;
    }

    public List<ScriptEditorAutoCompletion> getCompletionsFor(String enteredText, String allText) {
        final Map<String, Class> variables = getVariables(allText);

        String currentToken;
        Class currentTokenClass = null;
        final List<String> tokens = parseTokens(enteredText);

        if (tokens.size() == 1) {
            final List<ScriptEditorAutoCompletion> completions = new ArrayList<>();

            if (FACTORY.startsWith(tokens.get(0)))
                completions.add(new ScriptEditorAutoCompletion(FACTORY, factoryClass));

            // match other vars
            for (String varName : variables.keySet())
                if (varName.startsWith(tokens.get(0)))
                    completions.add(new ScriptEditorAutoCompletion(varName, variables.get(varName)));

            return completions;
        }

        for (int i = tokens.size(); i > 0; i--) {
            currentToken = tokens.get(i - 1);

            if (FACTORY.equals(currentToken)) {
                currentTokenClass = factoryClass;
            } else if (variables.containsKey(currentToken)) {
                currentTokenClass = variables.get(currentToken);
            } else if (currentTokenClass != null) {
                if (currentToken.startsWith("."))
                    currentToken = currentToken.substring(1);

                if ("".equals(currentToken))
                    break;

                final ClassMeta meta = getMeta(currentTokenClass);

                if (i == 1) {
                    final List<ScriptEditorAutoCompletion> completions = new ArrayList<>();

                    final List<Field> applicableFields = new ArrayList<>();
                    final List<Method> applicableMethods = new ArrayList<>();

                    for (Field field : meta.fields)
                        if (field.getName().startsWith(currentToken))
                            applicableFields.add(field);

                    for (Method method : meta.methods)
                        if (method.getName().startsWith(currentToken))
                            applicableMethods.add(method);

                    completions.addAll(getCompletionsForFields(applicableFields));
                    completions.addAll(getCompletionsForMethods(applicableMethods));

                    return completions;

                } else {
                    if (currentToken.contains("(") && currentToken.contains(")")) {
                        final int indexOfStartParams = currentToken.indexOf("(");
                        final int indexOfEndParams = currentToken.indexOf(")");
                        final String methodName = currentToken.substring(0, indexOfStartParams);
                        final String params = currentToken.substring(indexOfStartParams + 1, indexOfEndParams);
                        final int paramCount = calcParamCount(params);

                        for (Method method : meta.methods) {
                            if (method.getName().equals(methodName)) {
                                currentTokenClass = method.getReturnType();
                                break;
                            }
                        }
                    } else {
                        for (Field field : meta.fields) {
                            if (field.getName().equals(currentToken)) {
                                currentTokenClass = field.getType();
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (currentTokenClass != null) {
            final ClassMeta meta = getMeta(currentTokenClass);
            return getCompletionsFor(meta);
        }

        return null;
    }

    public List<String> parseTokens(String enteredText) {
        final List<String> tokens = new ArrayList<>();

        int lastTokenEnd = enteredText.length();
        int bracketOpened = 0;
        int bracketClosed = 0;

        char charAt;
        for (int i = enteredText.length() - 1; i > 0; i--) {
            charAt = enteredText.charAt(i);

            if (charAt == ')')
                bracketClosed++;

            if (charAt == '(') {
                bracketOpened++;
                if (bracketClosed != bracketOpened) {
                    tokens.add(enteredText.substring(i + 1, lastTokenEnd).trim());
                    return tokens;
                }
            }

            if ((charAt == '=') || (charAt == ' ')) {
                tokens.add(enteredText.substring(i + 1, lastTokenEnd).trim());
                return tokens;
            }

            if (charAt == '.') {
                tokens.add(enteredText.substring(i, lastTokenEnd).trim());
                lastTokenEnd = i;
            }
        }

        tokens.add(enteredText.substring(0, lastTokenEnd).trim());

        return tokens;
    }

    private String getParametersToolTip(List<MethodParams> methodParams, int highlightParamIndex) {
        final GroovyStringJoiner paramsJoiner = new GroovyStringJoiner("<br>", "<html>", "</html>");

        for (MethodParams params : methodParams) {
            final GroovyStringJoiner paramJoiner = new GroovyStringJoiner(", ");

            for (int i = 0; i < params.size(); i++) {
                if (i == highlightParamIndex)
                    paramJoiner.add("<b>" + params.get(i).type.getSimpleName() + " " + params.get(i).name + "</b>");
                else
                    paramJoiner.add(params.get(i).type.getSimpleName() + " " + params.get(i).name);
            }

            paramsJoiner.add(paramJoiner.toString());
        }

        return paramsJoiner.toString();
    }

/*
    public String getMethodParamsToolTip(Class methodClass, String methodName, int highlightParamIndex) {

    }
*/

/*
    public static void main(String[] args) {
        ScriptEditorAutoCompletionProvider provider = new ScriptEditorAutoCompletionProvider(Factory.class);
        Class<GroovyCSVFileReader> clazz = GroovyCSVFileReader.class;
        for (final Method method : getAllPublicMethods(clazz)) {
            List<MethodParam> methodParams = provider.getMethodParams(method);
            System.out.println(method.getName() + ": (" + provider.getParametersHint(methodParams, -1) + ")");
        }
    }
*/

}
