package com.camunda.consulting;

import org.apache.ibatis.logging.LogFactory;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.init;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;

@Deployment(resources = "userTaskCompleteModel.bpmn")
public class UserTaskCompleteJobCreateListenerTest {

    @Rule
    public ProcessEngineRule rule = new ProcessEngineRule();

    static {
        LogFactory.useSlf4jLogging(); // MyBatis
    }

    @Before
    public void setup() {
        init(rule.getProcessEngine());
    }

    @Test
    public void shouldCreateCustomJob() {

        ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("userTaskComplete");
        assertThat(processInstance).isStarted().isWaitingAt("Task_157nl2o");

        // Did we create a custom job?
        assertThat(jobQuery().count()).isEqualTo(1);

        // Does the job belong to our user tasK?
        String jobHandlerConfig = ((JobEntity) jobQuery().singleResult()).getJobHandlerConfigurationRaw();
        assertThat(jobHandlerConfig).isEqualTo(task().getId());

    }

}