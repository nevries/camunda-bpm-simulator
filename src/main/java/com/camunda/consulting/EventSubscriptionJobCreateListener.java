package com.camunda.consulting;

import java.util.Optional;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventSubscriptionJobCreateListener implements ExecutionListener {
  private static final Logger LOG = LoggerFactory.getLogger(EventSubscriptionJobCreateListener.class);

  private static EventSubscriptionJobCreateListener INSTANCE = null;

  public static EventSubscriptionJobCreateListener instance() {
    if (INSTANCE == null) {
      INSTANCE = new EventSubscriptionJobCreateListener();
    }
    return INSTANCE;
  }

  @Override
  public void notify(DelegateExecution execution) throws Exception {
    LOG.debug(this + " called");
    ((ExecutionEntity) execution).getEventSubscriptions().stream() //
        .filter(eventSubscription -> EventType.MESSAGE.name().equals(eventSubscription.getEventType())
            || EventType.SIGNAL.name().equals(eventSubscription.getEventType())) //
        .forEach(this::createEventSubscriptionJob);
  }

  private void createEventSubscriptionJob(EventSubscriptionEntity eventSubscription) {
    LOG.debug("creating job for " + eventSubscription.getActivityId());
    
    ExecutionEntity execution = eventSubscription.getExecution();
    ActivityImpl activity = eventSubscription.getActivity();

    ModelElementInstance modelElementInstance = execution.getBpmnModelInstance().getModelElementById(activity.getActivityId());
    Optional<String> nextFire = ModelPropertyUtil.getNextFire(modelElementInstance);

    // no property set, no simulation
    if (!nextFire.isPresent()) {
      return;
    }

    Expression nextFireExpression = SimulatorPlugin.getProcessEngineConfiguration().getExpressionManager().createExpression(nextFire.get());

    // TODO
    System.err.println("Create job for " + eventSubscription.getEventType() + " with time expression " + nextFire + " which evaluates to "
        + nextFireExpression.getValue(execution));

  }
}
