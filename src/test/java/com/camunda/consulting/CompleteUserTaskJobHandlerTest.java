package com.camunda.consulting;

import org.apache.ibatis.logging.LogFactory;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.*;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;

@Deployment(resources = "userTaskCompleteModel.bpmn")
public class CompleteUserTaskJobHandlerTest {

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
    public void shouldExecuteCompleteTaskJob() {

        ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("userTaskComplete");
        assertThat(processInstance).isStarted().isWaitingAt("Task_157nl2o");

        SimulationExecutor.execute(DateTime.now().minusMinutes(5).toDate(), DateTime.now().plusMinutes(5).toDate());

        assertThat(processInstance).isEnded();

    }

}