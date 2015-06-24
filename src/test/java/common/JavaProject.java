package common;

import com.thoughtworks.gauge.Table;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static common.Util.capitalize;
import static common.Util.getUniqueName;

public class JavaProject extends GaugeProject {
    public static final String DEFAULT_AGGREGATION = "AND";
    private static String stepImplementationsDir = "src/test/java";

    public JavaProject(File projectDir) {
        super(projectDir, "java");
    }

    public Map<String, String> getLanguageSpecificFiles() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("src", "dir");
        map.put("libs", "dir");
        map.put("src/test/java/StepImplementation.java", "file");
        map.put("env/default/java.properties", "file");
        return map;
    }

    public void implementStep(String stepText, String implementation, boolean appendCode) throws Exception {
        List<String> paramTypes = new ArrayList<String>();
        StepValueExtractor.StepValue stepValue = new StepValueExtractor().getFor(stepText);
        String className = getUniqueName();
        StringBuilder classText = new StringBuilder();
        classText.append("import com.thoughtworks.gauge.Step;\n");
        classText.append("public class ").append(className).append("{\n");
        classText.append("@Step(\"").append(stepValue.value).append("\")\n");
        classText.append("public void ").append("stepImplementation(");
        for (int i = 0; i < stepValue.paramCount; i++) {
            if (i + 1 == stepValue.paramCount) {
                classText.append("String param").append(i);
            } else {
                classText.append("String param").append(i).append(", ");
            }
            paramTypes.add("String");
        }
        implementation = getStepImplementation(stepValue, implementation, paramTypes, appendCode);
        classText.append(") {\n").append(implementation).append("\n}\n");
        classText.append("}");
        Util.writeToFile(Util.combinePath(getStepImplementationsDir(), className + ".java"), classText.toString());
    }

    @Override
    public void refactorStep(String oldStep, String newStep) throws IOException, InterruptedException {
        boolean exitStatus = currentProject.executeRefactor(oldStep, newStep);
        if (!exitStatus) {
            System.out.println(currentProject.getLastProcessStdout());
            System.out.println(currentProject.getLastProcessStderr());
        }
    }

    @Override
    public String getDataStoreWriteStatement(List<String> row) {
        String dataStoreType = row.get(3);
        String key = row.get(1);
        String value = row.get(2);
        return "com.thoughtworks.gauge.datastore.DataStoreFactory.get" + dataStoreType + "DataStore().put(\"" + key + "\",\"" + value + "\");";
    }

    @Override
    public String getDataStorePrintValueStatement(List<String> row) {
        String dataStoreType = row.get(3);
        String key = row.get(1);
        return "System.out.println(com.thoughtworks.gauge.datastore.DataStoreFactory.get" + dataStoreType + "DataStore().get(\"" + key + "\"));";
    }

    @Override
    public String getStepImplementation(StepValueExtractor.StepValue stepValue, String implementation, List<String> paramTypes, boolean appendCode) {
        StringBuilder builder = new StringBuilder();
        if (implementation.toLowerCase().equals(PRINT_PARAMS)) {
            builder.append("System.out.println(");
            for (int i = 0; i < stepValue.paramCount; i++) {
                if (paramTypes.get(i).toLowerCase().equals("string")) {
                    builder.append("\"param").append(i).append("=\"+").append("param").append(i);
                    if (i != stepValue.paramCount - 1) {
                        builder.append("+\",\"+");
                    }
                }
            }
            builder.append(");\n");
        } else if (implementation.toLowerCase().equals(THROW_EXCEPTION)) {
            return "throw new RuntimeException();";
        } else {
            if (appendCode) {
                builder.append(implementation);
            } else {
                builder.append("System.out.println(").append(implementation).append(");\n");
            }
        }
        return builder.toString();
    }

    @Override
    public void createHookWithPrint(String hookLevel, String hookType, String printStatement) throws Exception {
        String implementation = String.format("System.out.println(\"%s\");", printStatement);
        String method = createHookMethod(hookLevel, hookType, implementation, DEFAULT_AGGREGATION, new ArrayList<String>());
        createHook(hookLevel, hookType, method);
    }

    @Override
    public void createHookWithException(String hookLevel, String hookType) throws IOException {
        createHook(hookLevel, hookType, createHookMethod(hookLevel, hookType, "throw new RuntimeException();", DEFAULT_AGGREGATION, new ArrayList<String>()));
    }

    @Override
    public void createHooksWithTagsAndPrintMessage(String hookLevel, String hookType, String printString, String aggregation, Table tagsTable) throws IOException {
        String implementation = String.format("System.out.println(\"%s\");", printString);
        String method = createHookMethod(hookLevel, hookType, implementation, aggregation, Util.toList(tagsTable, 0));
        createHook(hookLevel, hookType, method);
    }

    private void createHook(String hookLevel, String hookType, String method) throws IOException {
        StringBuilder classText = new StringBuilder();
        classText.append(String.format("import com.thoughtworks.gauge.%s;\n", hookName(hookLevel, hookType)));
        classText.append("import com.thoughtworks.gauge.Operator;");
        String className = getUniqueName();
        classText.append("public class ").append(className).append("{\n");
        classText.append(method);
        classText.append("\n}");
        Util.writeToFile(Util.combinePath(getStepImplementationsDir(), className + ".java"), classText.toString());
    }

    private String createHookMethod(String hookLevel, String hookType, String implementation, String aggregation, List<String> tags) {
        StringBuilder methodText = new StringBuilder();
        String hookAttributes = isSuiteHook(hookLevel) ? "" : hookAttributesString(tags, aggregation);
        methodText.append(String.format("@%s(%s)\n", hookName(hookLevel, hookType), hookAttributes));
        methodText.append(String.format("public void hook() {\n"));
        methodText.append(String.format("%s\n", implementation));
        methodText.append("\n}\n");
        return methodText.toString();
    }

    private boolean isSuiteHook(String hookLevel) {
        return hookLevel.trim().equals("suite");
    }

    private String hookName(String hookLevel, String hookType) {
        return String.format("%s%s", capitalize(hookType), capitalize(hookLevel));
    }

    private String hookAttributesString(List<String> tags, String aggregation) {
        return String.format("tags = {%s}, tagAggregation = Operator.%s ", Util.commaSeparatedValues(Util.quotifyValues(tags)), aggregation);
    }

    private String getStepImplementationsDir() {
        return new File(getProjectDir(), "src/test/java").getAbsolutePath();
    }
}
