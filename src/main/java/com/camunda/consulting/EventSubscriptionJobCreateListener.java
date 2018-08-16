package com.camunda.consulting;

import java.util.Date;
import java.util.Optional;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventSubscriptionJobCreateListener extends AbstractJobCreateListener implements ExecutionListener {
  static final Logger LOG = LoggerFactory.getLogger(EventSubscriptionJobCreateListener.class);

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

    Optional<Expression> nextFireExpression = getCachedNextFireExpression(execution, activity);

    // create timer job only if we have the time configured... makes sense
    if (nextFireExpression.isPresent()) {
      createTimerJob(execution, FireEventJobHandler.TYPE, (Date) nextFireExpression.get().getValue(execution),
          new FireEventJobHandler.FireEventJobHandlerConfiguration(eventSubscription.getEventType(), eventSubscription.getEventName()));
    }
  }
}
