package com.camunda.consulting.listener;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.el.Expression;

import java.util.Date;
import java.util.Optional;

public class ExternalTaskJobCreateListener extends AbstractJobCreateListener implements ExecutionListener

        private static ExternalTaskJobCreateListener INSTANCE = null;

        public static ExternalTaskJobCreateListener instance() {
          if (INSTANCE == null) {
            INSTANCE = new ExternalTaskJobCreateListener();
          }
          return INSTANCE;
        }

        @Override
        public void notify(DelegateExecution execution) throws Exception {

          Optional<Expression> nextFireExpression = getCachedNextFireExpression(execution, execution.getCurrentActivityId());

          if(nextFireExpression.isPresent()) {
            Date dueDate = (Date) nextFireExpression.get().getValue(execution);
            createJobForExternalTaskCompletion(execution, dueDate);
          }

        }

        private void createJobForExternalTaskCompletion(DelegateExecution execution, Date dueDate) {
        }

} {
}
