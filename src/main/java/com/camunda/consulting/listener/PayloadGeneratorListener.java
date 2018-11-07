package com.camunda.consulting.listener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camunda.consulting.ModelPropertyUtil;
import com.camunda.consulting.SimulatorPlugin;

public class PayloadGeneratorListener extends AbstractTimerJobCreator implements ExecutionListener {
  static final Logger LOG = LoggerFactory.getLogger(PayloadGeneratorListener.class);

  private static PayloadGeneratorListener INSTANCE = null;
  private static Map<BaseElement, Work[]> generatePayloadPropertyCache = new HashMap<>();

  public static PayloadGeneratorListener instance() {
    if (INSTANCE == null) {
      INSTANCE = new PayloadGeneratorListener();
    }
    return INSTANCE;
  }

  @Override
  public void notify(DelegateExecution execution) throws Exception {
    Work[] workList = getGeneratePayloadValuesOrdered(execution.getBpmnModelElementInstance(), ModelPropertyUtil.CAMUNDA_PROPERTY_SIM_GENERATE_PAYLOAD);

    for (Work work : workList) {

      LOG.debug("Setting variable with name evaluated from '{}' to value evaluated from '{}'", work.variableExpression, work.valueExpression);

      String variableName = work.getVariableExpression().getValue(execution).toString().trim();
      Object value = work.getValueExpression().getValue(execution);

      LOG.debug("Setting variable '{}' to '{}'", variableName, value);

      execution.setVariable(variableName, value);
    }
  }

  /*
   * Cached read of "simulateSetVariable"-extensions for the given element.
   */
  public static Work[] getGeneratePayloadValuesOrdered(BaseElement element, String camundaPropertyName) {
    Work[] values = generatePayloadPropertyCache.get(element);
    if (values == null) {
      String[] expressions = ModelPropertyUtil.readCamundaPropertyMulti(element, camundaPropertyName).toArray(new String[] {});
      values = new Work[expressions.length];

      DirectedAcyclicGraph<Work, Object> graph = new DirectedAcyclicGraph<>(Object.class);
      for (int i = 0; i < expressions.length; i++) {
        values[i] = new Work(expressions[i]);
        graph.addVertex(values[i]);
      }
      for (Work currentWork : values) {
        for (Work otherWork : values) {
          if (currentWork.valueExpression.getExpressionText().matches(".*\\W" + Pattern.quote(otherWork.variableExpression.getExpressionText()) + "\\W.*")) {
            try {
              graph.addEdge(otherWork, currentWork);
            } catch (IllegalArgumentException e) {
              LOG.warn("Possible cycle in simulateSetVariable-dependencies detected when checking '{}'", currentWork.valueExpression);
            }
          }
        }
      }

      int i = 0;
      for (Iterator<Work> iterator = graph.iterator(); iterator.hasNext();) {
        Work next = iterator.next();
        values[i++] = next;
      }
      generatePayloadPropertyCache.put(element, values);
    }
    return values;
  }

  public static class Work {
    private Expression variableExpression;
    private Expression valueExpression;

    public Work(String setVariableValue) {
      String[] split = setVariableValue.split("=", 2);

      if (split.length != 2) {
        throw new RuntimeException("Expression does not evaluate to proper simulateSetVariable command: " + setVariableValue);
      }

      variableExpression = SimulatorPlugin.getProcessEngineConfiguration().getExpressionManager().createExpression(split[0]);
      valueExpression = SimulatorPlugin.getProcessEngineConfiguration().getExpressionManager().createExpression(split[1]);
    }
    
    public Expression getVariableExpression() {
      return variableExpression;
    }
    
    public Expression getValueExpression() {
      return valueExpression;
    }
  }
}
