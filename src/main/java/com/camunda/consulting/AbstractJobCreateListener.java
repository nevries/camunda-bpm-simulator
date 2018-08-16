package com.camunda.consulting;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractJobCreateListener {
  static final Logger LOG = LoggerFactory.getLogger(AbstractJobCreateListener.class);

  private Map<String, Optional<Expression>> nextFireExpressionCache = new HashMap<>();

  public AbstractJobCreateListener() {
    super();
  }

  protected Optional<Expression> getCachedNextFireExpression(ExecutionEntity execution, ActivityImpl activity) {
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

  protected void createTimerJob(ExecutionEntity execution, String jobHandlertype, Date duedate, FireEventJobHandler.FireEventJobHandlerConfiguration jobHandlerConfiguration) {
    TimerEntity timer = new TimerEntity();
    ProcessDefinitionEntity processDefinition = execution.getProcessDefinition();
  
    timer.setExecution(execution);
    timer.setDuedate(duedate);
    timer.setJobHandlerType(jobHandlertype);
    timer.setProcessDefinitionKey(processDefinition.getKey());
    timer.setDeploymentId(processDefinition.getDeploymentId());
    timer.setJobHandlerConfiguration(
        jobHandlerConfiguration);
  
    Context.getCommandContext().getJobManager().schedule(timer);
  }

}