package com.camunda.consulting.listener;

import com.camunda.consulting.PayloadGenerator;
import com.camunda.consulting.TestPayloadGenerator;
import org.apache.ibatis.logging.LogFactory;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.init;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;

@Deployment(resources = "payloadGeneratorTestModel.bpmn")
public class PayloadGeneratorListenerTest {

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  static {
    LogFactory.useSlf4jLogging(); // MyBatis
  }

  @Before
  public void setup() {
    init(rule.getProcessEngine());
    Mocks.register("generator", new PayloadGenerator());
  }

  @Test
  public void shouldCreateRandomValueWithTopologyCheck() {

    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("payloadGenerator");
    // async after service task
    assertThat(processInstance).isWaitingAt("ST");
    
    int firstVar = (int)runtimeService().getVariable(processInstance.getId(), "firstVar");
    assertThat(firstVar).isBetween(7, 13);
  }

}