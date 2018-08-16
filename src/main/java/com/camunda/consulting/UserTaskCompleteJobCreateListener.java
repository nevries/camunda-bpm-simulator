package com.camunda.consulting;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;

import java.util.Date;

public class UserTaskCompleteJobCreateListener implements TaskListener{

    private Date dueDate;

    public UserTaskCompleteJobCreateListener(ActivityImpl activity) {

        // TODO: load fireNext (dueDate) from extension property
        dueDate = new Date();

    }

    @Override
    public void notify(DelegateTask task) {

        createJobForUserTaskCompletion(task, dueDate);
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

        Context.getCommandContext().getJobManager().schedule(timer);
    }

}
