package com.camunda.consulting;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;

public class EventSubscriptionJobCreateListener implements ExecutionListener {

  private static EventSubscriptionJobCreateListener INSTANCE = null;

  public static EventSubscriptionJobCreateListener instance() {
    if (INSTANCE == null) {
      INSTANCE = new EventSubscriptionJobCreateListener();
    }
    return INSTANCE;
  }

  @Override
  public void notify(DelegateExecution execution) throws Exception {
    ((ExecutionEntity) execution).getEventSubscriptions().stream() //
        .filter(eventSubscription -> EventType.MESSAGE.name().equals(eventSubscription.getEventType())
            || EventType.SIGNAL.name().equals(eventSubscription.getEventType())) //
        .forEach(this::createEventSubscriptionJob);
  }

  private void createEventSubscriptionJob(EventSubscriptionEntity eventSubscription) {
    // TODO
  }
}
