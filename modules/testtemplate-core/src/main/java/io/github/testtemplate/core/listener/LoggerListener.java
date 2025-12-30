package io.github.testtemplate.core.listener;

import io.github.testtemplate.TestListener;
import io.github.testtemplate.TestType;
import io.github.testtemplate.Variable;
import io.github.testtemplate.VariableType;
import io.github.testtemplate.core.logger.BetterVariableLogger;

import org.slf4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

public class LoggerListener implements TestListener {

  private static final Logger LOGGER = getLogger(LoggerListener.class);

  private static final String KEY_LOG_CONTEXT = "ca.guig.testtemplate.core.listener.LoggerListener.context";

  @Override
  public void before(Test test) {
    test.setAttribute(KEY_LOG_CONTEXT, new LogContext(test.getName(), test.getType()));
  }

  @Override
  public void after(Test test) {
    var context = (LogContext) test.getAttribute(KEY_LOG_CONTEXT);
    LOGGER.info("\n{}", buildReport(context));
  }

  @Override
  public void result(Test test, Object result) {
    var context = (LogContext) test.getAttribute(KEY_LOG_CONTEXT);
    context.setResult(result);
  }

  @Override
  public void exception(Test test, Throwable exception) {
    var context = (LogContext) test.getAttribute(KEY_LOG_CONTEXT);
    context.setException(exception);
  }

  @Override
  public void variable(Test test, String name, VariableType type, Object value, Map<String, Object> metadata) {
    var context = (LogContext) test.getAttribute(KEY_LOG_CONTEXT);
    context.addVariable(name, type, value, metadata);
  }

  static String buildReport(LogContext context) {
    StringBuilder sb = new StringBuilder();

    sb.append("================================================================================\n");
    sb.append("(").append(context.getType()).append(") ").append(context.getName()).append("\n");
    sb.append("\n");

    var variables = context.getVariables();
    if (!variables.isEmpty()) {
      int length = getLongerVariableLength(variables);

      sb.append("Variables:\n");
      variables.forEach(v -> {
        sb.append("  ").append(String.format("%-" + length + "s", v.getName()));
        sb.append(v.getType() == VariableType.MODIFIED ? " (M)" : "    ");
        sb.append(" = ").append(BetterVariableLogger.toValueString(v).indent(length + 9).trim());
        sb.append("\n");
      });
      sb.append("\n");
    }

    if (context.isExceptionThrown()) {
      sb.append("Exception Thrown:\n");
      var writer = new StringWriter();
      context.getException().printStackTrace(new PrintWriter(writer));
      sb.append(writer.toString().replace("\t", "    ").indent(2));

    } else {
      sb.append("Result:\n");
      sb.append("  ").append(context.getResult()).append("\n");
    }
    sb.append("================================================================================\n");

    return sb.toString();
  }

  private static int getLongerVariableLength(Collection<LogContextVariable> variables) {
    int length = 0;
    for (LogContextVariable variable : variables) {
      length = Math.max(variable.getName().length(), length);
    }
    return length;
  }

  static final class LogContext {
    private final String name;
    private final TestType type;
    private Object result;
    private boolean exceptionThrown;
    private Throwable exception;
    private final Map<String, LogContextVariable> variables = new LinkedHashMap<>();

    LogContext(String name, TestType type) {
      this.name = name;
      this.type = type;
    }

    public String getName() {
      return name;
    }

    public TestType getType() {
      return type;
    }

    public Object getResult() {
      return result;
    }

    public void setResult(Object result) {
      this.result = result;
    }

    public boolean isExceptionThrown() {
      return exceptionThrown;
    }

    public Throwable getException() {
      return exception;
    }

    public void setException(Throwable exception) {
      this.exception = exception;
      this.exceptionThrown = true;
    }

    public Collection<LogContextVariable> getVariables() {
      return variables.values();
    }

    public void addVariable(String name, VariableType type, Object value, Map<String, Object> metadata) {
      variables.put(name, new LogContextVariable(name, type, value, metadata));
    }
  }

  public static final class LogContextVariable implements Variable {
    private final String name;
    private final VariableType type;
    private final Object value;
    private final Map<String, Object> metadata = new HashMap<>();

    private LogContextVariable(String name, VariableType type, Object value, Map<String, Object> metadata) {
      this.name = name;
      this.type = type;
      this.value = value;
      this.metadata.putAll(metadata);
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public VariableType getType() {
      return type;
    }

    @Override
    public Object getValue() {
      return value;
    }

    @Override
    public Object getMetadata(String key) {
      return metadata.get(key);
    }

    @Override
    public Object getMetadata(String key, Object defaultValue) {
      return metadata.getOrDefault(key, defaultValue);
    }
  }
}
