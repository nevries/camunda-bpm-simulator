package com.camunda.consulting.listener;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.camunda.consulting.ModelPropertyUtil;
import com.camunda.consulting.SimulatorPlugin;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandlerConfiguration;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractJobCreateListener {
  static final Logger LOG = LoggerFactory.getLogger(AbstractJobCreateListener.class);

  // process definition id -> activity id -> maybe expression
  private Map<String, Map<String, Optional<Expression>>> nextFireExpressionCache = new HashMap<>();

  public AbstractJobCreateListener() {
    super();
  }

  protected Optional<Expression> getCachedNextFireExpression(DelegateExecution execution, String activityId) {
    Map<String, Optional<Expression>> activityIdToExpression = nextFireExpressionCache.get(execution.getProcessDefinitionId());
    if (activityIdToExpression == null) {
      activityIdToExpression = new HashMap<>();
      nextFireExpressionCache.put(execution.getProcessDefinitionId(), activityIdToExpression);
    }
    Optional<Expression> nextFireExpression = activityIdToExpression.get(activityId);
    if (nextFireExpression == null) {
      ModelElementInstance modelElementInstance = execution.getBpmnModelInstance().getModelElementById(activityId);
      Optional<String> nextFire = ModelPropertyUtil.getNextFire(modelElementInstance);
      nextFireExpression = nextFire.map(SimulatorPlugin.getProcessEngineConfiguration().getExpressionManager()::createExpression);
      activityIdToExpression.put(activityId, nextFireExpression);
      LOG.debug("Return new expression");
    } else {
      LOG.debug("Return cached expression");
    }
    return nextFireExpression;
  }

  protected void createTimerJob(ExecutionEntity execution, String jobHandlertype, Date duedate, JobHandlerConfiguration jobHandlerConfiguration) {
    TimerEntity timer = new TimerEntity();
    ProcessDefinitionEntity processDefinition = execution.getProcessDefinition();

    timer.setExecution(execution);
    timer.setDuedate(duedate);
    timer.setJobHandlerType(jobHandlertype);
    timer.setProcessDefinitionKey(processDefinition.getKey());
    timer.setDeploymentId(processDefinition.getDeploymentId());
    timer.setJobHandlerConfiguration(jobHandlerConfiguration);

    Context.getCommandContext().getJobManager().schedule(timer);
  }

}