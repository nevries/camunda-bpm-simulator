package com.camunda.consulting.listener;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PayloadGeneratorListener extends AbstractJobCreateListener implements ExecutionListener {
  static final Logger LOG = LoggerFactory.getLogger(PayloadGeneratorListener.class);

  private static PayloadGeneratorListener INSTANCE = null;

  public static PayloadGeneratorListener instance() {
    if (INSTANCE == null) {
      INSTANCE = new PayloadGeneratorListener();
    }
    return INSTANCE;
  }

  @Override
  public void notify(DelegateExecution execution) throws Exception {
    
  }
}
