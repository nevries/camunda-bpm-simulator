package com.camunda.consulting.jobhandler;

import com.camunda.consulting.SimulationExecutor;
import com.camunda.consulting.TestPayloadGenerator;
import org.apache.ibatis.logging.LogFactory;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.*;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;

@Deployment(resources = "createEventSubscriptionTestModel.bpmn")
public class FireEventJobHandlerTest {

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  static {
    LogFactory.useSlf4jLogging(); // MyBatis
  }

  @Before
  public void setup() {
    init(rule.getProcessEngine());
    Mocks.register("testPayloadGenerator", new TestPayloadGenerator());
  }

  @Test
  public void shouldFireMessageThenSignal() {

    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("createEventSubscription");
    assertThat(processInstance).isStarted().isWaitingAt("UT");

    SimulationExecutor.execute(DateTime.now().toDate(), DateTime.now().plusHours(1).toDate());

    assertThat(processInstance).hasPassedInOrder("messageFired", "signalFired", "timerFired");

  }

}