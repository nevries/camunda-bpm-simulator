package com.camunda.consulting;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventSubscriptionJobCreateListener implements ExecutionListener {
  private static final Logger LOG = LoggerFactory.getLogger(EventSubscriptionJobCreateListener.class);

  private static EventSubscriptionJobCreateListener INSTANCE = null;
  private Map<String, Optional<Expression>> nextFireExpressionCache = new HashMap<>();

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

    Optional<Expression> nextFireExpression = getCachedNextFireExpression(execution, activity);

    // no job if no configuration - fair enough
    if (!nextFireExpression.isPresent()) {
      return;
    }

    // create job
    TimerEntity timer = new TimerEntity();
    ProcessDefinitionEntity processDefinition = execution.getProcessDefinition();

    timer.setExecution(execution);
    timer.setDuedate((Date) nextFireExpression.get().getValue(execution));
    timer.setJobHandlerType(FireEventJobHandler.TYPE);
    timer.setProcessDefinitionKey(processDefinition.getKey());
    timer.setDeploymentId(processDefinition.getDeploymentId());
    timer.setJobHandlerConfiguration(
        new FireEventJobHandler.FireEventJobHandlerConfiguration(eventSubscription.getEventType(), eventSubscription.getEventName()));

    Context.getCommandContext().getJobManager().schedule(timer);
  }

  private Optional<Expression> getCachedNextFireExpression(ExecutionEntity execution, ActivityImpl activity) {
    Optional<Expression> nextFireExpression = nextFireExpressionCache.get(activity.getActivityId());
    if (nextFireExpression == null) {
      ModelElementInstance modelElementInstance = execution.getBpmnModelInstance().getModelElementById(activity.getActivityId());
      Optional<String> nextFire = ModelPropertyUtil.getNextFire(modelElementInstance);
      nextFireExpression = nextFire.map(SimulatorPlugin.getProcessEngineConfiguration().getExpressionManager()::createExpression);
      nextFireExpressionCache.put(activity.getActivityId(), nextFireExpression);
      LOG.debug("Return new expression");
    } else {
      LOG.debug("Return cached expression");
    }
    return nextFireExpression;
  }
}
