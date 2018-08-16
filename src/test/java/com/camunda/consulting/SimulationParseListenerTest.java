package com.camunda.consulting;

import org.apache.ibatis.logging.LogFactory;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.init;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;

@Deployment(resources = "parseListenerTestModel.bpmn")
public class SimulationParseListenerTest {


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
    public void shouldStripListeners() {

        ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("parseListenerTest");
        assertThat(processInstance).isStarted();
        complete(task());
        assertThat(processInstance).isEnded();

        assertThat(historyService().createHistoricVariableInstanceQuery().count()).isEqualTo(0);

    }

}

