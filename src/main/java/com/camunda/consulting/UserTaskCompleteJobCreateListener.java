package com.camunda.consulting;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

import java.util.Date;
import java.util.Optional;

public class UserTaskCompleteJobCreateListener implements TaskListener{

    private Optional<Expression> nextFireExpression;
    private ProcessEngineConfigurationImpl processEngineConfiguration;

    public UserTaskCompleteJobCreateListener(ActivityImpl activity) {

        this.processEngineConfiguration = SimulatorPlugin.getProcessEngineConfiguration();
        BpmnModelInstance bpmnModelInstance ;
        //= processEngineConfiguration.getDeploymentCache().findBpmnModelInstanceForProcessDefinition(activity.getProcessDefinition())

        Optional<String> nextFire = Optional.empty();

        //= ModelPropertyUtil.getNextFire(bpmnModelInstance.getModelElementById(activity.getActivityId()));

        nextFireExpression = nextFire.map(processEngineConfiguration.getExpressionManager()::createExpression);

    }

    @Override
    public void notify(DelegateTask task) {

        if(nextFireExpression.isPresent()) {
            Date dueDate = (Date) nextFireExpression.get().getValue(task.getExecution());
            createJobForUserTaskCompletion(task, dueDate);
        }
    }

    private void createJobForUserTaskCompletion(DelegateTask task, Date dueDate) {

        TimerEntity timer = new TimerEntity();
        ProcessDefinitionEntity processDefinition = ((ExecutionEntity) task.getExecution()).getProcessDefinition();

        timer.setExecution((ExecutionEntity)task.getExecution());
        timer.setDuedate(dueDate);
        timer.setJobHandlerType(CompleteUserTaskJobHandler.TYPE);
        timer.setProcessDefinitionKey(processDefinition.getKey());
        timer.setDeploymentId(processDefinition.getDeploymentId());
        timer.setJobHandlerConfiguration(new CompleteUserTaskJobHandler.CompleteUserTaskJobHandlerConfiguration(task.getId()));

        //Context.getCommandContext().getJobManager().schedule(timer);
    }

}
