package com.camunda.consulting;

import com.camunda.consulting.listener.ExternalTaskJobCreateListener;
import com.camunda.consulting.listener.FireEventJobCreateListener;
import com.camunda.consulting.listener.PayloadGeneratorListener;
import com.camunda.consulting.listener.UserTaskCompleteJobCreateListener;
import org.camunda.bpm.engine.delegate.DelegateListener;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.bpmn.behavior.IntermediateThrowNoneEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.TaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.core.model.CoreModelElement;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.engine.impl.variable.VariableDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public class SimulationParseListener implements BpmnParseListener {

  private static final Logger LOG = LoggerFactory.getLogger(SimulationParseListener.class);

  public static class NoOpActivityBehavior extends TaskActivityBehavior {

  }

  @Override
  public void parseProcess(Element processElement, ProcessDefinitionEntity processDefinition) {
    stripExecutionListeners(processDefinition);
  }

  @Override
  public void parseStartEvent(Element startEventElement, ScopeImpl scope, ActivityImpl startEventActivity) {
    addPayloadGeneratingListener(startEventActivity);

    // TODO: create some fancy job
  }

  @Override
  public void parseExclusiveGateway(Element exclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
    addPayloadGeneratingListener(activity);
  }

  @Override
  public void parseInclusiveGateway(Element inclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
    addPayloadGeneratingListener(activity);
  }

  @Override
  public void parseParallelGateway(Element parallelGwElement, ScopeImpl scope, ActivityImpl activity) {
    addPayloadGeneratingListener(activity);
  }

  @Override
  public void parseScriptTask(Element scriptTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addPayloadGeneratingListener(activity);

    activity.setActivityBehavior(new NoOpActivityBehavior());
  }

  @Override
  public void parseServiceTask(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addPayloadGeneratingListener(activity);

    String type = serviceTaskElement.attributeNS(BpmnParse.CAMUNDA_BPMN_EXTENSIONS_NS, BpmnParse.TYPE);
    if (type.equalsIgnoreCase("external")) {
      addExternalTaskCompleteJobCreatingListener(activity);

      addFireEventJobCreatingListener(activity);
    } else {
      // strip behavior for everything but external task
      activity.setActivityBehavior(new NoOpActivityBehavior());
    }
  }

  @Override
  public void parseBusinessRuleTask(Element businessRuleTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addPayloadGeneratingListener(activity);

    // strip behavior for everything but DMN
    String decisionRef = businessRuleTaskElement.attributeNS(BpmnParse.CAMUNDA_BPMN_EXTENSIONS_NS, "decisionRef");
    if (decisionRef == null) {
      activity.setActivityBehavior(new NoOpActivityBehavior());
    }
  }

  @Override
  public void parseTask(Element taskElement, ScopeImpl scope, ActivityImpl activity) {
    addPayloadGeneratingListener(activity);
  }

  @Override
  public void parseManualTask(Element manualTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addPayloadGeneratingListener(activity);
  }

  @Override
  public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addPayloadGeneratingListener(activity);

    addFireEventJobCreatingListener(activity);

    ((UserTaskActivityBehavior) activity.getActivityBehavior()).getTaskDefinition().getTaskListeners().clear();

    addUserTaskCompleteJobCreatingListener(activity);

  }

  @Override
  public void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl activity) {
  }

  @Override
  public void parseBoundaryTimerEventDefinition(Element timerEventDefinition, boolean interrupting, ActivityImpl timerActivity) {
  }

  @Override
  public void parseBoundaryErrorEventDefinition(Element errorEventDefinition, boolean interrupting, ActivityImpl activity,
      ActivityImpl nestedErrorEventActivity) {
  }

  @Override
  public void parseSubProcess(Element subProcessElement, ScopeImpl scope, ActivityImpl activity) {
    addPayloadGeneratingListener(activity);
    addFireEventJobCreatingListener(activity);
  }

  @Override
  public void parseCallActivity(Element callActivityElement, ScopeImpl scope, ActivityImpl activity) {
    addPayloadGeneratingListener(activity);
    addFireEventJobCreatingListener(activity);
  }

  @Override
  public void parseProperty(Element propertyElement, VariableDeclaration variableDeclaration, ActivityImpl activity) {
  }

  @Override
  public void parseSequenceFlow(Element sequenceFlowElement, ScopeImpl scopeElement, TransitionImpl transition) {
  }

  @Override
  public void parseSendTask(Element sendTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addPayloadGeneratingListener(activity);
    activity.setActivityBehavior(new NoOpActivityBehavior());
  }

  @Override
  public void parseMultiInstanceLoopCharacteristics(Element activityElement, Element multiInstanceLoopCharacteristicsElement, ActivityImpl activity) {
  }

  @Override
  public void parseIntermediateTimerEventDefinition(Element timerEventDefinition, ActivityImpl timerActivity) {
  }

  @Override
  public void parseRootElement(Element rootElement, List<ProcessDefinitionEntity> processDefinitions) {
  }

  @Override
  public void parseReceiveTask(Element receiveTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addPayloadGeneratingListener(activity);

    addFireEventJobCreatingListener(activity);
  }

  @Override
  public void parseIntermediateSignalCatchEventDefinition(Element signalEventDefinition, ActivityImpl signalActivity) {
  }

  @Override
  public void parseIntermediateMessageCatchEventDefinition(Element messageEventDefinition, ActivityImpl nestedActivity) {
  }

  @Override
  public void parseBoundarySignalEventDefinition(Element signalEventDefinition, boolean interrupting, ActivityImpl signalActivity) {
  }

  @Override
  public void parseEventBasedGateway(Element eventBasedGwElement, ScopeImpl scope, ActivityImpl activity) {
    addPayloadGeneratingListener(activity);
  }

  @Override
  public void parseTransaction(Element transactionElement, ScopeImpl scope, ActivityImpl activity) {
    addPayloadGeneratingListener(activity);

    addFireEventJobCreatingListener(activity);
  }

  @Override
  public void parseCompensateEventDefinition(Element compensateEventDefinition, ActivityImpl compensationActivity) {
  }

  @Override
  public void parseIntermediateThrowEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
    addPayloadGeneratingListener(activity);

    // strip behavior only for message throw events
    Element messageEventDefinitionElement = intermediateEventElement.element(BpmnParse.MESSAGE_EVENT_DEFINITION);
    if (messageEventDefinitionElement != null) {
      activity.setActivityBehavior(new IntermediateThrowNoneEventActivityBehavior());
    }
  }

  @Override
  public void parseIntermediateCatchEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
    addPayloadGeneratingListener(activity);

    addFireEventJobCreatingListener(activity);
  }

  @Override
  public void parseBoundaryEvent(Element boundaryEventElement, ScopeImpl scopeElement, ActivityImpl nestedActivity) {
    addPayloadGeneratingListener(nestedActivity);
  }

  @Override
  public void parseBoundaryMessageEventDefinition(Element element, boolean interrupting, ActivityImpl messageActivity) {

  }

  @Override
  public void parseBoundaryEscalationEventDefinition(Element escalationEventDefinition, boolean interrupting, ActivityImpl boundaryEventActivity) {
  }

  @Override
  public void parseBoundaryConditionalEventDefinition(Element element, boolean interrupting, ActivityImpl conditionalActivity) {
  }

  @Override
  public void parseIntermediateConditionalEventDefinition(Element conditionalEventDefinition, ActivityImpl conditionalActivity) {
  }

  @Override
  public void parseConditionalStartEventForEventSubprocess(Element element, ActivityImpl conditionalActivity, boolean interrupting) {
    addPayloadGeneratingListener(conditionalActivity);
  }

  private void addPayloadGeneratingListener(ActivityImpl activity) {
    activity.addBuiltInListener(ExecutionListener.EVENTNAME_END, PayloadGeneratorListener.instance());
  }

  private void addExternalTaskCompleteJobCreatingListener(ScopeImpl scope) {
    scope.addBuiltInListener(ExecutionListener.EVENTNAME_START, ExternalTaskJobCreateListener.instance());

  }

  private void addUserTaskCompleteJobCreatingListener(ActivityImpl activity) {

    ((UserTaskActivityBehavior) activity.getActivityBehavior()).getTaskDefinition().addTaskListener(TaskListener.EVENTNAME_CREATE, UserTaskCompleteJobCreateListener.instance());

  }

  private void addFireEventJobCreatingListener(ActivityImpl activity) {
    LOG.debug("Adding event subscription jobs to " + activity);
    activity.addBuiltInListener(ExecutionListener.EVENTNAME_START, FireEventJobCreateListener.instance());
  }

  private void stripExecutionListeners(ProcessDefinitionEntity processDefinitionEntity) {

    processDefinitionEntity.getActivities().forEach(this::stripExecutionListeners);

    stripNonBuiltinListeners(processDefinitionEntity);

  }

  private void stripExecutionListeners(ActivityImpl activity) {
    stripNonBuiltinListeners(activity);

    List<ActivityImpl> children = activity.getActivities();

    // stop recursion
    if (children.isEmpty()) {
      return;
    }

    // recurse
    children.stream().forEach(this::stripExecutionListeners);

  }

  private void stripNonBuiltinListeners(CoreModelElement element) {
    element.getListeners().keySet().forEach(eventType -> {
      for (Iterator<DelegateListener<?>> i = element.getListeners(eventType).iterator(); i.hasNext();) {
        DelegateListener<?> currentListener = i.next();
        if (!element.getBuiltInListeners(eventType).contains(currentListener)) {
          i.remove();
        }
      }
    });
  }

}
